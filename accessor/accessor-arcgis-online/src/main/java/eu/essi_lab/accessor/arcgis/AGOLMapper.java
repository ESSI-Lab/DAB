package eu.essi_lab.accessor.arcgis;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.CREATION;
import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author ilsanto
 */
public class AGOLMapper extends OriginalIdentifierMapper {

    private static final String IDENTIFIER_KEY = "id";
    private static final String EXTENT_KEY = "extent";
    private static final String CREATED_KEY = "created";
    private static final String TITLE_KEY = "title";
    private static final String ABSTRACT_KEY = "description";
    private static final String TAGS_KEY = "tags";
    private static final String MODIFIED_KEY = "modified";
    private static final String THUMBNAIL_KEY = "thumbnail";

    private String removeHTML(String abstractText) {

	try {
	    return Jsoup.parse(abstractText).text().trim().replaceAll("\\s+", " ");
	} catch (Exception e) {

	    e.printStackTrace();

	    try {
		return Jsoup.parse(abstractText).text().trim();
	    } catch (Exception e1) {
		e1.printStackTrace();
		try {
		    return Jsoup.parse(abstractText).text();
		} catch (Exception e2) {
		    e2.printStackTrace();
		    return abstractText;
		}
	    }

	}
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject object = new JSONObject(resource.getOriginalMetadata().getMetadata());

	return object.getString(IDENTIFIER_KEY);
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());

	GSResource dataset = new Dataset();

	// id
	String id = object.getString(IDENTIFIER_KEY);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	if (object.has(EXTENT_KEY) && !"null".equalsIgnoreCase(object.get(EXTENT_KEY).toString())) {

	    JSONArray extentArray = object.getJSONArray(EXTENT_KEY);

	    if (extentArray.length() == 2) {
		JSONArray lowerLeft = extentArray.getJSONArray(0);
		JSONArray upperRight = extentArray.getJSONArray(1);

		/**
		 * https://developers.arcgis.com/rest/users-groups-and-items/search.htm
		 * "extent": [
		 * [minX, minY],
		 * [maxX, maxY]
		 * ],
		 */
		dataIdentification.addGeographicBoundingBox("Item extent", upperRight.getDouble(1), lowerLeft.getDouble(0),
			lowerLeft.getDouble(1), upperRight.getDouble(0));
	    }

	}

	if (object.has(CREATED_KEY) && !"null".equalsIgnoreCase(object.get(CREATED_KEY).toString())) {

	    long created = object.getLong(CREATED_KEY);

	    Date parsedTime = new Date(created);

	    String time = ISO8601DateTimeUtils.getISO8601DateTime(parsedTime);

	    dataIdentification.addCitationDate(time, CREATION);

	}

	if (object.has(MODIFIED_KEY) && !"null".equalsIgnoreCase(object.get(MODIFIED_KEY).toString())) {

	    long created = object.getLong(MODIFIED_KEY);

	    Date parsedTime = new Date(created);

	    String time = ISO8601DateTimeUtils.getISO8601DateTime(parsedTime);

	    dataIdentification.addCitationDate(time, REVISION);

	}

	String title = "ArcGIS Online Item";

	if (object.has(TITLE_KEY) && !"null".equalsIgnoreCase(object.get(TITLE_KEY).toString())) {
	    title = object.getString(TITLE_KEY);
	}

	dataIdentification.setCitationTitle(title);

	if (object.has(ABSTRACT_KEY) && !"null".equalsIgnoreCase(object.get(ABSTRACT_KEY).toString())) {

	    dataIdentification.setAbstract(removeHTML(object.getString(ABSTRACT_KEY)));
	}

	if (object.has(TAGS_KEY) && !"null".equalsIgnoreCase(object.get(TAGS_KEY).toString())) {

	    JSONArray tags = object.getJSONArray(TAGS_KEY);

	    for (int i = 0; i < tags.length(); i++) {

		dataIdentification.addKeyword(tags.getString(i));

	    }

	}

	if (object.has(THUMBNAIL_KEY) && !"null".equalsIgnoreCase(object.get(THUMBNAIL_KEY).toString())) {
	    String previewSuffix = object.getString(THUMBNAIL_KEY);

	    StringBuilder builder = new StringBuilder("http://www.arcgis.com/sharing/rest/");

	    builder.append("content/items/" + id + "/info/" + previewSuffix);

	    dataIdentification.addGraphicOverview(createGraphicOverview(builder.toString()));
	}

	return dataset;
    }

    private BrowseGraphic createGraphicOverview(String url) {
	BrowseGraphic graphic = new BrowseGraphic();
	// graphic.setFileDescription(url.getPath());
	graphic.setFileName(url);
	graphic.setFileType("image/png");
	return graphic;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return AGOLMetadataSchemas.AGOL_JSON.toString();
    }
}
