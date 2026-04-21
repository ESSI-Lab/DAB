/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

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
public class ConfigurationMapping extends IndexMapping {

    /**
     * 
     */
    private static final String CONFIGURATION_INDEX = "configuration-index";

    /**
     * 
     */
    public static final String CONFIGURATION_NAME = "configurationName";

    /**
     * 
     */
    public static final String CONFIGURATION = "configuration";

    /**
     * 
     */
    public static final String CONFIGURATION_LOCK = "configuration-lock";

    private static ConfigurationMapping instance;

    /**
     * @return
     */
    public static final ConfigurationMapping get() {

	if (instance == null) {

	    instance = new ConfigurationMapping();
	}

	return instance;
    }

    /**
     * 
     */
    private ConfigurationMapping() {

	super(CONFIGURATION_INDEX);

	addProperty(CONFIGURATION, FieldType.Binary.jsonValue());

	addProperty(CONFIGURATION_NAME, FieldType.Text.jsonValue());
    }
}
