package eu.essi_lab.cfga.patch;

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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class RemovePropertyPatch extends Patch {

    private final Property<?> property;
    private Object value;

    /**
     * @param configuration
     * @param property
     * @return
     */
    public static RemovePropertyPatch of(Configuration configuration, Property<?> property) {

	return new RemovePropertyPatch(configuration, property, null);
    }

    /**
     * @param configuration
     * @param property
     * @param value
     * @return
     */
    public static RemovePropertyPatch of(Configuration configuration, Property<?> property, Object value) {

	return new RemovePropertyPatch(configuration, property, value);
    }

    /**
     * @param configuration
     * @param property
     * @param value
     */
    private RemovePropertyPatch(Configuration configuration, Property<?> property, Object value) {

	setConfiguration(configuration);

	this.property = property;
	this.value = value;
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean doPatch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	getConfiguration().list().forEach(setting -> {

	    if (!find(setting,property,value).isEmpty()) {

		list.add(setting);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).debug("Found {} settings with {} property to patch", list.size(), property.getName());

	if (!list.isEmpty()) {

	    List<Setting> converted = list.//
		    stream().//
		    map(setting -> {

		List<Setting> l = find(setting,property,value);

		l.forEach(si -> si.getObject().remove(property.getKey()));

		return setting.getObject();
	    }).//
		    map(Setting::new).//
		    toList();

	    for (Setting setting : list) {
		getConfiguration().remove(setting.getIdentifier());
	    }

	    for (Setting s : converted) {
		getConfiguration().put(s);
	    }

	    return true;
	}

	return false;
    }

    /**
     *
     * @param setting
     * @param property
     * @param value
     * @return
     */
    private List<Setting> find(Setting setting, Property<?> property, Object value) {

	ArrayList<Setting> list = new ArrayList<>();

	SettingUtils.deepFind(setting, child -> child.getObject().has(property.getKey()) && //
		(value == null || child.getObject().get(property.getKey()).equals(value)), list);

	return list;
    }
}
