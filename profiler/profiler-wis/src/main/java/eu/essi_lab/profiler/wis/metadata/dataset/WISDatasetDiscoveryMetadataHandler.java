/**
 * 
 */
package eu.essi_lab.profiler.wis.metadata.dataset;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import eu.essi_lab.model.resource.stax.ResponsiblePartyParser;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.CollectionOperation;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class WISDatasetDiscoveryMetadataHandler extends StreamingRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    CollectionItems collectionItem = wr.getCollectionItem();
	    if (topRequest.equals(TopRequest.COLLECTIONS) && collectionItem.equals(CollectionItems.DATASETS)) {
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
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		try {

		    WISRequest wis = new WISRequest(webRequest);

		    CollectionOperation operation = wis.getCollectionOperation();
		    switch (operation) {
		    case GET_DESCRIPTION:
		    case GET_QUERYABLES:
		    default:
			GSLoggerFactory.getLogger(getClass()).error("not supported");
			throw new WebApplicationException("not supported");
		    case GET_ITEMS:
		    case GET_ITEM:

			// continue
		    }

		    String header = "{\"type\": \"FeatureCollection\", \"features\": [";
		    IOUtils.copy(new ByteArrayInputStream(header.getBytes()), output);

		
		    WISDatasetDiscoveryTransformer transformer = new WISDatasetDiscoveryTransformer();
		    DiscoveryMessage discoveryMessage = transformer.transform(webRequest);

		    // VIEW
		    Optional<String> optionalView = webRequest.extractViewId();
		    if (optionalView.isPresent()) {
			StorageInfo storageUri = ConfigurationWrapper.getDatabaseURI();
			Optional<View> v = WebRequestTransformer.findView(storageUri, optionalView.get());
			if (v.isPresent()) {
			    discoveryMessage.setView(v.get());
			}
		    }
		    ResultSet<String> resultSet = exec(discoveryMessage);
		    WMSTemplateGenerator wtg = new WMSTemplateGenerator();
		    for (int i = 0; i < resultSet.getResultsList().size(); i++) {
			String result = resultSet.getResultsList().get(i);

			GIResourceParser parser = new GIResourceParser(result);

			List<String> foundParties = parser.find(new QName("CI_ResponsibleParty"));

			JSONObject feature = new JSONObject();

			String country = parser.getCountry();
			if (country == null || country.isEmpty()) {
			    country = "whos";
			} else {
			    country = Country.decode(country).getISO3().toLowerCase();
			}
			String centreId = parser.getSourceId();
			String localId = parser.getResourceId();
			if (localId == null || localId.isEmpty()) {
			    localId = parser.getFileIdentifier();
			}

			String wmoID = "urn:x-wmo:md:" + country + ":" + centreId + ":" + localId;
			feature.put("id", wmoID);

			feature.put("type", "Feature"); // GeoJSON Feature

			JSONArray conformArray = new JSONArray();
			conformArray.put("http://wis.wmo.int/spec/wcmp/2/conf/core");
			feature.put("conformsTo", conformArray);

			JSONObject properties = new JSONObject();

			feature.put("properties", properties);

			properties.put("type", "dataset");

			properties.put("title", parser.getTitle());

			properties.put("description", parser.getAbstract());

			List<String> keywords = parser.getKeywords();
			JSONArray kArray = new JSONArray();
			for (String keyword : keywords) {
			    kArray.put(keyword);
			}
			properties.put("keywords", kArray);

			WISUtils.addTheme(feature, "https://github.com/wmo-im/wcmp2-codelists/blob/main/codelists/earthsystem-domain.csv",
				"hydrology", "https://github.com/wmo-im/wcmp2-codelists/blob/main/codelists/earthsystem-domain.csv#L2");

			// TODO variable theme

			WISUtils.addGeometry(feature, parser.getWest(), parser.getEast(), parser.getSouth(), parser.getNorth());

			String begin = parser.getTmpExtentBegin();
			String end = parser.getTmpExtentEnd();

			if (begin != null && end != null && !begin.isEmpty() && !end.isEmpty()) {
			    JSONObject time = new JSONObject();
			    JSONArray intervalArray = new JSONArray();
			    intervalArray.put(begin);
			    intervalArray.put(end);
			    time.put("interval", intervalArray);
			    String timeSpacing = parser.getTimeSpacing();
			    if (timeSpacing != null && !timeSpacing.isEmpty()) {
				String timeUnits = parser.getTimeUnits();
				time.put("resolution", timeSpacing + " " + timeUnits);
			    }
			    feature.put("time", time);
			} else {
			    feature.put("time", (JSONObject) null);
			}

			// properties.put("id", topic);
			// properties.put("identifier", topic);
			// properties.put("language", "en");
			//
			//
			// properties.put("rights", "WHOS Terms of Use");
			//
			// properties.put("type", "dataset");

			JSONArray contactArray = new JSONArray();
			properties.put("contacts", contactArray);

			for (String party : foundParties) {
			    ResponsiblePartyParser partyParser = new ResponsiblePartyParser(party);
			    JSONObject contact = new JSONObject();
			    contact.put("organization", partyParser.getOrganisation());
			    JSONArray emails = new JSONArray();
			    JSONObject jEmail = new JSONObject();
			    jEmail.put("value", partyParser.getEmail());
			    emails.put(jEmail);
			    contact.put("emails", emails);
			    JSONArray links = new JSONArray();
			    JSONObject jLink = new JSONObject();
			    jLink.put("href", partyParser.getUrl());
			    links.put(jLink);
			    contact.put("links", links);
			    JSONArray roles = new JSONArray();
			    roles.put(partyParser.getRole());
			    contact.put("roles", roles);

			    contactArray.put(contact);
			}

			properties.put("created", ISO8601DateTimeUtils.getISO8601DateTime());

			properties.put("wmo:dataPolicy", "recommended");

			JSONArray links = new JSONArray();

			// WISUtils.addLink(links, "text/htmlRec", "license", "Dataset license information",
			// "https://whos.geodab.eu/gs-service/whos/registration.html");

			// WISUtils.addLink(links, "OARec", "canonical", topic, url + "/collections/" + topic +
			// "/items/" + topic);

			// WISUtils.addLink(links, "OAFeat", "collection", topic, url + "/collections/" + topic);

			String distributionLinkage = parser.getDistributionLinkage();
			String distributionProtocol = parser.getDistributionProtocol();
			String distributionName = parser.getDistributionName();

			WISUtils.addLink(links, distributionProtocol, "item", distributionName, distributionLinkage);

			if (distributionProtocol.toLowerCase().contains("wms") || distributionProtocol.toLowerCase().contains("webmap")) {
			    try {
			    JSONObject link = wtg.generate(distributionLinkage, distributionName);
			    WISUtils.addLink(links, link);
			    } catch (Exception e) {
				// TODO: handle exception
			    }
			}

			feature.put("links", links);

			print(feature.toString(), output);

			if (i < resultSet.getResultsList().size() - 1) {
			    print(",", output);
			}

		    }

		    // }
		    print(" ],\"numberMatched\": " + resultSet.getCountResponse().getCount() + ",", output);
		    print("\"numberReturned\": " + resultSet.getResultsList().size() + ",", output);
		    print("", output);
		    print("", output);
		    print("", output);
		    print("\"timeStamp\": \"" + ISO8601DateTimeUtils.getISO8601DateTime() + "\"}", output);
		    GSLoggerFactory.getLogger(getClass()).info("matched xxx {}", resultSet.getCountResponse().getCount());
		    output.close();
		} catch (

		Exception e) {
		    e.printStackTrace();
		    throw new WebApplicationException(e.getMessage());

		}
	    }

	    private void print(String string, OutputStream output) throws IOException {

		IOUtils.copy(new ByteArrayInputStream(string.getBytes()), output);

	    }
	};

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }

}
