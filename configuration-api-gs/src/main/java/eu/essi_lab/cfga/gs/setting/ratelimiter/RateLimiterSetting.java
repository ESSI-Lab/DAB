package eu.essi_lab.cfga.gs.setting.ratelimiter;

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
import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class RateLimiterSetting extends Setting implements EditableSetting {

    private static final String COMPUTATION_TYPE_OPTION_KEY = "computationTypeOption";
    private static final String HOST_NAME_OPTION_KEY = "hostNameOption";
    private static final String PORT_OPTION_KEY = "portOption";

    private static final String DEFAULT_MAX_REQUESTS_PER_IP_OPTION_KEY = "defMaxReqPerIPOption";

    private static final String DEFAULT_CONCURRENT_MAX_REQUESTS_OPTION_KEY = "defConcurrentMaxReqOption";
    private static final String DEFAULT_CONCURRENT_MAX_REQUESTS_PER_IP_OPTION_KEY = "defConcurrentMaxReqPerIPOption";

    private static final String DEFAULT_DB_OPTION_KEY = "defDBOption";
    private static final String FRONTEND_SETTING_ID = "frontendSettingId";
    private static final String ACCCESS_SETTING_ID = "accessSettingId";
    private static final String AUGMENTER_SETTING_ID = "augmenterSettingId";
    private static final String INTENSIVE_SETTING_ID = "intensiveSettingId";
    private static final String LOCAL_PROD_SETTING_ID = "localProdSettingId";
    private static final String MIXED_MODE_SETTING_ID = "mixedSettingId";

    /**
     * @param object
     */
    public RateLimiterSetting(JSONObject object) {
	super(object);
    }

    /**
     * @param object
     */
    public RateLimiterSetting(String object) {
	super(object);
    }

    /**
     * @author Fabrizio
     */
    public enum ComputationType implements LabeledEnum {

	/**
	 *
	 */
	DISABLED("Disabled"),
	/**
	 *
	 */
	LOCAL("Local"),
	/**
	 *
	 */
	DISTRIBUTED("Distributed");

	private final String name;

	/**
	 * @param name
	 */
	ComputationType(String name) {

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
    public RateLimiterSetting() {

	setName("Rate limiter settings");
	enableCompactMode(false);
	setCanBeDisabled(false);

	//
	//
	//

	Option<ComputationType> compType = OptionBuilder.get(ComputationType.class).//
		withKey(COMPUTATION_TYPE_OPTION_KEY).//
		withLabel("Computation type").//
		withSingleSelection().//
		withValues(LabeledEnum.values(ComputationType.class)).//
		withSelectedValue(LabeledEnum.values(ComputationType.class).getFirst()).//
		cannotBeDisabled().//
		required().//
		build();

	addOption(compType);

	Option<String> hostName = StringOptionBuilder.get().//
		withKey(HOST_NAME_OPTION_KEY).//
		withLabel("Host name").//
		withValue("localhost").//
		cannotBeDisabled().//
		build();

	addOption(hostName);

	Option<Integer> port = IntegerOptionBuilder.get().//
		withKey(PORT_OPTION_KEY).//
		withLabel("Port").//
		withValue(6379).//
		cannotBeDisabled().//
		build();

	addOption(port);

	Option<Integer> defaultMaxRequestsPerIP = IntegerOptionBuilder.get().//
		withKey(DEFAULT_MAX_REQUESTS_PER_IP_OPTION_KEY).//
		withLabel("Default overall max requests per IP").//
		withValue(10).//
		cannotBeDisabled().//
		build();

	addOption(defaultMaxRequestsPerIP);

	Option<Integer> defaultConcurrentMaxRequests = IntegerOptionBuilder.get().//
		withKey(DEFAULT_CONCURRENT_MAX_REQUESTS_OPTION_KEY).//
		withLabel("Default concurrent max requests").//
		withValue(5).//
		cannotBeDisabled().//
		build();

	addOption(defaultConcurrentMaxRequests);

	Option<Integer> defaultConcurrentMaxRequestsPerIP = IntegerOptionBuilder.get().//
		withKey(DEFAULT_CONCURRENT_MAX_REQUESTS_PER_IP_OPTION_KEY).//
		withLabel("Default concurrent max requests per IP").//
		withValue(1).//
		cannotBeDisabled().//
		build();

	addOption(defaultConcurrentMaxRequestsPerIP);

	Option<String> defaultDb = StringOptionBuilder.get().//
		withKey(DEFAULT_DB_OPTION_KEY).//
		withLabel("Default DB").//
		withValue("default").//
		cannotBeDisabled().//
		build();

	addOption(defaultDb);

	//
	//
	//

	ExecutionModeSetting frontendSetting = new ExecutionModeSetting();
	frontendSetting.setName("Frontend");
	frontendSetting.setIdentifier(FRONTEND_SETTING_ID);
	addSetting(frontendSetting);

	ExecutionModeSetting accessSetting = new ExecutionModeSetting();
	accessSetting.setIdentifier(ACCCESS_SETTING_ID);
	accessSetting.setName("Access");
	addSetting(accessSetting);

	ExecutionModeSetting augmenterSetting = new ExecutionModeSetting();
	augmenterSetting.setIdentifier(AUGMENTER_SETTING_ID);
	augmenterSetting.setName("Augmenter");
	addSetting(augmenterSetting);

	ExecutionModeSetting intensiveSetting = new ExecutionModeSetting();
	intensiveSetting.setIdentifier(INTENSIVE_SETTING_ID);
	intensiveSetting.setName("Intensive");
	addSetting(intensiveSetting);

	ExecutionModeSetting localProdSetting = new ExecutionModeSetting();
	localProdSetting.setIdentifier(LOCAL_PROD_SETTING_ID);
	localProdSetting.setName("Local production");
	addSetting(localProdSetting);

	ExecutionModeSetting mixedSetting = new ExecutionModeSetting();
	mixedSetting.setIdentifier(MIXED_MODE_SETTING_ID);
	mixedSetting.setName("Mixed");
	addSetting(mixedSetting);

	//
	// set the rendering extension
	//
	setExtension(new RateLimiterSettingComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class RateLimiterSettingComponentInfo extends ComponentInfo {

	/**
	 *
	 */
	public RateLimiterSettingComponentInfo() {

	    setName(SystemSetting.class.getName());

	    TabDescriptor descriptor = TabDescriptorBuilder.get(RateLimiterSetting.class).//
		    withLabel("Rate limiter").//
		    withShowDirective("Rate limiter").//
		    build();

	    setPlaceholder(TabPlaceholder.of(GSTabIndex.RATE_LIMITER.getIndex(), descriptor));
	}
    }

    /**
     *
     */
    public void setComputationType(ComputationType computationType) {

	getOption(COMPUTATION_TYPE_OPTION_KEY, ComputationType.class).get().setValue(computationType);
    }

    /**
     * @return
     */
    public ComputationType getComputationType() {

	return getOption(COMPUTATION_TYPE_OPTION_KEY, ComputationType.class).get().getValue();
    }

    /**
     * @return
     */
    public String getHostName() {

	return getOption(HOST_NAME_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getPort() {

	return getOption(PORT_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getDefaultMaxRequestsPerIP() {

	return getOption(DEFAULT_MAX_REQUESTS_PER_IP_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getDefaultMaxConcurrentRequests() {

	return getOption(DEFAULT_CONCURRENT_MAX_REQUESTS_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getDefaultMaxConcurrenRequestsPerIP() {

	return getOption(DEFAULT_CONCURRENT_MAX_REQUESTS_PER_IP_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public String getDefaultDB() {

	return getOption(DEFAULT_DB_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public Optional<ExecutionModeSetting> getExecutionModeSetting(ExecutionMode mode) {

	return switch (mode) {
	    case ACCESS -> Optional.of(getSetting(ACCCESS_SETTING_ID, ExecutionModeSetting.class).get());
	    case AUGMENTER -> Optional.of(getSetting(AUGMENTER_SETTING_ID, ExecutionModeSetting.class).get());
	    case FRONTEND -> Optional.of(getSetting(FRONTEND_SETTING_ID, ExecutionModeSetting.class).get());
	    case INTENSIVE -> Optional.of(getSetting(INTENSIVE_SETTING_ID, ExecutionModeSetting.class).get());
	    case LOCAL_PRODUCTION -> Optional.of(getSetting(LOCAL_PROD_SETTING_ID, ExecutionModeSetting.class).get());
	    case MIXED -> Optional.of(getSetting(MIXED_MODE_SETTING_ID, ExecutionModeSetting.class).get());
	    default -> Optional.empty();
	};
    }
}
