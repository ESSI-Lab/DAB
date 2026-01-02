package eu.essi_lab.accessor.agrostac.harvested;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class AgrostacConnectorSetting extends HarvestedConnectorSetting {
    
    private static final String ACCESS_TOKEN = "accesstoken";

    /**
     * 
     */
    private int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public AgrostacConnectorSetting() {

	setConfigurableType(AgrostacConnector.TYPE);
	
	{

	    Option<String> option = StringOptionBuilder.get().//
		    withLabel("Access token").//
		    withKey(ACCESS_TOKEN).//
		    withLabel("AGROSTAC Access Token").//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}
	
    }
    
    
    /**
     * @param prefix
     */
    public void setAccessToken(String accessToken) {

	getOption(ACCESS_TOKEN, String.class).get().setValue(accessToken);
    }

    /**
     * @return
     */
    public Optional<String> getAccessToken() {

	return getOption(ACCESS_TOKEN, String.class).get().getOptionalValue();
    }

    /**
     * @param object
     */
    public AgrostacConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AgrostacConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return AgrostacConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Agrostac Connector settings";
    }
    
    /**
     * @return
     */
    protected int getDefaultPageSize() {

	return DEFAULT_PAGE_SIZE;
    }
    
    
}
