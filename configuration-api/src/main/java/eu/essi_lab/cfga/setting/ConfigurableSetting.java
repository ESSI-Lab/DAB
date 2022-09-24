package eu.essi_lab.cfga.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.cfga.Configurable;

/**
 * A special kind of {@link Setting} which is also a {@link Configurable}. It can be used to handle
 * settings that are not directly related to a specific component, for instance system settings
 * 
 * @author Fabrizio
 */
public abstract class ConfigurableSetting extends Setting implements Configurable<Setting> {
    
    /**
     * 
     */
    public ConfigurableSetting() {
    }
    
    /**
     * @param object
     */
    public ConfigurableSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ConfigurableSetting(String object) {

	super(object);
    }

    @Override
    public void configure(Setting setting) {

    }

    @Override
    public Setting getSetting() {

	return this;
    }
}
