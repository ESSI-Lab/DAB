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
public class PutSettingPatch extends Patch {

    private final String settingId;
    private Class<? extends Setting> settingClass;

    /**
     * @param configuration
     * @param settingClass
     * @param settingId
     */
    private PutSettingPatch(Configuration configuration, Class<? extends Setting> settingClass, String settingId) {

	setConfiguration(configuration);

	this.settingId = settingId;
	this.settingClass = settingClass;
    }

    /**
     * @param configuration
     * @param settingClass
     * @param settingId
     * @return
     */
    public static PutSettingPatch of( //
	    Configuration configuration, //
	    Class<? extends Setting> settingClass, //
	    String settingId) {//

	return new PutSettingPatch(configuration, settingClass, settingId);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    protected boolean doPatch() {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		s -> s.getSettingClass().equals(settingClass) && s.getIdentifier().equals(settingId),//
		list);//

	if (list.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Target setting missing, new setting required");

	    Setting target = SettingUtils.create(settingClass);

	    target.setIdentifier(settingId);

	    boolean put = getConfiguration().put(target);

	    if (!put) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to put new setting");

		return false;
	    }

	    return true;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Target setting already exists, no actions required");

	return false;
    }
}
