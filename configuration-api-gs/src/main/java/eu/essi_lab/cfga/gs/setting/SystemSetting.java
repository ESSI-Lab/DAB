package eu.essi_lab.cfga.gs.setting;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.ConfigurableSetting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class SystemSetting extends ConfigurableSetting implements EditableSetting {

    private static final String DATABASE_STATISTICS_SETTING_KEY = "databaseStatistics";

    private static final String PROXY_ENDPOINT_OPTION_KEY = "proxyEndpoint";

    private static final String ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY = "enableMailHarvestingReport";

    private static final String ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY = "enableMailAugmentationReport";

    private static final String EMAIL_SETTING_ID = "emailSetting";

    /**
    * 
    */
    public SystemSetting() {

	setName("System settings");
	enableCompactMode(false);
	setCanBeDisabled(false);

	//
	// Email harvesting report
	//

	Option<BooleanChoice> harvestingMailOption = BooleanChoiceOptionBuilder.get().//
		withKey(ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY).//
		withLabel("Send harvesting reports email").//
		withDescription("System restart required. This feature also require that E-mail settings are correctly compiled").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(harvestingMailOption);

	//
	// Email augmentation report
	//

	Option<BooleanChoice> augmentationMailOption = BooleanChoiceOptionBuilder.get().//
		withKey(ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY).//
		withLabel("Send augmentation reports email").//
		withDescription("System restart required. This feature also require that E-mail settings are correctly compiled").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(augmentationMailOption);

	//
	// Proxy
	//

	Option<String> proxyEndpoint = StringOptionBuilder.get().//
		withKey(PROXY_ENDPOINT_OPTION_KEY).//
		withLabel("Proxy endpoint").//
		withDescription("The GI-proxy endpoint used by accessors and downloaders (e.g. to circumvent firewalls)").//
		cannotBeDisabled().//
		build();

	addOption(proxyEndpoint);

	//
	// E-mail settings
	//

	EmailSetting emailSetting = new EmailSetting();
	emailSetting.setIdentifier(EMAIL_SETTING_ID);

	addSetting(emailSetting);

	//
	// Statistics
	//
	DatabaseSetting statsSetting = new DatabaseSetting();

	statsSetting.setCanBeDisabled(true);
	statsSetting.setEditable(false);
	statsSetting.setEnabled(false);

	statsSetting.enableCompactMode(false);
	statsSetting.setName("DAB statistics gathering");
	statsSetting.setIdentifier(DATABASE_STATISTICS_SETTING_KEY);
	statsSetting.setDescription(
		"System restart required. Enable/disable statistics gathering and computing by means of an Elasticsearch database");
	statsSetting.removeVolatileSettings();
	statsSetting.setSelectionMode(SelectionMode.UNSET);

	statsSetting.hideDatabaseConfigurationName();
	statsSetting.hideDatabaseConfigurationFolderOption();

	addSetting(statsSetting);

	//
	// set the rendering extension
	//
	setExtension(new SystemSettingComponentInfo());
    }

    /**
     * @param object
     */
    public SystemSetting(JSONObject object) {
	super(object);
    }

    /**
     * @param object
     */
    public SystemSetting(String object) {
	super(object);
    }

    /**
     * @author Fabrizio
     */
    public static class SystemSettingComponentInfo extends ComponentInfo {

	public static int tabIndex = 14;

	/**
	 * 
	 */
	public SystemSettingComponentInfo() {

	    setComponentName(SystemSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(tabIndex).//
		    withShowDirective("System").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * 
     */
    public boolean isHarvestingReportMailEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * 
     */
    public boolean isAugmentationReportMailEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    //
    // E-mail
    //

    /**
     * @return
     */
    public Optional<EmailSetting> getEmailSetting() {

	EmailSetting emailSetting = getSetting(EMAIL_SETTING_ID, EmailSetting.class).get();
	if (emailSetting.isEnabled()) {

	    return Optional.of(emailSetting);
	}

	return Optional.empty();
    }

    //
    // Proxy
    //

    /**
     * @param endpoint
     */
    public void setProxyEndpoint(String endpoint) {

	getOption(PROXY_ENDPOINT_OPTION_KEY, String.class).get().setValue(endpoint);
    }

    /**
     * @return
     */
    public Optional<String> getProxyEndpoint() {

	return getOption(PROXY_ENDPOINT_OPTION_KEY, String.class).get().getOptionalValue();
    }

    //
    // Statistics
    //

    /**
     * @return
     */
    public boolean areStatisticsEnabled() {

	return getSetting(DATABASE_STATISTICS_SETTING_KEY).get().isEnabled();
    }

    /**
     * @return
     */
    public Optional<DatabaseSetting> getStatisticsSetting() {

	DatabaseSetting setting = getSetting(DATABASE_STATISTICS_SETTING_KEY, DatabaseSetting.class).get();

	if (setting.isEnabled()) {

	    return Optional.of(setting);
	}

	return Optional.empty();
    }

    @Override
    public String getType() {

	return "SystemSetting";
    }
}
