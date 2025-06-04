/**
 * 
 */
package eu.essi_lab.profiler.wis.metadata.collection;

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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.CollectionOperation;
import eu.essi_lab.profiler.wis.WISRequest.Parameter;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class WISDiscoveryMetadataHandler extends DefaultRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    CollectionItems collectionItem = wr.getCollectionItem();
	    if (topRequest.equals(TopRequest.COLLECTIONS) && collectionItem.equals(CollectionItems.DISCOVERY_METADATA)) {
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

	try {

	    WISRequest wis = new WISRequest(webRequest);
	    String limit = wis.getParameterValue(Parameter.LIMIT);
	    if (limit == null || limit.equals("")) {
		limit = "10";
	    }
	    Integer l = Integer.parseInt(limit);
	    String offset = wis.getParameterValue(Parameter.OFFSET);
	    if (offset == null || offset.equals("")) {
		offset = "0";
	    }
	    Integer o = Integer.parseInt(offset);
	    CollectionOperation operation = wis.getCollectionOperation();
	    String ret = null;
	    JSONObject json = null;
	    switch (operation) {
	    case GET_DESCRIPTION:
		String template = WISUtils.getResourceAsString("wis2box/discoveryMetadata.json");
		// JSONObject json = new JSONObject(template);
		// ret = json.toString();
		ret = WISUtils.filter(webRequest, template);
		return ret;
	    case GET_QUERYABLES:
		break;
	    case GET_ITEMS:
		json = new JSONObject();
		json.put("type", "FeatureCollection");

	    case GET_ITEM:

		String itemIdentifier = wis.getCollectionParameter();
		Optional<String> optionalView = webRequest.extractViewId();
		ResultSet<GSResource> resultSet = WISUtils.getMetadataItems(itemIdentifier, optionalView);
		List<GSResource> resources = resultSet.getResultsList();

		JSONArray features = WISUtils.mapFeatures(resources);
		WISUtils.enrichFeaturesWithLinks(features, webRequest);

		if (json != null) {
		    json.put("features", features);
		    json.put("numberMatched", features.length());
		    json.put("numberReturned", features.length());
		    JSONArray jsonLinks = new JSONArray();

		    String url = WISUtils.getUrl(webRequest);

		    WISUtils.addLink(jsonLinks, "application/geo+json", "self", "This document as GeoJSON",
			    url + "/collections/discovery-metadata/items?f=json");

		    WISUtils.addLink(jsonLinks, "application/json", "collection", "Discovery metadata",
			    url + "/collections/discovery-metadata/items?f=html");

		    WISUtils.addLink(jsonLinks, "text/html", "alternate", "This document as HTML",
			    url + "/collections/discovery-metadata/items");

		    json.put("links", jsonLinks);
		    
		    json.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());
		    ret = json.toString();
		}else {
		    ret = features.getJSONObject(0).toString();
		}

		

		return ret;

	    default:
		break;
	    }

	    return null;

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
