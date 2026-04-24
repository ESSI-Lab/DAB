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
import java.util.function.*;

/**
 * @author Fabrizio
 */
public class RemoveSettingPatch extends Patch {

    private Predicate<Setting> predicate;

    /**
     * @param configuration
     * @param predicate
     */
    private RemoveSettingPatch(Configuration configuration, Predicate<Setting> predicate) {

	setConfiguration(configuration);

	this.predicate = predicate;
    }

    /**
     * @param configuration
     * @param predicate
     * @return
     */
    public static RemoveSettingPatch of( //
	    Configuration configuration, //
	    Predicate<Setting> predicate) {//

	return new RemoveSettingPatch(configuration, predicate);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    protected boolean doPatch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		predicate,//
		list);//

	GSLoggerFactory.getLogger(getClass()).debug("Found {} legacy settings to patch", list.size());

	if (!list.isEmpty()) {

	    for (Setting setting : list) {

		getConfiguration().remove(setting.getIdentifier());
	    }

	    return true;
	}

	return false;
    }
}
