package eu.essi_lab.accessor.healthsites;

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

import java.util.Optional;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.Option;

/**
 * @author Fabrizio
 */
public class HealthSitesConnectorSetting extends HarvestedConnectorSetting {

 
    private static final String API_KEY = "api-key";

    public HealthSitesConnectorSetting() {
	
	setConfigurableType(HealthSitesConnector.TYPE);
	
	{

	    Option<String> option = StringOptionBuilder.get().//
		    withLabel("HEALTH Sites Api key").//
		    withKey(API_KEY).//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}
    }

    /**
     * @param apikey
     */
    public void setAPIKey(String accessToken) {

	getOption(API_KEY, String.class).get().setValue(accessToken);
    }

    /**
     * @return
     */
    public Optional<String> getAPIkey() {

	return getOption(API_KEY, String.class).get().getOptionalValue();
    }
    
    @Override
    protected String initConnectorType() {

	return HealthSitesConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Health Sites Connector settings";
    }
}
