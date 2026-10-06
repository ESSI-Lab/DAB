package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * Configures the invalidation of the WMS cache at the end of the harvesting procedures: when a harvesting of a source
 * belonging to one of the configured views changes the source records (according to the selected
 * {@link TriggerCondition}), all the cached layers of that view are removed
 *
 * @author boldrini
 */
public class WMSCacheInvalidationSetting extends Setting {

    private static final String VIEWS_KEY = "wmsCacheInvalidationViews";
    private static final String TRIGGER_CONDITION_KEY = "wmsCacheInvalidationTriggerCondition";

    /**
     * @author boldrini
     */
    public enum TriggerCondition implements LabeledEnum {

	/**
	 * The number of records after the harvesting differs from the number of records before the harvesting
	 */
	RECORDS_COUNT_CHANGED("Record count changed"),

	/**
	 * Every harvesting which completes without errors, and without the consolidated folder surviving
	 */
	EVERY_COMPLETED_HARVESTING("Every completed harvesting");

	private final String label;

	/**
	 * @param label
	 */
	TriggerCondition(String label) {

	    this.label = label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return label;
	}
    }

    /**
     *
     */
    public WMSCacheInvalidationSetting() {

	setName("Cache invalidation on harvesting");
	setDescription("When a harvesting of a source belonging to one of these views changes its records, "
		+ "all the cached layers of that view are removed");

	setShowHeader(true);
	setCanBeDisabled(true);
	setEditable(false);
	setEnabled(false);
	enableCompactMode(false);

	addOption(StringOptionBuilder.get().//
		withKey(VIEWS_KEY).//
		withLabel("Views to check and clean").//
		withDescription("Comma-separated view identifiers. The sources to check are retrieved from each view").//
		cannotBeDisabled().//
		build());

	Option<TriggerCondition> condition = OptionBuilder.get(TriggerCondition.class).//
		withKey(TRIGGER_CONDITION_KEY).//
		withLabel("Trigger condition").//
		withDescription("Consolidated, canceled or failed harvestings never trigger the cleaning").//
		withSingleSelection().//
		withValues(LabeledEnum.values(TriggerCondition.class)).//
		withSelectedValue(TriggerCondition.RECORDS_COUNT_CHANGED).//
		cannotBeDisabled().//
		build();

	addOption(condition);
    }

    /**
     * @param object
     */
    public WMSCacheInvalidationSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WMSCacheInvalidationSetting(String object) {

	super(object);
    }

    /**
     * @param views comma-separated view identifiers
     */
    public void setViews(String views) {

	getOption(VIEWS_KEY, String.class).get().setValue(views);
    }

    /**
     * @return the configured view identifiers, possibly empty
     */
    public List<String> getViews() {

	return getOption(VIEWS_KEY, String.class).get().getOptionalValue().//
		map(v -> Arrays.stream(v.split(",")).//
			map(String::trim).//
			filter(s -> !s.isEmpty()).//
			distinct().//
			toList())
		.//
		orElse(List.of());
    }

    /**
     * @param condition
     */
    public void setTriggerCondition(TriggerCondition condition) {

	getOption(TRIGGER_CONDITION_KEY, TriggerCondition.class).get().setValue(condition);
    }

    /**
     * @return
     */
    public TriggerCondition getTriggerCondition() {

	return getOption(TRIGGER_CONDITION_KEY, TriggerCondition.class).get().getValue();
    }
}
