/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.messages.bond.spatial.IndexedShape;

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

/**
 * @author Fabrizio
 */
public class ShapeFileMapping extends IndexMapping {

    /**
     * 
     */
    private static final String SHAPE_FILE_INDEX = "shape-file-index";

    public static final String SHAPE = "shape";
    public static final String USER_ID = "userId";
    public static final String SHAPE_FILE = "shapeFile";
    public static final String UPLOAD_REGISTRY = "uploadRegistry";
    public static final String UPLOAD_REGISTRY_ENTRY_NAME = "__upload_registry__";
    public static final String SHAPE_UPLOAD_PREFIX = "shapeUploadPrefix";
    /** Human-readable polygon label from shapefile attributes (used as WMS layer title after harvest). */
    public static final String ENTRY_TITLE = "entryTitle";
    /** User-defined group label for related predefined search areas. */
    public static final String SHAPE_GROUP = "shapeGroup";
    /** Integer sort order used to rank groups in predefined search area selectors. */
    public static final String SHAPE_GROUP_ORDER = "shapeGroupOrder";
    /** User id of the uploader ({@code admin} for administrators). */
    public static final String OWNER = "owner";
    /** CRS identifier ({@code EPSG:xxxx}) of coordinates in the {@link #SHAPE} geojson. */
    public static final String SHAPE_CRS = "shapeCrs";

    private static ShapeFileMapping instance;
    
    /**
     * 
     * @param is
     */
    public static String getShapeFileId(DatabaseFolder folder, IndexedShape is) {
	
	return OpenSearchFolder.getEntryId(folder, is.getId());
    }

    /**
     *  
     */
    protected ShapeFileMapping() {

	super(SHAPE_FILE_INDEX);

	addProperty(SHAPE, FieldType.GeoShape.jsonValue(), true);

	addProperty(USER_ID, FieldType.Text.jsonValue());

	addProperty(SHAPE_FILE, FieldType.Binary.jsonValue());

	addProperty(UPLOAD_REGISTRY, FieldType.Text.jsonValue());

	addProperty(SHAPE_UPLOAD_PREFIX, FieldType.Keyword.jsonValue());

	addProperty(ENTRY_TITLE, FieldType.Text.jsonValue());

	addProperty(SHAPE_GROUP, FieldType.Text.jsonValue());

	addProperty(SHAPE_GROUP_ORDER, FieldType.Integer.jsonValue());

	addProperty(OWNER, FieldType.Keyword.jsonValue());

	addProperty(SHAPE_CRS, FieldType.Keyword.jsonValue());
    }

    /**
     * @return
     */
    public static final ShapeFileMapping get() {

	if (instance == null) {

	    instance = new ShapeFileMapping();
	}

	return instance;
    }
}
