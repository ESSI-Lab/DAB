package eu.essi_lab.accessor.copernicus.dataspace.distributed;

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
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.copernicus.dataspace.harvested.CopernicusDataspaceConnector;
import eu.essi_lab.accessor.satellite.common.SatelliteUtils;
import eu.essi_lab.cdk.query.DistributedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.ParentIdBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class CopernicusDataspaceGranulesConnector extends DistributedQueryConnector<CopernicusDataspaceGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CopernicusDataspaceGranulesConnector";

    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_QUERY_ERROR = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_QUERY_ERROR";
    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_ERR_ID_RETRIEVE = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_ERR_ID_RETRIEVE";
    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT";
    private static final String CANT_READ_RESPONSE = "Can't read response stream from Copernicus Dataspace granule search";
    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR";
    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR";
    private static final String COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR = "COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR";

    public final static String COPERNICUS_DATASPACE_TEMPLATE_URL = "maxRecords={count?}&index={startIndex?}&page={startPage?}&box={geo:box?}&startDate={time:start?}&completionDate={time:end?}&sortParam={resto:sortParam?}&sortOrder={resto:sortOrder?}&productType={eo:productType?}&processingLevel={eo:processingLevel?}&platform={eo:platform?}&instrument={eo:instrument?}&sensorMode={eo:sensorMode?}&orbitNumber={eo:orbitNumber?}&exactCount={resto:exactCount?}&orbitDirection={eo:orbitDirection?}&relativeOrbitNumber={sentinel:relativeOrbitNumber?}&cloudCover={eo:cloudCover?}&timeliness={sentinel:timeliness?}&polarisation={resto:polarisation?}&swath={resto:swath?}&";

    public final static String COPERNICUS_DATASPACE_BASE_URL = "https://catalogue.dataspace.copernicus.eu/resto/api/collections/";

    public final static String COPERNICUS_DATASPACE_REQUEST = "search.json?";

    private static final String SENTINEL1_REQUEST = "Sentinel1";
    private static final String SENTINEL2_REUQEST = "Sentinel2";
    private static final String SENTINEL3_REUQEST = "Sentinel3";

    private final InputStream SENTINEL_1 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("termFrequency/sentinel1.xml");
    private final InputStream SENTINEL_2 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("termFrequency/sentinel2.xml");
    private final InputStream SENTINEL_3 = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("termFrequency/sentinel3.xml");

    private final InputStream SENTINEL_1_ESTIMATED_COUNT = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("count/sentinel1.json");
    private final InputStream SENTINEL_2_ESTIMATED_COUNT = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("count/sentinel2.json");
    private final InputStream SENTINEL_3_ESTIMATED_COUNT = CopernicusDataspaceConnector.class.getClassLoader()
	    .getResourceAsStream("count/sentinel3.json");

    private ClonableInputStream s1Stream;
    private ClonableInputStream s2Stream;
    private ClonableInputStream s3Stream;

    private ClonableInputStream s1estimatedCountStream;
    private ClonableInputStream s2estimatedCountStream;
    private ClonableInputStream s3estimatedCountStream;

    /**
     * 
     */
    public CopernicusDataspaceGranulesConnector() {
	try {
	    s1Stream = new ClonableInputStream(SENTINEL_1);
	    s2Stream = new ClonableInputStream(SENTINEL_2);
	    s3Stream = new ClonableInputStream(SENTINEL_3);
	    s1estimatedCountStream = new ClonableInputStream(SENTINEL_1_ESTIMATED_COUNT);
	    s2estimatedCountStream = new ClonableInputStream(SENTINEL_2_ESTIMATED_COUNT);
	    s3estimatedCountStream = new ClonableInputStream(SENTINEL_3_ESTIMATED_COUNT);
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(SatelliteUtils.class).error(e.getMessage(), e);
	}

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level query for Copernicus Dataspace");

	Optional<String> parentId = ParentIdBondHandler.readParentId(message);

	List<OriginalMetadata> omList = new ArrayList<>();

	if (parentId.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Copernicus Dataspace Parent id {}", parentId.get());

	    Optional<GSResource> parent = message.getParentGSResource(parentId.get());

	    if (parent.isPresent()) {

		GSResource parentGSResource = parent.get();

		try {
		    OriginalMetadata om = parentGSResource.getOriginalMetadata();

		    XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }

		    String url = COPERNICUS_DATASPACE_BASE_URL;
		    boolean isSentinel3 = false;
		    if (fileIdentifier.endsWith("SENTINEL_1")) {
			url += SENTINEL1_REQUEST + "/";
		    } else if (fileIdentifier.endsWith("SENTINEL_2")) {
			url += SENTINEL2_REUQEST + "/";
		    } else if (fileIdentifier.endsWith("SENTINEL_3")) {
			url += SENTINEL3_REUQEST + "/";
			isSentinel3 = true;
		    } else {
			// error: no use-case for now
		    }
		    url += COPERNICUS_DATASPACE_REQUEST + COPERNICUS_DATASPACE_TEMPLATE_URL;
		    GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		    GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		    HttpResponse<InputStream> response = retrieve(message, page, fileIdentifier, url, false, isSentinel3);

		    GSLoggerFactory.getLogger(getClass()).trace("Extracting original metadata");

		    omList = convertResponseToOriginalMD(response);

		} catch (XPathExpressionException e) {

		    GSLoggerFactory.getLogger(getClass())
			    .error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

		} catch (SAXException e) {
		    GSLoggerFactory.getLogger(getClass())
			    .error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass())
			    .error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);
		}

	    } else {
		GSLoggerFactory.getLogger(getClass()).warn(
			"Unable to find parent resource in message for Copernicus Dataspace collection {}, returning zero matches",
			parentId.get());
	    }

	}
	GSLoggerFactory.getLogger(getClass()).trace("Creating result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	GSLoggerFactory.getLogger(getClass()).info("Result set created (size: {})", omList.size());

	return rSet;

    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level count for Copernicus Dataspace");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	Optional<String> parentId = ParentIdBondHandler.readParentId(message);

	Integer matches = 0;

	InputStream staticTermFrequencyStream = null;
	InputStream estimatedCountStream = null;

	if (parentId.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Copernicus Dataspace Parent id {}", parentId.get());

	    Optional<GSResource> parent = message.getParentGSResource(parentId.get());

	    if (parent.isPresent()) {

		GSResource parentGSResource = parent.get();
		try {
		    OriginalMetadata om = parentGSResource.getOriginalMetadata();

		    XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }

		    String url = COPERNICUS_DATASPACE_BASE_URL;
		    boolean isSentinel3 = false;
		    if (fileIdentifier.endsWith("SENTINEL_1")) {
			url += SENTINEL1_REQUEST + "/";
			staticTermFrequencyStream = s1Stream.clone();
			estimatedCountStream = s1estimatedCountStream.clone();
		    } else if (fileIdentifier.endsWith("SENTINEL_2")) {
			url += SENTINEL2_REUQEST + "/";
			staticTermFrequencyStream = s2Stream.clone();
			estimatedCountStream = s2estimatedCountStream.clone();
		    } else if (fileIdentifier.endsWith("SENTINEL_3")) {
			url += SENTINEL3_REUQEST + "/";
			staticTermFrequencyStream = s3Stream.clone();
			estimatedCountStream = s3estimatedCountStream.clone();
			isSentinel3 = true;
		    } else {
			// error: no use-case for now
		    }

		    url += COPERNICUS_DATASPACE_REQUEST + COPERNICUS_DATASPACE_TEMPLATE_URL;
		    GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		    HttpResponse<InputStream> response = retrieve(message, countPage(), fileIdentifier, url, true, isSentinel3);

		    GSLoggerFactory.getLogger(getClass()).trace("Extracting count");

		    //
		    // count mode: fixed/dynamic
		    //
		    boolean fixedCountMode = isFixedCountMode();

		    //
		    // value used for fixed count mode or as fallback value in case the dynamic count fails due to a
		    // timeout
		    //
		    int fixedCountValue = getFixedCountValue();

		    if (response != null) {

			if (fixedCountMode) {

			    matches = fixedCountValue;

			} else {

			    InputStream stream = response.body();
			    ClonableInputStream cis = new ClonableInputStream(stream);

			    JSONObject jsonObj = new JSONObject(IOStreamUtils.asUTF8String(cis.clone()));
			    String error = jsonObj.optString("TIMEOUT_ERROR");

			    if (!error.isEmpty()) {
				matches = fixedCountValue; // count(estimatedCountStream);
			    } else {
				matches = count(cis.clone());
			    }
			}
		    }

		    GSLoggerFactory.getLogger(getClass()).info("Found {} matches", matches);

		    GSLoggerFactory.getLogger(getClass()).info("START Creating Static TFMap");

		    if (staticTermFrequencyStream != null) {

			TermFrequencyMap map = TermFrequencyMap.create(staticTermFrequencyStream);

			for (String target : map.getTargets()) {
			    List<TermFrequencyItem> items = map.getItems(TermFrequencyTarget.fromValue(target));
			    items.forEach(item -> item.setFreq(fixedCountValue));
			}

			// TermFrequencyMap map = new TermFrequencyMap(new TermFrequencyMapType());
			countResponse.setTermFrequencyMap(map);
		    }

		    GSLoggerFactory.getLogger(getClass()).info("END Creating Static TFMap");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

	    } else {
		GSLoggerFactory.getLogger(getClass()).warn(
			"Unable to find parent resource in message for Copernicus Dataspace collection {}, returning zero matches",
			parentId.get());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Creating Copernicus Dataspace count set");

	countResponse.setCount(matches);

	return countResponse;

    }

    /**
     * count mode: fixed/dynamic
     * 
     * @return
     */
    private int getFixedCountValue() {

	Properties properties = getSetting().getKeyValueOptions().orElse(new Properties());
	return Integer.valueOf(properties.getOrDefault("fixedCountValue", "100").toString());
    }

    /**
     * value used for fixed count mode or as fallback value in case the dynamic count fails due to a timeout
     * 
     * @return
     */
    private boolean isFixedCountMode() {

	Properties properties = getSetting().getKeyValueOptions().orElse(new Properties());
	String countMode = properties.getOrDefault("countMode", "fixed").toString();
	return countMode.equals("fixed");
    }

    /**
     * to use with dynamic count mode, limits the results starting from the given ISO8601 date time
     * (e.g.: 2024-10-16T10:00:00Z)
     * 
     * @return
     */
    private Optional<String> getFixedTemporalConstraint() {

	Properties properties = getSetting().getKeyValueOptions().orElse(new Properties());
	String fixedTemporalConstraint = properties.getOrDefault("fixedTemporalConstraint", "none").toString();

	if (fixedTemporalConstraint.equals("none")) {

	    return Optional.empty();
	}

	if (fixedTemporalConstraint.equals("lastYear")) {

	    long lastYear = (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(364));
	    return Optional.of(ISO8601DateTimeUtils.getISO8601DateTime(new Date(lastYear)));
	}

	return Optional.of(fixedTemporalConstraint);
    }

    private JSONObject createResponseReader(HttpResponse<InputStream> response) throws GSException {

	JSONObject ret = null;
	try {
	    InputStream stream = response.body();
	    ret = new JSONObject(IOStreamUtils.asUTF8String(stream));

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(//
		    getClass(), //
		    CANT_READ_RESPONSE, //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR, //
		    e);

	}

	return ret;

    }

    private Integer count(InputStream response) throws GSException {

	int totResults = 0;

	JSONObject responseObject;
	try {
	    responseObject = new JSONObject(IOStreamUtils.asUTF8String(response));

	    if (responseObject != null) {
		JSONObject properties = responseObject.optJSONObject("properties");
		if (properties != null) {
		    totResults = properties.optInt("totalResults");
		}
	    }

	    if (response != null)
		response.close();

	} catch (JSONException | IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, COPERNICUS_DATASPACE_GRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR, e);

	}

	return totResults;
    }

    private String createRequest(ReducedDiscoveryMessage message, Page page, String productType, String templateUrl, boolean isCount,
	    boolean isSentinel3) {

	CopernicusDataspaceGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), productType, templateUrl,
		isCount, isSentinel3);

	return bondHandler.getQueryString();

    }

    private HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(get);
    }

    private static class HttpGetCallable implements Callable<HttpResponse<InputStream>> {
	private final Downloader httpClient;
	private final String url;

	public HttpGetCallable(Downloader httpClient, String url) {
	    this.httpClient = httpClient;
	    this.url = url;
	}

	@Override
	public HttpResponse<InputStream> call() throws Exception {

	    return httpClient.downloadResponse(this.url);
	}
    }

    private HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String datasetId, String templateUrl,
	    boolean isCount, boolean isSentinel3) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, datasetId, templateUrl, isCount, isSentinel3);

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = null;
	    if (isCount) {
		response = executeHttpGetWithTimeout(finalRequest);
	    } else {
		response = executeGet(finalRequest);
	    }

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(finalRequest, e);

	    throw GSException.createException(getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    COPERNICUS_DATASPACE_GRANULES_CONNECTOR_ERR_ID_RETRIEVE, //
		    e);

	}
    }

    private DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    private CopernicusDataspaceGranulesBondHandler parse(//
	    ReducedDiscoveryMessage message, //
	    int start, //
	    int count, //
	    String productType, //
	    String templateUrl, //
	    boolean isCount, //
	    boolean isSentinel3) {

	CopernicusDataspaceGranulesBondHandler bondHandler = new CopernicusDataspaceGranulesBondHandler(templateUrl);

	if (!isCount) {

	    bondHandler.setStart(start);

	    if ((start - 1) % 10 == 0 || start % 10 == 0) {
		start = (start / 10) + 1;
	    } else if ((start - 1) % 12 == 0 || start % 12 == 0) {
		start = (start / 12) + 1;
	    } else {
		// default page
		start = 1;
	    }

	} else if (!isFixedCountMode()) {

	    bondHandler.setExactCount(true);

	    // if (isSentinel3)
	    // bondHandler.setStartTime("2023-01-01");
	}

	bondHandler.setSort("startDate");
	bondHandler.setOrder("descending");

	// bondHandler.setProductType(productType);

	if (count != 0) {
	    bondHandler.setCount(count);
	}

	Optional<String> temporalConstraint = getFixedTemporalConstraint();
	if (temporalConstraint.isPresent()) {

	    bondHandler.setStartTime(temporalConstraint.get());
	}

	DiscoveryBondParser bondParser = getParser(message);

	GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

	return bondHandler;

    }

    private List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse<InputStream> response) throws GSException {

	List<OriginalMetadata> ret = new ArrayList<OriginalMetadata>();

	JSONObject jsonRes = createResponseReader(response);

	if (jsonRes != null) {

	    JSONArray granulesObj = jsonRes.optJSONArray("features");

	    for (int i = 0; i < granulesObj.length(); i++) {
		OriginalMetadata original = new OriginalMetadata();
		original.setMetadata(granulesObj.get(i).toString());
		original.setSchemeURI(CopernicusDataspaceGranulesMetadataSchemas.JSON_COPERNICUS_DATASPACE.toString());
		ret.add(original);
	    }
	}

	return ret;
    }

    private Page countPage() {

	return new Page(1, 1);
    }

    private HttpResponse<InputStream> executeHttpGetWithTimeout(String url) {

	// RequestConfig requestConfig = RequestConfig.custom().
	// setConnectTimeout(10_000) // 10 seconds connection timeout
	// .setSocketTimeout(10_000) // 10 seconds socket timeout
	// .build();
	//
	// CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 10);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 10);

	ExecutorService executor = Executors.newSingleThreadExecutor();
	FutureTask<HttpResponse<InputStream>> futureTask = new FutureTask<>(new HttpGetCallable(downloader, url));

	executor.execute(futureTask);

	try {

	    return futureTask.get(10, TimeUnit.SECONDS); // 10 seconds timeout

	} catch (Exception e) {

	    // try {
	    // httpClient.close();
	    // } catch (IOException ioException) {
	    // GSLoggerFactory.getLogger(getClass()).error(ioException);
	    // }

	    return createDefaultResp();

	} finally {
	    executor.shutdown();
	    // try {
	    // httpClient.close();
	    // } catch (Exception e) {
	    // e.printStackTrace();
	    // }
	}
    }

    private static HttpResponse<InputStream> createDefaultResp() {

	return new HttpResponse<InputStream>() {

	    @Override
	    public int statusCode() {

		return 200;
	    }

	    @Override
	    public HttpRequest request() {

		return null;
	    }

	    @Override
	    public Optional<HttpResponse<InputStream>> previousResponse() {

		return Optional.empty();
	    }

	    @Override
	    public HttpHeaders headers() {

		return HttpHeaderUtils.buildEmpty();
	    }

	    @Override
	    public InputStream body() {

		return IOUtils.toInputStream("{\"TIMEOUT_ERROR\": \"TRUE\"}", StandardCharsets.UTF_8);
	    }

	    @Override
	    public Optional<SSLSession> sslSession() {

		return Optional.empty();
	    }

	    @Override
	    public URI uri() {

		return null;
	    }

	    @Override
	    public Version version() {

		return Version.HTTP_1_1;
	    }
	};
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
    protected CopernicusDataspaceGranulesConnectorSetting initSetting() {

	return new CopernicusDataspaceGranulesConnectorSetting();
    }

}
