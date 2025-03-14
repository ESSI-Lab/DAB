package eu.essi_lab.stress.plan.discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.stress.plan.IStressTest;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTest implements IStressTest {

    private String searchText;

    private String bbox;

    private BBOXREL bboxrel = BBOXREL.OVERLAPS;

    private List<String> sources = new ArrayList<>();

    private String view;

    private String createRequestParameters(String rid) {

	String st = "";
	if (searchText != null)
	    st = searchText;

	String boundignbox = "";
	if (bbox != null)
	    boundignbox = bbox;

	String viewid = "";
	if (view != null)
	    viewid = view;

	StringBuilder builder = new StringBuilder("");

	builder.append("searchFields=title,keywords")//
		.append("&")//
		.append("reqID=").append(rid)//
		.append("&")//
		.append("si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,sscScore").append("&")//
		.append("rel=").append(bboxrel)//
		.append("&")//
		.append("viewid=").append(viewid)//
		.append("&")//
		.append("st=").append(st)//
		.append("&")//
		.append("sources=")//
		.append("&bbox=").append(boundignbox);

	sources.stream().forEach(s -> builder.append(s).append(","));

	return builder.toString();
    }

    @Override
    public String createTestKey() {
	StringBuilder contraintsBuilder = new StringBuilder();

	if (getSearchText() != null)
	    contraintsBuilder.append("searchtext").append("__");

	if (getBbox() != null)
	    contraintsBuilder.append("bbox__").append(getBboxrel()).append("__");

	if (getView() != null)
	    contraintsBuilder.append("view__").append(getView()).append("__");

	contraintsBuilder.append("n_sources=").append(getSources().size());

	String testcontraints = contraintsBuilder.toString();

	return testcontraints;
    }

    public String getSearchText() {
	return searchText;
    }

    public void setSearchText(String searchText) {
	this.searchText = searchText;
    }

    public String getBbox() {
	return bbox;
    }

    public void setBbox(String bbox) {
	this.bbox = bbox;
    }

    public List<String> getSources() {
	return sources;
    }

    public void addSource(String source) {
	getSources().add(source);
    }

    public enum BBOXREL {
	OVERLAPS,
	CONTAINS;
    }

    public BBOXREL getBboxrel() {
	return bboxrel;
    }

    public void setBboxrel(BBOXREL bboxrel) {
	this.bboxrel = bboxrel;
    }

    public String getView() {
	return view;
    }

    public void setView(String view) {
	this.view = view;
    }

    private String createPath() {
	return "/gs-service/services/opensearch/query";
    }

    @Override
    public HttpRequest createRequest(String host, String rid) throws URISyntaxException {
	String params = createRequestParameters(rid);

	String path = createPath();

	String requestUrl = host + path + "?" + params;

	return HttpRequestUtils.build(HttpRequestUtils.MethodNoBody.GET, requestUrl);
    }

    @Override
    public String requestString(String host, String rid) {
	String params = createRequestParameters(rid);

	String path = createPath();

	String requestUrl = host + path + "?" + params;

	return requestUrl;
    }

    @Override
    public String getResponseFileExtension() {
	return ".xml";
    }

    @Override
    public List<String> getResponseMetrics() {

	return List.of("resultsnum");
    }

    @Override
    public Long readMetric(String metric, String filePath) {

	if ("resultsnum".equalsIgnoreCase(metric)) {

	    try {
		XMLDocumentReader reader = new XMLDocumentReader(new FileInputStream(new File(filePath)));

		Number results = reader.evaluateNumber("/*:feed/*:totalResults");

		Long value = results.longValue();

		return value;
	    } catch (SAXException | IOException | XPathExpressionException e) {
		e.printStackTrace();

	    }

	}
	return 0L;

    }

    @Override
    public List<String> getServerMetrics() {

	return List.of("MESSAGE_AUTHORIZATION", "BOND_NORMALIZATION", "OPENSEARCH_FINDER_GET_SOURCES_DATA_DIR_MAP",
		"OPENSEARCH_FINDER_COUNT", "RESULT_SET_COUNTING", "OPENSEARCH_FINDER_GET_SOURCES_DATA_DIR_MAP",
		"OPENSEARCH_FINDER_DISCOVERY", "OPENSEARCH_FINDER_RESOURCES_CREATION", "RESULT_SET_RETRIEVING",
		"RESULT_SET_OVERALL_RETRIEVING", "RESULT_SET_MAPPING", "RESULT_SET_FORMATTING", "REQUEST_HANDLING");
    }

    private Map<String, FilterLogEventsResponse> responses = new HashMap<>();

    @Override
    public Long readServerMetric(String serverMetric, String requestId, String logGroup, String logNamePrefix) {
	CloudWatchLogsClient client = CloudWatchLogsClient.builder().region(Region.US_EAST_1).credentialsProvider(
		ProfileCredentialsProvider.create()
	).build();

	FilterLogEventsResponse response = responses.get(requestId);
	if (response == null) {
	    Long now = Instant.now().toEpochMilli();
	    FilterLogEventsRequest request =
		    FilterLogEventsRequest.builder().filterPattern("%." + requestId + ".%")
			    .logGroupName(logGroup)
			    .logStreamNamePrefix(logNamePrefix)
			    .startTime(now - (1000L * 60 * 60 * 1))
			    .endTime(now)

			    .build();
	    response = client.filterLogEvents(request);
	    responses.put(requestId, response);

	}

	return readCloudWatchMetric(serverMetric, response);

    }

    private Long readCloudWatchMetric(String metric, FilterLogEventsResponse response) {

	Optional<Long> val = response.events().stream().map(event -> {
	    String evstring = event.toString();
	    if (evstring.contains(metric)) {
		String time = evstring.split(metric + "] \\[")[1].split("] \\[secs]")[0];

		Long ltime = Long.valueOf(time.replace(".", ""));

		return ltime;
	    }

	    return Long.valueOf("0");
	}).filter(v -> v > 0).findFirst();

	return val.orElse(-10L);
    }
}
