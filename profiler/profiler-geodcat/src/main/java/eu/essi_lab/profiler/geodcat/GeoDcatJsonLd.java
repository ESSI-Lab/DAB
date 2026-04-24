package eu.essi_lab.profiler.geodcat;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;

/**
 * GeoDCAT-AP 3.1.0–oriented JSON-LD (aligned with DCAT 3 / DCAT-AP 3) from harmonized {@link GSResource} metadata.
 * <p>
 * Spatial coverage uses {@code dct:spatial} / {@code dcat:bbox} with a GeoSPARQL WKT literal, as in the GeoDCAT-AP
 * examples. Dataset-level mapping is informed by ISO 19115 elements also covered in
 * {@code eu.essi_lab.gssrv.conf.task.turtle.TurtleMapper} (agents, keywords, themes, dates, language, constraints,
 * distributions).
 */
public final class GeoDcatJsonLd {

    public static final String MEDIA_TYPE = "application/ld+json";

    /** GeoDCAT-AP application profile namespace (prefix {@code geodcatap}). */
    public static final String NS_GEODCAT_AP = "http://data.europa.eu/930/";

    /** DCAT-AP 3 namespace for EU extensions (prefix {@code dcatap}). */
    public static final String NS_DCATAP = "http://data.europa.eu/r5r/";

    /** Hydra Core vocabulary (collection paging). */
    public static final String NS_HYDRA = "http://www.w3.org/ns/hydra/core#";

    private static final String GSP_WKT_LITERAL = "http://www.opengis.net/ont/geosparql#wktLiteral";

    private static final Set<String> SKIP_KEYWORD_TYPES = Set.of("platform", "platform_class", "parameter", "instrument",
	    "cruise", "project");

    private GeoDcatJsonLd() {
    }

    /**
     * JSON-LD {@code @context} with prefixes from GeoDCAT-AP 3.1.0 and DCAT 3 usage.
     */
    public static JSONObject jsonLdContext() {

	JSONObject ctx = new JSONObject();
	ctx.put("dcat", "http://www.w3.org/ns/dcat#");
	ctx.put("dct", "http://purl.org/dc/terms/");
	ctx.put("dctype", "http://purl.org/dc/dcmitype/");
	ctx.put("foaf", "http://xmlns.com/foaf/0.1/");
	ctx.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	ctx.put("locn", "http://www.w3.org/ns/locn#");
	ctx.put("skos", "http://www.w3.org/2004/02/skos/core#");
	ctx.put("prov", "http://www.w3.org/ns/prov#");
	ctx.put("adms", "http://www.w3.org/ns/adms#");
	ctx.put("xsd", "http://www.w3.org/2001/XMLSchema#");
	ctx.put("geodcatap", NS_GEODCAT_AP);
	ctx.put("dcatap", NS_DCATAP);
	ctx.put("gsp", "http://www.opengis.net/ont/geosparql#");
	return ctx;
    }

    private static JSONObject catalogJsonLdContext() {

	JSONObject ctx = jsonLdContext();
	ctx.put("hydra", NS_HYDRA);
	return ctx;
    }

    public static JSONObject catalogDocument(WebRequest request, JSONArray datasetSummaries) {

	return catalogDocument(request, datasetSummaries, null, null, 0);
    }

    /**
     * @param totalMatched
     *            total hits from discovery (may be null if count was not requested)
     * @param page
     *            current page (start &ge; 1, size &gt; 0); if null, no Hydra paging is added
     * @param returnedCount
     *            number of resources in this response; used to infer {@code hydra:next} when {@code totalMatched} is null
     */
    public static JSONObject catalogDocument(WebRequest request, JSONArray datasetSummaries, Integer totalMatched, Page page,
	    int returnedCount) {

	JSONObject doc = new JSONObject();
	boolean paging = page != null && page.getSize() > 0;
	doc.put("@context", paging ? catalogJsonLdContext() : jsonLdContext());

	String catalogUri = GeoDcatUrlHelper.currentResourceUri(request);
	doc.put("@id", catalogUri);
	doc.put("@type", "dcat:Catalog");
	doc.put("dct:title", "DAB DCAT catalog");
	doc.put("dct:description", "Dataset entries discovered via the DAB GeoDCAT profiler.");
	doc.put("dct:conformsTo", new JSONObject().put("@id", NS_GEODCAT_AP));
	doc.put("dcat:dataset", datasetSummaries);

	if (paging) {

	    int start = page.getStart();
	    int size = page.getSize();
	    if (totalMatched != null) {
		doc.put("hydra:totalItems", totalMatched);
	    }
	    String firstUrl = GeoDcatUrlHelper.catalogPageUrl(request, 1, size);
	    if (!firstUrl.isEmpty()) {
		doc.put("hydra:first", jsonLdIri(firstUrl));
	    }
	    if (totalMatched != null && totalMatched > 0) {

		int lastStart = 1 + ((totalMatched - 1) / size) * size;
		String lastUrl = GeoDcatUrlHelper.catalogPageUrl(request, lastStart, size);
		if (!lastUrl.isEmpty()) {
		    doc.put("hydra:last", jsonLdIri(lastUrl));
		}
	    }
	    if (start > 1) {

		int prevStart = Math.max(1, start - size);
		String prevUrl = GeoDcatUrlHelper.catalogPageUrl(request, prevStart, size);
		if (!prevUrl.isEmpty()) {
		    doc.put("hydra:previous", jsonLdIri(prevUrl));
		}
	    }
	    boolean hasNext = false;
	    if (totalMatched != null) {
		hasNext = start + size <= totalMatched;
	    } else {
		hasNext = returnedCount >= size && size > 0;
	    }
	    if (hasNext) {

		String nextUrl = GeoDcatUrlHelper.catalogPageUrl(request, start + size, size);
		if (!nextUrl.isEmpty()) {
		    doc.put("hydra:next", jsonLdIri(nextUrl));
		}
	    }
	}

	return doc;
    }

    private static JSONObject jsonLdIri(String url) {

	return new JSONObject().put("@id", url);
    }

    public static JSONObject datasetSummary(WebRequest request, GSResource resource) {

	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();
	String title = safe(core.getTitle());
	String id = safe(resource.getPublicId());
	String landing = GeoDcatUrlHelper.datasetLandingPageUri(request, resource.getPublicId());

	JSONObject ds = new JSONObject();
	ds.put("@id", landing);
	ds.put("@type", "dcat:Dataset");
	ds.put("dct:title", title.isEmpty() ? id : title);
	ds.put("dct:identifier", id);
	ds.put("dcat:landingPage", new JSONObject().put("@id", landing));

	return ds;
    }

    public static JSONObject datasetDescription(WebRequest request, GSResource resource) {

	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata mi = core.getMIMetadata();
	DataIdentification info = mi.getDataIdentification();
	String landing = GeoDcatUrlHelper.currentResourceUri(request);

	JSONObject ds = new JSONObject();
	ds.put("@context", jsonLdContext());
	ds.put("@id", landing);
	ds.put("@type", "dcat:Dataset");

	ds.put("dct:conformsTo", new JSONObject().put("@id", NS_GEODCAT_AP));

	String id = safe(resource.getPublicId());

	String citationId = "";
	if (info != null) {
	    citationId = safe(info.getResourceIdentifier());
	}

	if (!citationId.isEmpty() && !citationId.equals(id)) {

	    JSONArray ids = new JSONArray();
	    ids.put(id);
	    if (citationId.startsWith("http://") || citationId.startsWith("https://")) {
		ids.put(new JSONObject().put("@id", citationId));
	    } else {
		ids.put(citationId);
	    }
	    ds.put("dct:identifier", ids);
	} else {
	    ds.put("dct:identifier", id);
	}

	String title = info != null ? safe(info.getCitationTitle()) : safe(core.getTitle());
	ds.put("dct:title", title.isEmpty() ? id : title);

	String abs = info != null ? safe(info.getAbstract()) : safe(core.getAbstract());
	if (!abs.isEmpty()) {
	    ds.put("dct:description", abs);
	}

	if (isDoi(citationId)) {
	    ds.put("adms:identifier", admsDoiIdentifier(citationId));
	} else if (isDoi(id)) {
	    ds.put("adms:identifier", admsDoiIdentifier(id));
	}

	if (info != null) {

	    putTopicCategories(info, ds);
	    putDescriptiveKeywordsAndThemes(info, ds);
	    putLanguages(info, ds);

	    String revision = safe(info.getCitationRevisionDate());
	    if (!revision.isEmpty()) {
		ds.put("dct:issued", revision);
	    }

	    putResponsibleParties(info, ds);
	    putTemporal(info.getTemporalExtent(), ds);
	    putSpatialBbox(info.getGeographicBoundingBox(), ds);
	    putAccessRights(info, ds);
	    putDistributions(mi, info, ds);

	} else {

	    putTemporal(core.getTemporalExtent(), ds);
	    putSpatialBbox(core.getBoundingBox(), ds);
	    putDistributionsMinimal(core, mi, ds);
	}

	ds.put("dcat:landingPage", new JSONObject().put("@id", landing));

	return ds;
    }

    private static JSONObject admsDoiIdentifier(String doi) {

	JSONObject node = new JSONObject();
	node.put("@type", "adms:Identifier");
	node.put("skos:notation", doi);
	node.put("adms:schemaAgency", new JSONObject().put("@id", "https://registry.identifiers.org/registry/doi"));
	return node;
    }

    private static boolean isDoi(String id) {

	if (id == null || id.isEmpty()) {
	    return false;
	}
	return id.startsWith("10.") || id.contains("doi.org");
    }

    private static void putTopicCategories(DataIdentification info, JSONObject ds) {

	try {

	    JSONArray arr = new JSONArray();
	    Iterator<String> topics = info.getTopicCategoriesStrings();
	    while (topics.hasNext()) {
		arr.put(topics.next());
	    }
	    mergeJsonArray(ds, "dcat:theme", arr);

	} catch (RuntimeException ignored) {
	    //
	}
    }

    private static void putDescriptiveKeywordsAndThemes(DataIdentification info, JSONObject ds) {

	JSONArray themes = new JSONArray();
	JSONArray keywords = new JSONArray();
	Set<String> seenTheme = new HashSet<>();
	Set<String> seenKw = new HashSet<>();

	Iterator<Keywords> it = info.getKeywords();
	while (it.hasNext()) {

	    Keywords kw = it.next();
	    String typeCode = kw.getTypeCode();
	    if (typeCode != null && SKIP_KEYWORD_TYPES.contains(typeCode.toLowerCase(Locale.ROOT))) {
		continue;
	    }

	    Iterator<String> values = kw.getKeywords();
	    while (values.hasNext()) {

		String v = values.next();
		if (v == null || v.isEmpty()) {
		    continue;
		}
		if ("theme".equalsIgnoreCase(typeCode)) {
		    if (seenTheme.add(v)) {
			themes.put(v);
		    }
		} else {
		    if (seenKw.add(v)) {
			keywords.put(v);
		    }
		}
	    }
	}

	mergeJsonArray(ds, "dcat:theme", themes);
	if (keywords.length() > 0) {
	    ds.put("dcat:keyword", keywords.length() == 1 ? keywords.get(0) : keywords);
	}
    }

    private static void mergeJsonArray(JSONObject ds, String key, JSONArray extra) {

	if (extra.length() == 0) {
	    return;
	}
	if (!ds.has(key)) {
	    ds.put(key, extra.length() == 1 ? extra.get(0) : extra);
	    return;
	}
	JSONArray merged = new JSONArray();
	Object cur = ds.get(key);
	if (cur instanceof JSONArray) {
	    JSONArray a = (JSONArray) cur;
	    for (int i = 0; i < a.length(); i++) {
		merged.put(a.get(i));
	    }
	} else {
	    merged.put(cur);
	}
	for (int i = 0; i < extra.length(); i++) {
	    merged.put(extra.get(i));
	}
	ds.put(key, merged);
    }

    private static void putLanguages(DataIdentification info, JSONObject ds) {

	JSONArray langs = new JSONArray();
	Iterator<String> it = info.getLanguages();
	while (it.hasNext()) {

	    String code = it.next();
	    if (code == null || code.isEmpty()) {
		continue;
	    }
	    String uri = mapLanguageCodeToUri(code.trim());
	    if (uri != null) {
		langs.put(new JSONObject().put("@id", uri));
	    }
	}
	if (langs.length() == 1) {
	    ds.put("dct:language", langs.get(0));
	} else if (langs.length() > 1) {
	    ds.put("dct:language", langs);
	}
    }

    /**
     * Map common ISO 639 codes to LOC URIs (same family as TurtleMapper example).
     */
    private static String mapLanguageCodeToUri(String code) {

	String c = code.toLowerCase(Locale.ROOT);
	return switch (c) {
	    case "eng", "en" -> "http://publications.europa.eu/resource/authority/language/ENG";
	    case "ita", "it" -> "http://publications.europa.eu/resource/authority/language/ITA";
	    case "deu", "ger", "de" -> "http://publications.europa.eu/resource/authority/language/DEU";
	    case "fra", "fre", "fr" -> "http://publications.europa.eu/resource/authority/language/FRA";
	    case "spa", "es" -> "http://publications.europa.eu/resource/authority/language/SPA";
	    default -> {
		if (c.length() < 2) {
		    yield null;
		}
		yield "http://id.loc.gov/vocabulary/iso639-1/" + c.substring(0, 2);
	    }
	};
    }

    private static void putResponsibleParties(DataIdentification info, JSONObject ds) {

	List<ResponsibleParty> parties = new ArrayList<>();
	parties.addAll(info.getPointOfContactParty());
	for (ResponsibleParty p : info.getCitationResponsibleParties()) {
	    parties.add(p);
	}

	JSONArray publishers = new JSONArray();
	JSONArray creators = new JSONArray();
	JSONArray attributions = new JSONArray();

	for (ResponsibleParty p : parties) {

	    String role = p.getRoleCode();
	    if (role == null) {
		role = "pointOfContact";
	    }
	    role = role.toLowerCase(Locale.ROOT);

	    switch (role) {
		case "publisher" -> publishers.put(agentNode(p));
		case "author", "originator" -> creators.put(agentNode(p));
		case "pointofcontact" -> {
		    if (!ds.has("dcat:contactPoint")) {
			ds.put("dcat:contactPoint", agentNode(p));
		    }
		}
		default -> {
		    JSONObject attr = new JSONObject();
		    attr.put("@type", "prov:Attribution");
		    attr.put("prov:agent", agentNode(p));
		    JSONObject roleConcept = new JSONObject();
		    roleConcept.put("@type", "skos:Concept");
		    roleConcept.put("skos:prefLabel", role);
		    attr.put("dcat:hadRole", roleConcept);
		    attributions.put(attr);
		}
	    }
	}

	if (publishers.length() == 1) {
	    ds.put("dct:publisher", publishers.get(0));
	} else if (publishers.length() > 1) {
	    ds.put("dct:publisher", publishers);
	}
	if (creators.length() == 1) {
	    ds.put("dct:creator", creators.get(0));
	} else if (creators.length() > 1) {
	    ds.put("dct:creator", creators);
	}
	if (attributions.length() == 1) {
	    ds.put("prov:qualifiedAttribution", attributions.get(0));
	} else if (attributions.length() > 1) {
	    ds.put("prov:qualifiedAttribution", attributions);
	}
    }

    private static JSONObject agentNode(ResponsibleParty p) {

	String orgUri = p.getOrganisationURI();
	String orgName = p.getOrganisationName();
	String indName = p.getIndividualName();

	JSONObject agent = new JSONObject();
	agent.put("@type", "foaf:Agent");

	if (orgUri != null && (orgUri.startsWith("http://") || orgUri.startsWith("https://"))) {
	    agent.put("@id", orgUri);
	}
	if (orgName != null && !orgName.isEmpty()) {
	    agent.put("foaf:name", orgName);
	} else if (indName != null && !indName.isEmpty()) {
	    agent.put("foaf:name", indName);
	}

	return agent;
    }

    private static void putTemporal(TemporalExtent time, JSONObject ds) {

	if (time == null) {
	    return;
	}

	String begin = safe(time.getBeginPosition());
	String end = safe(time.getEndPosition());
	if (begin.isEmpty() && end.isEmpty()) {
	    return;
	}

	JSONObject period = new JSONObject();
	period.put("@type", "dcat:PeriodOfTime");
	if (!begin.isEmpty()) {
	    period.put("dcat:startDate", begin);
	}
	if (!end.isEmpty()) {
	    period.put("dcat:endDate", end);
	}
	ds.put("dct:temporal", period);
    }

    private static void putSpatialBbox(GeographicBoundingBox bbox, JSONObject ds) {

	String wkt = toWktPolygon(bbox);
	if (wkt == null) {
	    return;
	}

	JSONObject bboxLiteral = new JSONObject();
	bboxLiteral.put("@value", wkt);
	bboxLiteral.put("@type", GSP_WKT_LITERAL);

	JSONObject location = new JSONObject();
	location.put("@type", "dct:Location");
	location.put("dcat:bbox", bboxLiteral);

	ds.put("dct:spatial", location);
    }

    private static void putAccessRights(DataIdentification info, JSONObject ds) {

	Iterator<LegalConstraints> it = info.getLegalConstraints();
	while (it.hasNext()) {

	    LegalConstraints lc = it.next();
	    String code = lc.getAccessConstraintCode();
	    if (code != null && !code.isEmpty() && !"otherRestrictions".equalsIgnoreCase(code)) {

		if (code.startsWith("http://") || code.startsWith("https://")) {
		    ds.put("dct:accessRights", new JSONObject().put("@id", code));
		} else {
		    ds.put("dct:accessRights", code);
		}
		return;
	    }
	    String other = lc.getOtherConstraint();
	    if (other != null) {

		String uri = extractFirstHttpUri(other);
		if (uri != null) {
		    ds.put("dct:accessRights", new JSONObject().put("@id", uri));
		    return;
		}
	    }
	}
    }

    private static String extractFirstHttpUri(String text) {

	int i = text.indexOf("http");
	if (i < 0) {
	    return null;
	}
	String rest = text.substring(i);
	int end = rest.length();
	for (int j = 0; j < rest.length(); j++) {
	    char c = rest.charAt(j);
	    if (c == ' ' || c == '\n' || c == ')' || c == '>' || c == '"') {
		end = j;
		break;
	    }
	}
	return rest.substring(0, end);
    }

    private static void putDistributions(MIMetadata mi, DataIdentification info, JSONObject ds) {

	Distribution distribution = mi.getDistribution();
	if (distribution == null) {
	    return;
	}

	Iterator<Online> onIt = distribution.getDistributionOnlines();
	List<Online> onlines = new ArrayList<>();
	while (onIt.hasNext()) {
	    onlines.add(onIt.next());
	}
	if (onlines.isEmpty()) {
	    return;
	}

	List<String> formatNames = new ArrayList<>();
	Iterator<Format> fit0 = distribution.getFormats();
	while (fit0.hasNext()) {
	    Format f = fit0.next();
	    if (f.getName() != null && !f.getName().isEmpty()) {
		formatNames.add(f.getName());
	    }
	}

	JSONObject licenseFromLegal = info != null ? licenseNodeFromLegal(info) : null;

	JSONArray dists = new JSONArray();
	for (int i = 0; i < onlines.size(); i++) {

	    Online online = onlines.get(i);

	    JSONObject dist = new JSONObject();
	    dist.put("@type", "dcat:Distribution");

	    if (licenseFromLegal != null) {
		dist.put("dct:license", licenseFromLegal);
	    }

	    String linkage = online.getLinkage();
	    if (linkage != null && !linkage.isEmpty() && !isIncompleteUrl(linkage)) {

		boolean download = false;
		String protocol = online.getProtocol();
		if (protocol != null && protocol.toLowerCase(Locale.ROOT).contains("download")) {
		    download = true;
		}
		if (download) {
		    dist.put("dcat:downloadURL", urlNode(linkage));
		} else {
		    dist.put("dcat:accessURL", urlNode(linkage));
		}
	    }

	    if (online.getProtocol() != null && !online.getProtocol().isEmpty()) {
		String prot = online.getProtocol();
		if (prot.startsWith("http://") || prot.startsWith("https://")) {
		    dist.put("dct:conformsTo", new JSONObject().put("@id", prot));
		} else {
		    dist.put("dct:conformsTo", prot);
		}
	    }

	    String name = online.getName();
	    if (name != null && !name.isEmpty()) {
		dist.put("dct:title", name);
	    }

	    if (i < formatNames.size()) {
		dist.put("dct:format", formatNames.get(i));
	    } else if (!formatNames.isEmpty()) {
		dist.put("dct:format", formatNames.get(0));
	    }

	    dists.put(dist);
	}

	if (dists.length() == 1) {
	    ds.put("dcat:distribution", dists.get(0));
	} else {
	    ds.put("dcat:distribution", dists);
	}
    }

    /** Fallback when {@link DataIdentification} is not available. */
    private static void putDistributionsMinimal(CoreMetadata core, MIMetadata mi, JSONObject ds) {

	Online online = core.getOnline();
	if (online != null && online.getLinkage() != null && !online.getLinkage().isEmpty() && !isIncompleteUrl(online.getLinkage())) {

	    JSONObject dist = new JSONObject();
	    dist.put("@type", "dcat:Distribution");
	    dist.put("dcat:accessURL", urlNode(online.getLinkage()));
	    String name = online.getName();
	    if (name != null && !name.isEmpty()) {
		dist.put("dct:title", name);
	    }
	    ds.put("dcat:distribution", dist);
	    return;
	}

	putDistributions(mi, null, ds);
    }

    private static boolean isIncompleteUrl(String linkage) {

	return "http://".equals(linkage) || "https://".equals(linkage) || "http".equals(linkage) || "https".equals(linkage);
    }

    private static JSONObject urlNode(String url) {

	return new JSONObject().put("@id", url);
    }

    private static JSONObject licenseNodeFromLegal(DataIdentification info) {

	Iterator<LegalConstraints> it = info.getLegalConstraints();
	while (it.hasNext()) {

	    LegalConstraints legal = it.next();
	    String code = legal.getAccessConstraintCode();
	    if (code != null && !code.equals("otherRestrictions")) {
		continue;
	    }
	    String other = legal.getOtherConstraint();
	    if (other == null) {
		continue;
	    }
	    String uri = extractFirstHttpUri(other);
	    if (uri != null) {
		return new JSONObject().put("@id", uri);
	    }
	    return new JSONObject().put("rdfs:label", other);
	}
	return null;
    }

    private static String toWktPolygon(GeographicBoundingBox bbox) {

	if (bbox == null) {

	    return null;
	}

	BigDecimal w = bbox.getBigDecimalWest();
	BigDecimal e = bbox.getBigDecimalEast();
	BigDecimal s = bbox.getBigDecimalSouth();
	BigDecimal n = bbox.getBigDecimalNorth();

	if (w == null || e == null || s == null || n == null) {

	    return null;
	}

	return String.format(Locale.ROOT, "POLYGON ((%s %s, %s %s, %s %s, %s %s, %s %s))", //
		w, s, //
		e, s, //
		e, n, //
		w, n, //
		w, s);
    }

    private static String safe(String v) {

	return v == null ? "" : v;
    }
}
