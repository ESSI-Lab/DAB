package eu.essi_lab.stress.plan.discovery;

import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.stress.plan.IStressTest;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTest implements IStressTest {

    private String searchText;

    private String bbox;

    private BBOXREL bboxrel = BBOXREL.OVERLAPS;

    private List<String> sources = new ArrayList<>();

    private String view;

    private String createRequestParameters() {
	String rid = "stresstest-" + UUID.randomUUID().toString();

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
		.append("si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,sscScore")//
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
    public HttpRequest createRequest(String host) throws URISyntaxException {
	String params = createRequestParameters();

	String path = createPath();

	String requestUrl = host + path + "?" + params;

	return HttpRequestUtils.build(HttpRequestUtils.MethodNoBody.GET, requestUrl);
    }

    @Override
    public String requestString(String host) {
	String params = createRequestParameters();

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
}
