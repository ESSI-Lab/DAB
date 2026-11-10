package eu.essi_lab.accessor.stac.polarwatch;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * Maps the envelope produced by {@link PolarWatchSTACConnector} (a STAC collection, optionally with the ERDDAP
 * <code>info</code> document of the dataset it references) to a {@link Dataset}.<br>
 * <br>
 * ERDDAP global attributes are used where they are richer or more reliable than the STAC JSON: the geographic
 * bounding box (the STAC bbox of several collections is expressed in projected meters), keywords, platforms,
 * instruments, contacts and the variable list.
 *
 * @author Enrico Boldrini
 */
public class PolarWatchSTACMapper extends OriginalIdentifierMapper {

    /**
     *
     */
    public static final String POLARWATCH_STAC_SCHEME_URI = "http://stacspec.org/polarwatch";

    //
    // envelope members
    //
    static final String CATALOG_URL = "catalogUrl";
    static final String COLLECTION_URL = "collectionUrl";
    static final String COLLECTION = "collection";
    static final String ERDDAP = "erddap";
    static final String ERDDAP_BASE = "base";
    static final String ERDDAP_PROTOCOL = "protocol";
    static final String ERDDAP_DATASET_ID = "datasetId";
    static final String ERDDAP_INFO = "info";

    private static final Pattern COLLECTION_FOLDER_PATTERN = Pattern.compile("/collections/([^/]+)/collection\\.json$");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("^\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(km|m)\\s*$",
	    Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?");

    /**
     * Mean Earth radius used by the spherical inverse projections
     */
    private static final double EARTH_RADIUS = 6371007.0;

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return POLARWATCH_STAC_SCHEME_URI;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	return readIdentifier(new JSONObject(resource.getOriginalMetadata().getMetadata()));
    }

    /**
     * The collection folder name is used since in the PolarWatch catalog the collection <code>id</code> does not
     * always match the folder (e.g. folder <code>noaaIABPbuoyDrift</code>, id <code>buoy_drift_velocities</code>)
     *
     * @param envelope
     * @return
     */
    static String readIdentifier(JSONObject envelope) {

	Matcher matcher = COLLECTION_FOLDER_PATTERN.matcher(envelope.optString(COLLECTION_URL));
	if (matcher.find()) {
	    return matcher.group(1);
	}

	return envelope.getJSONObject(COLLECTION).optString("id", null);
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject envelope = new JSONObject(originalMD.getMetadata());
	JSONObject collection = envelope.getJSONObject(COLLECTION);
	Optional<JSONObject> erddap = Optional.ofNullable(envelope.optJSONObject(ERDDAP));
	ERDDAPInfo info = new ERDDAPInfo(erddap.map(e -> e.optJSONObject(ERDDAP_INFO)).orElse(null));

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	MIMetadata md = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	md.setHierarchyLevelName("dataset");
	md.addHierarchyLevelScopeCodeListValue("dataset");

	//
	// title and abstract
	//
	String title = firstNonEmpty(collection.optString("title"), info.global("title"), readIdentifier(envelope));
	md.getDataIdentification().setCitationTitle(title);

	String description = collection.optString("description").trim();
	String summary = info.global("summary");
	if (summary != null && summary.trim().length() > description.length()) {
	    description = summary.trim();
	}
	if (!description.isEmpty()) {
	    md.getDataIdentification().setAbstract(description);
	}

	//
	// keywords, platforms, instruments
	//
	addKeywords(md, collection, info);

	addPlatformsAndInstruments(md, collection, info);

	//
	// constraints
	//
	String license = firstNonEmpty(collection.optString("pw:license_text"), collection.optString("license"), info.global("license"));
	if (license != null) {
	    LegalConstraints constraints = new LegalConstraints();
	    constraints.addUseLimitation(license);
	    md.getDataIdentification().addLegalConstraints(constraints);
	}

	//
	// contacts
	//
	addContacts(md, collection, info);

	//
	// extents
	//
	Hemisphere hemisphere = Hemisphere.detect(collection, info, title);

	computeBoundingBox(collection, info, hemisphere).ifPresent(b -> md.getDataIdentification().addGeographicBoundingBox(b[2], b[1], b[0], b[3]));

	addTemporalExtent(md, collection, info);

	//
	// resolution, reference system
	//
	parseResolution(collection.optString("pw:resolution_spatial")).ifPresent(r -> md.getDataIdentification().setSpatialResolution(r));

	readEPSG(collection, info).ifPresent(epsg -> {
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:" + epsg);
	    referenceSystem.setCodeSpace("EPSG");
	    md.addReferenceSystemInfo(referenceSystem);
	});

	//
	// variables
	//
	addCoverageDescriptions(md, collection, info);

	//
	// graphic overviews and distribution
	//
	addOnlineResources(md, envelope, collection, erddap);

	return dataset;
    }

    //
    // keywords
    //

    private void addKeywords(MIMetadata md, JSONObject collection, ERDDAPInfo info) {

	Set<String> seen = new HashSet<>();
	List<String> hierarchical = new ArrayList<>();
	List<String> plain = new ArrayList<>();

	List<String> all = new ArrayList<>(toStringList(collection.optJSONArray("keywords")));
	String erddapKeywords = info.global("keywords");
	if (erddapKeywords != null) {
	    for (String keyword : erddapKeywords.split(",")) {
		all.add(keyword);
	    }
	}

	for (String keyword : all) {

	    String k = keyword.trim();
	    if (k.isEmpty() || !seen.add(normalizeKeyword(k))) {
		continue;
	    }

	    if (k.contains(">")) {
		hierarchical.add(k);
	    } else {
		plain.add(k);
	    }
	}

	if (!hierarchical.isEmpty()) {

	    Keywords keywords = new Keywords();
	    keywords.setTypeCode("theme");
	    keywords.setThesaurusNameCitationTitle(firstNonEmpty(info.global("keywords_vocabulary"), "GCMD Science Keywords"));
	    keywords.addKeywords(hierarchical);
	    md.getDataIdentification().addKeywords(keywords);
	}

	if (!plain.isEmpty()) {

	    Keywords keywords = new Keywords();
	    keywords.addKeywords(plain);
	    md.getDataIdentification().addKeywords(keywords);
	}
    }

    private static String normalizeKeyword(String keyword) {

	return keyword.replaceAll("\\s*>\\s*", ">").toLowerCase(Locale.ROOT);
    }

    private void addPlatformsAndInstruments(MIMetadata md, JSONObject collection, ERDDAPInfo info) {

	JSONObject summaries = Optional.ofNullable(collection.optJSONObject("summaries")).orElse(new JSONObject());

	Set<String> platforms = new LinkedHashSet<>();
	toStringList(summaries.optJSONArray("platform")).forEach(p -> splitList(p, platforms));
	Optional.ofNullable(info.global("platform")).ifPresent(p -> splitList(p, platforms));

	for (String name : platforms) {

	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(name);
	    Citation citation = new Citation();
	    citation.setTitle(name);
	    platform.setCitation(citation);
	    md.addMIPlatform(platform);
	}

	if (!platforms.isEmpty()) {

	    Keywords keywords = new Keywords();
	    keywords.setTypeCode("platform");
	    keywords.addKeywords(new ArrayList<>(platforms));
	    md.getDataIdentification().addKeywords(keywords);
	}

	Set<String> instruments = new LinkedHashSet<>();
	toStringList(summaries.optJSONArray("instruments")).forEach(i -> splitList(i, instruments));
	Optional.ofNullable(info.global("instrument")).ifPresent(i -> splitList(i, instruments));

	for (String name : instruments) {

	    MIInstrument instrument = new MIInstrument();
	    instrument.setMDIdentifierTypeCode(name);
	    instrument.setTitle(name);
	    md.addMIInstrument(instrument);
	}

	if (!instruments.isEmpty()) {

	    Keywords keywords = new Keywords();
	    keywords.setTypeCode("instrument");
	    keywords.addKeywords(new ArrayList<>(instruments));
	    md.getDataIdentification().addKeywords(keywords);
	}
    }

    private static void splitList(String value, Set<String> target) {

	for (String v : value.split(",")) {
	    if (!v.trim().isEmpty()) {
		target.add(v.trim());
	    }
	}
    }

    //
    // contacts
    //

    private void addContacts(MIMetadata md, JSONObject collection, ERDDAPInfo info) {

	JSONArray providers = collection.optJSONArray("providers");

	if (providers != null) {

	    for (int i = 0; i < providers.length(); i++) {

		JSONObject provider = providers.optJSONObject(i);
		if (provider == null || provider.optString("name").trim().isEmpty()) {
		    continue;
		}

		ResponsibleParty party = new ResponsibleParty();
		party.setOrganisationName(provider.getString("name").trim());
		party.setRoleCode(toRoleCode(provider));

		String url = provider.optString("url").trim();
		if (!url.isEmpty()) {
		    Contact contact = new Contact();
		    Online online = new Online();
		    online.setLinkage(url);
		    contact.setOnline(online);
		    party.setContactInfo(contact);
		}

		md.getDataIdentification().addPointOfContact(party);
	    }
	}

	String creator = firstNonEmpty(info.global("creator_name"), collection.optString("pw:creator_name"));
	String email = firstNonEmpty(info.global("creator_email"), collection.optString("pw:creator_email"));
	String creatorURL = info.global("creator_url");

	if (creator != null || email != null) {

	    ResponsibleParty party = new ResponsibleParty();
	    party.setOrganisationName(firstNonEmpty(creator, info.global("institution"), email));
	    party.setRoleCode("pointOfContact");

	    Contact contact = new Contact();
	    if (email != null) {
		Address address = new Address();
		address.addElectronicMailAddress(email);
		contact.setAddress(address);
	    }
	    if (creatorURL != null) {
		Online online = new Online();
		online.setLinkage(creatorURL);
		contact.setOnline(online);
	    }
	    party.setContactInfo(contact);

	    md.getDataIdentification().addPointOfContact(party);
	}
    }

    /**
     * Maps the STAC provider roles (licensor, producer, processor, host) to ISO role codes
     */
    private static String toRoleCode(JSONObject provider) {

	List<String> roles = toStringList(provider.optJSONArray("roles"));
	if (roles.isEmpty() && !provider.optString("role").isEmpty()) {
	    roles.add(provider.getString("role"));
	}

	for (String role : roles) {
	    switch (role.trim().toLowerCase(Locale.ROOT)) {
	    case "producer":
	    case "creator":
		return "originator";
	    case "processor":
		return "processor";
	    case "host":
		return "distributor";
	    case "licensor":
		return "owner";
	    default:
	    }
	}

	return "pointOfContact";
    }

    //
    // extents
    //

    /**
     * @return south, west, north, east
     */
    static Optional<double[]> computeBoundingBox(JSONObject collection, ERDDAPInfo info, Hemisphere hemisphere) {

	// 1) ERDDAP geographic attributes
	Double latMin = info.globalDouble("geospatial_lat_min");
	Double latMax = info.globalDouble("geospatial_lat_max");
	Double lonMin = info.globalDouble("geospatial_lon_min");
	Double lonMax = info.globalDouble("geospatial_lon_max");

	if (latMin != null && latMax != null && lonMin != null && lonMax != null) {

	    if (lonMax > 180) {
		lonMin = -180.0;
		lonMax = 180.0;
	    }
	    return Optional.of(new double[] { latMin, lonMin, latMax, lonMax });
	}

	// 2) ERDDAP WKT bounds (lon lat pairs)
	Optional<double[]> wkt = parseWKTBounds(info.global("geospatial_bounds"), hemisphere);
	if (wkt.isPresent()) {
	    return wkt;
	}

	// 3) ERDDAP projected axes ranges
	Optional<PolarProjection> projection = PolarProjection.of(collection, info, hemisphere);

	Optional<double[]> xRange = info.axisRange("projection_x_coordinate");
	Optional<double[]> yRange = info.axisRange("projection_y_coordinate");

	if (projection.isPresent() && xRange.isPresent() && yRange.isPresent()) {

	    return Optional.of(projection.get().toGeographic(xRange.get()[0], yRange.get()[0], xRange.get()[1], yRange.get()[1]));
	}

	// 4) STAC bbox
	JSONArray bboxes = Optional.ofNullable(collection.optJSONObject("extent")).//
		map(e -> e.optJSONObject("spatial")).//
		map(s -> s.optJSONArray("bbox")).//
		orElse(null);

	JSONArray bbox = bboxes == null ? null : bboxes.optJSONArray(0);

	if (bbox == null || bbox.length() != 4) {
	    return Optional.empty();
	}

	double west = bbox.optDouble(0);
	double south = bbox.optDouble(1);
	double east = bbox.optDouble(2);
	double north = bbox.optDouble(3);

	if (Double.isNaN(west) || Double.isNaN(south) || Double.isNaN(east) || Double.isNaN(north)) {
	    return Optional.empty();
	}

	boolean geographic = Math.abs(west) <= 180 && Math.abs(east) <= 180 && Math.abs(south) <= 90 && Math.abs(north) <= 90;

	if (geographic) {
	    return Optional.of(new double[] { south, west, north, east });
	}

	if (projection.isPresent()) {
	    return Optional.of(projection.get().toGeographic(west, south, east, north));
	}

	GSLoggerFactory.getLogger(PolarWatchSTACMapper.class).warn("Unable to convert projected bbox of {}", collection.optString("id"));

	return Optional.empty();
    }

    private static Optional<double[]> parseWKTBounds(String wkt, Hemisphere hemisphere) {

	if (wkt == null || !wkt.trim().toUpperCase(Locale.ROOT).startsWith("POLYGON")) {
	    return Optional.empty();
	}

	Matcher matcher = NUMBER_PATTERN.matcher(wkt);
	List<Double> values = new ArrayList<>();
	while (matcher.find()) {
	    values.add(Double.valueOf(matcher.group()));
	}

	if (values.size() < 6 || values.size() % 2 != 0) {
	    return Optional.empty();
	}

	double latMin = 90, latMax = -90, lonMin = 180, lonMax = -180;
	for (int i = 0; i < values.size(); i += 2) {
	    lonMin = Math.min(lonMin, values.get(i));
	    lonMax = Math.max(lonMax, values.get(i));
	    latMin = Math.min(latMin, values.get(i + 1));
	    latMax = Math.max(latMax, values.get(i + 1));
	}

	// a ring around a pole does not contain the pole vertex itself
	switch (hemisphere) {
	case NORTH:
	    return Optional.of(new double[] { latMin, -180, 90, 180 });
	case SOUTH:
	    return Optional.of(new double[] { -90, -180, latMax, 180 });
	default:
	    return Optional.of(new double[] { latMin, lonMin, latMax, lonMax });
	}
    }

    private void addTemporalExtent(MIMetadata md, JSONObject collection, ERDDAPInfo info) {

	String begin = null;
	String end = null;

	JSONArray intervals = Optional.ofNullable(collection.optJSONObject("extent")).//
		map(e -> e.optJSONObject("temporal")).//
		map(t -> t.optJSONArray("interval")).//
		orElse(null);

	JSONArray interval = intervals == null ? null : intervals.optJSONArray(0);
	boolean openEnded = true;

	if (interval != null && interval.length() == 2) {

	    begin = interval.isNull(0) ? null : interval.optString(0).trim();
	    end = interval.isNull(1) ? null : interval.optString(1).trim();
	}

	if (isEmptyOrOpen(begin)) {
	    begin = info.global("time_coverage_start");
	}

	if (!isEmptyOrOpen(end)) {
	    openEnded = false;
	} else if (interval == null) {
	    // no STAC interval: rely on ERDDAP; near real time datasets declare 'testOutOfDate'
	    end = info.global("time_coverage_end");
	    openEnded = end == null || info.global("testOutOfDate") != null;
	}

	if (isEmptyOrOpen(begin)) {
	    return;
	}

	TemporalExtent extent = new TemporalExtent();
	extent.setBeginPosition(begin);

	if (openEnded) {
	    extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	} else {
	    extent.setEndPosition(end);
	}

	md.getDataIdentification().addTemporalExtent(extent);
    }

    private static boolean isEmptyOrOpen(String value) {

	if (value == null) {
	    return true;
	}

	String v = value.trim().toLowerCase(Locale.ROOT);
	return v.isEmpty() || v.equals("null") || v.equals("current") || v.equals("now") || v.equals("present") || v.equals("..");
    }

    /**
     * @return the resolution in meters
     */
    static Optional<Double> parseResolution(String value) {

	if (value == null) {
	    return Optional.empty();
	}

	Matcher matcher = RESOLUTION_PATTERN.matcher(value);
	if (!matcher.matches()) {
	    return Optional.empty();
	}

	double number = Double.parseDouble(matcher.group(1));
	return Optional.of(matcher.group(2).equalsIgnoreCase("km") ? number * 1000 : number);
    }

    private static Optional<Integer> readEPSG(JSONObject collection, ERDDAPInfo info) {

	if (collection.has("proj:epsg") && !collection.isNull("proj:epsg")) {
	    return Optional.of(collection.optInt("proj:epsg"));
	}

	JSONObject summaries = collection.optJSONObject("summaries");
	if (summaries != null && summaries.optJSONArray("proj:epsg") != null && summaries.getJSONArray("proj:epsg").length() > 0) {
	    return Optional.of(summaries.getJSONArray("proj:epsg").optInt(0));
	}

	String srid = info.global("srid");
	if (srid != null && srid.toUpperCase(Locale.ROOT).startsWith("EPSG:")) {
	    try {
		return Optional.of(Integer.valueOf(srid.substring(5).trim()));
	    } catch (NumberFormatException e) {
		// ignore
	    }
	}

	return Optional.empty();
    }

    //
    // variables
    //

    private void addCoverageDescriptions(MIMetadata md, JSONObject collection, ERDDAPInfo info) {

	List<Map<String, String>> variables = info.dataVariables();

	if (!variables.isEmpty()) {

	    for (Map<String, String> variable : variables) {

		String name = variable.get("name");
		String longName = firstNonEmpty(variable.get("long_name"), name);

		StringBuilder description = new StringBuilder(longName);
		Optional.ofNullable(variable.get("units")).ifPresent(u -> description.append(" (").append(u).append(")"));
		Optional.ofNullable(variable.get("standard_name")).ifPresent(s -> description.append(" - CF standard name: ").append(s));

		CoverageDescription coverage = new CoverageDescription();
		coverage.setAttributeIdentifier(name);
		coverage.setAttributeTitle(longName);
		coverage.setAttributeDescription(description.toString());
		md.addCoverageDescription(coverage);
	    }

	    return;
	}

	JSONObject itemAssets = collection.optJSONObject("item_assets");
	if (itemAssets == null) {
	    return;
	}

	for (String key : itemAssets.keySet()) {

	    if (ERDDAPInfo.isCoordinate(key)) {
		continue;
	    }

	    JSONObject asset = itemAssets.optJSONObject(key);
	    String assetTitle = asset == null ? key : firstNonEmpty(asset.optString("title"), key);

	    CoverageDescription coverage = new CoverageDescription();
	    coverage.setAttributeIdentifier(key);
	    coverage.setAttributeTitle(assetTitle);
	    coverage.setAttributeDescription(assetTitle);
	    md.addCoverageDescription(coverage);
	}
    }

    //
    // online resources
    //

    private void addOnlineResources(MIMetadata md, JSONObject envelope, JSONObject collection, Optional<JSONObject> erddap) {

	String catalogURL = envelope.optString(CATALOG_URL);
	String collectionURL = envelope.optString(COLLECTION_URL);

	Set<String> added = new HashSet<>();

	//
	// ERDDAP
	//
	erddap.ifPresent(e -> {

	    String base = e.getString(ERDDAP_BASE);
	    String protocol = e.getString(ERDDAP_PROTOCOL);
	    String id = e.getString(ERDDAP_DATASET_ID);
	    String datasetURL = base + "/" + protocol + "/" + id;

	    addOnline(md, added, datasetURL, NetProtocolWrapper.OPENDAP.getCommonURN(), id, "ERDDAP " + protocol + " (OPeNDAP) endpoint",
		    "download");

	    addOnline(md, added, datasetURL + ".html", NetProtocolWrapper.HTTP.getCommonURN(), id, "ERDDAP data access form", "information");

	    addOnline(md, added, base + "/info/" + id + "/index.html", NetProtocolWrapper.HTTP.getCommonURN(), id,
		    "ERDDAP dataset information", "information");
	});

	//
	// assets
	//
	JSONObject assets = collection.optJSONObject("assets");
	if (assets != null) {

	    for (String key : assets.keySet()) {

		JSONObject asset = assets.optJSONObject(key);
		if (asset == null || asset.optString("href").trim().isEmpty()) {
		    continue;
		}

		String url = resolveHref(asset.getString("href"), catalogURL, collectionURL);
		List<String> roles = toStringList(asset.optJSONArray("roles"));
		String type = asset.optString("type");
		String assetTitle = firstNonEmpty(asset.optString("title"), key);

		if (roles.contains("thumbnail") || type.startsWith("image/")) {

		    BrowseGraphic graphic = new BrowseGraphic();
		    graphic.setFileName(url);
		    graphic.setFileDescription(assetTitle);
		    graphic.setFileType(type.isEmpty() ? null : type);
		    md.getDataIdentification().addGraphicOverview(graphic);
		    continue;
		}

		String function = roles.contains("data") && !roles.contains("overview") ? "download" : "information";

		addOnline(md, added, url, NetProtocolWrapper.HTTP.getCommonURN(), assetTitle, assetTitle, function);
	    }
	}

	//
	// links
	//
	JSONArray links = collection.optJSONArray("links");
	if (links != null) {

	    for (int i = 0; i < links.length(); i++) {

		JSONObject link = links.optJSONObject(i);
		if (link == null || link.optString("href").trim().isEmpty()) {
		    continue;
		}

		String rel = link.optString("rel");
		if (!rel.equals("about") && !rel.equals("describedby") && !rel.equals("service") && !rel.equals("license")
			&& !rel.equals("cite-as")) {
		    continue;
		}

		String url = resolveHref(link.getString("href"), catalogURL, collectionURL);
		String linkTitle = firstNonEmpty(link.optString("title"), rel);

		addOnline(md, added, url, NetProtocolWrapper.HTTP.getCommonURN(), linkTitle, linkTitle, "information");
	    }
	}

	if (!collectionURL.isEmpty()) {

	    addOnline(md, added, collectionURL, NetProtocolWrapper.HTTP.getCommonURN(), "STAC Collection", "PolarWatch STAC collection",
		    "information");
	}
    }

    private static void addOnline(MIMetadata md, Set<String> added, String url, String protocol, String name, String description,
	    String function) {

	// the same resource is often linked more than once (e.g. ERDDAP endpoint as STAC 'service' and 'about' link)
	String normalized = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	if (!added.add(normalized)) {
	    return;
	}

	Online online = new Online();
	online.setLinkage(url);
	online.setProtocol(protocol);
	online.setName(name);
	online.setDescription(description);
	online.setFunctionCode(function);

	md.getDistribution().addDistributionOnline(online);
    }

    /**
     * Resolves relative hrefs. The PolarWatch catalog uses three conventions: hrefs relative to the collection file,
     * hrefs relative to the catalog (<code>./collections/...</code>) and hrefs relative to the site root
     * (<code>stac/collections/...</code>)
     */
    static String resolveHref(String href, String catalogURL, String collectionURL) {

	String h = href.trim();

	try {

	    URI uri = URI.create(h);
	    if (uri.isAbsolute()) {
		return h;
	    }

	    if (h.startsWith("stac/") && catalogURL.contains("/stac/")) {
		return catalogURL.substring(0, catalogURL.indexOf("/stac/") + 1) + h;
	    }

	    if ((h.startsWith("./collections/") || h.startsWith("collections/")) && !catalogURL.isEmpty()) {
		return URI.create(catalogURL).resolve(h).toString();
	    }

	    if (!collectionURL.isEmpty()) {
		return URI.create(collectionURL).resolve(h).toString();
	    }

	} catch (IllegalArgumentException e) {

	    GSLoggerFactory.getLogger(PolarWatchSTACMapper.class).warn("Invalid href: {}", h);
	}

	return h;
    }

    //
    // utils
    //

    private static List<String> toStringList(JSONArray array) {

	List<String> out = new ArrayList<>();
	if (array != null) {
	    for (int i = 0; i < array.length(); i++) {
		if (!array.isNull(i) && !array.optString(i).trim().isEmpty()) {
		    out.add(array.optString(i).trim());
		}
	    }
	}
	return out;
    }

    private static String firstNonEmpty(String... values) {

	for (String value : values) {
	    if (value != null && !value.trim().isEmpty()) {
		return value.trim();
	    }
	}
	return null;
    }

    /**
     *
     */
    enum Hemisphere {

	NORTH, SOUTH, UNKNOWN;

	static Hemisphere detect(JSONObject collection, ERDDAPInfo info, String title) {

	    String proj4 = firstNonEmpty(collection.optString("proj:proj4"), info.global("proj4text"));
	    if (proj4 != null) {
		Double lat0 = PolarProjection.param(proj4, "lat_0");
		if (lat0 != null && Math.abs(lat0) == 90) {
		    return lat0 > 0 ? NORTH : SOUTH;
		}
	    }

	    Optional<Integer> epsg = readEPSG(collection, info);
	    if (epsg.isPresent()) {
		switch (epsg.get()) {
		case 3411:
		case 3413:
		case 3995:
		case 5041:
		case 6931:
		case 3408:
		case 3571:
		case 3572:
		case 3573:
		case 3574:
		case 3575:
		case 3576:
		    return NORTH;
		case 3031:
		case 3412:
		case 3976:
		case 5042:
		case 6932:
		case 3409:
		    return SOUTH;
		default:
		}
	    }

	    String t = title == null ? "" : title.toLowerCase(Locale.ROOT);
	    if (t.contains("antarctic") || t.contains("(south)") || t.contains("south pole")) {
		return SOUTH;
	    }
	    if (t.contains("arctic") || t.contains("(north)") || t.contains("north pole")) {
		return NORTH;
	    }

	    return UNKNOWN;
	}
    }

    /**
     * Spherical inverse of the polar stereographic and polar Lambert azimuthal equal area projections, accurate enough
     * to derive a geographic bounding box from a projected one
     */
    static class PolarProjection {

	private final boolean north;
	private final boolean stereographic;
	private final double latTs;
	private final double k0;

	private PolarProjection(boolean north, boolean stereographic, double latTs, double k0) {
	    this.north = north;
	    this.stereographic = stereographic;
	    this.latTs = latTs;
	    this.k0 = k0;
	}

	static Optional<PolarProjection> of(JSONObject collection, ERDDAPInfo info, Hemisphere hemisphere) {

	    if (hemisphere == Hemisphere.UNKNOWN) {
		return Optional.empty();
	    }

	    boolean north = hemisphere == Hemisphere.NORTH;
	    String proj4 = firstNonEmpty(collection.optString("proj:proj4"), info.global("proj4text"));

	    if (proj4 != null) {

		if (proj4.contains("+proj=laea")) {
		    return Optional.of(new PolarProjection(north, false, 90, 1));
		}

		if (proj4.contains("+proj=stere")) {
		    Double latTs = param(proj4, "lat_ts");
		    Double k = Optional.ofNullable(param(proj4, "k")).orElse(param(proj4, "k_0"));
		    return Optional.of(new PolarProjection(north, true, latTs == null ? 90 : Math.abs(latTs), k == null ? 1 : k));
		}

		return Optional.empty();
	    }

	    // well known codes without proj4 definition
	    Optional<Integer> epsg = readEPSG(collection, info);
	    if (epsg.isPresent()) {
		switch (epsg.get()) {
		case 3413:
		case 3411:
		    return Optional.of(new PolarProjection(true, true, 70, 1));
		case 3031:
		case 3976:
		    return Optional.of(new PolarProjection(false, true, 71, 1));
		case 3412:
		    return Optional.of(new PolarProjection(false, true, 70, 1));
		case 6931:
		    return Optional.of(new PolarProjection(true, false, 90, 1));
		case 6932:
		    return Optional.of(new PolarProjection(false, false, 90, 1));
		default:
		}
	    }

	    return Optional.empty();
	}

	static Double param(String proj4, String name) {

	    Matcher matcher = Pattern.compile("\\+" + name + "=(-?[0-9.]+)").matcher(proj4);
	    return matcher.find() ? Double.valueOf(matcher.group(1)) : null;
	}

	/**
	 * @param rho distance from the pole in projected meters
	 * @return the latitude in degrees
	 */
	double latitude(double rho) {

	    double colatitude;

	    if (stereographic) {

		double scale;
		if (latTs >= 90) {
		    scale = 2 * EARTH_RADIUS * k0;
		} else {
		    double phiC = Math.toRadians(latTs);
		    scale = EARTH_RADIUS * Math.cos(phiC) / Math.tan(Math.PI / 4 - phiC / 2);
		}
		colatitude = 2 * Math.atan(rho / scale);

	    } else {

		colatitude = 2 * Math.asin(Math.min(1, rho / (2 * EARTH_RADIUS)));
	    }

	    double lat = 90 - Math.toDegrees(colatitude);
	    return north ? lat : -lat;
	}

	/**
	 * @return south, west, north, east
	 */
	double[] toGeographic(double minX, double minY, double maxX, double maxY) {

	    double farX = Math.max(Math.abs(minX), Math.abs(maxX));
	    double farY = Math.max(Math.abs(minY), Math.abs(maxY));
	    double rhoMax = Math.hypot(farX, farY);

	    double nearX = (minX <= 0 && maxX >= 0) ? 0 : Math.min(Math.abs(minX), Math.abs(maxX));
	    double nearY = (minY <= 0 && maxY >= 0) ? 0 : Math.min(Math.abs(minY), Math.abs(maxY));
	    double rhoMin = Math.hypot(nearX, nearY);

	    double far = latitude(rhoMax);
	    double near = latitude(rhoMin);

	    // longitudes are not computed: polar grids usually span all of them
	    return north ? new double[] { far, -180, near, 180 } : new double[] { near, -180, far, 180 };
	}
    }

    /**
     * Read-only view of an ERDDAP <code>/info/{datasetID}/index.json</code> document
     */
    static class ERDDAPInfo {

	private final Map<String, Map<String, String>> attributes = new LinkedHashMap<>();
	private final List<String> variables = new ArrayList<>();
	private final Set<String> dimensions = new HashSet<>();

	ERDDAPInfo(JSONObject info) {

	    if (info == null || info.optJSONObject("table") == null) {
		return;
	    }

	    JSONObject table = info.getJSONObject("table");
	    JSONArray columns = table.optJSONArray("columnNames");
	    JSONArray rows = table.optJSONArray("rows");
	    if (columns == null || rows == null) {
		return;
	    }

	    int rowType = indexOf(columns, "Row Type");
	    int variableName = indexOf(columns, "Variable Name");
	    int attributeName = indexOf(columns, "Attribute Name");
	    int value = indexOf(columns, "Value");
	    if (rowType < 0 || variableName < 0 || attributeName < 0 || value < 0) {
		return;
	    }

	    for (int i = 0; i < rows.length(); i++) {

		JSONArray row = rows.optJSONArray(i);
		if (row == null) {
		    continue;
		}

		String type = row.optString(rowType);
		String variable = row.optString(variableName);

		switch (type) {
		case "attribute":
		    String v = row.isNull(value) ? null : row.optString(value);
		    if (v != null) {
			attributes.computeIfAbsent(variable, k -> new LinkedHashMap<>()).put(row.optString(attributeName), v);
		    }
		    break;
		case "variable":
		    variables.add(variable);
		    break;
		case "dimension":
		    dimensions.add(variable);
		    break;
		default:
		}
	    }
	}

	private static int indexOf(JSONArray array, String value) {

	    for (int i = 0; i < array.length(); i++) {
		if (value.equals(array.optString(i))) {
		    return i;
		}
	    }
	    return -1;
	}

	String global(String name) {

	    Map<String, String> global = attributes.get("NC_GLOBAL");
	    String value = global == null ? null : global.get(name);
	    return value == null || value.trim().isEmpty() ? null : value.trim();
	}

	Double globalDouble(String name) {

	    String value = global(name);
	    if (value == null) {
		return null;
	    }
	    try {
		return Double.valueOf(value);
	    } catch (NumberFormatException e) {
		return null;
	    }
	}

	/**
	 * @param standardName
	 * @return the actual range of the dimension with the given CF standard name
	 */
	Optional<double[]> axisRange(String standardName) {

	    for (String dimension : dimensions) {

		Map<String, String> attrs = attributes.get(dimension);
		if (attrs == null || !standardName.equals(attrs.get("standard_name")) || attrs.get("actual_range") == null) {
		    continue;
		}

		String[] range = attrs.get("actual_range").split(",");
		if (range.length == 2) {
		    try {
			double a = Double.parseDouble(range[0].trim());
			double b = Double.parseDouble(range[1].trim());
			return Optional.of(new double[] { Math.min(a, b), Math.max(a, b) });
		    } catch (NumberFormatException e) {
			// ignore
		    }
		}
	    }

	    return Optional.empty();
	}

	/**
	 * @return the data variables (coordinate variables excluded) with their name, long_name, units and
	 *         standard_name
	 */
	List<Map<String, String>> dataVariables() {

	    List<Map<String, String>> out = new ArrayList<>();

	    for (String variable : variables) {

		Map<String, String> attrs = attributes.getOrDefault(variable, new LinkedHashMap<>());

		String standardName = attrs.getOrDefault("standard_name", "");
		if (isCoordinate(variable) || standardName.equals("latitude") || standardName.equals("longitude")
			|| standardName.equals("grid_latitude") || standardName.equals("grid_longitude")) {
		    continue;
		}

		Map<String, String> v = new LinkedHashMap<>();
		v.put("name", variable);
		for (String key : new String[] { "long_name", "units", "standard_name" }) {
		    String value = attrs.get(key);
		    if (value != null && !value.trim().isEmpty()) {
			v.put(key, value.trim());
		    }
		}
		out.add(v);
	    }

	    return out;
	}

	static boolean isCoordinate(String name) {

	    String n = name.toLowerCase(Locale.ROOT);
	    return n.equals("lat") || n.equals("lon") || n.equals("latitude") || n.equals("longitude") || n.equals("grid_lat")
		    || n.equals("grid_lon") || n.equals("time_bounds") || n.endsWith("_proj") || n.equals("projection") || n.equals("crs");
	}
    }
}
