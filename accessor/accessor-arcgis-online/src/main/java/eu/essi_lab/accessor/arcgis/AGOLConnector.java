package eu.essi_lab.accessor.arcgis;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.arcgis.handler.AGOLBondHandler;
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
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class AGOLConnector extends DistributedQueryConnector<AGOLConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "AGOLConnector";

    private static final String NUM_PARAM = "num=";
    private static final String START_PARAM = "start=";
    private static final String AND = "&";

    private Logger logger = GSLoggerFactory.getLogger(AGOLConnector.class);
    private static final String RESULTS_KEY = "results";
    private static final String TOTAL_KEY = "total";

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	Page page = new Page();

	Optional<HttpResponse<InputStream>> response = retrieve(message, page);

	if (!response.isPresent()) {

	    countResponse.setCount(0);

	} else {

	    logger.trace("Extracting count");

	    Integer matches = count(response.get());

	    logger.info("Found {} matches", matches);

	    countResponse.setCount(matches);
	}

	return countResponse;
    }

    Integer count(HttpResponse<InputStream> response) throws GSException {

	JSONObject jsonObject = toJson(response);

	return jsonObject.getInt(TOTAL_KEY);
    }

    Optional<String> createRequest(ReducedDiscoveryMessage message, Page page) {

	String baseUrl = getSourceURL();

	if (!baseUrl.endsWith("?"))
	    baseUrl += "?";

	StringBuilder builder = new StringBuilder(baseUrl);

	int start = page.getStart();
	int count = page.getSize();

	appendAnd(builder, NUM_PARAM + count);
	appendAnd(builder, START_PARAM + (start - 1));

	AGOLBondHandler bondHandler = parse(message);

	if (!bondHandler.isSupported()) {

	    logger.warn("Invalid query parameters");
	    return Optional.empty();
	}

	String queryString = bondHandler.getQueryString();

	appendAnd(builder, queryString);

	return Optional.of(builder.toString());
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    AGOLBondHandler parse(ReducedDiscoveryMessage message) {

	// String ignoreString = (String) getSupportedOptions().get(GS_COMPLEX_QUERY_IGNORE_KEY).getValue();
	// boolean ignore = ignoreString == null || ignoreString.equals("yes") ? true : false;
	//
	// Optional<String> eventOrder = reducedMessage.getQuakeMLEventOrder();

	AGOLBondHandler bondHandler = new AGOLBondHandler();
	// bondHandler.setIgnoreComplexQuery(ignore);
	// if (eventOrder.isPresent()) {
	// bondHandler.setEventOrder(eventOrder.get());
	// }

	DiscoveryBondParser bondParser = getParser(message);

	logger.trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	logger.trace("Parsed reduced discovery message");

	return bondHandler;

    }

    HttpResponse<InputStream> executeGet(String uri) throws IOException, InterruptedException, URISyntaxException {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, uri));
    }

    Optional<HttpResponse<InputStream>> retrieve(ReducedDiscoveryMessage message, Page page) throws GSException {
	logger.info("Retrieving Original Metadata");

	Optional<String> finalRequest = createRequest(message, page);

	if (!finalRequest.isPresent()) {

	    return Optional.empty();
	}

	logger.trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest.get());

	    logger.trace("Original Metadata obtained");

	    return Optional.of(response);

	} catch (Exception e) {

	    throw GSException.createException(AGOLConnector.class, "AGOLConnectorRetrieveError", e);
	}
    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	Optional<HttpResponse<InputStream>> response = retrieve(message, page);

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	if (response.isPresent()) {

	    logger.trace("Extracting original metadata");

	    List<OriginalMetadata> omList = convertResponseToOriginalMD(response.get());

	    logger.trace("Creating result set");

	    rSet.setResultsList(omList);

	    logger.info("Original Metadata Retrieved (size: {})", omList.size());
	}

	return rSet;
    }

    JSONObject toJson(HttpResponse<InputStream> response) throws GSException {

	String responseString;
	try {

	    InputStream stream = response.body();

	    responseString = IOStreamUtils.asUTF8String(stream);

	} catch (IOException e) {

	    throw GSException.createException(getClass(), "AGOLConnectorToJsonError", e);
	}

	return new JSONObject(responseString);
    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse<InputStream> response) throws GSException {

	List<OriginalMetadata> list = new ArrayList<>();

	JSONObject jsonObject = toJson(response);

	JSONArray results = jsonObject.getJSONArray(RESULTS_KEY);

	results.iterator().forEachRemaining(o -> {

	    JSONObject json = (JSONObject) o;

	    OriginalMetadata original = new OriginalMetadata();

	    original.setMetadata(json.toString());

	    original.setSchemeURI(AGOLMetadataSchemas.AGOL_JSON.toString());

	    list.add(original);

	});

	return list;
    }

    private void appendAnd(StringBuilder builder, String toappend) {
	builder.append(toappend);
	builder.append(AND);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("http://www.arcgis.com/sharing/rest/search");

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected AGOLConnectorSetting initSetting() {

	return new AGOLConnectorSetting();
    }
}
