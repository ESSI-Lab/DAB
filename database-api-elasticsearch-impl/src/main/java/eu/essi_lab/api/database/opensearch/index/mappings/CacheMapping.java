/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class CacheMapping extends IndexMapping {

    /**
     * 
     */
    private static final String CACHE_INDEX = Database.CACHE_FOLDER + "-index";

    /**
     * 
     */
    public static final String CACHED_ENTRY = "cachedData";

    /**
     * @param index
     */
    protected CacheMapping() {

	super(CACHE_INDEX);

	addProperty(CACHED_ENTRY, FieldType.Binary.jsonValue());
    }

    /**
     * @return
     */
    public static final CacheMapping get() {

	return new CacheMapping();
    }

}