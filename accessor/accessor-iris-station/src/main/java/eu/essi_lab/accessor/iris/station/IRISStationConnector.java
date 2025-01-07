/**
 *
 */
package eu.essi_lab.accessor.iris.station;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceType;

/**
 * @author Fabrizio
 */
public class IRISStationConnector extends HarvestedQueryConnector<IRISStationConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "IRISStationConnector";

    /**
     *
     */
    private static final int STEP = 100;
    private static final String IRIS_STATION_CONNECTION_ERROR = "IRIS_STATION_CONNETCTION_ERROR";

    private String currentStaCode;
    private IRISStationWrapper collectionWrapper;
    private boolean newCollection;
    private String staSiteName;
    private String netDesc;
    private List<String> channelList;
    private HashMap<String, String> netDescs;

    private HashMap<String, String> staSiteNames;
    private static final String CHANNEL_QUERY = "http://service.iris.edu/fdsnws/station/1/query?output=text&level=chan";
    private static final String NETWORK_QUERY = "http://service.iris.edu/fdsnws/station/1/query?output=text&level=net";
    private static final String STATION_QUERY = "http://service.iris.edu/fdsnws/station/1/query?output=text&level=sta";

    private static final String IRIS_STATION_CONNECTOR_DO_EXEC_REQUEST_ERROR = "IRIS_STATION_CONNECTOR_DO_EXEC_REQUEST_ERROR";
    private static final String IRIS_STATION_CONNECTOR_GET_NETWORK_DESCRIPTION_ERROR = "IRIS_STATION_CONNECTOR_GET_NETWORK_DESCRIPTION_ERROR";
    private static final String IRIS_STATION_CONNECTOR_GET_SITE_NAME_ERROR = "IRIS_STATION_CONNECTOR_GET_SITE_NAME_ERROR";

    /**
     *
     */
    public IRISStationConnector() {
	currentStaCode = "";
	staSiteName = "";
	netDesc = "";
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (channelList == null) {

	    List<String> list = getChannelList();

	    if (list == null || list.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve channel list");
		return null;
	    }

	    channelList = list;

	    Optional<Integer> mr = getSetting().getMaxRecords();

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).debug("Channel list reduced to {} channels", getSetting().getMaxRecords());
		channelList = channelList.subList(0, mr.get());
	    }
	}

	if (netDescs == null) {

	    HashMap<String, String> networksDescription = getNetworksDescription();

	    if (networksDescription == null || networksDescription.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve network descriptions");
		return null;
	    }

	    netDescs = networksDescription;
	}

	if (staSiteNames == null) {

	    HashMap<String, String> stationsSiteNames = getStationsSiteName();

	    if (stationsSiteNames == null || stationsSiteNames.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve stations site names");
		return null;
	    }

	    staSiteNames = stationsSiteNames;
	}

	String token = request.getResumptionToken();

	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	int toIndex = start + STEP;

	if (toIndex > channelList.size()) {

	    // no resumption token
	    toIndex = channelList.size();

	} else {

	    response.setResumptionToken(String.valueOf(toIndex));
	}

	List<String> subList = channelList.subList(start, toIndex);

	GSLoggerFactory.getLogger(getClass()).debug("Channels [{}-{}/{}] STARTED", start, toIndex, channelList.size());

	for (String channel : subList) {

	    List<OriginalMetadata> md = create(channel, staSiteNames, netDescs);
	    response.addRecord(md.get(0));

	    if (md.size() == 2) {
		response.addRecord(md.get(1));
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Channels [{}-{}/{}] ENDED", start, toIndex, channelList.size());

	return response;
    }

    private Double asDouble(String value) {

	try {

	    return Double.valueOf(value);

	} catch (NumberFormatException ex) {

	    GSLoggerFactory.getLogger(getClass()).warn("Number format exception converting {}", value, ex);
	}

	return null;
    }

    private List<OriginalMetadata> create(//
	    String channel, //
	    HashMap<String, String> staSiteNames, //
	    HashMap<String, String> netDescs) {

	String[] chanInfo = channel.split("\\|");

	String netCode = chanInfo[0];
	String staCode = chanInfo[1];

	String chanCode = chanInfo[3];

	Double lat = asDouble(chanInfo[4]);
	Double lon = asDouble(chanInfo[5]);
	Double elev = asDouble(chanInfo[6]);

	String instrument = chanInfo[10];

	String startTime = chanInfo[15];
	String endTime = chanInfo.length == 17 ? chanInfo[16] : "now";

	if (!currentStaCode.equals(staCode)) {

	    newCollection = true;

	    currentStaCode = staCode;

	    collectionWrapper = new IRISStationWrapper(ResourceType.DATASET_COLLECTION);

	    String collectionID = netCode + "_" + staCode + "_" + createShortID();
	    collectionWrapper.setId(collectionID);

	    staSiteName = staSiteNames.get(staCode);
	    if (staSiteName == null || staSiteName.equals("")) {
		staSiteName = "(no Station site name)";
	    }
	    collectionWrapper.setStaSiteName(staSiteName);

	    netDesc = netDescs.get(netCode);
	    if (netDesc == null || netDesc.equals("")) {
		netDesc = "(no Network description)";
	    }
	    collectionWrapper.setNetDesc(netDesc);

	    collectionWrapper.setStaCode(staCode);
	    collectionWrapper.setNetCode(netCode);
	    collectionWrapper.setChanCode(chanCode);
	    collectionWrapper.setStartTime(startTime);
	    collectionWrapper.setEndTime(endTime);
	    collectionWrapper.setLat(lat);
	    collectionWrapper.setLon(lon);
	    collectionWrapper.setElev(elev);
	}

	String datasetID = netCode + "_" + staCode + "_" + chanCode + "_" + createShortID();

	IRISStationWrapper datasetWrapper = new IRISStationWrapper(ResourceType.DATASET);

	datasetWrapper.setId(datasetID);
	datasetWrapper.setParentId(collectionWrapper.getId());
	datasetWrapper.setInstrument(instrument);
	datasetWrapper.setNetCode(netCode);
	datasetWrapper.setNetDesc(netDesc);
	datasetWrapper.setStaCode(staCode);
	datasetWrapper.setStaSiteName(staSiteName);
	datasetWrapper.setChanCode(chanCode);
	datasetWrapper.setStartTime(startTime);
	datasetWrapper.setEndTime(endTime);
	datasetWrapper.setLat(lat);
	datasetWrapper.setLon(lon);
	datasetWrapper.setElev(elev);

	ArrayList<OriginalMetadata> out = Lists.newArrayList();

	if (newCollection) {

	    newCollection = false;

	    out.add(IRISStationWrapper.asOriginalMetadata(collectionWrapper));
	}

	out.add(IRISStationWrapper.asOriginalMetadata(datasetWrapper));

	return out;
    }

    private HashMap<String, String> parseNetworkDescription(InputStream stream) {

	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	HashMap<String, String> out = new HashMap<>();

	String line = "";
	boolean firstLine = true;
	while (line != null) {

	    try {
		line = reader.readLine();
		if (!firstLine && line != null) {

		    String netCode = line.split("\\|")[0];
		    String desc = line.split("\\|")[1];

		    out.put(netCode, desc);
		}
		firstLine = false;
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		line = null;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("[2/4] Done. Networks description map size: {}", out.size());

	return out;
    }

    private List<String> parseChannelList(InputStream stream) {

	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	List<String> out = new ArrayList<>();

	String channel = "";
	boolean firstLine = true;

	while (channel != null) {

	    try {
		channel = reader.readLine();
		if (!firstLine && channel != null) {
		    out.add(channel);
		}
		firstLine = false;

	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		channel = null;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("[1/4] Done. Channels list size: {}", out.size());

	return out;
    }

    private HashMap<String, String> parseStationsSiteName(InputStream stream) {

	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	HashMap<String, String> out = new HashMap<>();

	String line = "";
	boolean firstLine = true;
	while (line != null) {

	    try {
		line = reader.readLine();
		if (!firstLine && line != null) {

		    String staCode = line.split("\\|")[1];
		    String siteName = line.split("\\|")[5];

		    out.put(staCode, siteName);
		}
		firstLine = false;
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		line = null;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("[3/4] Done. Stations site name map size: {}", out.size());

	return out;
    }

    private String createChannelQuery() {

	return CHANNEL_QUERY;

    }

    private String createNetworkQuery() {

	return NETWORK_QUERY;

    }

    private String createStationQuery() {

	return STATION_QUERY;

    }

    private List<String> getChannelList() throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("[1/4] Retrieving Channel list");

	return parseChannelList(doExecRequest(createChannelQuery()));
    }

    private InputStream doExecRequest(String httpGet) throws GSException {

	try {

	    HttpResponse<InputStream> httpResponse = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, httpGet));

	    return httpResponse.body();

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IRIS_STATION_CONNECTOR_DO_EXEC_REQUEST_ERROR, //
		    e);
	}
    }

    private HashMap<String, String> getNetworksDescription() throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("[2/4] Retrieving Networks description");

	try {

	    return parseNetworkDescription(doExecRequest(createNetworkQuery()));

	} catch (GSException e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IRIS_STATION_CONNECTOR_GET_NETWORK_DESCRIPTION_ERROR, //
		    e);

	}
    }

    private HashMap<String, String> getStationsSiteName() throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("[3/4] Retrieving Stations site name");

	try {

	    return parseStationsSiteName(doExecRequest(createStationQuery()));

	} catch (GSException e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IRIS_STATION_CONNECTOR_GET_SITE_NAME_ERROR, //
		    e);
	}

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(IRISStationMapper.IRIS_STATION_SCHEMA);
	return ret;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("http://service.iris.edu/fdsnws/station");
    }

    private String createShortID() {
	String uuid = UUID.randomUUID().toString();
	uuid = uuid.replaceAll("-", "");
	uuid = uuid.replaceAll("_", "");

	StringBuilder builder = new StringBuilder();

	for (int i = 0; i < 8; i++) {

	    builder.append(uuid.charAt((int) (Math.random() * uuid.length() - 1)));

	}

	return builder.toString();
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected IRISStationConnectorSetting initSetting() {

	return new IRISStationConnectorSetting();
    }
}
