/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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

import org.opensearch.client.opensearch._types.mapping.FieldType;

/**
 * @author Fabrizio
 */
public class MetaFolderMapping extends IndexMapping {

    /**
     * 
     */
    private static final String META_FOLDER_INDEX = "meta-folder-index";

    //
    // meta-folder-index properties
    //
    public static final String SOURCE_ID = "sourceId";
    public static final String DATA_FOLDER = "dataFolder";
    public static final String HARVESTING_PROPERTIES = "harvestingProperties";
    public static final String INDEX_DOC = "indexDoc";
    public static final String ERRORS_REPORT = "errorsReport";
    public static final String WARN_REPORT = "warnReport";

    private static MetaFolderMapping instance;

    /**
     * @return
     */
    public static final MetaFolderMapping get() {

	if (instance == null) {

	    instance = new MetaFolderMapping();
	}

	return instance;
    }

    /**
     * 
     */
    private MetaFolderMapping() {

	super(META_FOLDER_INDEX);

	// mandatory
	addProperty(SOURCE_ID, FieldType.Text.jsonValue());
	addProperty(IndexMapping.toKeywordField(SOURCE_ID), FieldType.Keyword.jsonValue());

	// set only when the index doc is stored
	addProperty(DATA_FOLDER, FieldType.Text.jsonValue()); // data-1 or data-2
	addProperty(IndexMapping.toKeywordField(DATA_FOLDER), FieldType.Keyword.jsonValue()); // data-1 or data-2

	// optional, only one of them can be set
	addProperty(HARVESTING_PROPERTIES, FieldType.Binary.jsonValue());
	addProperty(ERRORS_REPORT, FieldType.Binary.jsonValue());
	addProperty(WARN_REPORT, FieldType.Binary.jsonValue());
	addProperty(INDEX_DOC, FieldType.Binary.jsonValue());
    }
}
