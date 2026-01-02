/**
 * 
 */
package eu.essi_lab.cfga.check;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.setting.Property;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public class PropertiesMethod implements CheckMethod {

    private List<Property<?>> properties;

    /**
     * @param reponse
     * @return
     */
    public static List<String> getFailedProperties(CheckResponse reponse) {

	return reponse.getMessages().//
		stream().//
		map(m -> m.substring(m.indexOf("'") + 1, m.lastIndexOf("'"))).//
		distinct().//
		sorted().//
		toList();
    }

    /**
     * 
     */
    public PropertiesMethod() {

    }

    /**
     * @param properties
     */
    public void setProperties(Property<?>... properties) {

	setProperties(Arrays.asList(properties));
    }

    /**
     * @param properties
     */
    public void setProperties(List<Property<?>> properties) {

	this.properties = properties;
    }

    @Override
    public CheckResponse check(Configuration configuration) {

	if (properties == null || properties.isEmpty()) {

	    throw new IllegalArgumentException("No properties set");
	}

	GSLoggerFactory.getLogger(getClass()).info("Properties check STARTED");

	CheckResponse checkResponse = new CheckResponse(getName());

	configuration.list().forEach(configSetting -> {

	    Setting newSetting = SelectionUtils.resetAndSelect(configSetting, false);

	    SelectionUtils.deepClean(newSetting);

	    SelectionUtils.deepAfterClean(newSetting);

	    compare(checkResponse, configSetting, newSetting, properties);

	    if (!checkResponse.getSettings().isEmpty()) {

		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).info("Properties check ENDED");

	return checkResponse;
    }

    /**
     * @param value1
     * @param value2
     * @param property
     * @return
     */
    private boolean checkUUIDName(String value1, String value2, Property<?> property) {

	return property.getName().equals(Setting.NAME.getName()) && value1 != null && value2 != null && //
		StringUtils.isUUID(value1) && StringUtils.isUUID(value2);

    }

    /**
     * @param configSetting
     * @param newSetting
     * @param properties
     * @return
     */
    private void compare(CheckResponse checkResponse, Setting configSetting, Setting newSetting, List<Property<?>> properties) {

	for (Property<?> property : properties) {

	    String default_ = property.getDefaultValue().map(Object::toString).orElse("");

	    String configProp = configSetting.getStringPropertyValue(property).orElse(default_);

	    String newProp = newSetting.getStringPropertyValue(property).orElse(default_);

	    if (!newProp.equals(configProp) && !checkUUIDName(newProp, configProp, property)) {

		if (checkResponse.getSettings().stream().noneMatch(s -> s.getIdentifier().equals(configSetting.getIdentifier()))) {

		    checkResponse.getSettings().add(configSetting);
		}

		checkResponse.getMessages()
			.add("Property '" + property.getName() + "' check failed for setting class: " + configSetting.getSettingClass());
	    }
	}

	configSetting.getSettings().forEach(set -> {

	    // it should always be present!
	    Optional<Setting> opt = newSetting.getSettings().//
		    stream().//
		    filter(s -> s.getIdentifier().equals(set.getIdentifier())).//
		    findFirst();

	    //
	    opt.ifPresent(setting -> compare(checkResponse, //
		    set, //
		    setting, //
		    properties));
	});
    }
}
