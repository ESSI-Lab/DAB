package eu.essi_lab.accessor.rihmi;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

public class RIHMIConnector extends StationConnector<RIHMIConnectorSetting> {

    public static final String TYPE = "RIHMIConnector";
    private static final String RIHMI_CONNECTOR_ERROR = "RIHMI_CONNECTOR_ERROR";

    private static boolean isAral = false;

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("ws.meteo.ru");
    }

    private RIHMIClient client = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	List<String> stations = new ArrayList<>();
	stations.add(stationId);
	return listRecords(stations);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	List<String> stationIdentifiers;

	isAral = getSetting().isAral();

	try {
	    stationIdentifiers = getStationIdentifiers(isAral);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_CONNECTOR_ERROR, //
		    e//
	    );
	}
	stationIdentifiers.sort(new Comparator<String>() {
	    @Override
	    public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	    }
	});
	String token = request.getResumptionToken();
	if (token == null) {
	    token = stationIdentifiers.get(0);
	}
	ListRecordsResponse<OriginalMetadata> ret = listTimeseries(token);
	for (int i = 0; i < stationIdentifiers.size(); i++) {
	    String stationIdentifier = stationIdentifiers.get(i);
	    if (token.equals(stationIdentifier)) {
		if (i != stationIdentifiers.size() - 1) {
		    ret.setResumptionToken(stationIdentifiers.get(i + 1));
		    break;
		}
	    }
	}
	return ret;

    }

    private static List<String> stationIdentifiers = null;

    private List<String> getStationIdentifiers(boolean isAral) throws Exception {

	if (client == null) {
	    client = new RIHMIClient();
	}

	if (stationIdentifiers != null) {
	    return stationIdentifiers;
	}
	stationIdentifiers = client.getStationIdentifiers(isAral);
	return stationIdentifiers;
    }

    public ListRecordsResponse<OriginalMetadata> listRecords(List<String> stationIdentifiers) throws GSException {

	try {
	    if (client == null) {
		client = new RIHMIClient();
	    }

	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	    for (String stationId : stationIdentifiers) {

		if (stationId.length() == 4) {
		    stationId = "0" + stationId;
		    GSLoggerFactory.getLogger(getClass()).error("modified {}", stationId);
		}

		List<String> downloadUrls = new ArrayList<>();

		if (isAral) {
		    // water level
		    downloadUrls.add(getRealtimeDownloadUrl(client.getAralWaterLevelEndpoint(), stationId));
		    // discharges
		    downloadUrls.add(getRealtimeDownloadUrl(client.getAralDischargeEndpoint(), stationId));

		} else {
		    // real time download url
		    downloadUrls.add(getRealtimeDownloadUrl(getSourceURL(), stationId));
		    // historical download url
		    downloadUrls.add(client.getHistoricalDownloadUrl(stationId));
		}

		int count = 0;
		for (String url : downloadUrls) {
		    count++;
		    OriginalMetadata metadataRecord = new OriginalMetadata();

		    metadataRecord.setSchemeURI(CommonNameSpaceContext.RIHMI_URI);

		    HttpResponse<InputStream> response;
		    try {
			response = client.getDownloadResponse(url);
		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error("Error ({}) while downloading from station {} Reference URL: {}",
				e.getMessage(), stationId, url);
			continue;
		    }

		    Integer status = response.statusCode();

		    if (status != 200) {
			GSLoggerFactory.getLogger(getClass())
				.error("Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			continue;
		    }

		    XMLDocumentReader reader = new XMLDocumentReader(response.body());

		    if (reader.asString().contains("Internal Server Error")) {
			GSLoggerFactory.getLogger(getClass())
				.error("Error (HTTP code {}) while downloading from station {} Reference URL: {}", status, stationId, url);
			continue;
		    }

		    String from = normalizeTime(reader.evaluateString("//*:TimePeriod/*:beginPosition"));
		    if (from == null || from.isEmpty()) {
			from = reader.evaluateString("//*:MeasurementTVP[1]/*:time[1]");
		    }

		    String to = normalizeTime(reader.evaluateString("//*:TimePeriod/*:endPosition"));
		    if (to == null || to.isEmpty()) {
			to = ISO8601DateTimeUtils.getISO8601DateTime();
		    }

		    RIHMIMetadata rm = new RIHMIMetadata();
		    if (from == null || from.isEmpty()) {
			if (count == 2) {
			    ret.setResumptionToken(null);
			    return ret;
			} else {
			    continue;
			}

		    }
		    ISO8601DateTimeUtils.parseISO8601ToDate(from).ifPresent(d -> rm.setBegin(d));
		    ISO8601DateTimeUtils.parseISO8601ToDate(to).ifPresent(d -> rm.setEnd(d));

		    String pos = reader.evaluateString("//*:shape/*:Point/*:pos");
		    String[] split = new String[2];
		    if (isAral) {
			String[] splittedPos = pos.split(", ");
			if (splittedPos != null && splittedPos.length > 1) {
			    split[0] = splittedPos[0].replace(",", ".");
			    split[1] = splittedPos[1].replace(",", ".");
			}
		    } else {
			pos = pos.replace(",", "");
			split = pos.split(" ");
		    }

		    if (split.length > 1 && split[0] != null) {
			rm.setLatitude(Double.parseDouble(split[0]));
			rm.setLongitude(Double.parseDouble(split[1]));
		    }
		    if (url.contains(client.getAralWaterLevelEndpoint())) {
			rm.setParameterId("RIHMI:WaterLevel");
			rm.setParameterName("Water Level");
		    } else {
			rm.setParameterId("RIHMI:Discharge");
			rm.setParameterName("Discharge");
		    }
		    rm.setStationId(stationId);
		    String name = reader
			    .evaluateString("//*:MonitoringPoint[1]/*:parameter[1]/*:NamedValue[1]/*:value[1]/*:CharacterString[1]");
		    rm.setStationName(name);

		    String units = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:uom[1]/@code");
		    rm.setUnits(units);

		    String interpolation = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:interpolationType[1]/@*:href");

		    if (interpolation != null && !interpolation.isEmpty()) {
			if (interpolation.equals("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc")) {
			    rm.setInterpolation(InterpolationType.AVERAGE_SUCC);
			} else {
			    GSLoggerFactory.getLogger(getClass()).error("Interpolation not recognized: {}", interpolation);
			}
		    }

		    String aggregationDuration = reader.evaluateString(
			    "//*:MeasurementTimeseries[1]/*:defaultPointMetadata[1]/*:DefaultTVPMeasurementMetadata[1]/*:aggregationDuration[1]");
		    if (aggregationDuration != null && !aggregationDuration.isEmpty()) {
			rm.setAggregationDuration(aggregationDuration);
		    }

		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    rm.marshal(baos);
		    String str = new String(baos.toByteArray());
		    try {
			baos.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }

		    metadataRecord.setMetadata(str);

		    GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<Boolean>("isAral", isAral));
		    if (isAral) {
			handler.add(//
				new GSProperty<String>("downloadLink", url));
		    }
		    metadataRecord.setAdditionalInfo(handler);

		    ret.addRecord(metadataRecord);

		}
	    }

	    ret.setResumptionToken(null);

	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_CONNECTOR_ERROR, //
		    e//
	    );
	}
    }

    private String normalizeTime(String time) {
	if (time == null) {
	    return null;
	}
	time = time.trim();
	if (time.isEmpty()) {
	    return null;
	}
	if (time.endsWith("Z")) {
	    return time.replace(" ", "");
	} else {
	    return time.replace(" ", "") + "Z";
	}
    }

    public static String getRealtimeDownloadUrl(String url, String stationId) {
	if (url.contains("?")) {
	    url = url.substring(0, url.indexOf("?"));
	}
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");// 2020-01-02T06:06:30.000+0000
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	String dateFrom = sdf.format(new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 60l));
	try {
	    dateFrom = URLEncoder.encode(dateFrom, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	String dateTo = sdf.format(new Date());
	try {
	    dateTo = URLEncoder.encode(dateTo, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	url = url + "?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&index=" + stationId;
	return url;
    }

    public static String extractStationId(String url) {
	String ret = "70801"; // by default
	if (url.contains("index=")) {
	    ret = url.substring(url.indexOf("index="));
	    ret = ret.substring(ret.indexOf("=") + 1);
	    if (ret.contains("&")) {
		ret = ret.substring(0, ret.indexOf("&"));
	    }
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.RIHMI_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected RIHMIConnectorSetting initSetting() {

	return new RIHMIConnectorSetting();
    }

}
