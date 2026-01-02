/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;

/**
 * @author Fabrizio
 */
public class FolderRegistryMapping extends IndexMapping {

    /**
     * 
     */
    private static final String FOLDER_REGISTRY_INDEX = "folder-registry-index";

    /**
     * 
     */
    public static final String ENTRY_POSTFIX = "_registered-folder";

    private static FolderRegistryMapping instance;

    /**
     * 
     */
    protected FolderRegistryMapping() {

	super(FOLDER_REGISTRY_INDEX);
    }

    /**
     * @param folder
     * @return
     */
    public static String getEntryId(DatabaseFolder folder) {

	return OpenSearchFolder.getFolderId(folder) + FolderRegistryMapping.ENTRY_POSTFIX;
    }

    /**
     * @param folder
     * @return
     */
    public static String getEntryName(DatabaseFolder folder) {

	return folder.getName() + FolderRegistryMapping.ENTRY_POSTFIX;
    }

    /**
     * @return
     */
    public static final FolderRegistryMapping get() {

	if (instance == null) {

	    instance = new FolderRegistryMapping();
	}

	return instance;
    }
}
