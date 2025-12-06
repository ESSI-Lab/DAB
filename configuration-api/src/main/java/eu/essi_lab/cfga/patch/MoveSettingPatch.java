package eu.essi_lab.cfga.patch;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class MoveSettingPatch extends Patch {

    private final Class<? extends Setting> targetClass;
    private final Setting parent;

    /**
     * @param configuration
     * @param target
     * @param parent
     * @return
     */
    public static MoveSettingPatch of(Configuration configuration, //
	    Class<? extends Setting> target, //
	    Setting parent) {//

	return new MoveSettingPatch(configuration, target, parent);
    }

    /**
     * @param configuration
     * @param target
     * @param targetSettingId
     * @param parent
     * @return
     */
    public static MoveSettingPatch of( //
	    Configuration configuration, //
	    Class<? extends Setting> target, //
	    String targetSettingId, //
	    Setting parent) {//

	return new MoveSettingPatch(configuration, target, parent);
    }

    /**
     * @param targetClass
     * @param parent
     */
    private MoveSettingPatch(Configuration configuration,//
	    Class<? extends Setting> targetClass,//
	    Setting parent) {

	setConfiguration(configuration);

	this.targetClass = targetClass;
	this.parent = parent;
    }

    @Override
    public boolean doPatch() throws Exception {

	List<Setting> targetSettings = parent. //
		getSettings().//
		stream().//
		filter(s -> s.getSettingClass().equals(targetClass)).//
		toList();

	GSLoggerFactory.getLogger(getClass()).debug("Found {} settings to move", targetSettings.size());

	if (!targetSettings.isEmpty()) {

	    for (Setting setting : targetSettings) {

		parent.removeSetting(setting);
	    }

	    getConfiguration().replace(parent);

	    for (Setting setting : targetSettings) {

		getConfiguration().put(setting);
	    }

	    return true;
	}

	return false;
    }
}
