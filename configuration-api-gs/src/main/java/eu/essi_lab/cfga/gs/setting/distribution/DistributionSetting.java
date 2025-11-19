package eu.essi_lab.cfga.gs.setting.distribution;

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

import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.BrokeringSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSettingLoader;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.TabDescriptorBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class DistributionSetting extends Setting implements BrokeringSetting {

    /**
     * 
     */
    private static final String ACCESSORS_SETTING_IDENTIFIER = "accessorsSetting";

    /**
     * 
     */
    public DistributionSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	setName("Distributed accessor settings");

	setValidator(createValidator());

	//
	// Settings with available distributed accessors, only one can be chosen
	//
	Setting accessorsSetting = new Setting();
	accessorsSetting.setCanBeDisabled(false);
	accessorsSetting.setEditable(false);
	accessorsSetting.setShowHeader(false);

	accessorsSetting.setName("Accessors settings");

	accessorsSetting.setIdentifier(ACCESSORS_SETTING_IDENTIFIER);
	accessorsSetting.setSelectionMode(SelectionMode.SINGLE);

	addSetting(accessorsSetting);

	List<AccessorSetting> distributed = AccessorSettingLoader.loadDistributed();
	
	distributed.forEach(s -> {

	    Setting setting = s.clone();
 	    setting.setIdentifier(s.getConfigurableType());
 	    accessorsSetting.addSetting(setting);
	});

	// selects the first, because one must be selected
	getAccessorsSetting().//
		getSettings().//
		stream().//
		min(Comparator.comparing(Setting::getName)).//
		get().//
		setSelected(true);

	//
	//
	//
	setAfterCleanFunction(new DistributionSettingAfterCleanFunction());

	//
	// set the component extension
	//
	setExtension(new DistributionSettingComponentInfo());
    }

    /**
     * @return
     */
    public Setting getAccessorsSetting() {

	return getSetting(ACCESSORS_SETTING_IDENTIFIER).get();
    }

    /**
     * @author Fabrizio
     */
    public static class DistributionSettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DistributionSettingComponentInfo() {

	    setComponentName(AccessorSetting.class.getName());

	    TabDescriptor tabDescriptor = TabDescriptorBuilder.get().//
		    withIndex(GSTabIndex.DISTRIBUTION.getIndex()).//
		    withShowDirective("Distribution", "Manage DAB distributed sources", SortDirection.ASCENDING).//
		    withAddDirective("Add distributed accessor", DistributionSetting.class).//
		    withRemoveDirective("Remove accessor", true, DistributionSetting.class).//
		    withEditDirective("Edit accessor", ConfirmationPolicy.ON_WARNINGS).//
		    build();

	    setTabDescriptor(tabDescriptor);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DistributionSettingAfterCleanFunction implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    DistributionSetting thisSetting = SettingUtils.downCast(setting, DistributionSetting.class);

	    AccessorSetting accessorSetting = thisSetting.getSelectedAccessorSetting();

	    accessorSetting.setShowHeader(false);

	    thisSetting.setName(accessorSetting.getGSSourceSetting().getSourceLabel());
	}
    }

    /**
     * @param object
     */
    public DistributionSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DistributionSetting(String object) {

	super(object);
    }

}
