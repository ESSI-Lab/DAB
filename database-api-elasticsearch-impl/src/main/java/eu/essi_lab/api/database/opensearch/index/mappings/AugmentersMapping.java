/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.api.database.Database;

/**
 * This index includes the properties files with the runtime status of the standalone augmenters and
 * has no search properties
 * 
 * @author Fabrizio
 */
public class AugmentersMapping extends IndexMapping {

    /**
     * 
     */
    private static final String AUGMENTERS_INDEX = Database.AUGMENTERS_FOLDER + "-index";

    /**
     * 
     */
    public static final String AUGMENTER_PROPERTIES = "augmenterProperties";

    private static AugmentersMapping instance;

    /**
     * @param index
     */
    protected AugmentersMapping() {

	super(AUGMENTERS_INDEX);

	addProperty(AUGMENTER_PROPERTIES, FieldType.Binary.jsonValue());
    }

    /**
     * @return
     */
    public static final AugmentersMapping get() {

	if (instance == null) {

	    instance = new AugmentersMapping();
	}

	return instance;
    }
}
