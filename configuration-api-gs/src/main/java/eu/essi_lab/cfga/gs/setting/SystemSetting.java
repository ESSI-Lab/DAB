package eu.essi_lab.cfga.gs.setting;

import java.util.ArrayList;
import java.util.List;

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

import java.util.Optional;

import eu.essi_lab.cfga.gui.extension.*;
import eu.essi_lab.cfga.setting.ConfigurationObject;
import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.UsersDatabaseSetting;
import eu.essi_lab.cfga.gs.setting.ontology.DefaultSemanticSearchSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.KeyValueOptionDecorator;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class SystemSetting extends Setting implements EditableSetting, KeyValueOptionDecorator {

    private static final String DATABASE_STATISTICS_SETTING_KEY = "databaseStatistics";
    private static final String PROXY_ENDPOINT_OPTION_KEY = "proxyEndpoint";
    private static final String ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY = "enableMailHarvestingReport";
    private static final String ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY = "enableMailAugmentationReport";
    private static final String ENABLE_DOWNLOAD_MAIL_REPORTS_OPTION_KEY = "enableMailDownloadReport";
    private static final String ENABLE_ERROR_LOGS_MAIL_REPORTS_OPTION_KEY = "enableErrorLogsReport";
    private static final String EMAIL_SETTING_ID = "emailSetting";
    private static final String USERS_DATABASE_SETTING_ID = "usersDatabase";
    private static final String SEM_SEARCH_SETTING_ID = "defSemanticSearch";

    /**
     * @author Fabrizio
     */
    public enum KeyValueOptionKeys implements LabeledEnum {

	/**
	 * MarkLogic option
	 */
	COVERING_MODE("coveringMode"), //
	ENABLE_FILTERED_TRAILING_WILDCARD_QUERIES("enableFilteredTrailingWildcardQueries"),

	/**
	 * SPARQL proxy endpoint and forced accept header
	 */
	SPARQL_PROXY_ENDPOINT("sparqlProxyEndpoint"), //
	FORCE_SPARQL_PROXY_ACCEPT_HEADER("forceSparqlProxyAcceptHeader"), //

	/**
	 * ADMIN users
	 */
	ADMIN_USERS("adminUsers"), //

	/**
	 * MQTT broker
	 */
	MQTT_BROKER_HOST("mqttBrokerHost"), //
	MQTT_BROKER_PORT("mqttBrokerPort"), //
	MQTT_BROKER_USER("mqttBrokerUser"), //
	MQTT_BROKER_PWD("mqttBrokerPwd"), //

	/**
	 * HealthCheck options
	 */
	TASK_AGE_HEALTH_CHECK_METHOD_TRESHOLD("taskAgeHealthCheckMethodTreshold"), //
	FREE_MEMORY_HEALTH_CHECK_METHOD_TRESHOLD("freeMemoryHealthCheckMethodTreshold"), //
	SEND_HEALTH_CHECK_REPORT("sendHealthCheckReport"), //
	PROFILER_HEALTH_CHECK_METHOD_ENABLED("profilerHealthCheckMethodEnabled"),

	/**
	 * DABStarter option
	 */
	SCHEDULER_START_DELAY("schedulerStartDelay"),

	/**
	 * SOSConnector option
	 */
	SOS_100_PARALLEL_TASKS("sos100ParallelTasks"),

	/**
	 * Configurator option
	 */
	MULTIPLE_CONFIGURATION_TABS("multipleConfigurationTabs"),

	/**
	 * ConfigService option
	 */
	CONFIG_SERVICE_AUTHTOKEN("configServiceAuthToken"),

	/**
	 * XACMLAutorizer option
	 */
	DEV_MACHINE_AUTH("devMachineAuth");

	/**
	 * MirrorSiteTokenGeneratorHandler option prefix
	 */
	public static final String MIRROR_SITE_HEADER_NAME_PREFIX = "mirrorsiteclient";

	private final String name;

	/**
	 * @param name
	 */
	KeyValueOptionKeys(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

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
	// Email download report
	//

	Option<BooleanChoice> downloadMailOption = BooleanChoiceOptionBuilder.get().//
		withKey(ENABLE_DOWNLOAD_MAIL_REPORTS_OPTION_KEY).//
		withLabel("Send download reports email").//
		withDescription("System restart required. This feature also require that E-mail settings are correctly compiled").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(downloadMailOption);

	//
	// Error logs report
	//

	Option<BooleanChoice> errorLogsMailOption = BooleanChoiceOptionBuilder.get().//
		withKey(ENABLE_ERROR_LOGS_MAIL_REPORTS_OPTION_KEY).//
		withLabel("Send error logs reports email (the related custom task must be enabled)").//
		withDescription("System restart required. This feature also require that E-mail settings are correctly compiled").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(errorLogsMailOption);

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
	// Key-value options
	//

	addKeyValueOption();

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
	// User database
	//
	UsersDatabaseSetting userdbSetting = new UsersDatabaseSetting();

	userdbSetting.setCanBeDisabled(true);
	userdbSetting.setEditable(false);
	userdbSetting.setEnabled(false);

	userdbSetting.enableCompactMode(false);
	userdbSetting.setName("User database setting");
	userdbSetting.setIdentifier(USERS_DATABASE_SETTING_ID);
	userdbSetting
		.setDescription("If enabled and configured, this setting allows to retrieve users information from a specific database");
	userdbSetting.removeVolatileSettings();
	userdbSetting.setSelectionMode(SelectionMode.UNSET);

	userdbSetting.hideDatabaseConfigurationName();
	userdbSetting.hideDatabaseConfigurationFolderOption();

	addSetting(userdbSetting);

	//
	// Ontology default settings
	//

	DefaultSemanticSearchSetting semSearchSetting = new DefaultSemanticSearchSetting();
	semSearchSetting.setIdentifier(SEM_SEARCH_SETTING_ID);

	addSetting(semSearchSetting);

	//
	// set the rendering extension
	//
	setExtension(new SystemSettingComponentInfo());

	//
	// set the validator
	//
	setValidator(new SystemSettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class SystemSettingValidator implements Validator {

	private boolean error;

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    SystemSetting sysSetting = (SystemSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    DefaultSemanticSearchSetting semSetting = sysSetting.getDefaultSemanticSearchSetting();

	    error = semSetting.getDefaultSemanticRelations().isEmpty();

	    Optional<EmailSetting> emailSetting = sysSetting.getEmailSetting();

	    emailSetting.ifPresent(s -> error |= check(s));

	    Optional<DatabaseSetting> statSetting = sysSetting.getStatisticsSetting();

	    statSetting.ifPresent(s -> error |= check(s));

	    Optional<UsersDatabaseSetting> usersSetting = sysSetting.getUsersDatabaseSetting();

	    usersSetting.ifPresent(s -> error |= check(s));

	    ValidationResponse response = new ValidationResponse();

	    if (error) {

		response.getErrors().add("Please provide all the required fields");
		response.setResult(ValidationResult.VALIDATION_FAILED);
	    }

	    return response;
	}

	/**
	 * @param setting
	 * @return
	 */
	private boolean check(Setting setting) {

	    List<Setting> list = new ArrayList<>();

	    SettingUtils.deepFind(setting, ConfigurationObject::isEnabled, list);

	    return list.//
		    stream().//
		    flatMap(s -> s.getOptions().stream()). //
		    anyMatch(o -> o.isRequired() && !o.getKey().equals("configFolder")
			    && o.getOptionalValue().isEmpty() && o.getOptionalSelectedValue().isEmpty());
 	}
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

	/**
	 * 
	 */
	public SystemSettingComponentInfo() {

	    setName(SystemSetting.class.getName());

	    TabDescriptor descriptor = TabDescriptorBuilder.get(SystemSetting.class).//
		    withLabel("System").//
		    build();

	    setPlaceholder(TabPlaceholder.of(GSTabIndex.SYSTEM.getIndex(), descriptor));
	}
    }

    /**
     * @param enable
     */
    public void enableHarvestingReportEmail(boolean enable) {

	Option<BooleanChoice> option = getOption(ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get();
	option.select(v -> BooleanChoice.toBoolean(v) == enable);
    }

    /**
     * 
     */
    public boolean isHarvestingReportMailEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_HARVESTING_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @param enable
     */
    public void enableAugmentationReportMail(boolean enable) {

	Option<BooleanChoice> option = getOption(ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get();
	option.select(v -> BooleanChoice.toBoolean(v) == enable);
    }

    /**
     * 
     */
    public boolean isAugmentationReportMailEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_AUGMENTION_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @param enable
     */
    public void enableDownloadReportMail(boolean enable) {

	Option<BooleanChoice> option = getOption(ENABLE_DOWNLOAD_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get();
	option.select(v -> BooleanChoice.toBoolean(v) == enable);
    }

    /**
     * 
     */
    public boolean isDownloadReportMailEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_DOWNLOAD_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @param enable
     */
    public void enableErrorLogsReportEmail(boolean enable) {

	Option<BooleanChoice> option = getOption(ENABLE_ERROR_LOGS_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get();
	option.select(v -> BooleanChoice.toBoolean(v) == enable);
    }

    /**
     * 
     */
    public boolean isErrorLogsReportEnabled() {

	return BooleanChoice.toBoolean(//
		getOption(ENABLE_ERROR_LOGS_MAIL_REPORTS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
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

    /**
     * @return
     */
    public Optional<UsersDatabaseSetting> getUsersDatabaseSetting() {

	UsersDatabaseSetting setting = getSetting(USERS_DATABASE_SETTING_ID, UsersDatabaseSetting.class).get();

	if (setting.isEnabled()) {

	    return Optional.of(setting);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public DefaultSemanticSearchSetting getDefaultSemanticSearchSetting() {

	return getSetting(SEM_SEARCH_SETTING_ID, DefaultSemanticSearchSetting.class).get();
    }
}
