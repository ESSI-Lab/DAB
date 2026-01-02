package eu.essi_lab.profiler.pubsub;

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

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
public class Subscription {

    private String id;
    private String label;
    private String clientID;
    private Long creationDate;
    private Long expirationDate;
    private boolean init;

    private String bbox;
    private String timeStart;
    private String timeEnd;
    private String searchTerms;
    private String sources;
    private Long from;
    private Long until;
    private String parents;
    private PrintWriter writer;
    private String requestURL;
    private boolean enable;
    private Thread currentThread;

    private boolean equivalent;
    private String groupID;
    private String start;
    private String pageSize;
    private String timeOut;
    private String searchFields;
    private String spatialRelation;
    private String termFrequency;
    private String extensionRelation;
    private String extensionConcepts;
    private WebRequest webRequest;
    private String osQuery;
    private static final String CONSTRAINTS = "constraints";

    public Subscription() {
    }

    public Subscription(JSONObject subscription) {

	setId(subscription.getString("id"));
	setLabel(subscription.getString("label"));
	setClientID(subscription.getString("clientID"));
	setCreationDate(subscription.getLong("creation"));
	setExpirationDate(subscription.getLong("expiration"));

	String localOsQuery = "si=1";
	localOsQuery += "&ct=10";

	if (subscription.has(CONSTRAINTS)) {

	    JSONObject constraints = subscription.getJSONObject(CONSTRAINTS);

	    if (constraints.has("when")) {

		JSONObject when = constraints.getJSONObject("when");
		if (when.has("from")) {
		    setTimeStart(when.getString("from"));
		    localOsQuery += "&ts=" + getTimeStart();
		}

		if (when.has("to")) {
		    setTimeEnd(when.getString("to"));
		    localOsQuery += "&te=" + getTimeEnd();
		}
	    }

	    if (constraints.has("bbox")) {

		String west = constraints.getJSONObject("bbox").getString("west");
		String south = constraints.getJSONObject("bbox").getString("south");
		String east = constraints.getJSONObject("bbox").getString("east");
		String north = constraints.getJSONObject("bbox").getString("north");

		setBbox(west + "," + south + "," + east + "," + north);
		localOsQuery += "&bbox=" + getBbox();
	    }

	    if (constraints.has("what")) {
		setSearchTerms(constraints.getString("what"));
		localOsQuery += "&st=" + getSearchTerms();
	    }
	}

	localOsQuery += "&outputFormat=application/json";
	setOpenSearchQuery(localOsQuery);
    }

    public JSONObject toJSON() {

	JSONObject object = new JSONObject();

	object.put("id", id);
	object.put("label", label);
	object.put("clientID", clientID);
	object.put("creation", creationDate);
	object.put("expiration", expirationDate);

	// constraints

	JSONObject contraints = new JSONObject();

	JSONObject when = null;
	if (timeStart != null && !timeStart.equals("")) {
	    when = new JSONObject();
	    when.put("from", timeStart);
	}

	if (timeEnd != null && !timeEnd.equals("")) {
	    if (when == null) {
		when = new JSONObject();
	    }
	    when.put("to", timeEnd);
	}

	if (when != null) {
	    contraints.put("when", when);
	}

	if (bbox != null) {
	    JSONObject lbbox = new JSONObject();
	    if (this.bbox != null && !this.bbox.equals("")) {
		lbbox.put("west", this.bbox.split(",")[0]);
		lbbox.put("south", this.bbox.split(",")[1]);
		lbbox.put("east", this.bbox.split(",")[2]);
		lbbox.put("north", this.bbox.split(",")[3]);
		contraints.put("bbox", lbbox);
	    }
	}

	if (searchTerms != null && !searchTerms.equals("")) {
	    contraints.put("what", searchTerms);
	}

	if (parents != null && !parents.equals("")) {
	    contraints.put("who", parents);
	}

	object.put(CONSTRAINTS, contraints);

	// options
	JSONObject options = new JSONObject();

	if (checkStringValue(start)) {
	    options.put("start", start);
	}

	if (checkStringValue(pageSize)) {
	    options.put("pageSize", pageSize);
	}

	if (checkStringValue(timeOut)) {
	    options.put("timeout", timeOut);
	}

	if (checkStringValue(searchFields)) {
	    options.put("searchFields", searchFields);
	}

	if (checkStringValue(spatialRelation)) {
	    options.put("spatialRelation", spatialRelation);
	}

	if (checkStringValue(termFrequency)) {
	    options.put("termFrequency", termFrequency);
	}

	if (checkStringValue(extensionRelation)) {
	    JSONObject extension = new JSONObject();

	    extension.put("relation", extensionRelation);

	    if (checkStringValue(extensionConcepts)) {
		extension.put("concepts", extensionConcepts);
	    } else if (checkStringValue(searchTerms)) {
		extension.put("keyword", searchTerms);
	    }

	    options.put("extension", extension);
	}

	object.put("options", options);

	// kvp params
	JSONArray kvp = new JSONArray();

	if (checkStringValue(sources)) {
	    JSONObject lsources = new JSONObject();
	    lsources.put("key", "sources");
	    lsources.put("value", this.sources);

	    kvp.put(lsources);
	}

	if (kvp.length() > 0) {
	    object.put("kvp", kvp);
	}

	return object;
    }

    private boolean checkStringValue(String value) {

	return value != null && !value.equals("");
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getClientID() {
	return clientID;
    }

    public void setClientID(String clientID) {
	this.clientID = clientID;
    }

    public Long getCreationDate() {
	return creationDate;
    }

    public void setCreationDate(Long creationDate) {
	this.creationDate = creationDate;
    }

    public Long getExpirationDate() {
	return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
	this.expirationDate = expirationDate;
    }

    public void setBbox(String bbox) {
	this.bbox = bbox;
    }

    public String getBbox() {
	return bbox;
    }

    public String getTimeStart() {
	return timeStart;
    }

    public void setTimeStart(String timeStart) {
	this.timeStart = timeStart;
    }

    public String getTimeEnd() {
	return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
	this.timeEnd = timeEnd;
    }

    public String getSearchTerms() {
	return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
	this.searchTerms = searchTerms;
    }

    public String getSources() {
	return sources;
    }

    public void setSources(String sources) {
	this.sources = sources;
    }

    public Long getFrom() {
	return from;
    }

    public void setFrom(Long from) {
	this.from = from;
    }

    public Long getUntil() {
	return until;
    }

    public void toLong(Long until) {
	this.until = until;
    }

    public String getParents() {
	return parents;
    }

    public void setParents(String parents) {
	this.parents = parents;
    }

    public String getStart() {
	return start;
    }

    public void setStart(String start) {
	this.start = start;
    }

    public String getPageSize() {
	return pageSize;
    }

    public void setPageSize(String pageSize) {
	this.pageSize = pageSize;
    }

    public String getTimeOut() {
	return timeOut;
    }

    public void setTimeOut(String timeOut) {
	this.timeOut = timeOut;
    }

    public String getSearchFields() {
	return searchFields;
    }

    public void setSearchFields(String searchFields) {
	this.searchFields = searchFields;
    }

    public String getSpatialRelation() {
	return spatialRelation;
    }

    public void setSpatialRelation(String spatialRelation) {
	this.spatialRelation = spatialRelation;
    }

    public String getTermFrequency() {
	return termFrequency;
    }

    public void setTermFrequency(String termFrequency) {
	this.termFrequency = termFrequency;
    }

    public String getExtensionRelation() {
	return extensionRelation;
    }

    public void setExtensionRelation(String extensionRelation) {
	this.extensionRelation = extensionRelation;
    }

    public String getExtensionConcepts() {
	return extensionConcepts;
    }

    public void setExtensionConcepts(String extensionConcepts) {
	this.extensionConcepts = extensionConcepts;
    }

    public void setWriter(PrintWriter out) {

	this.writer = out;
    }

    public PrintWriter getWriter() {

	return writer;
    }

    public void setRequestURL(String requestURL) {

	this.requestURL = requestURL;
    }

    public String getRequestURL() {

	return this.requestURL;
    }

    public void setEnabled(boolean enable) {

	this.enable = enable;
    }

    public boolean isEnabled() {

	return enable;
    }

    public boolean isInit() {
	return init;
    }

    public void setInit(boolean init) {

	this.init = init;
    }

    public void setThread(Thread currentThread) {

	this.currentThread = currentThread;
    }

    public Thread getThread() {

	return this.currentThread;
    }

    public void setOpenSearchQuery(String osQuery) {

	this.osQuery = osQuery;
    }

    public String getOpenSearchQuery() {

	return osQuery;
    }

    public boolean isEquivalent() {

	return equivalent;
    }

    public void setEquivalent(boolean equivalent) {

	this.equivalent = equivalent;
    }

    public String getGroupID() {

	return groupID;
    }

    public void setGroupID(String groupID) {

	this.groupID = groupID;
    }

    public void setWebRequest(WebRequest webRequest) {

	this.webRequest = webRequest;
    }

    public WebRequest getWebRequest() {
	return webRequest;
    }

    public boolean equivalent(Object o) {

	Subscription s = (Subscription) o;
	return equals(this.timeStart, s.timeStart) //
		&& equals(this.timeEnd, s.timeEnd) //
		&& equals(this.bbox, s.bbox) //
		&& equals(this.searchTerms, s.searchTerms) //
		&& equals(this.sources, s.sources) //

		&& equals(this.parents, s.parents) //

		&& equals(this.start, s.start) //
		&& equals(this.pageSize, s.pageSize) //
		&& equals(this.searchFields, s.searchFields) //
		&& equals(this.spatialRelation, s.spatialRelation) //
		&& equals(this.extensionConcepts, s.extensionConcepts) //
		&& equals(this.extensionRelation, s.extensionRelation) //
		&& equals(this.termFrequency, s.termFrequency);
    }

    public String createGroupID() {

	try {
	    return URLEncoder.encode(
		    getBbox() + getTimeStart() + getTimeEnd() + getSearchTerms() + getParents() + getStart() + getPageSize()
			    + getSearchFields() + getSpatialRelation() + getExtensionConcepts() + getExtensionRelation()
			    + getTermFrequency(), "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Can't encode request", e);

	}

	return null;

    }

    public boolean equals(Object o) {

	if (o == null)
	    return false;

	if (!(o instanceof Subscription))
	    return false;

	return this.id.equals(((Subscription) o).id);
    }

    private boolean equals(Object o1, Object o2) {

	return o1 == null && o2 == null || (o1 != null && o2 != null && o1.equals(o2));
    }

}
