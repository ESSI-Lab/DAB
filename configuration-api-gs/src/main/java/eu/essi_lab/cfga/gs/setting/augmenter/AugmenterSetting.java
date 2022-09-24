package eu.essi_lab.cfga.gs.setting.augmenter;

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

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class AugmenterSetting extends Setting {

    /**
     * 
     */
    private static final String AUGMENTER_PRIORITY_KEY = "augmenterPriority";

    /**
     * 
     */
    public AugmenterSetting() {

	super();
	enableCompactMode(false);
	setEditable(false);

	Option<Integer> option = IntegerOptionBuilder.get().//
		withKey(AUGMENTER_PRIORITY_KEY).//
		withLabel("Execution priority").//
		withDescription(
			"Set the priority (lower value, higher priority) of the augmenter which defines the order by which all the augmenters are executed. "
			+ "Augmenters with same priority are executed with random order")
		.//
		withMinValue(1).//
		withValue(1).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public AugmenterSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AugmenterSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public int getPriority() {

	return getOption(AUGMENTER_PRIORITY_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public void setPriority(int priority) {

	getOption(AUGMENTER_PRIORITY_KEY, Integer.class).get().setValue(priority);
    }
}
