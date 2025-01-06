package eu.essi_lab.accessor.hiscentral.piemonte;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpResponse;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class HISCentralPiemonteClient {

    private static final String PIEMONTE_CLIENT_LOGIN_ERROR = "PIEMONTE_CLIENT_LOGIN_ERROR";
    private static final String PIEMONTE_REMOTE_SERVICE_ERROR = "PIEMONTE_REMOTE_SERVICE_ERROR";
    private static final String PIEMONTE_CLIENT_UNABLE_TO_GET_STATIONS_ERROR = "PIEMONTE_CLIENT_UNABLE_TO_GET_STATIONS_ERROR";

    private String endpoint;
    private GSLogger logger;
    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }

    public HISCentralPiemonteClient(String endpoint) {

	this.endpoint = endpoint;
	this.logger = GSLoggerFactory.getLogger(HISCentralPiemonteClient.class);
    }

    public String getData(String path) throws GSException {

	return getResponse(path);

    }

    public String getStations(String path) throws GSException {

	return getResponse(path);

    }

    /**
     * Returns available data for the given station (it is allowed to get data only from the latest 5 days)
     * 
     * @param startTime
     * @param endTime
     * @return
     * @throws GSException
     */
    public String getLastData(String startTime, String endTime) throws GSException {
	String ret = null;
	try {

	    String parameter = endpoint.trim() + "&data_min=" + startTime + "&data_max=" + endTime + "&format=json";
	    parameter = URLEncoder.encode(parameter, "UTF-8");

	    String url = parameter;

	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		url = proxyEndpoint + "/get?url=" + url;
	    }

	    logger.info("Sending request to: {}", url);

	    HttpResponse<InputStream> response = new Downloader()
		    .downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.build("accept", "application/json")));

	    InputStream input = response.body();

	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    IOUtils.copy(input, output);

	    input.close();

	    ret = new String(output.toByteArray());

	    if (ret.contains("Too many requests, please try again later") //
		    || ret.contains("504 Gateway Time-out")//

	    ) {
		logger.warn("Invalid response: {}", ret);

	    } else {
		// responseCache.put(path, ret);
		return ret;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

    private static ExpiringCache<String> responseCache;

    static {
	responseCache = new ExpiringCache<>();
	responseCache.setDuration(1000 * 60 * 30l);
	responseCache.setMaxSize(500);
    }

    public synchronized String getResponse(String path) throws GSException {

	String ret = responseCache.get(path);
	if (ret != null) {
	    return ret;
	}

	int tries = 20;
	do {
	    try {

		String parameter = endpoint.trim() + path;
		parameter = URLEncoder.encode(parameter, "UTF-8");

		String url = parameter;

		String proxyEndpoint = getGiProxyEndpoint();
		if (proxyEndpoint != null) {
		    url = proxyEndpoint + "/get?url=" + url;
		}

		logger.info("Sending request to: {}", url);

		HttpResponse<InputStream> response = new Downloader()
			.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.build("accept", "application/json")));

		InputStream input = response.body();

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IOUtils.copy(input, output);

		input.close();

		ret = new String(output.toByteArray());

		if (ret.contains("Too many requests, please try again later") //
			|| ret.contains("504 Gateway Time-out")//

		) {
		    logger.warn("Invalid response: {}", ret);

		} else {
		    responseCache.put(path, ret);
		    return ret;
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    logger.info("Waiting 40s...(left #{} tries}", tries);
	    try {
		Thread.sleep(40000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	} while (tries-- > 0);

	throw GSException.createException( //
		getClass(), //
		"Remote service issue", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		PIEMONTE_REMOTE_SERVICE_ERROR //
	);
    }
}
