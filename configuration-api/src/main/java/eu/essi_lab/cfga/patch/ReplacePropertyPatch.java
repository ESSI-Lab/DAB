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
 * This patch finds all the configuration settings having the given <code>property</code> value equals
 * to <code>legacyValue</code>, and replaces it with <code>newValue</code>
 *
 * @author Fabrizio
 */
public class ReplacePropertyPatch extends Patch {

    private final Property<?> property;
    private final String legacyValue;
    private final String newClass;

    /**
     *
     * @param configuration
     * @param property
     * @param legacyValue
     * @param newValue
     * @return
     */
    public static ReplacePropertyPatch of(Configuration configuration, Property<?> property, String legacyValue, String newValue) {

	return new ReplacePropertyPatch(configuration, property, legacyValue, newValue);
    }

    /**
     *
     * @param configuration
     * @param property
     * @param legacyValue
     * @param newClass
     */
    private ReplacePropertyPatch(Configuration configuration, Property<?> property, String legacyValue, String newClass) {

	setConfiguration(configuration);

	this.property = property;
	this.legacyValue = legacyValue;
	this.newClass = newClass;
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean doPatch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		s -> s.getObject().getString(property.getKey()).equals(legacyValue),//
		list);//

	GSLoggerFactory.getLogger(getClass()).debug("Found {} legacy {} properties to patch", list.size(), property.getName());

	if (!list.isEmpty()) {

	    List<Setting> converted = list.//
		    stream().//
		    map(s -> s.getObject().toString().replace( //
		    legacyValue, //
		    newClass)).//
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
}
