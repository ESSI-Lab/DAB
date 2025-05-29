package eu.essi_lab.accessor.worldcereal.distributed;

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
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCollectionMapper;
import eu.essi_lab.cdk.query.DistributedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.ParentIdBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class WorldCerealGranulesConnector extends DistributedQueryConnector<WorldCerealGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "WorldcerealGranulesConnector";

    private Logger logger = GSLoggerFactory.getLogger(getClass());
    private static final String WORLDCEREAL_CONNECTOR_QUERY_ERROR = "NEXTGEOSSGRANULES_CONNECTOR_QUERY_ERROR";
    private static final String WORLDCEREAL_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT = "NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT";
    private static final String WORLDCEREAL_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT = "NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT";
    private static final String CANT_READ_RESPONSE = "Can't read response stream from NEXTGEOSS granule search";

    public final static String GRANULE_REQUEST = "items?";

    public final static String WORLDCEREAL_BASE_URL = "https://ewoc-rdm-api.iiasa.ac.at/";

    private static final String COUNT_KEY = "NumberMatched";

    private static final String RESULTS_KEY = "features";

    /**
     * 
     */

    private static final String WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID = "WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID";
    private static final String WORLDCEREAL_GRANULES_CONNECTOR_QUERY_ERROR = "WORLDCEREAL_GRANULES_CONNECTOR_QUERY_ERROR";
    private static final String WORLDCEREAL_CONNECTOR_RESPONSE_STREAM_ERROR = "WORLDCEREAL_CONNECTOR_RESPONSE_STREAM_ERROR";

    /**
     * 
     */
    public WorldCerealGranulesConnector() {
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	logger.trace("Received second-level count for WorldCereal");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	String parentid = getParentId(message);

	logger.trace("WorldCereal Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	Integer matches = 0;

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    String collectionID = "";

	    Optional<String> optionalCollectionId = readCollectionIdFromParent(parentGSResource);
	    if (optionalCollectionId.isPresent()) {
		collectionID = optionalCollectionId.get();
	    }

	    logger.trace("WorldCereal Parent Node id {}", collectionID);

	    HttpResponse response = retrieve(message, countPage(), collectionID);

	    logger.trace("Extracting count of node {}", collectionID);

	    matches = count(response);

	    logger.info("Found {} matches", matches);

	} else
	    logger.warn("Unable to find parent resource in message for Worldcereal collection {}, returning zero matches", parentid);

	countResponse.setCount(matches);

	return countResponse;

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	List<OriginalMetadata> omList = new ArrayList<>();

	logger.trace("Received second-level query for WorldCereal");

	String parentid = getParentId(message);

	logger.trace("WorldCereal Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    Optional<String> optionalID = parentGSResource.getOriginalId();
	    String collectionID = "";

	    Optional<String> optionalCollectionId = readCollectionIdFromParent(parentGSResource);
	    if (optionalCollectionId.isPresent()) {
		collectionID = optionalCollectionId.get();
	    }

	    logger.trace("WorldCereal Parent Node id {}", collectionID);

	    HttpResponse response = retrieve(message, page, collectionID);

	    logger.trace("Extracting WorldCereal original metadata");

	    omList = convertResponseToOriginalMD(response);

	} else {

	    logger.warn("Unable to find parent resource in message for WorldCereal collection {}, returning zero results", parentid);
	}

	logger.trace("Creating WorldCereal result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	logger.info("WorldCereal Result set created (size: {})", omList.size());

	return rSet;
    }

    String getParentId(ReducedDiscoveryMessage message) throws GSException {

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getReducedBond());

	ParentIdBondHandler parentIdBondHandler = new ParentIdBondHandler();

	bondParser.parse(parentIdBondHandler);

	if (!parentIdBondHandler.isParentIdFound())
	    throw GSException.createException(getClass(), "No Parent Identifier specified to cmr second-level search", null,
		    ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID);

	return parentIdBondHandler.getParentValue();
    }

    Integer count(HttpResponse<InputStream> response) throws GSException {

	JSONObject json = toJson(response);

	return WorldCerealCollectionMapper.readInt(json, COUNT_KEY).orElse(0);
    }

    JSONObject toJson(HttpResponse<InputStream> response) throws GSException {

	String responseString;
	try {

	    InputStream stream = response.body();

	    responseString = IOStreamUtils.asUTF8String(stream);

	} catch (IOException e) {

	    logger.error("Can't read response stream from WorldCereal", e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, //
		    WORLDCEREAL_CONNECTOR_RESPONSE_STREAM_ERROR, //
		    e);
	}

	return new JSONObject(responseString);
    }

    Optional<String> readCollectionIdFromParent(GSResource parentGSResource) {

	return parentGSResource.getExtensionHandler().getSTACSecondLevelInfo();
    }

    private Page countPage() {

	return new Page(1, 1);
    }

    HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, get));
    }

    String createRequest(ReducedDiscoveryMessage message, Page page, String datasetId) {

	WorldCerealGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), datasetId);

	StringBuilder builder = new StringBuilder(WORLDCEREAL_BASE_URL + "collections/" + datasetId + "/items?");
	builder.append(bondHandler.getQueryString());

	return builder.toString();
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    WorldCerealGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int count, String datasetId) {

	WorldCerealGranulesBondHandler bondHandler = new WorldCerealGranulesBondHandler(datasetId);

	bondHandler.setStart(start);

	bondHandler.setCount(count);

	DiscoveryBondParser bondParser = getParser(message);

	logger.trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	logger.trace("Parsed reduced discovery message");

	return bondHandler;
    }

    HttpResponse retrieve(ReducedDiscoveryMessage message, Page page, String datasetId) throws GSException {

	logger.info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, datasetId);

	logger.trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest);

	    logger.trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WORLDCEREAL_GRANULES_CONNECTOR_QUERY_ERROR, //
		    e);

	}
    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse<InputStream> response) throws GSException {

	List<OriginalMetadata> list = new ArrayList<>();

	JSONObject json = toJson(response);

	JSONArray array = json.getJSONArray(RESULTS_KEY);

	for (int i = 0; i < array.length(); i++) {

	    JSONObject j = array.getJSONObject(i);

	    OriginalMetadata om = new OriginalMetadata();

	    om.setSchemeURI(WorldCerealGranulesMapper.WORLDCEREAL_GRANULES_SCHEME_URI);

	    om.setMetadata(j.toString());

	    list.add(om);
	}

	return list;
    }

    /**
     * This always returns false because this connector is supposed to be used anly in a mixed configuration
     *
     * @param source
     * @return
     */
    @Override
    public boolean supports(GSSource source) {
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected WorldCerealGranulesConnectorSetting initSetting() {

	return new WorldCerealGranulesConnectorSetting();
    }

}
