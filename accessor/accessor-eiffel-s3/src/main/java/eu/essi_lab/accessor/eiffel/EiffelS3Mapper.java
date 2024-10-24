package eu.essi_lab.accessor.eiffel;

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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Fabrizio
 */
public class EiffelS3Mapper extends FileIdentifierMapper {

    /**
     * 
     */
    public static final String EIFFEL_SCHEMA = "eiffel/schema";
    /**
     * 
     */
    private static final String EIFFEL_ID_PREFIX = "eiffel_record_";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return EIFFEL_SCHEMA;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());

	//
	//
	//

	Dataset dataset = new Dataset();

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	dataset.setSource(source);
	
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	ExtensionHandler handler = dataset.getExtensionHandler();

	//
	// Source
	//

//	if (object.has("source")) {
//
//	    JSONObject objectSource = object.getJSONObject("source");
//	    
//	    String sourceId = objectSource.optString("id");
//	    String sourceTitle = objectSource.optString("title");
//	    
//	    boolean sourceOnline = objectSource.has("online");
//
//	    if (!sourceId.isEmpty() && !sourceTitle.isEmpty()) {
//
//		GSSource gsSource = new GSSource();
//		gsSource.setUniqueIdentifier(sourceId);
//		gsSource.setLabel(sourceTitle);
//
//		if (sourceOnline) {
//
//		    JSONArray jsonArray = objectSource.getJSONArray("online");
//		    JSONObject sourceOnlineObject = jsonArray.getJSONObject(0);
//		    String sourceUrl = sourceOnlineObject.optString("url");
//
//		    if (!sourceUrl.isEmpty()) {
//			gsSource.setEndpoint(sourceUrl);
//		    }
//		}
//
//		dataset.setSource(gsSource);
//	    }
//	}

	//
	// Identifier
	//

	String id = object.getString("id");
	id = EIFFEL_ID_PREFIX + id;

	coreMetadata.setIdentifier(id);

	//
	// Parent id
	//

	String parentId = object.optString("parentId");
	if (!parentId.isEmpty()) {

	    coreMetadata.getMIMetadata().setParentIdentifier(parentId);
	}
	
	//
	// Title
	//
	
	String title = object.optString("title");
	if (!title.isEmpty()) {

	    coreMetadata.setTitle(title);
	}

	//
	// Abstract
	//

	String description = object.optString("description");
	if (!description.isEmpty()) {
	    coreMetadata.setAbstract(description);
	}

	//
	// Timestamp
	//

	String update = object.optString("update");
	if (!update.isEmpty()) {
	    dataId.setCitationRevisionDate(update);
	}

	//
	// Created
	//

	String created = object.optString("created");
	if (!created.isEmpty()) {
	    dataId.setCitationCreationDate(created);
	}

	//
	// Topic
	//

	if (object.has("topic")) {

	    try {

		JSONArray topics = object.getJSONArray("topic");
		for (Object topic : topics) {

		    MDTopicCategoryCodeType code = MDTopicCategoryCodeType.fromValue(//
			    topic.toString());

		    dataId.addTopicCategory(code);
		}

	    } catch (Exception ex) {
	    }
	}

	//
	// Keywords
	//

	if (object.has("keyword")) {

	    JSONArray keywordsArray = object.getJSONArray("keyword");
	    for (Object keyword : keywordsArray) {
		keywords.addKeyword(keyword.toString());
	    }
	}

	//
	// Originator organisation
	//

	if (object.has("origOrgDesc")) {

	    JSONArray orgArray = object.getJSONArray("origOrgDesc");
	    for (Object desc : orgArray) {
		handler.addOriginatorOrganisationDescription(desc.toString());
	    }
	}

	//
	// Theme category
	//

	if (object.has("themeCategory")) {

	    String themeCategory = object.getString("themeCategory");
	    handler.addThemeCategory(themeCategory);
	}

	//
	// Rights
	//

	if (object.has("rights")) {

	    JSONArray rights = object.getJSONArray("rights");
	    for (Object right : rights) {
		LegalConstraints legalConstraints = new LegalConstraints();
		legalConstraints.addAccessConstraintsCode(right.toString());

		dataId.addLegalConstraints(legalConstraints);
	    }
	}

	//
	// Online
	//

	if (object.has("online")) {

	    JSONArray onlineArray = object.getJSONArray("online");
	    for (int i = 0; i < onlineArray.length(); i++) {

		JSONObject onlineObject = onlineArray.getJSONObject(i);
		String name = onlineObject.optString("name");
		String protocol = onlineObject.optString("protocol");
		String onDescription = onlineObject.optString("description");
		String function = onlineObject.optString("function");
		String url = onlineObject.optString("url");

		Online online = new Online();

		if (!name.isEmpty()) {
		    online.setName(name);
		}

		if (!onDescription.isEmpty()) {
		    online.setDescription(onDescription);
		}

		if (!protocol.isEmpty()) {
		    online.setProtocol(protocol);
		}

		if (!url.isEmpty()) {
		    online.setLinkage(url);
		}

		if (!function.isEmpty()) {
		    online.setFunctionCode(function);
		}

		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	}

	//
	// Dist format
	//

	if (object.has("format")) {

	    JSONArray formatArray = object.getJSONArray("format");
	    for (Object format : formatArray) {
		coreMetadata.addDistributionFormat(format.toString());
	    }
	}

	//
	// Spatial extent
	//

	if (object.has("where")) {

	    JSONObject where = object.getJSONArray("where").getJSONObject(0);
	    String west = where.optString("west");
	    String south = where.optString("south");
	    String east = where.optString("east");
	    String north = where.optString("north");

	    if (!west.isEmpty() && !south.isEmpty() && !east.isEmpty() && !north.isEmpty()) {

		coreMetadata.addBoundingBox(//
			Double.valueOf(north), //
			Double.valueOf(west), //
			Double.valueOf(south), //
			Double.valueOf(east));//
	    }
	}

	//
	// Temporal extent
	//

	if (object.has("when")) {

	    JSONObject where = object.getJSONArray("when").getJSONObject(0);

	    String from = where.optString("from");
	    String to = where.optString("to");

	    TemporalExtent temporalExtent = new TemporalExtent();

	    if (!from.isEmpty()) {
		temporalExtent.setBeginPosition(from);
	    }

	    if (!to.isEmpty()) {
		temporalExtent.setEndPosition(to);
	    }

	    dataId.addTemporalExtent(temporalExtent);
	}

	return dataset;
    }
}
