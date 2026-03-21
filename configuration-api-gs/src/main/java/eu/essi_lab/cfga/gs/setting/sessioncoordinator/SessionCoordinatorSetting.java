package eu.essi_lab.cfga.gs.setting.sessioncoordinator;

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
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;
import org.json.*;

import java.util.*;

/**
 * Global setting for distributed session coordination when running on multiple nodes (e.g. for ARPA Lombardia). When enabled, uses Redis
 * for distributed token acquisition (queue + heartbeat).
 */
public class SessionCoordinatorSetting extends Setting implements EditableSetting {

    private static final String USE_DIST_TOKEN_OPTION_KEY = "useDistToken";
    private static final String REDIS_ENDPOINT_OPTION_KEY = "redisEndpoint";
    private static final String REDIS_USERNAME_OPTION_KEY = "redisUsername";
    private static final String REDIS_PASSWORD_OPTION_KEY = "redisPassword";
    private static final String MAX_SERVICES_OPTION_KEY = "maxServices";
    private static final String TTL_OPTION_KEY = "ttl";
    private static final String HEARTBEAT_OPTION_KEY = "heartbeat";
    private static final String DIST_SERVICES_SETTING_ID = "distServicesSetting";
    private static final String LOCAL_SERVICES_SETTING_ID = "localServicesSetting";

    /**
     * @author Fabrizio
     */
    public enum ServiceCoordinatorMode {

	/**
	 *
	 */
	LOCAL,

	/**
	 *
	 */
	DISTRIBUTED
    }

    /**
     * @param object
     */
    public SessionCoordinatorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SessionCoordinatorSetting(String object) {

	super(object);
    }

    /**
     *
     */
    public SessionCoordinatorSetting() {

	setName("Session coordinator settings");

	setCanBeDisabled(false);
	enableCompactMode(false);
	setShowHeader(false);
	setSelectionMode(SelectionMode.SINGLE);
	setCanBeCleaned(false);

	Option<BooleanChoice> distToken = BooleanChoiceOptionBuilder.get().//
		withKey(USE_DIST_TOKEN_OPTION_KEY).//
		withLabel("Use for distributed token acquisition").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(distToken);

	Option<String> endpointOption = StringOptionBuilder.get().//
		withKey(REDIS_ENDPOINT_OPTION_KEY).//
		withLabel("Redis endpoint (host:port)").//
		withValue("localhost:6379").//
		cannotBeDisabled().//
		build();

	addOption(endpointOption);

	Option<String> usernameOption = StringOptionBuilder.get().//
		withKey(REDIS_USERNAME_OPTION_KEY).//
		withLabel("Redis username").//
		cannotBeDisabled().//
		build();

	addOption(usernameOption);

	Option<String> passwordOption = StringOptionBuilder.get().//
		withKey(REDIS_PASSWORD_OPTION_KEY).//
		withLabel("Redis password").//
		cannotBeDisabled().//
		build();

	addOption(passwordOption);

	//
	//
	//

	Setting localServiceSetting = new Setting();
	localServiceSetting.setIdentifier(LOCAL_SERVICES_SETTING_ID);
	localServiceSetting.setName("Local services coordination");

	localServiceSetting.setDescription(
		"Services coordination is limited to the local node, without limitation on the maximum number of runnable services");
	localServiceSetting.setSelected(true);
	localServiceSetting.setCanBeDisabled(false);
	localServiceSetting.enableCompactMode(false);
	localServiceSetting.setEditable(false);

	addSetting(localServiceSetting);

	//
	//
	//

	Setting distServicesSetting = new Setting();
	distServicesSetting.setIdentifier(DIST_SERVICES_SETTING_ID);
	distServicesSetting.setName("Distributed services coordination");
	distServicesSetting.setDescription("Services coordination is distributed on the cluster nodes with Redis");
	distServicesSetting.setEditable(false);
	distServicesSetting.enableCompactMode(false);
	distServicesSetting.setCanBeDisabled(false);

	Option<Integer> maxServicesOption = IntegerOptionBuilder.get(). //
		withKey(MAX_SERVICES_OPTION_KEY).//
		withLabel("Max. number of services per node").//
		cannotBeDisabled().//
		withSingleSelection().//
		withValues(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).//
		withSelectedValue(3).//
		build();

	distServicesSetting.addOption(maxServicesOption);

	Option<Integer> ttlOption = IntegerOptionBuilder.get(). //
		withKey(TTL_OPTION_KEY).//
		withLabel("Lock duration (TTL) in seconds. Minimum value is 30 seconds").//
		cannotBeDisabled().//
		withMinValue(30).//
		withValue(30).//
		build();

	distServicesSetting.addOption(ttlOption);

	Option<Integer> hearthbeatOption = IntegerOptionBuilder.get(). //
		withKey(HEARTBEAT_OPTION_KEY).//
		withLabel("Heartbeat frequency in seconds (recommended 1/3 of TTL). Minimum value is 10 seconds").//
		cannotBeDisabled().//
		withMinValue(10).//
		withValue(10).//
		build();

	distServicesSetting.addOption(hearthbeatOption);

	addSetting(distServicesSetting);
    }

    /**
     * @author Fabrizio
     */
    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	/**
	 *
	 */
	public DescriptorProvider() {
	    descriptor = TabContentDescriptorBuilder.get(SessionCoordinatorSetting.class).//
		    withLabel("Session coordinator").//
		    withShowDirective("Global setting for distributed session coordination with Redis on a multi-node cluster."
		    + " Redis can be used for distributed token acquisition (queue + heartbeat), or for distributed services coordination")
		    .build();
	}

	/**
	 * @return
	 */
	public TabContentDescriptor get() {

	    return descriptor;
	}
    }

    /**
     * @return
     */
    public boolean isDistributedTokenUsed() {

	return getOption(USE_DIST_TOKEN_OPTION_KEY, BooleanChoice.class).//
		map(opt -> BooleanChoice.toBoolean(opt.getSelectedValue())).//
		orElse(false);
    }

    /**
     * @param value
     */
    public void useDistributedToken(boolean value) {

	getOption(USE_DIST_TOKEN_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(value));
    }

    /**
     * @return
     */
    public String getRedisEndpoint() {

	return getRedisEndpoint(true);
    }

    /**
     * @param includePort
     * @return
     */
    public String getRedisEndpoint(boolean includePort) {

	String redisEndpoint = getOption(REDIS_ENDPOINT_OPTION_KEY, String.class).get().getValue();

	if (!includePort) {

	    redisEndpoint = redisEndpoint.substring(0, redisEndpoint.indexOf(':'));
	}

	return redisEndpoint;
    }

    /**
     * @param endpoint
     */
    public void setRedisEndpoint(String endpoint) {

	getOption(REDIS_ENDPOINT_OPTION_KEY, String.class).get().setValue(endpoint != null ? endpoint : "");
    }

    /**
     * @return
     */
    public Optional<String> getRedisUsername() {

	return getOption(REDIS_USERNAME_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param username
     */
    public void setRedisUsername(String username) {

	if (username != null && !username.isEmpty()) {

	    getOption(REDIS_USERNAME_OPTION_KEY, String.class).get().setValue(username);
	}
    }

    /**
     * @return
     */
    public Optional<String> getRedisPassword() {

	return getOption(REDIS_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setRedisPassword(String password) {

	if (password != null && !password.isEmpty()) {

	    getOption(REDIS_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
	}
    }

    /**
     * @return
     */
    public ServiceCoordinatorMode getServiceCoordinatorMode() {

	return getSetting(DIST_SERVICES_SETTING_ID).//
		isPresent() && getSetting(DIST_SERVICES_SETTING_ID).get().isSelected() ? //
		ServiceCoordinatorMode.DISTRIBUTED : ServiceCoordinatorMode.LOCAL;
    }

    /**
     * @return
     */
    public int getMaxServices() {

	return getSetting(DIST_SERVICES_SETTING_ID).//
		get(). //
		getOption(MAX_SERVICES_OPTION_KEY, Integer.class).get().//
		getSelectedValue();
    }

    /**
     * @return
     */
    public int getTTL() {

	return getSetting(DIST_SERVICES_SETTING_ID).//
		get(). //
		getOption(TTL_OPTION_KEY, Integer.class).get().//
		getValue();
    }

    /**
     * @return
     */
    public int getHeartbeat() {

	return getSetting(DIST_SERVICES_SETTING_ID).//
		get(). //
		getOption(HEARTBEAT_OPTION_KEY, Integer.class).get().//
		getValue();
    }
}
