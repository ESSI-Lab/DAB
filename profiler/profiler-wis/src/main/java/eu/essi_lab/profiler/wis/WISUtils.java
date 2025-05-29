package eu.essi_lab.profiler.wis;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wis.station.info.WISStationInfoHandler;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmx.v_20060504.AnchorType;

public class WISUtils {

    public static String[] getViews() {

	return new String[] { "whos", "whos-plata", "whos-arctic" };
    }

    public static void addGeometry(JSONObject feature, Double w, Double s) {
	JSONObject geometry = new JSONObject();
	geometry.put("type", "Point");
	geometry.put("coordinates", getJSONArray(w, s));
	feature.put("geometry", geometry);

    }

    public static void addGeometry(JSONObject feature, String w, String e, String s, String n) {
	if (w == null || e == null || s == null || n == null) {
	    return;
	}
	double wd = Double.parseDouble(w);
	double sd = Double.parseDouble(s);
	double ed = Double.parseDouble(e);
	double nd = Double.parseDouble(n);
	addGeometry(feature, wd, ed, sd, nd);

    }

    public static void addGeometry(JSONObject feature, Double w, Double e, Double s, Double n) {
	JSONObject geometry = new JSONObject();
	geometry.put("type", "Polygon");
	JSONArray outerCoordinates = new JSONArray();
	JSONArray coordinates = new JSONArray();
	coordinates.put(getJSONArray(w, s));
	coordinates.put(getJSONArray(w, n));
	coordinates.put(getJSONArray(e, n));
	coordinates.put(getJSONArray(e, s));
	coordinates.put(getJSONArray(w, s));
	outerCoordinates.put(coordinates);
	geometry.put("coordinates", outerCoordinates);
	feature.put("geometry", geometry);

    }

    public static void addExtent(JSONObject feature, Double w, Double e, Double s, Double n) {
	JSONObject extent = new JSONObject();
	JSONObject spatial = new JSONObject();
	spatial.put("crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");
	JSONArray bbox = new JSONArray();
	JSONArray inner = new JSONArray();
	inner.put(w);
	inner.put(s);
	inner.put(e);
	inner.put(n);
	bbox.put(inner);
	spatial.put("bbox", bbox);
	extent.put("spatial", spatial);
	feature.put("extent", extent);

    }

    public static JSONArray getJSONArray(Double lon, Double lat) {
	JSONArray ret = new JSONArray();
	ret.put(lon);
	ret.put(lat);
	return ret;
    }

    public static void addLink(JSONArray links, JSONObject link) {
	links.put(link);
    }

    public static void addLink(JSONArray links, String type, String rel, String title, String href) {
	JSONObject link = new JSONObject();
	link.put("type", type);
	link.put("rel", rel);
	link.put("title", title);
	link.put("href", href);
	addLink(links, link);
    }

    public static String getResourceAsString(String name) throws IOException {
	InputStream stream = WISStationInfoHandler.class.getClassLoader().getResourceAsStream(name);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	return new String(baos.toByteArray(), StandardCharsets.UTF_8);

    }

    public static String filter(WebRequest request, String ret) {
	String url = getUrl(request);
	ret = ret.replace("${BASE_ENDPOINT}", url);
	return ret;
    }

    public static String getUrl(WebRequest webRequest) {
	String url = "";
	try {
	    UriInfo uri = webRequest.getUriInfo();
	    String viewPart = "";
	    Optional<String> view = webRequest.extractViewId();
	    if (view.isPresent()) {
		viewPart = "view/" + view.get() + "/";
	    }
	    url = uri.getBaseUri().toString() + "/" + viewPart + new WISProfilerSetting().getServicePath();
	} catch (Exception e) {
	}
	return url;
    }

    public static void addTheme(JSONObject feature, String scheme, String... concepts) {
	Concept[] cobjs = new Concept[concepts.length];
	for (int i = 0; i < concepts.length; i++) {
	    cobjs[i] = new Concept(concepts[i], concepts[i], concepts[i]);
	}
	addTheme(feature, scheme, cobjs);
    }

    public static void addTheme(JSONObject feature, String scheme, Concept... concepts) {
	JSONObject properties;
	if (feature.has("properties")) {
	    properties = feature.getJSONObject("properties");
	} else {
	    properties = new JSONObject();
	    feature.put("properties", properties);
	}
	JSONArray tArray;
	if (properties.has("themes")) {
	    tArray = properties.getJSONArray("themes");
	} else {
	    tArray = new JSONArray();
	    properties.put("themes", tArray);
	}
	JSONObject variableConcepts = new JSONObject();
	JSONArray cArray = new JSONArray();
	for (int j = 0; j < concepts.length; j += 1) {
	    JSONObject concept = new JSONObject();
	    if (concepts[j].getId() != null) {
		concept.put("id", concepts[j].getId());
	    }
	    if (concepts[j].getTitle() != null) {
		concept.put("title", concepts[j].getTitle());
	    }
	    if (concepts[j].getUrl() != null) {
		concept.put("url", concepts[j].getUrl());
	    }
	    cArray.put(concept);
	}
	variableConcepts.put("concepts", cArray);
	variableConcepts.put("scheme", scheme);
	tArray.put(variableConcepts);
	properties.put("themes", tArray);
    }

    public static void addTimeInterval(JSONObject feature, String begin, String end) {
	JSONObject time = new JSONObject();
	JSONArray timeArray = new JSONArray();
	timeArray.put(begin);
	timeArray.put(end);
	time.put("interval", timeArray);
	feature.put("time", time);

    }

    public static void addKeywords(JSONObject feature, String... keywords) {
	JSONArray kArray;
	if (feature.has("keywords")) {
	    kArray = feature.getJSONArray("keywords");
	} else {
	    kArray = new JSONArray();
	    feature.put("keywords", kArray);
	}
	for (String k : keywords) {
	    kArray.put(k);
	}
    }

    /**
     * From the metadata identifier (e.g. urn:wmo:md:sourceId:parameterConceptId),
     * it extracts the sourceId and the parameter URI according to WHOS Hydro
     * Ontology
     * 
     * @param identifier
     * @return
     */
    public static SimpleEntry<String, String> extractSourceAndParameter(String identifier) {
	identifier = identifier.substring(identifier.indexOf(":") + 1);
	identifier = identifier.substring(identifier.indexOf(":") + 1);
	identifier = identifier.substring(identifier.indexOf(":") + 1);
	String sourceId = identifier;
	String parameter = null;
	if (sourceId.contains(":")) {
	    sourceId = sourceId.substring(0, sourceId.indexOf(":"));
	    parameter = sourceId.substring(sourceId.indexOf(":") + 1);
	}
	SimpleEntry<String, String> ret = new SimpleEntry<>(sourceId, parameter);
	return ret;

    }

    public static String getWHOSView(String sourceId, String parameterURI) {
	if (parameterURI == null) {
	    return "gs-view-and(whos,gs-view-source(" + sourceId + "))";
	} else {
	    return "gs-view-and(whos,gs-view-source(" + sourceId + "),gs-view-observedPropertyURI(" + parameterURI + "))";
	}
    }

    public static JSONArray mapFeatures(List<GSResource> resources) throws GSException {
	JSONArray features = new JSONArray();

	for (GSResource resource : resources) {
	    JSONObject feature = mapFeature(resource);
	    if (feature != null) {
		features.put(feature);
	    }
	}

	return features;
    }

    public static JSONObject mapFeature(GSResource resource) throws GSException {

	JSONObject feature = new JSONObject();
	JSONObject properties = new JSONObject();
	feature.put("properties", properties);

	JSONArray conformArray = new JSONArray();
	conformArray.put("http://wis.wmo.int/spec/wcmp/2.0");
	conformArray.put("http://wis.wmo.int/spec/wcmp/2/conf/core");
	feature.put("conformsTo", conformArray);
	String id = resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
	feature.put("id", id);
	feature.put("type", "Feature");

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	Double w = bbox.getWest();
	Double e = bbox.getEast();
	Double s = bbox.getSouth();
	Double n = bbox.getNorth();
	WISUtils.addGeometry(feature, w, e, s, n);

	TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (temporalExtent != null) {
	    WISUtils.addTimeInterval(feature, temporalExtent.getBeginPosition(), temporalExtent.getEndPosition());
	}

	Iterator<Keywords> keywords = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getKeywords();
	while (keywords.hasNext()) {
	    Keywords ks = (Keywords) keywords.next();
	    String title = ks.getThesaurusNameCitationTitle();
	    List<CharacterStringPropertyType> keys = ks.getElementType().getKeyword();
	    List<Concept> concepts = new ArrayList<Concept>();
	    List<String> values = new ArrayList<String>();
	    boolean uris = false;
	    for (CharacterStringPropertyType keyType : keys) {
		Object keyValue = keyType.getCharacterString().getValue();
		if (keyValue instanceof AnchorType) {
		    uris = true;
		    AnchorType anchor = (AnchorType) keyValue;
		    String href = anchor.getHref();
		    String kid = null;
		    if (href.contains("/")) {
			kid = href.substring(href.lastIndexOf("/") + 1);
		    }
		    Concept concept = new Concept(kid, anchor.getTitle(), anchor.getHref());
		    concepts.add(concept);
		} else if (keyValue instanceof String) {
		    values.add(keyValue.toString());
		} else {
		    GSLoggerFactory.getLogger(WISUtils.class).error("Unexpcted string type: {}", keyValue.getClass().getSimpleName());

		}
	    }
	    if (title != null && uris) {
		WISUtils.addTheme(feature, title, concepts.toArray(new Concept[] {}));
	    } else {
		WISUtils.addKeywords(properties, values.toArray(new String[] {}));
	    }
	}

	WISUtils.addTheme(feature, "http://codes.wmo.int/wis/topic-hierarchy/earth-system-discipline",
		new Concept("hydrology", "Hydrology", "https://codes.wmo.int/wis/topic-hierarchy/earth-system-discipline/hydrology"));

	properties.put("description", resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	properties.put("id", id);
	properties.put("identifier", id);
	properties.put("language", "en");
	properties.put("type", "dataset");
	String datestamp = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDateStamp();
	if (datestamp != null) {
	    properties.put("created", datestamp);
	}
	List<ResponsibleParty> parties = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getPointOfContactParty();
	JSONArray contacts = new JSONArray();
	properties.put("contacts", contacts);
	for (ResponsibleParty party : parties) {
	    JSONObject contact = new JSONObject();
	    contact.put("organization", party.getOrganisationName());
	    JSONArray roles = new JSONArray();
	    roles.put("producer");
	    contact.put("roles", roles);
	    contacts.put(contact);
	}
	properties.put("wmo:dataPolicy", "core");
	// properties.put("rights", "WHOS Terms of Use");
	properties.put("title", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	properties.put("type", "dataset");

	// feature.put("properties", properties);

	return feature;
    }

    public static ResultSet<GSResource> getMetadataItems(String metadataIdentifier, Optional<String> optionalView) throws GSException {
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(UUID.randomUUID().toString());

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	discoveryMessage.setPage(new Page(1, 1000));
	discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	discoveryMessage.setDataBaseURI(uri);

	String viewId = null;

	if (optionalView.isPresent()) {
	    viewId = optionalView.get();
	    WebRequestTransformer.setView(viewId, uri, discoveryMessage);
	}
	if (metadataIdentifier != null) {
	    discoveryMessage.setUserBond(BondFactory.createOriginalIdentifierBond(metadataIdentifier));
	} else {
	    discoveryMessage.setUserBond(BondFactory.createExistsSimpleValueBond(MetadataElement.WIS_TOPIC_HIERARCHY));
	}

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	return resultSet;
    }

    public static void enrichFeaturesWithLinks(JSONArray features, WebRequest webRequest) {
	for (int i = 0; i < features.length(); i++) {
	    JSONObject feature = features.getJSONObject(i);
	    String id = feature.getString("id");
	    JSONArray links = new JSONArray();
	    // the base URL, e.g.:
	    // http://localhost:9090/gs-service/services/essi/view/whos/oapi
	    SimpleEntry<String, String> sourceParameter = WISUtils.extractSourceAndParameter(id);
	    String sourceId = sourceParameter.getKey();
	    String parameterURI = sourceParameter.getValue();

	    String view = WISUtils.getWHOSView(sourceId, parameterURI);
	    String url = WISUtils.getUrl(webRequest);
	    url = url.replace("/view/whos/", "/view/" + view + "/");
	    WISUtils.addLink(links, "application/json", "canonical", id, url + "/collections/discovery-metadata/items/" + id);
	    WISUtils.addLink(links, "application/json", "collection", id, url + "/collections/" + id);
	    feature.put("links", links);
	}

    }
}
