package eu.essi_lab.stress.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressTest {

    private String searchText;

    private String bbox;

    private BBOXREL bboxrel = BBOXREL.OVERLAPS;

    private List<String> sources = new ArrayList<>();

    public String createRequestParameters() {
	String rid = "stresstest-" + UUID.randomUUID().toString();

	String st = "";
	if (searchText != null)
	    st = searchText;

	String boundignbox = "";
	if (bbox != null)
	    boundignbox = bbox;

	StringBuilder builder = new StringBuilder("");

	builder.append("searchFields=title,keywords")//
		.append("&")//
		.append("reqID=").append(rid)//
		.append("&")//
		.append("si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,sscScore")//
		.append("rel=").append(bboxrel)//
		.append("&")//
		.append("viewid=")//
		.append("&")//
		.append("st=").append(st)//
		.append("&")//
		.append("sources=")//
		.append("&bbox=").append(boundignbox);

	sources.stream().forEach(s -> builder.append(s).append(","));

	return builder.toString();
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
}
