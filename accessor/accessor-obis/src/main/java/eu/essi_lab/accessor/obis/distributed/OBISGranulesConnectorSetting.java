package eu.essi_lab.accessor.obis.distributed;

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

import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class OBISGranulesConnectorSetting extends DistributedConnectorSetting {

    private static final String DEFAULT_OCCURRENCE_SEARCH_PATH_OPTION_KEY = "defaultOccurrencePath";
    private static final String DEFAULT_OCCURRENCE_SEARCH_PATH_SUFFIX = "occurrence";

    /**
     * 
     */
    public OBISGranulesConnectorSetting() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(DEFAULT_OCCURRENCE_SEARCH_PATH_OPTION_KEY).//
		withLabel("Path Suffix to Search Occurrences").//
		withValue(DEFAULT_OCCURRENCE_SEARCH_PATH_SUFFIX).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    @Override
    protected String initConnectorType() {

	return OBISGranulesConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "OBIS Granules Connector settings";
    }
}
