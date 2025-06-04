package eu.essi_lab.cfga.gs.demo;

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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.DoubleOptionBuilder;
import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.option.ISODateTimeOptionBuilder;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * This setting is intended to be used to test the client functionalities
 * 
 * @author Fabrizio
 */
public class DemoSetting3 extends Setting {

    /**
     * 
     */
    public DemoSetting3() {

	setEditable(false);
	enableCompactMode(false);

	setName("Setting demo 3");

	//
	//
	//

	Option<String> option1 = StringOptionBuilder.get().//
		withKey("option1").//
		withLabel("Option 1").//
		withDescription("String option that cannot be disable, with single value").//
		cannotBeDisabled().//
		withValue("a").//
		build();

	addOption(option1);

	//
	//
	//

	Option<String> option1_1 = StringOptionBuilder.get().//
		withKey("option1_1").//
		withLabel("Option 1.1").//
		withDescription("String option with aphanumeric input pattern").//
		withInputPattern(InputPattern.ALPHANUMERIC).//
		withValue("a").//
		build();

	addOption(option1_1);

	//
	//
	//

	Option<String> option1_2 = StringOptionBuilder.get().//
		withKey("option1_2").//
		withLabel("Option 1.2").//
		withDescription("String option with with aphanumeric and space input pattern, required value").//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_SPACE).//
		required().//
		build();

	addOption(option1_2);

	//
	//
	//

	Option<String> option1_3 = StringOptionBuilder.get().//
		withKey("option1_3").//
		withLabel("Option 1.3").//
		withDescription("String option with with aphanumeric and underscore input pattern").//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
		build();

	addOption(option1_3);

	//
	//
	//

	Option<String> option1_4 = StringOptionBuilder.get().//
		withKey("option1_4").//
		withLabel("Option 1.4").//
		withDescription("String option with with aphanumeric and underscore and space input pattern, required value").//
		cannotBeDisabled().//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE).//
		required().//
		build();

	addOption(option1_4);

	//
	//
	//

	Option<Integer> option1_1_1 = IntegerOptionBuilder.get().//
		withKey("option1_1_1").//
		withLabel("Option 1.1.1").//
		readOnly().//
		withDescription("Integer option readonly with single value").//
		withValue(1).//
		build();

	addOption(option1_1_1);

	//
	//
	//

	Option<Integer> option1_1_2 = IntegerOptionBuilder.get().//
		withKey("option1_1_2").//
		withLabel("Option 1.1.2").//
		required().//
		withDescription("Integer option required with single value").//
		build();

	addOption(option1_1_2);

	//
	//
	//

	Option<Integer> option1_1_3 = IntegerOptionBuilder.get().//
		withKey("option1_1_3").//
		withLabel("Option 1.1.3").//
		withDescription("Integer option not required with min value 1 and max value 10").//
		withMinValue(1).//
		withMaxValue(10).//
		build();

	addOption(option1_1_3);

	//
	//
	//

	Option<Integer> option1_1_4 = IntegerOptionBuilder.get().//
		withKey("option1_1_4").//
		withLabel("Option 1.1.4").//
		disabled().//
		withDescription("Integer option disabled with single value").//
		withValue(1).//
		build();

	addOption(option1_1_4);

	//
	//
	//
	Option<String> option1_2_0 = StringOptionBuilder.get().//
		withKey("option1_2_0").//
		withLabel("Option 1.2.0").//
		withMultiSelection().//
		withDescription("String option with multi selection").//
		withValues(Arrays.asList("a", "b", "c", "d", "e")).//
		withSelectedValues(Arrays.asList("a", "c", "e")).//
		build();

	addOption(option1_2_0);

	//
	//
	//
	Option<String> option1_2_1 = StringOptionBuilder.get().//
		withKey("option1_2_1").//
		withLabel("Option 1.2.1").//
		withMultiSelection().//
		disabled().//
		withDescription("String option with multi selection, disabled").//
		withValues(Arrays.asList("a", "b", "c", "d", "e")).//
		withSelectedValues(Arrays.asList("a", "c", "e")).//
		build();

	addOption(option1_2_1);

	//
	//
	//

	Option<Integer> option2_1 = IntegerOptionBuilder.get().//
		withKey("option2.1").//
		withLabel("Option 2.1").//
		withDescription("Integer option with single selection").//
		withSingleSelection().//
		withValues(Arrays.asList(1, 2, 3, 4, 5)).//
		withSelectedValue(3).//
		build();

	addOption(option2_1);

	//
	//
	//

	Option<Double> option3 = DoubleOptionBuilder.get().//
		withKey("option3").//
		withLabel("Option 3").//
		withDescription("Double option with multi selection").//
		withMultiSelection().//
		withValues(Arrays.asList(1.34, 2.43, 3.345, 4.543, 5.12)).//
		withSelectedValue(5.12).//
		build();

	addOption(option3);

	Option<Double> option3_1 = DoubleOptionBuilder.get().//
		withKey("option3").//
		withLabel("Option 3.1").//
		withDescription("Double option with min value 0.5 and max value 8.5").//
		withMinValue(0.5).//
		withMaxValue(8.5).//
		build();

	addOption(option3_1);

	//
	//
	//

	Option<BooleanChoice> option4 = BooleanChoiceOptionBuilder.getDefault().//
		withKey("option4").//
		withLabel("Option 4").//
		withDescription("Boolean choice option, labeled enum").//
		build();

	addOption(option4);

	//
	//
	//

	Option<TimeUnit> option5 = OptionBuilder.get(TimeUnit.class).//
		withKey("option5").//
		withLabel("Option 5").//
		withDescription("Time unit option (enum), single selection").//
		withSingleSelection().//
		withValues(Arrays.asList(TimeUnit.values())).//
		withSelectedValue(TimeUnit.DAYS).//
		build();

	addOption(option5);

	//
	//
	//

	Option<TimeUnit> option5_1 = OptionBuilder.get(TimeUnit.class).//
		withKey("option5_1").//
		withLabel("Option 5.1").//
		disabled().//
		withDescription("Time unit option (enum), single selection, disabled").//
		withSingleSelection().//
		withValues(Arrays.asList(TimeUnit.values())).//
		withSelectedValue(TimeUnit.DAYS).//
		build();

	addOption(option5_1);

	//
	//
	//

	Option<ISODateTime> option6 = ISODateTimeOptionBuilder.get().//
		withKey("option6").//
		withLabel("Option 6").//
		withDescription("ISODateTime option").//
		build();

	addOption(option6);

	//
	// set the component extension
	//
	setExtension(new DemoSetting3ComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting3ComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DemoSetting3ComponentInfo() {

	    setComponentName(DemoSetting3.class.getName());

	    setForceReadOnly(false);

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(2).//
		    withShowDirective("Demo setting 3").//
		    build();

	    setTabInfo(tabInfo);
	}
    }
}
