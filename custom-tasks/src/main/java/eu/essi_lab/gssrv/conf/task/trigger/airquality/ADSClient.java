package eu.essi_lab.gssrv.conf.task.trigger.airquality;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Minimal client for the Copernicus Atmosphere Data Store (ADS) Retrieve API, the same REST API used by cdsapi >= 0.7 and
 * ecmwf-datastores-client:
 * <ul>
 * <li>POST {base}/retrieve/v1/processes/{dataset}/execution with body {"inputs": {...}} submits a job</li>
 * <li>GET {base}/retrieve/v1/jobs/{jobID} returns its status (accepted, running, successful, failed, rejected)</li>
 * <li>GET {base}/retrieve/v1/jobs/{jobID}/results returns the asset to download (asset.value.href)</li>
 * </ul>
 * Authentication is done with the personal access token in the PRIVATE-TOKEN header. The dataset licence must be accepted once
 * from the ADS web portal, otherwise the job submission is refused.<br>
 * <br>
 * The results are downloaded from the ECMWF object store, whose certificate is issued by "HARICA TLS RSA Root CA 2021", missing from
 * the JDK cacerts: the root must be in the DAB trust store
 */
public class ADSClient {

    public static final String DEFAULT_URL = "https://ads.atmosphere.copernicus.eu/api";

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(15);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(2);

    private final String baseUrl;
    private final String key;
    private final Downloader downloader;
    private Duration timeout = DEFAULT_TIMEOUT;

    public ADSClient(String baseUrl, String key) {
	this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	this.key = key;

	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
    }

    public void setTimeout(Duration timeout) {
	this.timeout = timeout;
    }

    /**
     * Submits the request, waits for the job to complete and downloads the result into the target file
     */
    public void retrieve(String dataset, JSONObject inputs, Path target) throws Exception {

	String jobId = submit(dataset, inputs);
	waitAndDownload(jobId, target);
    }

    /**
     * Waits for a job previously submitted with {@link #submit(String, JSONObject)} and downloads its result into the target file
     */
    public void waitAndDownload(String jobId, Path target) throws Exception {

	waitForCompletion(jobId);
	download(jobId, target);
    }

    /**
     * Submits the request and returns the job identifier without waiting. Several jobs can be submitted in a row so that they are
     * queued in parallel on the ADS side
     */
    public String submit(String dataset, JSONObject inputs) throws Exception {

	String url = baseUrl + "/retrieve/v1/processes/" + dataset + "/execution";
	JSONObject body = new JSONObject().put("inputs", inputs);

	GSLoggerFactory.getLogger(getClass()).info("ADS submitting request to " + url + ": " + inputs);

	HashMap<String, String> headers = authorizationHeaders();
	headers.put("Content-Type", "application/json");

	HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, url, body.toString(), headers);

	String jobId = new JSONObject(sendForString(request, "job submission")).getString("jobID");
	GSLoggerFactory.getLogger(getClass()).info("ADS job submitted: " + jobId);
	return jobId;
    }

    private void waitForCompletion(String jobId) throws Exception {

	String url = baseUrl + "/retrieve/v1/jobs/" + jobId;
	Instant deadline = Instant.now().plus(timeout);
	String lastStatus = null;

	while (Instant.now().isBefore(deadline)) {

	    HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, url, authorizationHeaders());
	    String status = new JSONObject(sendForString(request, "job status")).getString("status");

	    if (!status.equals(lastStatus)) {
		GSLoggerFactory.getLogger(getClass()).info("ADS job " + jobId + " status: " + status);
		lastStatus = status;
	    }

	    switch (status) {
	    case "successful":
		return;
	    case "failed":
	    case "rejected":
		throw new IOException("ADS job " + jobId + " " + status + ": " + readErrorDetails(jobId));
	    default:
		Thread.sleep(POLL_INTERVAL.toMillis());
	    }
	}

	throw new IOException("ADS job " + jobId + " not completed within " + timeout);
    }

    private void download(String jobId, Path target) throws Exception {

	String url = baseUrl + "/retrieve/v1/jobs/" + jobId + "/results";
	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, url, authorizationHeaders());

	JSONObject asset = new JSONObject(sendForString(request, "job results")).getJSONObject("asset").getJSONObject("value");
	URI href = URI.create(url).resolve(asset.getString("href"));

	GSLoggerFactory.getLogger(getClass()).info("ADS downloading " + href + " (" + asset.optLong("file:size", -1) + " bytes)");

	HttpResponse<InputStream> response = send(HttpRequestUtils.build(MethodNoBody.GET, href.toString()));
	try (InputStream body = response.body()) {
	    if (response.statusCode() != 200) {
		throw new IOException("ADS download failed with HTTP " + response.statusCode() + ": " + href);
	    }
	    Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING);
	}
    }

    private String readErrorDetails(String jobId) {
	try {
	    String url = baseUrl + "/retrieve/v1/jobs/" + jobId + "/results";
	    try (InputStream body = send(HttpRequestUtils.build(MethodNoBody.GET, url, authorizationHeaders())).body()) {
		return new String(body.readAllBytes(), StandardCharsets.UTF_8);
	    }
	} catch (Exception e) {
	    return e.getMessage();
	}
    }

    private HashMap<String, String> authorizationHeaders() {
	HashMap<String, String> headers = new HashMap<>();
	headers.put("PRIVATE-TOKEN", key);
	return headers;
    }

    /**
     * Sends the request and returns the response body, failing if the status code is not 2xx
     */
    private String sendForString(HttpRequest request, String operation) throws Exception {

	HttpResponse<InputStream> response = send(request);
	String body;
	try (InputStream in = response.body()) {
	    body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
	}

	int code = response.statusCode();
	if (code < 200 || code >= 300) {
	    throw new IOException("ADS " + operation + " failed with HTTP " + code + ": " + body);
	}
	return body;
    }

    private HttpResponse<InputStream> send(HttpRequest request) throws Exception {

	return downloader.downloadResponse(request);
    }
}
