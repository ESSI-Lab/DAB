package eu.essi_lab.accessor.obis.distributed;

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

import eu.essi_lab.accessor.obis.harvested.OBISResourceMapper;
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
 * @author ilsanto
 */
public class OBISGranulesConnector extends DistributedQueryConnector<OBISGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "OBISGranulesConnector";

    private Logger logger = GSLoggerFactory.getLogger(getClass());
    private static final String OBIS_GRANULES_NO_PARENT_ERR_ID = "OBIS_GRANULES_NO_PARENT_ERR_ID";
    private static final String OBISGRANULES_CONNECTOR_QUERY_ERROR = "OBISGRANULES_CONNECTOR_QUERY_ERROR";
    private static final String OBIS_CONNECTOR_RESPONSE_STREAM_ERROR = "OBIS_CONNECTOR_RESPONSE_STREAM_ERROR";
    private static final String COUNT_KEY = "total";

    private static final String RESULTS_KEY = "results";

    /**
     * 
     */
    private static final String OCCURRENCE_URL = "https://api.obis.org/v3/occurrence?";

    /**
     * 
     */
    public OBISGranulesConnector() {
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	logger.trace("Received second-level count for OBIS");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	String parentid = getParentId(message);

	logger.trace("OBIS Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	Integer matches = 0;

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    String datasetId = parentGSResource.getOriginalId().get();

	    logger.trace("OBIS Parent Node id {}", datasetId);

	    HttpResponse response = retrieve(message, countPage(), datasetId);

	    logger.trace("Extracting count of node {}", datasetId);

	    matches = count(response);

	    logger.info("Found {} matches", matches);

	} else
	    logger.warn("Unable to find parent resource in message for OBIS collection {}, returning zero matches", parentid);

	countResponse.setCount(matches);

	return countResponse;

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	List<OriginalMetadata> omList = new ArrayList<>();

	logger.trace("Received second-level query for OBIS");

	String parentid = getParentId(message);

	logger.trace("OBIS Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    String datasetId = parentGSResource.getOriginalId().get();

	    logger.trace("OBIS Parent Node id {}", datasetId);

	    HttpResponse response = retrieve(message, page, datasetId);

	    logger.trace("Extracting OBIS original metadata");

	    omList = convertResponseToOriginalMD(response);

	} else {

	    logger.warn("Unable to find parent resource in message for OBIS collection {}, returning zero results", parentid);
	}

	logger.trace("Creating OBIS result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	logger.info("OBIS Result set created (size: {})", omList.size());

	return rSet;
    }

    String getParentId(ReducedDiscoveryMessage message) throws GSException {

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getReducedBond());

	ParentIdBondHandler parentIdBondHandler = new ParentIdBondHandler();

	bondParser.parse(parentIdBondHandler);

	if (!parentIdBondHandler.isParentIdFound())
	    throw GSException.createException(getClass(), "No Parent Identifier specified to cmr second-level search", null,
		    ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, OBIS_GRANULES_NO_PARENT_ERR_ID);

	return parentIdBondHandler.getParentValue();
    }

    Integer count(HttpResponse<InputStream> response) throws GSException {

	JSONObject json = toJson(response);

	return OBISResourceMapper.readInt(json, COUNT_KEY).orElse(0);
    }

    JSONObject toJson(HttpResponse<InputStream> response) throws GSException {

	String responseString;
	try {

	    InputStream stream = response.body();

	    responseString = IOStreamUtils.asUTF8String(stream);

	} catch (IOException e) {

	    logger.error("Can't read response stream from OBIS", e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, //
		    OBIS_CONNECTOR_RESPONSE_STREAM_ERROR, //
		    e);
	}

	return new JSONObject(responseString);
    }

    private Page countPage() {

	return new Page(1, 1);
    }

    HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, get));
    }

    String createRequest(ReducedDiscoveryMessage message, Page page, String datasetId) {

	OBISGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), datasetId);

	StringBuilder builder = new StringBuilder(OCCURRENCE_URL);
	builder.append(bondHandler.getQueryString());

	return builder.toString();
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    OBISGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int count, String datasetId) {

	OBISGranulesBondHandler bondHandler = new OBISGranulesBondHandler(datasetId);

	bondHandler.setStart(start);

	bondHandler.setCount(count);

	DiscoveryBondParser bondParser = getParser(message);

	logger.trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	logger.trace("Parsed reduced discovery message");

	return bondHandler;
    }

    HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String datasetId) throws GSException {

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
		    OBISGRANULES_CONNECTOR_QUERY_ERROR, //
		    e);

	}
    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse response) throws GSException {

	List<OriginalMetadata> list = new ArrayList<>();

	JSONObject json = toJson(response);

	JSONArray array = json.getJSONArray(RESULTS_KEY);

	for (int i = 0; i < array.length(); i++) {

	    JSONObject j = array.getJSONObject(i);

	    OriginalMetadata om = new OriginalMetadata();

	    om.setSchemeURI(OBISGranulesResultMapper.OBIS_GRANULES_SCHEME_URI);

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
    protected OBISGranulesConnectorSetting initSetting() {

	return new OBISGranulesConnectorSetting();
    }
}
