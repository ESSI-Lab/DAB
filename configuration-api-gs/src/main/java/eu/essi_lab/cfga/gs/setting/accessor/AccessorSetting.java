/**
 *
 */
package eu.essi_lab.cfga.gs.setting.accessor;

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

import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public final class AccessorSetting extends Setting {

    private static final String SOURCE_SETTING_ID = "sourceSetting";
    private static final String ACCESSOR_TYPE_KEY = "accessorType";

    /**
     *
     */
    public AccessorSetting() {

	setEditable(false);
	enableCompactMode(false);

	GSSourceSetting sourceSetting = new GSSourceSetting();
	sourceSetting.setIdentifier(SOURCE_SETTING_ID);
	sourceSetting.setName("Source settings");

	addSetting(sourceSetting);

	Option<String> accessorTypeOption = StringOptionBuilder.get().//
		readOnly().//
		withKey(ACCESSOR_TYPE_KEY).//
		withLabel("Accessor type").//
		cannotBeDisabled().build();

	addOption(accessorTypeOption);

	setValidator(new AccessorSettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class AccessorSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    AccessorSetting thisSetting = SettingUtils.downCast(setting, AccessorSetting.class);

	    return thisSetting.getGSSourceSetting().validate(configuration, context).get();
	}
    }

    /**
     * @param object
     */
    public AccessorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AccessorSetting(String object) {

	super(object);
    }

    /**
     * @param sharedSourceLabel
     * @param sharedSourceEndpoint
     * @param settingName
     * @param harvestedAccessorType
     * @param distributedAccessorType
     * @param harvSettig
     * @param distSetting
     * @return
     */
    public static AccessorSetting createMixed(//
	    String sharedSourceLabel, //
	    String sharedSourceEndpoint, //

	    String settingName, //
	    String harvestedAccessorType, //
	    String distributedAccessorType, //
	    String accessorType, //

	    HarvestedConnectorSetting harvSettig, //
	    DistributedConnectorSetting distSetting) {

	AccessorSetting setting = new AccessorSetting();

	setting.setIdentifier(harvestedAccessorType + "_" + distributedAccessorType + "_setting");

	setting.setName(settingName);

	setting.setHarvestedAccessorType(harvestedAccessorType);

	setting.setDistributedAccessorType(distributedAccessorType);

	setting.setHarvestedConnectorSetting(harvSettig);

	setting.getOption(ACCESSOR_TYPE_KEY, String.class).get().setValue(accessorType);

	setting.setDistributedConnectorSetting(distSetting);

	setting.setBrokeringStrategy(BrokeringStrategy.MIXED);

	//
	//
	//

	setting.getGSSourceSetting().setIdentifier(harvestedAccessorType + "_" + distributedAccessorType + "_sourceSetting");

	setting.getGSSourceSetting().setBrokeringStrategy(BrokeringStrategy.MIXED);

	setting.getGSSourceSetting().setSourceLabel(sharedSourceLabel);

	setting.getGSSourceSetting().setSourceEndpoint(sharedSourceEndpoint);

	setting.getGSSourceSetting().setName(settingName + " source settings");

	return setting;
    }

    /**
     * @param accessorType
     * @param connectorSetting
     * @param settingName
     * @return
     */
    public static AccessorSetting createHarvested(//
	    String accessorType, //
	    HarvestedConnectorSetting connectorSetting, //
	    String settingName) {

	AccessorSetting accessorSetting = new AccessorSetting();

	//
	// the name of the configurable to configure when the job raises
	//
	accessorSetting.setConfigurableType(accessorType);

	accessorSetting.getOption(ACCESSOR_TYPE_KEY, String.class).get().setValue(accessorType);

	accessorSetting.setHarvestedConnectorSetting(connectorSetting);

	// the setting name, e.g.: "OAIPMH Accessor settings"
	accessorSetting.setName(settingName);

	//
	//
	//

	accessorSetting.getGSSourceSetting().setIdentifier(accessorType + "_sourceSetting");

	accessorSetting.getGSSourceSetting().setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	accessorSetting.getGSSourceSetting().setName(settingName + " source settings");

	//
	//
	//

	accessorSetting.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	return accessorSetting;
    }

    /**
     * @param accessorType
     * @param connectorSetting
     * @param settingName
     * @return
     */
    public static AccessorSetting createDistributed(//
	    String accessorType, //
	    DistributedConnectorSetting connectorSetting, //
	    String settingName) {

	AccessorSetting accessorSetting = new AccessorSetting();

	//
	// the name of the configurable to configure when the job raises
	//
	accessorSetting.setConfigurableType(accessorType);

	accessorSetting.getOption(ACCESSOR_TYPE_KEY, String.class).get().setValue(accessorType);

	accessorSetting.setDistributedConnectorSetting(connectorSetting);

	// the setting name, e.g.: "OAIPMH Accessor settings"
	accessorSetting.setName(settingName);

	accessorSetting.getGSSourceSetting().setIdentifier(accessorType + "_sourceSetting");

	accessorSetting.getGSSourceSetting().setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	accessorSetting.getGSSourceSetting().setName(settingName + " source settings");

	//
	//
	//

	accessorSetting.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	return accessorSetting;
    }

    /**
     * @return
     */
    public void setDistributedConnectorSetting(DistributedConnectorSetting setting) {

	addSetting(setting);
    }

    /**
     * @return
     */
    public DistributedConnectorSetting getDistributedConnectorSetting() {

	List<DistributedConnectorSetting> settings = getSettings(//
		DistributedConnectorSetting.class, false);

	if (!settings.isEmpty()) {

	    return settings.getFirst();
	}

	return null;

    }

    /**
     * @return
     */
    public void setHarvestedConnectorSetting(HarvestedConnectorSetting setting) {

	addSetting(setting);
    }

    /**
     * @return
     */
    public HarvestedConnectorSetting getHarvestedConnectorSetting() {

	List<HarvestedConnectorSetting> settings = getSettings(//
		HarvestedConnectorSetting.class, false);

	if (!settings.isEmpty()) {

	    return settings.getFirst();
	}

	return null;
    }

    /**
     * @param harvestedAccessorType
     */
    public void setHarvestedAccessorType(String harvestedAccessorType) {

	getObject().put("harvestedAccessorType", harvestedAccessorType);
    }

    /**
     * @return
     */
    public String getHarvestedAccessorType() {

	if (getObject().has("harvestedAccessorType")) {

	    return getObject().getString("harvestedAccessorType");
	}

	return null;
    }

    /**
     * @param distributedAccessorType
     */
    public void setDistributedAccessorType(String distributedAccessorType) {

	getObject().put("distributedAccessorType", distributedAccessorType);
    }

    /**
     * @return
     */
    public String getDistributedAccessorType() {

	if (getObject().has("distributedAccessorType")) {

	    return getObject().getString("distributedAccessorType");
	}

	return null;
    }

    /**
     * @return
     */
    public BrokeringStrategy getBrokeringStrategy() {

	return LabeledEnum.valueOf(BrokeringStrategy.class, readStrategy()).get();
    }

    /**
     * @param strategy
     */
    public void setBrokeringStrategy(BrokeringStrategy strategy) {

	getObject().put("brokeringStrategy", strategy.getLabel());
    }

    /**
     * @return
     */
    public GSSourceSetting getGSSourceSetting() {

	return getSettings(GSSourceSetting.class).getFirst();
    }

    /**
     * @return
     */
    public GSSource getSource() {

	return getGSSourceSetting().asSource();
    }

    /**
     * For non mixed accessors, this is the same as {@link #getConfigurableType()}, while for
     * mixed accessors this method returns the composition of {@link #getDistributedAccessorType()}
     * {@link #getHarvestedAccessorType()} separated by an underscore
     *
     * @return
     */
    public String getAccessorType() {

	return getOption(ACCESSOR_TYPE_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    private String readStrategy() {

	return getObject().getString("brokeringStrategy");
    }
}
