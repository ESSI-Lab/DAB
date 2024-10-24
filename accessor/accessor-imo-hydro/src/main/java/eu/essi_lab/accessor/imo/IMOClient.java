package eu.essi_lab.accessor.imo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.UnicodeBOMInputStream;

public class IMOClient {

    private String endpoint = "https://customer.vedur.is/HYCOS/";

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

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

    private Logger logger;

    public IMOClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());
	cache.clear();
    }

    private static ExpiringCache<List<ZRXPDocument>> cache = null;

    static {
	cache = new ExpiringCache<List<ZRXPDocument>>();
	cache.setDuration(1000 * 60 * 30l);
    }

    public List<ZRXPDocument> downloadAll() throws Exception {

	List<ZRXPDocument> ret = cache.get("docs");
	if (ret != null && !ret.isEmpty()) {
	    return ret;
	} else {
	    ret = new ArrayList<ZRXPDocument>();
	}

	String targetEndpoint = URLEncoder.encode(getEndpoint(), "UTF-8");

	String proxyEndpoint = getGiProxyEndpoint();

	if (proxyEndpoint != null) {
	    targetEndpoint = proxyEndpoint + "/get?url=" + targetEndpoint;
	}

	InputStream stream = downloadStream(targetEndpoint);

	UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(stream);
	InputStreamReader reader = new InputStreamReader(ubis, "Cp1252");
	ubis.skipBOM();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8); // charset conversion here...

	IOUtils.copy(reader, writer);
	stream.close();
	baos.close();
	reader.close();
	writer.close();
	String html = new String(baos.toByteArray());
	List<String> links = extractLinks(html);

	IMOCSVMetadata metadata = null;
	for (String link : links) {
	    String url;
	    if (targetEndpoint.contains("url=")) {
		url = targetEndpoint + link;
	    } else {
		URL realURL = new URL(targetEndpoint);
		URL childURL = new URL(realURL, link);
		url = childURL.toExternalForm();
	    }
	    String suffix;
	    if (url.toLowerCase().endsWith(".zrxp")) {
		suffix = ".zrxp";
	    } else {
		suffix = ".csv";
	    }
	    InputStream linkStream = downloadStream(url);

	    UnicodeBOMInputStream ubis2 = new UnicodeBOMInputStream(linkStream);
	    InputStreamReader linkReader = new InputStreamReader(ubis2, "Cp1252");
	    ubis2.skipBOM();

	    File file = File.createTempFile(getClass().getSimpleName(), suffix);
	    file.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(file);

	    OutputStreamWriter linkWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

	    IOUtils.copy(linkReader, linkWriter);
	    linkReader.close();
	    linkWriter.close();
	    fos.close();
	    linkStream.close();
	    if (suffix.equals(".zrxp")) {
		ZRXPDocument document = new ZRXPDocument(file);
		ret.add(document);
	    } else {
		metadata = new IMOCSVMetadata(file);
	    }
	}

	for (ZRXPDocument zrxpDocument : ret) {
	    List<ZRXPBlock> blocks = zrxpDocument.getBlocks();
	    for (ZRXPBlock block : blocks) {
		String stationId = block.getStationIdentifier();
		IMOStationMetadata stationMetadata = metadata.getStationMetadata().get(stationId);
		if (stationMetadata != null) {
		    try {
			block.addHeaderInFile(ZRXPKeyword.LATITUDE, stationMetadata.getLatitude());
			block.addHeaderInFile(ZRXPKeyword.LONGITUDE, stationMetadata.getLongitude());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	}

	cache.put("docs", ret);

	return ret;

    }

    private InputStream downloadStream(String url) throws Exception {

	HttpResponse<InputStream> response = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	String statusInfo = "Status code: " + response.statusCode();
	logger.info(statusInfo);
	InputStream stream = response.body();

	return stream;
    }

    private List<String> extractLinks(String html) {
	List<String> ret = new ArrayList<String>();
	String hrefToken = "href=\"";
	while (true) {
	    int hrefIndex = html.indexOf(hrefToken);
	    if (hrefIndex == -1) {
		break;
	    }
	    html = html.substring(hrefIndex + hrefToken.length());
	    String hrefValue = html.substring(0, html.indexOf('"'));
	    String lowerHref = hrefValue.toLowerCase();
	    if (lowerHref.endsWith(".zrxp") || lowerHref.endsWith(".csv")) {
		ret.add(hrefValue);
	    }
	}
	return ret;
    }

}
