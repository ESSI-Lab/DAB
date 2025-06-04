/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

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
