package eu.essi_lab.accessor.rihmi;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class RIHMIClient {

    public static String realtimeEndpoint = "http://hydroweb.meteo.ru/hydro-service/rest/GetHydroDischargesRF/xml?";

    public static String historicalEndpoint = "http://hydroweb.meteo.ru/hydro-service/rest/GetHydroAveMonDischargesRF/xml/";

    public static String stationListendpoint = "http://hydroweb.meteo.ru/hydro-service/rest/GetWHOSHydroStationsRF/whos";

    public static String aralStationListendpoint = "http://hydroweb.meteo.ru/hydro-service/rest/GetWHOSHydroStationsAral/whos";

    public static String aralDischargeEndpoint = "http://hydroweb.meteo.ru/hydro-service/rest/GetHydroDischargesAral/xml?";

    public static String aralWaterLevelEndpoint = " http://hydroweb.meteo.ru/hydro-service/rest/GetHydroWaterLevelAral/xml?";

    public static String aralWaterTemperatureEndpoint = " http://hydroweb.meteo.ru/hydro-service/rest/GetHydroWaterTemperatureAral/xml?";

    public String getEndpoint() {
	return realtimeEndpoint;
    }

    public String getAralDischargeEndpoint() {
	return aralDischargeEndpoint;
    }

    public String getAralWaterLevelEndpoint() {
	return aralWaterLevelEndpoint;
    }

    public String getAralWaterTemperatureEndpoint() {
	return aralWaterTemperatureEndpoint;
    }

    public void setEndpoint(String endpoint) {
	this.realtimeEndpoint = endpoint;
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

    public RIHMIClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    static SimpleDateFormat sdf = null;

    static {
	sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'z'");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public InputStream getWaterML(String stationId, Date start, Date end) throws IOException, InterruptedException, URISyntaxException {

	String from = sdf.format(start);
	String to = sdf.format(end);

	String url = realtimeEndpoint + "dateFrom=" + from + "&dateTo=" + to + "&index=" + stationId;

	return downloadStream(url);

    }

    public InputStream getAralWaterML(String stationId, Date start, Date end, boolean isDischarge)
	    throws IOException, InterruptedException, URISyntaxException {

	String from = sdf.format(start);
	String to = sdf.format(end);

	String url = isDischarge ? aralDischargeEndpoint + "dateFrom=" + from + "&dateTo=" + to + "&index=" + stationId
		: aralWaterLevelEndpoint + "dateFrom=" + from + "&dateTo=" + to + "&index=" + stationId;

	return downloadStream(url);

    }

    public List<String> getStationIdentifiers(boolean isAral) throws Exception {
	String identifierEndpoint = isAral ? aralStationListendpoint : stationListendpoint;
	InputStream stream = downloadStream(identifierEndpoint);
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	Node[] nodes = reader.evaluateNodes("//*:MonitoringPoint/*:identifier");
	List<String> ret = new ArrayList<>();
	for (Node node : nodes) {
	    String id = reader.evaluateString(node, ".");
	    if (id != null) {
		id = id.trim();
		if (!id.isEmpty()) {
		    ret.add(id);
		}
	    }
	}
	GSLoggerFactory.getLogger(getClass()).info("Got {} station identifiers", ret.size());
	stream.close();
	return ret;

    }

    public HttpResponse<InputStream> getDownloadResponse(String url) throws IOException, InterruptedException, URISyntaxException {
	if (url.contains("ws.meteo.ru")) {
	    url = getProxiedURL(url);
	} else {
	    url = url.trim();
	}
	logger.info("Request url:" + url);
	HttpResponse<InputStream> response = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	int responseCode = response.statusCode();
	String statusInfo = "Status code: " + responseCode;
	logger.info(statusInfo);

	return response;
    }

    private InputStream downloadStream(String url) throws IOException, InterruptedException, URISyntaxException {
	return getDownloadResponse(url).body();
    }

    public String getHistoricalDownloadUrl(String stationId) {
	return historicalEndpoint + stationId;
    }

    public HttpResponse<InputStream> getHistoricalWaterML(String stationId) throws IOException, InterruptedException, URISyntaxException {
	return getDownloadResponse(getHistoricalDownloadUrl(stationId));
    }

    public String getProxiedURL(String url) {
	url = url.trim();

	String giProxyEndpoint = getGiProxyEndpoint();

	if (giProxyEndpoint != null) {
	    try {
		url = URLEncoder.encode(url, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	    }
	    url = giProxyEndpoint + "/get?url=" + url;
	}

	return url;
    }

}
