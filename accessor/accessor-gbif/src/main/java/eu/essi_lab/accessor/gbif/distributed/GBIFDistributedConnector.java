package eu.essi_lab.accessor.gbif.distributed;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import static eu.essi_lab.accessor.gbif.distributed.GBIFMapper.GBIFOCCURRENCE_SCHEMA;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.gbif.GBIFQueryHandler;
import eu.essi_lab.accessor.gbif.GBIFUtils;
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
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class GBIFDistributedConnector extends DistributedQueryConnector<GBIFDistributedConnectorSetting> {

    static final String TOTAL_KEY = "count";
    private static final String NUM_PARAM = "limit=";
    private static final String START_PARAM = "offset=";
    private static final String AND = "&";
    private static final String RESULTS_KEY = "results";
    private static final String GBIF_RETRIEVE_ERROR = "GBIF_RETRIEVE_ERROR";
    private static final String GBIF_CONNECTOR_RESPONSE_STREAM_ERROR = "GBIF_CONNECTOR_RESPONSE_STREAM_ERROR";

    private static final int MAX_OFFSET = 200000;

    public static final String CONNECTOR_TYPE = "GBIFDistributedConnector";

    public GBIFDistributedConnector() {

	super();
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	Optional<String> datasetKey = findDatasetKey(message);

	if (datasetKey.isPresent()) {

	    Optional<HttpResponse> response = retrieve(message, datasetKey.get(), new Page());

	    if (!response.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).trace("No matches found");

		countResponse.setCount(0);

	    } else {

		int matches = count(response.get());

		GSLoggerFactory.getLogger(getClass()).info("Found {} matches", matches);

		countResponse.setCount(matches);
	    }
	}

	return countResponse;
    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	ResultSet<OriginalMetadata> out = new ResultSet<>();

	Optional<String> datasetKey = findDatasetKey(message);

	Optional<HttpResponse> response = retrieve(message, datasetKey.get(), page);

	if (response.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Creating result set of original metadata");

	    List<OriginalMetadata> list = convertResponseToOriginalMD(response.get());

	    out.setResultsList(list);

	    GSLoggerFactory.getLogger(getClass()).info("Response size: ", list.size());
	}

	return out;
    }

    Integer count(HttpResponse response) throws GSException {

	JSONObject jsonObject = toJson(response);

	return jsonObject.getInt(TOTAL_KEY);
    }

    @Override
    public boolean supports(GSSource source) {

	return GBIFUtils.supportsSource(source);
    }

    Optional<String> createRequest(ReducedDiscoveryMessage message, String datasetKey, Page page) {

	String suffix = getSetting().getOccurrenceSearchPath();

	String baseUrl = GBIFUtils.refineBaseUrl(getSourceURL()) + suffix + "?datasetKey=" + datasetKey + "&";

	StringBuilder builder = new StringBuilder(baseUrl);

	int start = page.getStart();
	int count = page.getSize();

	if (start > MAX_OFFSET) {
	    GSLoggerFactory.getLogger(getClass()).warn("Invalid offset: {}", start);
	    start = MAX_OFFSET - count;
	    GSLoggerFactory.getLogger(getClass()).warn("Offset reduced to {}", start);
	}

	appendAnd(builder, NUM_PARAM + count);
	appendAnd(builder, START_PARAM + (start - 1));

	GBIFQueryHandler bondHandler = parse(message);

	if (bondHandler.isUnsupported()) {

	    return Optional.empty();
	}

	String queryString = bondHandler.getSearchTerms();

	builder.append(queryString);

	return Optional.of(builder.toString());
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {

	return new DiscoveryBondParser(message.getReducedBond());
    }

    GBIFQueryHandler parse(ReducedDiscoveryMessage message) {

	GBIFQueryHandler bondHandler = new GBIFQueryHandler();

	DiscoveryBondParser bondParser = getParser(message);

	GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

	return bondHandler;
    }

    HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, get));
    }

    Optional<HttpResponse> retrieve(ReducedDiscoveryMessage message, String datasetKey, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	Optional<String> finalRequest = createRequest(message, datasetKey, page);

	if (!finalRequest.isPresent()) {

	    return Optional.empty();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse response = executeGet(finalRequest.get());

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return Optional.of(response);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GBIF_RETRIEVE_ERROR, //
		    e);
	}
    }

    JSONObject toJson(HttpResponse<InputStream> response) throws GSException {

	String responseString;
	try {

	    InputStream stream = response.body();

	    responseString = IOStreamUtils.asUTF8String(stream);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GBIF_CONNECTOR_RESPONSE_STREAM_ERROR, //
		    e);
	}

	return new JSONObject(responseString);
    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse response) throws GSException {

	List<OriginalMetadata> list = new ArrayList<>();

	JSONObject jsonObject = toJson(response);

	JSONArray results = jsonObject.getJSONArray(RESULTS_KEY);

	results.iterator().forEachRemaining(o -> {

	    JSONObject json = (JSONObject) o;

	    OriginalMetadata original = new OriginalMetadata();

	    original.setMetadata(json.toString());

	    original.setSchemeURI(GBIFOCCURRENCE_SCHEMA);

	    list.add(original);
	});

	return list;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private Optional<String> findDatasetKey(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level request");

	Optional<String> parentId = ParentIdBondHandler.readParentId(message);

	if (parentId.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Parent id {}", parentId.get());

	    Optional<GSResource> parent = message.getParentGSResource(parentId.get());

	    if (parent.isPresent()) {

		GSResource parentRes = parent.get();

		// e.g.: https://www.gbif.org/dataset/5888c533-f265-41c3-9078-bf0630ef4aa7
		String originalId = parentRes.getOriginalId().get();

		String datasetKey = originalId.substring(originalId.lastIndexOf("/") + 1, originalId.length());

		GSLoggerFactory.getLogger(getClass()).trace("Dataset key found: " + datasetKey);

		return Optional.of(datasetKey);

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Parent id not found");
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Dataset key not found");

	return Optional.empty();
    }

    private void appendAnd(StringBuilder builder, String toAppend) {
	builder.append(toAppend);
	builder.append(AND);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getType() {

	return CONNECTOR_TYPE;
    }

    @Override
    protected GBIFDistributedConnectorSetting initSetting() {

	return new GBIFDistributedConnectorSetting();
    }
}
