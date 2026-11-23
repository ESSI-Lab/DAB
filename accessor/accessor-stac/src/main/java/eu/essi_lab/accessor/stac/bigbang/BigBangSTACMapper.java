package eu.essi_lab.accessor.stac.bigbang;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * Maps the envelopes produced by {@link BigBangSTACConnector}:
 * <ul>
 * <li>a BIGBANG STAC collection (i.e. one hydrological variable) is mapped to a {@link Dataset} or, when the full
 * harvest is enabled, to a {@link DatasetCollection} (series) parent of the item records</li>
 * <li>a BIGBANG STAC item (i.e. one monthly or annual grid) is mapped to a {@link Dataset} whose parent identifier is
 * the collection id</li>
 * </ul>
 * Besides the common STAC fields, the mapper uses:
 * <ul>
 * <li>the <code>bigbang:variable</code> summary and the collection title to describe the variable, with the unit
 * declared by the assets</li>
 * <li>the <code>proj:code</code>/<code>proj:epsg</code> and <code>gsd</code> fields for reference system and
 * resolution</li>
 * <li>the <code>sci:citation</code> field as supplemental information</li>
 * <li>the provider descriptions to extract the corresponding author and email</li>
 * <li>the collection and catalog license, documentation (<code>describedby</code>) and <code>via</code> links when
 * the mapped object lacks them</li>
 * </ul>
 *
 * @author Enrico Boldrini
 */
public class BigBangSTACMapper extends OriginalIdentifierMapper {

    /**
     *
     */
    public static final String BIGBANG_STAC_SCHEME_URI = "http://stacspec.org/bigbang";

    //
    // envelope members
    //
    static final String CATALOG_URL = "catalogUrl";
    static final String COLLECTION_URL = "collectionUrl";
    static final String ITEM_URL = "itemUrl";
    static final String HARVEST_ITEMS = "harvestItems";
    static final String CATALOG = "catalog";
    static final String COLLECTION = "collection";
    static final String ITEM = "item";

    /**
     * e.g. "BIGBANG 10.0 - Total Precipitation (TP)"
     */
    private static final Pattern VARIABLE_TITLE_PATTERN = Pattern.compile("^.*?\\s+-\\s+(.+?)\\s*\\(([^()]+)\\)\\s*$");

    /**
     * e.g. "BIGBANG 10.0 - ..."
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("BIGBANG\\s+(?:version\\s+)?([0-9]+(?:\\.[0-9]+)*)",
	    Pattern.CASE_INSENSITIVE);

    /**
     * e.g. "Corresponding author: Giovanni Braca (ISPRA): giovanni.braca@isprambiente.it"
     */
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("(?:corresponding\\s+)?author\\s*:\\s*([^(:\\n]+?)\\s*(?:\\([^)]*\\))?\\s*:",
	    Pattern.CASE_INSENSITIVE);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+");

    private static final Pattern MONTHLY_ITEM_PATTERN = Pattern.compile(".*\\b[0-9]{4}-[0-9]{2}$");
    private static final Pattern ANNUAL_ITEM_PATTERN = Pattern.compile(".*\\b[0-9]{4}$");

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return BIGBANG_STAC_SCHEME_URI;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	return readIdentifier(new JSONObject(resource.getOriginalMetadata().getMetadata()));
    }

    /**
     * @param envelope
     * @return the collection id for collection records, <code>collectionId/itemId</code> for item records (STAC item
     *         ids are only unique within their collection)
     */
    static String readIdentifier(JSONObject envelope) {

	String collectionId = envelope.getJSONObject(COLLECTION).optString("id", null);

	JSONObject item = envelope.optJSONObject(ITEM);
	if (item == null) {
	    return collectionId;
	}

	return collectionId == null ? item.optString("id", null) : collectionId + "/" + item.optString("id");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject envelope = new JSONObject(originalMD.getMetadata());

	if (envelope.optJSONObject(ITEM) != null) {

	    return mapItem(envelope, source);
	}

	return mapCollection(envelope, source);
    }

    //
    // collection
    //

    private GSResource mapCollection(JSONObject envelope, GSSource source) {

	JSONObject collection = envelope.getJSONObject(COLLECTION);
	JSONObject catalog = Optional.ofNullable(envelope.optJSONObject(CATALOG)).orElse(new JSONObject());
	boolean parent = envelope.optBoolean(HARVEST_ITEMS, false);

	GSResource resource = parent ? new DatasetCollection() : new Dataset();
	resource.setSource(source);

	MIMetadata md = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	String level = parent ? "series" : "dataset";
	md.setHierarchyLevelName(level);
	md.addHierarchyLevelScopeCodeListValue(level);

	String id = collection.optString("id");
	if (!id.isEmpty()) {
	    md.getDataIdentification().setResourceIdentifier(id);
	}

	//
	// title, abstract, edition, citation
	//
	String title = firstNonEmpty(collection.optString("title"), id);
	md.getDataIdentification().setCitationTitle(title);

	String description = stripMarkdown(collection.optString("description"));
	String itemsSummary = summarizeItems(collection);
	if (itemsSummary != null) {
	    description = description.isEmpty() ? itemsSummary : description + "\n\n" + itemsSummary;
	}
	if (!description.isEmpty()) {
	    md.getDataIdentification().setAbstract(description);
	}

	readVersion(title, catalog.optString("title")).ifPresent(v -> md.getDataIdentification().setCitationEdition(v));

	addCitation(md, collection.optString("sci:citation"), catalog.optString("sci:citation"));

	//
	// keywords, topic categories
	//
	JSONObject summaries = Optional.ofNullable(collection.optJSONObject("summaries")).orElse(new JSONObject());

	Set<String> keywords = new LinkedHashSet<>(toStringList(collection.optJSONArray("keywords")));
	keywords.addAll(toStringList(summaries.optJSONArray("bigbang:aggregation")));
	addKeywords(md, keywords);

	//
	// constraints, contacts
	//
	addLegalConstraints(md, collection.optString("license"), collection, catalog);

	addContacts(md, collection.optJSONArray("providers"));

	//
	// extents
	//
	JSONObject extent = Optional.ofNullable(collection.optJSONObject("extent")).orElse(new JSONObject());

	addBoundingBox(md, Optional.ofNullable(extent.optJSONObject("spatial")).//
		map(s -> s.optJSONArray("bbox")).//
		map(b -> b.optJSONArray(0)).//
		orElse(null));

	JSONArray interval = Optional.ofNullable(extent.optJSONObject("temporal")).//
		map(t -> t.optJSONArray("interval")).//
		map(i -> i.optJSONArray(0)).//
		orElse(null);

	if (interval != null && interval.length() == 2) {
	    addTemporalExtent(md, readNullableString(interval, 0), readNullableString(interval, 1));
	}

	//
	// grid
	//
	JSONArray gsd = summaries.optJSONArray("gsd");
	addGrid(md, gsd != null && gsd.length() > 0 ? gsd.optDouble(0) : Double.NaN, readEPSG(collection));

	//
	// variable
	//
	String unit = readUnit(collection);
	addCoverageDescription(md, collection, unit);
	addUnits(resource, unit);

	//
	// distribution
	//
	addFormat(md);

	String collectionURL = envelope.optString(COLLECTION_URL);

	addOnlineResources(md, collection.optJSONObject("assets"), collectionURL, //
		collectionURL, "STAC Collection", "BIGBANG STAC collection, with one item per monthly or annual grid", //
		collection, catalog);

	return resource;
    }

    //
    // item
    //

    private GSResource mapItem(JSONObject envelope, GSSource source) {

	JSONObject item = envelope.getJSONObject(ITEM);
	JSONObject collection = envelope.getJSONObject(COLLECTION);
	JSONObject catalog = Optional.ofNullable(envelope.optJSONObject(CATALOG)).orElse(new JSONObject());
	JSONObject properties = Optional.ofNullable(item.optJSONObject("properties")).orElse(new JSONObject());
	JSONObject summaries = Optional.ofNullable(collection.optJSONObject("summaries")).orElse(new JSONObject());

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	MIMetadata md = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	md.setHierarchyLevelName("dataset");
	md.addHierarchyLevelScopeCodeListValue("dataset");

	String collectionId = collection.optString("id");
	if (!collectionId.isEmpty()) {
	    // the identifier decorator turns it into the persistent identifier of the collection record
	    md.setParentIdentifier(collectionId);
	}

	String itemId = item.optString("id");
	if (!itemId.isEmpty()) {
	    md.getDataIdentification().setResourceIdentifier(itemId);
	}

	//
	// title, abstract, edition, citation
	//
	String collectionTitle = collection.optString("title");
	Optional<String> version = readVersion(collectionTitle, catalog.optString("title"));

	String title = firstNonEmpty(properties.optString("title"), itemId);
	if (version.isPresent() && !title.toUpperCase(Locale.ROOT).contains("BIGBANG")) {
	    title = "BIGBANG " + version.get() + " - " + title;
	}
	md.getDataIdentification().setCitationTitle(title);

	String description = stripMarkdown(properties.optString("description"));
	String collectionDescription = stripMarkdown(collection.optString("description"));
	if (!collectionDescription.isEmpty()) {
	    String context = "Grid of the collection \"" + firstNonEmpty(collectionTitle, collectionId) + "\". " + collectionDescription;
	    description = description.isEmpty() ? context : description + ".\n\n" + context;
	}
	if (!description.isEmpty()) {
	    md.getDataIdentification().setAbstract(description);
	}

	version.ifPresent(v -> md.getDataIdentification().setCitationEdition(v));

	addCitation(md, properties.optString("sci:citation"), collection.optString("sci:citation"), catalog.optString("sci:citation"));

	//
	// keywords, topic categories
	//
	List<String> keywords = toStringList(properties.optJSONArray("keywords"));
	addKeywords(md, keywords.isEmpty() ? toStringList(collection.optJSONArray("keywords")) : keywords);

	//
	// constraints, contacts
	//
	addLegalConstraints(md, firstNonEmpty(properties.optString("license"), collection.optString("license")), item, collection, catalog);

	JSONArray providers = properties.optJSONArray("providers");
	addContacts(md, providers != null && providers.length() > 0 ? providers : collection.optJSONArray("providers"));

	//
	// extents
	//
	JSONArray bbox = item.optJSONArray("bbox");
	if (bbox == null) {
	    bbox = Optional.ofNullable(collection.optJSONObject("extent")).//
		    map(e -> e.optJSONObject("spatial")).//
		    map(s -> s.optJSONArray("bbox")).//
		    map(b -> b.optJSONArray(0)).//
		    orElse(null);
	}
	addBoundingBox(md, bbox);

	String begin = firstNonEmpty(readNullableString(properties, "start_datetime"), readNullableString(properties, "datetime"));
	String end = firstNonEmpty(readNullableString(properties, "end_datetime"), readNullableString(properties, "datetime"));
	addTemporalExtent(md, begin, end);

	//
	// grid
	//
	double gsd = properties.optDouble("gsd");
	if (Double.isNaN(gsd) && summaries.optJSONArray("gsd") != null && summaries.getJSONArray("gsd").length() > 0) {
	    gsd = summaries.getJSONArray("gsd").optDouble(0);
	}

	Optional<Integer> epsg = readEPSG(properties.opt("proj:epsg"), properties.opt("proj:code"));
	addGrid(md, gsd, epsg.isPresent() ? epsg : readEPSG(collection));

	//
	// variable
	//
	String unit = Optional.ofNullable(readUnit(item)).orElse(readUnit(collection));
	addCoverageDescription(md, collection, unit);
	addUnits(dataset, unit);

	//
	// distribution
	//
	addFormat(md);

	String itemURL = envelope.optString(ITEM_URL);

	addOnlineResources(md, item.optJSONObject("assets"), itemURL, //
		itemURL, "STAC Item", "BIGBANG STAC item", //
		item, collection, catalog);

	return dataset;
    }

    //
    // abstract
    //

    /**
     * @return a sentence with the number of monthly and annual grids published as STAC items
     */
    static String summarizeItems(JSONObject collection) {

	JSONArray links = collection.optJSONArray("links");
	if (links == null) {
	    return null;
	}

	int total = 0;
	int monthly = 0;
	int annual = 0;

	for (int i = 0; i < links.length(); i++) {

	    JSONObject link = links.optJSONObject(i);
	    if (link == null || !"item".equals(link.optString("rel"))) {
		continue;
	    }

	    total++;
	    String itemTitle = link.optString("title").trim();
	    if (MONTHLY_ITEM_PATTERN.matcher(itemTitle).matches()) {
		monthly++;
	    } else if (ANNUAL_ITEM_PATTERN.matcher(itemTitle).matches()) {
		annual++;
	    }
	}

	if (total == 0) {
	    return null;
	}

	StringBuilder out = new StringBuilder("The collection includes ").append(total).append(" individual grids");
	if (monthly + annual == total) {
	    out.append(" (").append(monthly).append(" monthly, ").append(annual).append(" annual)");
	}
	return out.append(", each published as a STAC item.").toString();
    }

    /**
     * Removes the CommonMark emphasis and code markers used in the BIGBANG descriptions
     */
    static String stripMarkdown(String text) {

	if (text == null) {
	    return "";
	}

	return text.replace("**", "").replace("`", "").trim();
    }

    static Optional<String> readVersion(String... titles) {

	for (String t : titles) {
	    if (t != null) {
		Matcher matcher = VERSION_PATTERN.matcher(t);
		if (matcher.find()) {
		    return Optional.of(matcher.group(1));
		}
	    }
	}

	return Optional.empty();
    }

    private static void addCitation(MIMetadata md, String... citations) {

	String citation = firstNonEmpty(citations);
	if (citation != null) {
	    md.getDataIdentification().setSupplementalInformation("Cite data as follows: " + citation);
	}
    }

    private static void addKeywords(MIMetadata md, Iterable<String> keywords) {

	keywords.forEach(k -> md.getDataIdentification().addKeyword(k));

	md.getDataIdentification().addTopicCategory("inlandWaters");
	md.getDataIdentification().addTopicCategory("climatologyMeteorologyAtmosphere");
    }

    //
    // constraints
    //

    /**
     * @param md
     * @param license the STAC license field, used only if no license link is found
     * @param linkHolders the objects whose license links are used, the first having some wins
     */
    private static void addLegalConstraints(MIMetadata md, String license, JSONObject... linkHolders) {

	LegalConstraints constraints = new LegalConstraints();
	boolean any = false;

	for (JSONObject link : findLinks("license", linkHolders)) {

	    String linkTitle = firstNonEmpty(link.optString("title"), link.optString("href"));
	    constraints.addUseLimitation(link.optString("href").trim(), linkTitle);
	    any = true;
	}

	if (!any && license != null && !license.trim().isEmpty()) {
	    constraints.addUseLimitation(license.trim());
	    any = true;
	}

	if (any) {
	    md.getDataIdentification().addLegalConstraints(constraints);
	}
    }

    //
    // contacts
    //

    private static void addContacts(MIMetadata md, JSONArray providers) {

	if (providers == null) {
	    return;
	}

	for (int i = 0; i < providers.length(); i++) {

	    JSONObject provider = providers.optJSONObject(i);
	    if (provider == null || provider.optString("name").trim().isEmpty()) {
		continue;
	    }

	    String name = provider.getString("name").trim();
	    String url = provider.optString("url").trim();
	    String providerDescription = provider.optString("description");

	    ResponsibleParty party = new ResponsibleParty();
	    party.setOrganisationName(name);
	    party.setRoleCode(toRoleCode(provider));
	    party.setContactInfo(createContact(url, null));
	    md.getDataIdentification().addPointOfContact(party);

	    //
	    // corresponding author, from the provider description
	    //
	    Matcher email = EMAIL_PATTERN.matcher(providerDescription);
	    if (email.find()) {

		ResponsibleParty author = new ResponsibleParty();
		author.setOrganisationName(name);

		Matcher authorMatcher = AUTHOR_PATTERN.matcher(providerDescription);
		if (authorMatcher.find()) {
		    author.setIndividualName(authorMatcher.group(1).trim());
		}

		author.setRoleCode("pointOfContact");
		author.setContactInfo(createContact(url, email.group()));
		md.getDataIdentification().addPointOfContact(author);
	    }
	}
    }

    private static Contact createContact(String url, String email) {

	Contact contact = new Contact();

	if (email != null) {
	    Address address = new Address();
	    address.addElectronicMailAddress(email);
	    contact.setAddress(address);
	}

	if (url != null && !url.isEmpty()) {
	    Online online = new Online();
	    online.setLinkage(url);
	    contact.setOnline(online);
	}

	return contact;
    }

    /**
     * Maps the STAC provider roles (producer, processor, licensor, host) to ISO role codes; the first role wins
     */
    static String toRoleCode(JSONObject provider) {

	for (String role : toStringList(provider.optJSONArray("roles"))) {
	    switch (role.toLowerCase(Locale.ROOT)) {
	    case "producer":
		return "originator";
	    case "processor":
		return "processor";
	    case "licensor":
		return "owner";
	    case "host":
		return "distributor";
	    default:
	    }
	}

	return "pointOfContact";
    }

    //
    // extents
    //

    /**
     * @param md
     * @param bbox west, south, east, north
     */
    private static void addBoundingBox(MIMetadata md, JSONArray bbox) {

	if (bbox == null || bbox.length() != 4) {
	    return;
	}

	double west = bbox.optDouble(0);
	double south = bbox.optDouble(1);
	double east = bbox.optDouble(2);
	double north = bbox.optDouble(3);

	if (Double.isNaN(west) || Double.isNaN(south) || Double.isNaN(east) || Double.isNaN(north)) {
	    return;
	}

	md.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
    }

    private static void addTemporalExtent(MIMetadata md, String begin, String end) {

	if (begin == null) {
	    return;
	}

	TemporalExtent extent = new TemporalExtent();
	extent.setBeginPosition(begin);

	if (end != null) {
	    extent.setEndPosition(end);
	}

	md.getDataIdentification().addTemporalExtent(extent);
    }

    //
    // grid
    //

    private static void addGrid(MIMetadata md, double gsd, Optional<Integer> epsg) {

	md.getDataIdentification().setSpatialRepresentationType("grid");

	if (!Double.isNaN(gsd) && gsd > 0) {
	    md.getDataIdentification().setSpatialResolution(gsd);
	}

	epsg.ifPresent(code -> {
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:" + code);
	    referenceSystem.setCodeSpace("EPSG");
	    md.addReferenceSystemInfo(referenceSystem);
	});
    }

    /**
     * @param collection
     * @return the EPSG code from the collection <code>proj:epsg</code> or <code>proj:code</code> summaries
     */
    static Optional<Integer> readEPSG(JSONObject collection) {

	JSONObject summaries = collection.optJSONObject("summaries");
	if (summaries == null) {
	    return Optional.empty();
	}

	JSONArray epsg = summaries.optJSONArray("proj:epsg");
	JSONArray code = summaries.optJSONArray("proj:code");

	return readEPSG(epsg != null && epsg.length() > 0 ? epsg.opt(0) : null, code != null && code.length() > 0 ? code.opt(0) : null);
    }

    /**
     * @param epsg the <code>proj:epsg</code> value, e.g. 3035
     * @param code the <code>proj:code</code> value, e.g. "EPSG:3035"
     * @return
     */
    static Optional<Integer> readEPSG(Object epsg, Object code) {

	if (epsg instanceof Number && ((Number) epsg).intValue() > 0) {
	    return Optional.of(((Number) epsg).intValue());
	}

	if (code instanceof String) {

	    String c = ((String) code).trim().toUpperCase(Locale.ROOT);
	    if (c.startsWith("EPSG:")) {
		try {
		    return Optional.of(Integer.valueOf(c.substring(5).trim()));
		} catch (NumberFormatException e) {
		    GSLoggerFactory.getLogger(BigBangSTACMapper.class).warn("Invalid projection code: {}", c);
		}
	    }
	}

	return Optional.empty();
    }

    //
    // variable
    //

    private static void addCoverageDescription(MIMetadata md, JSONObject collection, String unit) {

	JSONObject summaries = Optional.ofNullable(collection.optJSONObject("summaries")).orElse(new JSONObject());
	List<String> symbols = toStringList(summaries.optJSONArray("bigbang:variable"));

	String name = null;
	String symbol = symbols.isEmpty() ? null : symbols.get(0);

	Matcher matcher = VARIABLE_TITLE_PATTERN.matcher(collection.optString("title"));
	if (matcher.matches()) {
	    name = matcher.group(1).trim();
	    if (symbol == null) {
		symbol = matcher.group(2).trim();
	    }
	}

	if (symbol == null && name == null) {
	    return;
	}

	StringBuilder description = new StringBuilder(firstNonEmpty(name, symbol));
	if (unit != null) {
	    description.append(" (").append(unit).append(")");
	}

	CoverageDescription coverage = new CoverageDescription();
	coverage.setAttributeIdentifier(firstNonEmpty(symbol, name));
	coverage.setAttributeTitle(firstNonEmpty(name, symbol));
	coverage.setAttributeDescription(description.toString());
	md.addCoverageDescription(coverage);
    }

    /**
     * Sets the attribute units (full name) and units abbreviation (the symbol declared by the STAC assets)
     *
     * @param resource
     * @param unit the unit symbol, e.g. "mm"
     */
    static void addUnits(GSResource resource, String unit) {

	if (unit == null) {
	    return;
	}

	resource.getExtensionHandler().setAttributeUnits(readUnitName(unit));
	resource.getExtensionHandler().setAttributeUnitsAbbreviation(unit);
    }

    /**
     * @param unit the unit symbol used by the BIGBANG catalog
     * @return the unit full name, or the symbol itself when unknown
     */
    static String readUnitName(String unit) {

	switch (unit.trim()) {
	case "mm":
	    return "millimetre";
	case "mm/m":
	    return "millimetre per metre";
	case "°C":
	    return "degree Celsius";
	case "1":
	    return "dimensionless";
	default:
	    return unit.trim();
	}
    }

    /**
     * @return the unit declared by the item assets or, failing that, by the first asset declaring one
     */
    static String readUnit(JSONObject object) {

	for (String member : new String[] { "item_assets", "assets" }) {

	    JSONObject assets = object.optJSONObject(member);
	    if (assets == null) {
		continue;
	    }

	    for (String key : assets.keySet()) {
		JSONObject asset = assets.optJSONObject(key);
		if (asset != null && !asset.optString("unit").trim().isEmpty()) {
		    return asset.getString("unit").trim();
		}
	    }
	}

	return null;
    }

    //
    // distribution
    //

    private static void addFormat(MIMetadata md) {

	Format format = new Format();
	format.setName("ESRI ASCII Grid (zip)");
	md.getDistribution().addFormat(format);
    }

    /**
     * @param md
     * @param assets the assets to add as downloads
     * @param baseURL the URL used to resolve relative hrefs
     * @param stacURL the URL of the mapped STAC object
     * @param stacName
     * @param stacDescription
     * @param linkHolders the objects whose documentation, source and license links are added; for each relation, the
     *            first object having some wins
     */
    private static void addOnlineResources(MIMetadata md, JSONObject assets, String baseURL, String stacURL, String stacName,
	    String stacDescription, JSONObject... linkHolders) {

	Set<String> added = new LinkedHashSet<>();

	//
	// assets
	//
	if (assets != null) {

	    List<String> keys = new ArrayList<>(assets.keySet());
	    keys.sort(null);

	    for (String key : keys) {

		JSONObject asset = assets.optJSONObject(key);
		if (asset == null || asset.optString("href").trim().isEmpty()) {
		    continue;
		}

		String url = resolve(asset.getString("href"), baseURL);
		String assetTitle = firstNonEmpty(asset.optString("title"), key);
		String assetDescription = firstNonEmpty(asset.optString("description"), assetTitle);
		List<String> roles = toStringList(asset.optJSONArray("roles"));
		String function = roles.isEmpty() || roles.contains("data") ? "download" : "information";

		Double megaBytes = asset.has("file:size") && !Double.isNaN(asset.optDouble("file:size"))
			? asset.optDouble("file:size") / (1024 * 1024)
			: null;

		addOnline(md, added, url, assetTitle, assetDescription, function, megaBytes);
	    }
	}

	//
	// documentation, source and license links
	//
	for (String rel : new String[] { "describedby", "via", "license" }) {

	    for (JSONObject link : findLinks(rel, linkHolders)) {

		String url = resolve(link.getString("href"), baseURL);
		String linkTitle = firstNonEmpty(link.optString("title"), rel);
		addOnline(md, added, url, linkTitle, linkTitle, "information", null);
	    }
	}

	if (stacURL != null && !stacURL.isEmpty()) {

	    addOnline(md, added, stacURL, stacName, stacDescription, "information", null);
	}
    }

    private static void addOnline(MIMetadata md, Set<String> added, String url, String name, String description, String function,
	    Double megaBytes) {

	if (!added.add(url)) {
	    return;
	}

	Online online = new Online();
	online.setLinkage(url);
	online.setProtocol(NetProtocolWrapper.HTTP.getCommonURN());
	online.setName(name);
	online.setDescription(description);
	online.setFunctionCode(function);

	md.getDistribution().addDistributionOnline(online, megaBytes);
    }

    private static String resolve(String href, String baseURL) {

	String h = href.trim();

	try {

	    if (URI.create(h).isAbsolute() || baseURL == null || baseURL.isEmpty()) {
		return h;
	    }

	    return URI.create(baseURL).resolve(h).toString();

	} catch (IllegalArgumentException e) {

	    GSLoggerFactory.getLogger(BigBangSTACMapper.class).warn("Invalid href: {}", h);
	    return h;
	}
    }

    //
    // utils
    //

    /**
     * @param rel
     * @param holders
     * @return the links with the given relation of the first holder having some
     */
    private static List<JSONObject> findLinks(String rel, JSONObject... holders) {

	for (JSONObject holder : holders) {

	    List<JSONObject> out = new ArrayList<>();

	    JSONArray links = holder.optJSONArray("links");
	    if (links != null) {
		for (int i = 0; i < links.length(); i++) {
		    JSONObject link = links.optJSONObject(i);
		    if (link != null && rel.equals(link.optString("rel")) && !link.optString("href").trim().isEmpty()) {
			out.add(link);
		    }
		}
	    }

	    if (!out.isEmpty()) {
		return out;
	    }
	}

	return new ArrayList<>();
    }

    private static String readNullableString(JSONArray array, int index) {

	return array.isNull(index) ? null : firstNonEmpty(array.optString(index));
    }

    private static String readNullableString(JSONObject object, String key) {

	return object.isNull(key) ? null : firstNonEmpty(object.optString(key));
    }

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
}
