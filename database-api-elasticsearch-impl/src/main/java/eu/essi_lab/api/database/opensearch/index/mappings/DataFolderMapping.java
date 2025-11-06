/**
 *
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import org.json.JSONObject;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.FieldType;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.opensearch.client.opensearch.indices.PutMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

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

/**
 * @author Fabrizio
 */
public class DataFolderMapping extends IndexMapping {

    /**
     *
     */
    private static final String DATA_FOLDER_INDEX = "data-folder-index";

    /**
     *
     */
    public static final String GS_RESOURCE = "gsResource";
    // the source storage worker key must be preserved
    public static final String WRITING_FOLDER_TAG = SourceStorageWorker.WRITING_FOLDER_TAG;

    public static final String CENTROID = "centroid";

    private static DataFolderMapping instance;

    /**
     *
     */
    @SuppressWarnings("incomplete-switch")
    protected DataFolderMapping() {

	super(DATA_FOLDER_INDEX, true);

	addProperty(GS_RESOURCE, FieldType.Binary.jsonValue());
	addProperty(WRITING_FOLDER_TAG, FieldType.Binary.jsonValue());

	addProperty(MetaFolderMapping.DATA_FOLDER, FieldType.Text.jsonValue());

	// centroid
	addProperty(CENTROID, FieldType.GeoPoint.jsonValue());

	// --------------------------------------------------
	//
	// gs-resource explicit mappings from MetadataElement
	//
	// --------------------------------------------------

	MetadataElement.listValues().forEach(el -> {

	    switch (el.getContentType()) {
	    case BOOLEAN -> addProperty(el.getName(), FieldType.Boolean.jsonValue());
	    case DOUBLE -> addProperty(el.getName(), FieldType.Double.jsonValue());
	    case INTEGER -> addProperty(el.getName(), FieldType.Integer.jsonValue());
	    case LONG -> addProperty(el.getName(), FieldType.Long.jsonValue());
	    case ISO8601_DATE, ISO8601_DATE_TIME -> {

		// indexed as long to save also date before the epoch
		addProperty(el.getName(), FieldType.Long.jsonValue());

		// indexes as date (when possible) for manual searches and for dashboard
		// ignoring malformed dates
		addProperty(toDateField(el.getName()), FieldType.Date.jsonValue(), true);
	    }

	    case SPATIAL -> addProperty(el.getName(), FieldType.GeoShape.jsonValue(), true); // ignoring malformed
	    // shapes
	    case TEXTUAL -> {
		addProperty(el.getName(), FieldType.Text.jsonValue());

		// textual fields are mapped also as 'keyword' type in order to be aggregated
		// since 'text' fields are not optimised for aggregations
		addProperty(toKeywordField(el.getName()), FieldType.Keyword.jsonValue());
	    }
	    }

	    //
	    // composed elements mapping
	    //
	    if (el.hasComposedElement()) {

		JSONObject nestedProperties = new JSONObject();

		el.createComposedElement().get().getProperties().forEach(prop -> {

		    switch (prop.getType()) {
		    case BOOLEAN -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Boolean));
		    case DOUBLE -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Double));
		    case INTEGER -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Integer));
		    case LONG -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Long));
		    case ISO8601_DATE, ISO8601_DATE_TIME -> {
			nestedProperties.put(prop.getName(), createTypeObject(FieldType.Long));
			nestedProperties.put(toDateField(prop.getName()), createTypeObject(FieldType.Date));
		    }
		    case TEXTUAL -> {
			nestedProperties.put(prop.getName(), createTypeObject(FieldType.Text));
			nestedProperties.put(toKeywordField(prop.getName()), createTypeObject(FieldType.Keyword));
		    }
		    }
		});

		addNested(el.getName(), nestedProperties);
	    }
	});

	//
	// temp extent extra properties
	//

	// text value from the enum TemporalExtent.FrameValue
	addProperty(MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW.getName(), FieldType.Text.jsonValue());

	// true if indeterminate and TimeIndeterminateValueType.NOW
	addProperty(MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName(), FieldType.Boolean.jsonValue());

	// true if missing
	addProperty(IndexedElements.TEMP_EXTENT_BEGIN_NULL.getElementName(), FieldType.Boolean.jsonValue());

	// true if indeterminate and TimeIndeterminateValueType.NOW
	addProperty(MetadataElement.TEMP_EXTENT_END_NOW.getName(), FieldType.Boolean.jsonValue());

	// true if missing
	addProperty(IndexedElements.TEMP_EXTENT_END_NULL.getElementName(), FieldType.Boolean.jsonValue());

	// true if missing
	addProperty(IndexedElements.BOUNDING_BOX_NULL.getElementName(), FieldType.Boolean.jsonValue());

	//
	// bbox extra properties
	//
	addProperty(BoundingBox.SOUTH_ELEMENT_NAME, FieldType.Double.jsonValue());
	addProperty(BoundingBox.EAST_ELEMENT_NAME, FieldType.Double.jsonValue());
	addProperty(BoundingBox.NORTH_ELEMENT_NAME, FieldType.Double.jsonValue());
	addProperty(BoundingBox.WEST_ELEMENT_NAME, FieldType.Double.jsonValue());

	addProperty(BoundingBox.IS_CROSSED_ELEMENT_NAME, FieldType.Boolean.jsonValue());
	addProperty(BoundingBox.AREA_ELEMENT_NAME, FieldType.Double.jsonValue());

	// ---------------------------------------------------
	//
	// gs-resource explicit mappings from ResourceProperty
	//
	// ---------------------------------------------------

	ResourceProperty.listValues().forEach(rp -> {

	    switch (rp.getContentType()) {
	    case BOOLEAN -> addProperty(rp.getName(), FieldType.Boolean.jsonValue());
	    case DOUBLE -> addProperty(rp.getName(), FieldType.Double.jsonValue());
	    case INTEGER -> addProperty(rp.getName(), FieldType.Integer.jsonValue());
	    case ISO8601_DATE, ISO8601_DATE_TIME -> {

		addProperty(rp.getName(), FieldType.Long.jsonValue());

		// indexes as date (when possible) for manual searches and for dashboard
		// ignoring malformed dates (it should never happen)
		addProperty(toDateField(rp.getName()), FieldType.Date.jsonValue(), true);

	    }
	    case LONG -> addProperty(rp.getName(), FieldType.Long.jsonValue());
	    case TEXTUAL -> {
		addProperty(rp.getName(), FieldType.Text.jsonValue());
		// textual fields are mapped also as 'keyword' type in order to be aggregated
		// since 'text' fields are not optimised for aggregations
		addProperty(toKeywordField(rp.getName()), FieldType.Keyword.jsonValue());
	    }
	    }
	});
    }

    /**
     * @param client
     */
    public void checkAndUpdate(OpenSearchClient client) throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Checking data-folder index for changes STARTED");

	GetMappingResponse mapping = client.indices()
		.getMapping(GetMappingRequest.of(builder -> builder.index(DataFolderMapping.get().getIndex())));

	IndexMappingRecord record = mapping.result().values().iterator().next();

	Set<String> properties = record.mappings().properties().keySet();

	HashMap<String, Property> map = new HashMap<>();

	MetadataElement.listValues().forEach(el -> {

	    if (!properties.contains(el.getName())) {

		switch (el.getContentType()) {
		case BOOLEAN -> map.put(el.getName(), createProperty(FieldType.Boolean.jsonValue()));
		case DOUBLE -> map.put(el.getName(), createProperty(FieldType.Double.jsonValue()));
		case INTEGER -> map.put(el.getName(), createProperty(FieldType.Integer.jsonValue()));
		case LONG -> map.put(el.getName(), createProperty(FieldType.Long.jsonValue()));
		case ISO8601_DATE, ISO8601_DATE_TIME -> {

		    map.put(el.getName(), createProperty(FieldType.Long.jsonValue()));
		    map.put(toDateField(el.getName()), createProperty(FieldType.Date.jsonValue(), true));
		}

		case TEXTUAL -> {

		    map.put(el.getName(), createProperty(FieldType.Text.jsonValue()));
		    map.put(toKeywordField(el.getName()), createProperty(FieldType.Keyword.jsonValue()));
		}
		}

		if (el.hasComposedElement()) {

		    JSONObject nestedProperties = new JSONObject();

		    el.createComposedElement().get().getProperties().forEach(prop -> {

			switch (prop.getType()) {
			case BOOLEAN -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Boolean));
			case DOUBLE -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Double));
			case INTEGER -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Integer));
			case LONG -> nestedProperties.put(prop.getName(), createTypeObject(FieldType.Long));
			case ISO8601_DATE, ISO8601_DATE_TIME -> {
			    nestedProperties.put(prop.getName(), createTypeObject(FieldType.Long));
			    nestedProperties.put(toDateField(prop.getName()), createTypeObject(FieldType.Date));
			}
			case TEXTUAL -> {
			    nestedProperties.put(prop.getName(), createTypeObject(FieldType.Text));
			    nestedProperties.put(toKeywordField(prop.getName()), createTypeObject(FieldType.Keyword));
			}
			}
		    });

		    map.put(el.getName(), createNestedProperty(nestedProperties));
		}
	    }
	});

	ResourceProperty.listValues().forEach(rp -> {

	    if (!properties.contains(rp.getName())) {

		switch (rp.getContentType()) {
		case BOOLEAN -> map.put(rp.getName(), createProperty(FieldType.Boolean.jsonValue()));
		case DOUBLE -> map.put(rp.getName(), createProperty(FieldType.Double.jsonValue()));
		case INTEGER -> map.put(rp.getName(), createProperty(FieldType.Integer.jsonValue()));
		case ISO8601_DATE, ISO8601_DATE_TIME -> {

		    map.put(rp.getName(), createProperty(FieldType.Long.jsonValue()));
		    map.put(toDateField(rp.getName()), createProperty(FieldType.Date.jsonValue(), true));

		}
		case LONG -> map.put(rp.getName(), createProperty(FieldType.Long.jsonValue()));
		case TEXTUAL -> {

		    map.put(rp.getName(), createProperty(FieldType.Text.jsonValue()));
		    map.put(toKeywordField(rp.getName()), createProperty(FieldType.Keyword.jsonValue()));
		}
		}
	    }
	});

	if (!map.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Following fields missing: {}", map.keySet());

	    GSLoggerFactory.getLogger(getClass()).info("Updating index STARTED");

	    PutMappingRequest request = new PutMappingRequest.Builder().//
		    index(get().getIndex()).//
		    properties(map).//
		    build();

	    PutMappingResponse response = client.indices().putMapping(request);

	    if (!response.acknowledged()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to update data-folder mapping");
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Updating index ENDED");

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("No changes found");
	}

	GSLoggerFactory.getLogger(getClass()).info("Checking data-folder index for changes ENDED");
    }

    /**
     * @param field
     * @return
     */
    public static String toDateField(String field) {

	return field + "_date";
    }

    /**
     * @return
     */
    public static final DataFolderMapping get() {

	if (instance == null) {

	    instance = new DataFolderMapping();
	}

	return instance;
    }
}
