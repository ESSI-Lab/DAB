/**
 * 
 */
package eu.essi_lab.profiler.wis.collections;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionOperation;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class WISCollectionsHandler extends DefaultRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    CollectionOperation operation = wr.getCollectionOperation();
	    if (topRequest != null && topRequest.equals(TopRequest.COLLECTIONS) && operation == null) {
		ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    } else {
		ret.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}
	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	// DataCacheConnector dataCacheConnector = null;

	try {

	    String template = WISUtils.getResourceAsString("wis2box/collections.json");

	    String ret = WISUtils.filter(webRequest, template);
	    JSONObject json = new JSONObject(ret);

	    JSONArray collections = json.getJSONArray("collections");

	    int tempSize = 0;

	    String url = WISUtils.getUrl(webRequest);

	    List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	    Optional<String> optionalView = webRequest.extractViewId();
	    String view = "whos";
	    if (optionalView.isPresent()) {
		view = optionalView.get();
	    }

	    ResultSet<GSResource> resultSet = WISUtils.getMetadataItems(null, optionalView);
	    List<GSResource> resources = resultSet.getResultsList();

	    Double w = null;
	    Double e = null;
	    Double s = null;
	    Double n = null;
	    for (GSResource resource : resources) {
		CoreMetadata metadata = resource.getHarmonizedMetadata().getCoreMetadata();

		JSONObject accessCollection = new JSONObject();
		JSONObject discoveryCollection = new JSONObject();

		GSSource source = resource.getSource();

		String accessId = metadata.getIdentifier() + "-access";
		String discoveryId = metadata.getIdentifier();

		accessCollection.put("id", accessId);
		discoveryCollection.put("id", discoveryId);

		GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		w = bbox.getWest();
		e = bbox.getEast();
		s = bbox.getSouth();
		n = bbox.getNorth();

		WISUtils.addGeometry(accessCollection, w, e, s, n);
		WISUtils.addGeometry(discoveryCollection, w, e, s, n);

		JSONArray keywords = new JSONArray();
		keywords.put("wis2");
		keywords.put("default");
		keywords.put("wmo");
		keywords.put("whos");
		keywords.put("dab");
		// access collection
		accessCollection.put("id", accessId);
		accessCollection.put("title", metadata.getTitle() + " (access)");
		accessCollection.put("description", metadata.getAbstract());
		accessCollection.put("keywords", keywords);

		// discovery collection
		discoveryCollection.put("id", discoveryId);
		discoveryCollection.put("title", metadata.getTitle() + " (discovery)");
		discoveryCollection.put("description", metadata.getAbstract());
		discoveryCollection.put("keywords", keywords);

		// access collection
		JSONArray accessLinks = new JSONArray();
		WISUtils.addLink(accessLinks, "OAFeat", "collection", accessId, url + "/collections/" + accessId);
		WISUtils.addLink(accessLinks, "OARec", "canonical", accessId, url + "/collections/discovery-metadata/items/" + accessId);
		WISUtils.addLink(accessLinks, "application/json", "root", "The landing page of this server as JSON", url + "?f=json");
		WISUtils.addLink(accessLinks, "text/html", "root", "The landing page of this server as HTML", url + "?f=html");
		WISUtils.addLink(accessLinks, "application/json", "self", "This document as JSON",
			url + "/collections/" + accessId + "?f=json");
		WISUtils.addLink(accessLinks, "text/html", "alternate", "This document as HTML",
			url + "/collections/" + accessId + "?f=html");
		WISUtils.addLink(accessLinks, "application/geo+json", "items", "items as JSON",
			url + "/collections/" + accessId + "/items?f=json");
		WISUtils.addLink(accessLinks, "text/html", "items", "items as HTML", url + "/collections/" + accessId + "/items?f=html");
		accessCollection.put("links", accessLinks);
		WISUtils.addExtent(accessCollection, w, e, s, n);
		accessCollection.put("itemType", "feature");
		collections.put(accessCollection);

		// discovery collection
		JSONArray discoveryLinks = new JSONArray();
		// WISUtils.addLink(discoveryLinks, "OAFeat", "collection", accessId, url + "/collections/" + accessId);
		// WISUtils.addLink(discoveryLinks, "text/html", "canonical", accessId, url +
		// "/collections/discovery-metadata/items/" + accessId);
		WISUtils.addLink(discoveryLinks, "application/json", "root", "The landing page of this server as JSON", url + "?f=json");
		// WISUtils.addLink(discoveryLinks, "text/html", "root", "The landing page of this server as HTML", url
		// + "?f=html");
		WISUtils.addLink(discoveryLinks, "application/json", "self", "This document as JSON",
			url + "/collections/" + discoveryId + "?f=json");
		// WISUtils.addLink(discoveryLinks, "text/html", "alternate", "This document as HTML", url +
		// "/collections/" + discoveryId + "?f=html");
		WISUtils.addLink(discoveryLinks, "application/geo+json", "items", "items as JSON",
			url + "/collections/" + discoveryId + "/items?f=json");
		// WISUtils.addLink(discoveryLinks, "text/html", "items", "items as HTML", url + "/collections/" +
		// discoveryId + "/items?f=html");
		discoveryCollection.put("links", discoveryLinks);
		WISUtils.addExtent(discoveryCollection, w, e, s, n);
		discoveryCollection.put("itemType", "record");
		collections.put(discoveryCollection);

	    }

	    ret = json.toString();
	    return ret;

	} catch (

	Exception e) {
	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WIS_HANDLER_ERROR, //
		    e);
	}

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
