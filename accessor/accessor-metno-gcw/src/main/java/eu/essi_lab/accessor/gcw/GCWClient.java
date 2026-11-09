package eu.essi_lab.accessor.gcw;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * Lists the records of the GCW catalogue (pycsw, https://gcw.csw.met.no/csw) distributed as NetCDF through OPeNDAP.
 * The query selects the parent records (the child records are single files of gridded products) whose distribution
 * format is NetCDF or NcML: in the catalogue these are exactly the records having an OPeNDAP online resource.<br>
 * The service returns at most 10 records per page and its paging is not stable (the same record can be returned in
 * two pages and another one skipped), so the pages are sorted by modification date and requested again until all the
 * matched records have been collected.
 *
 * @author boldrini
 */
public class GCWClient {

    /**
     *
     */
    public static final String DEFAULT_ENDPOINT = "https://gcw.csw.met.no/csw";

    /**
     * Keys of the record descriptions
     */
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String ABSTRACT = "abstract";
    public static final String KEYWORDS = "keywords";
    public static final String CONTACTS = "contacts";
    public static final String CONTACT_INDIVIDUAL = "individual";
    public static final String CONTACT_ORGANISATION = "organisation";
    public static final String CONTACT_EMAIL = "email";
    public static final String CONTACT_ROLE = "role";
    public static final String CONSTRAINTS = "constraints";
    public static final String DISTRIBUTOR = "distributor";
    public static final String LANDING_PAGE = "landingPage";
    public static final String OPENDAP = "opendap";
    public static final String WEST = "west";
    public static final String EAST = "east";
    public static final String SOUTH = "south";
    public static final String NORTH = "north";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final int PAGE_SIZE = 10;

    /**
     * Maximum number of times the result set is paged to collect all the matched records
     */
    private static final int MAX_PASSES = 6;

    private static final String OPENDAP_PROTOCOL = "OPeNDAP:OPeNDAP";

    private static final String FILTER = "<ogc:Filter><ogc:And>" //
	    + "<ogc:PropertyIsNull><ogc:PropertyName>apiso:ParentIdentifier</ogc:PropertyName></ogc:PropertyIsNull>" //
	    + "<ogc:Or>" //
	    + "<ogc:PropertyIsLike wildCard=\"%\" singleChar=\"!\" escapeChar=\"\\\"><ogc:PropertyName>apiso:Format</ogc:PropertyName><ogc:Literal>NetCDF%</ogc:Literal></ogc:PropertyIsLike>" //
	    + "<ogc:PropertyIsLike wildCard=\"%\" singleChar=\"!\" escapeChar=\"\\\"><ogc:PropertyName>apiso:Format</ogc:PropertyName><ogc:Literal>NcML%</ogc:Literal></ogc:PropertyIsLike>" //
	    + "</ogc:Or></ogc:And></ogc:Filter>";

    private static final String SORT = "<ogc:SortBy><ogc:SortProperty><ogc:PropertyName>apiso:Modified</ogc:PropertyName>"
	    + "<ogc:SortOrder>DESC</ogc:SortOrder></ogc:SortProperty></ogc:SortBy>";

    private final String endpoint;

    /**
     * @param endpoint the CSW endpoint of the GCW catalogue
     */
    public GCWClient(String endpoint) {

	this.endpoint = endpoint == null || endpoint.isEmpty() ? DEFAULT_ENDPOINT : endpoint;
    }

    /**
     * @return the records having an OPeNDAP online resource, sorted by identifier
     * @throws Exception if the catalogue can not be queried
     */
    public List<JSONObject> listOPeNDAPRecords() throws Exception {

	Map<String, JSONObject> records = new LinkedHashMap<>();
	int matched = -1;

	for (int pass = 1; pass <= MAX_PASSES && (matched < 0 || records.size() < matched); pass++) {

	    int start = 1;
	    do {
		XMLDocumentReader reader = getRecords(start);
		matched = reader.evaluateNumber("//*:SearchResults/@numberOfRecordsMatched").intValue();

		for (Node metadata : reader.evaluateNodes("//*:MD_Metadata")) {
		    JSONObject record = parse(reader, metadata);
		    if (record.has(ID) && record.has(OPENDAP)) {
			records.putIfAbsent(record.getString(ID), record);
		    }
		}

		start += PAGE_SIZE;

	    } while (start <= matched);

	    GSLoggerFactory.getLogger(getClass()).debug("Pass {}: collected {}/{} records", pass, records.size(), matched);
	}

	if (records.size() < matched) {
	    GSLoggerFactory.getLogger(getClass()).warn("Collected {} of the {} matched records", records.size(), matched);
	}

	List<JSONObject> ret = new ArrayList<>(records.values());
	ret.sort((r1, r2) -> r1.getString(ID).compareTo(r2.getString(ID)));
	return ret;
    }

    /**
     * @param start the 1-based position of the first record
     * @return the GetRecords response, with ISO 19139 records
     * @throws Exception
     */
    private XMLDocumentReader getRecords(int start) throws Exception {

	String body = "<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:ogc=\"http://www.opengis.net/ogc\" "
		+ "service=\"CSW\" version=\"2.0.2\" resultType=\"results\" startPosition=\"" + start + "\" maxRecords=\"" + PAGE_SIZE + "\" "
		+ "outputSchema=\"http://www.isotc211.org/2005/gmd\"><csw:Query typeNames=\"csw:Record\">"
		+ "<csw:ElementSetName>full</csw:ElementSetName><csw:Constraint version=\"1.1.0\">" + FILTER + "</csw:Constraint>" + SORT
		+ "</csw:Query></csw:GetRecords>";

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Content-Type", "application/xml");
	HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, endpoint, body, HttpHeaderUtils.build(headers));

	Exception error = null;
	for (int attempt = 0; attempt < 3; attempt++) {
	    try {
		HttpResponse<InputStream> response = new Downloader().downloadResponse(request);
		if (response.statusCode() != 200) {
		    throw new IOException("Status code " + response.statusCode());
		}
		try (InputStream stream = response.body()) {
		    XMLDocumentReader reader = new XMLDocumentReader(stream);
		    if (reader.evaluateNode("//*:SearchResults") == null) {
			throw new IOException("Unexpected response: " + reader.evaluateString("//*:ExceptionText"));
		    }
		    return reader;
		}
	    } catch (Exception e) {
		error = e;
		GSLoggerFactory.getLogger(getClass()).warn("GetRecords from {} failed: {}", start, e.getMessage());
		Thread.sleep(5000l * (attempt + 1));
	    }
	}

	throw error;
    }

    /**
     * @param reader
     * @param metadata a gmd:MD_Metadata node
     * @return the record description
     * @throws Exception
     */
    static JSONObject parse(XMLDocumentReader reader, Node metadata) throws Exception {

	JSONObject ret = new JSONObject();

	put(ret, ID, reader.evaluateString(metadata, "*:fileIdentifier/*:CharacterString"));

	Node identification = reader.evaluateNode(metadata, "*:identificationInfo/*[1]");
	if (identification != null) {

	    put(ret, TITLE, reader.evaluateString(identification, "*:citation/*:CI_Citation/*:title/*[1]"));
	    put(ret, ABSTRACT, reader.evaluateString(identification, "*:abstract/*:CharacterString"));

	    JSONArray keywords = new JSONArray();
	    for (Node keyword : reader.evaluateNodes(identification, "*:descriptiveKeywords/*:MD_Keywords/*:keyword/*")) {
		String value = keyword.getTextContent().trim();
		if (!value.isEmpty()) {
		    keywords.put(value);
		}
	    }
	    ret.put(KEYWORDS, keywords);

	    JSONArray contacts = new JSONArray();
	    for (Node party : reader.evaluateNodes(identification, "*:pointOfContact/*:CI_ResponsibleParty")) {
		JSONObject contact = new JSONObject();
		put(contact, CONTACT_INDIVIDUAL, reader.evaluateString(party, "*:individualName/*[1]"));
		put(contact, CONTACT_ORGANISATION, reader.evaluateString(party, "*:organisationName/*[1]"));
		put(contact, CONTACT_EMAIL, reader.evaluateString(party, ".//*:electronicMailAddress/*:CharacterString"));
		put(contact, CONTACT_ROLE, reader.evaluateString(party, "*:role/*:CI_RoleCode/@codeListValue"));
		if (!contact.isEmpty()) {
		    contacts.put(contact);
		}
	    }
	    ret.put(CONTACTS, contacts);

	    JSONArray constraints = new JSONArray();
	    for (Node constraint : reader.evaluateNodes(identification,
		    "*:resourceConstraints/*/*:otherConstraints/* | *:resourceConstraints/*/*:useLimitation/*")) {
		String value = constraint.getTextContent().trim();
		if (!value.isEmpty()) {
		    constraints.put(value);
		}
	    }
	    ret.put(CONSTRAINTS, constraints);

	    Node box = reader.evaluateNode(identification, "*:extent/*:EX_Extent/*:geographicElement/*:EX_GeographicBoundingBox");
	    if (box != null) {
		put(ret, WEST, reader.evaluateString(box, "*:westBoundLongitude/*:Decimal"));
		put(ret, EAST, reader.evaluateString(box, "*:eastBoundLongitude/*:Decimal"));
		put(ret, SOUTH, reader.evaluateString(box, "*:southBoundLatitude/*:Decimal"));
		put(ret, NORTH, reader.evaluateString(box, "*:northBoundLatitude/*:Decimal"));
	    }

	    put(ret, BEGIN, reader.evaluateString(identification, "*:extent/*:EX_Extent/*:temporalElement//*:beginPosition"));
	    put(ret, END, reader.evaluateString(identification, "*:extent/*:EX_Extent/*:temporalElement//*:endPosition"));
	}

	put(ret, DISTRIBUTOR, reader.evaluateString(metadata, "*:distributionInfo//*:distributorContact//*:organisationName/*[1]"));

	for (Node online : reader.evaluateNodes(metadata, "*:distributionInfo//*:CI_OnlineResource")) {

	    String linkage = reader.evaluateString(online, "*:linkage/*:URL").trim();
	    String protocol = reader.evaluateString(online, "*:protocol/*[1]").trim();
	    String name = reader.evaluateString(online, "*:name/*[1]").trim();

	    if (linkage.isEmpty()) {
		continue;
	    }
	    if (!ret.has(OPENDAP) && (protocol.equals(OPENDAP_PROTOCOL) || linkage.contains("/dodsC/") || linkage.contains("/opendap/"))) {
		ret.put(OPENDAP, stripOPeNDAPSuffix(linkage));
	    } else if (!ret.has(LANDING_PAGE) && name.equalsIgnoreCase("Dataset landing page")) {
		ret.put(LANDING_PAGE, linkage);
	    }
	}

	if (!ret.has(LANDING_PAGE)) {
	    put(ret, LANDING_PAGE, reader.evaluateString(metadata, "*:dataSetURI/*:CharacterString"));
	}

	return ret;
    }

    /**
     * @param linkage
     * @return the dataset URL, without the ".html" suffix of the OPeNDAP data request form
     */
    static String stripOPeNDAPSuffix(String linkage) {

	return linkage.endsWith(".html") ? linkage.substring(0, linkage.length() - ".html".length()) : linkage;
    }

    /**
     * @param record
     * @return
     */
    public static Optional<String> getOPeNDAPURL(JSONObject record) {

	return Optional.ofNullable(record.optString(OPENDAP, null));
    }

    /**
     * Puts the trimmed value, if not empty
     */
    private static void put(JSONObject object, String key, String value) {

	if (value != null && !value.trim().isEmpty()) {
	    object.put(key, value.trim());
	}
    }
}
