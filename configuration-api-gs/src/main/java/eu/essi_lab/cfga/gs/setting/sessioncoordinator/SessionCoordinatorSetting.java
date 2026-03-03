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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.TabContentDescriptor;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.TabContentDescriptorBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * Global setting for distributed session coordination when running on multiple nodes (e.g. ARPA Lombardia).
 * When enabled, uses Redis for distributed token acquisition (queue + heartbeat).
 */
public class SessionCoordinatorSetting extends Setting implements EditableSetting {

    private static final String DISTRIBUTED_SESSION_COORDINATOR_OPTION_KEY = "distributedSessionCoordinator";
    private static final String REDIS_ENDPOINT_OPTION_KEY = "redisEndpoint";
    private static final String REDIS_USERNAME_OPTION_KEY = "redisUsername";
    private static final String REDIS_PASSWORD_OPTION_KEY = "redisPassword";

    public SessionCoordinatorSetting(JSONObject object) {
	super(object);
    }

    public SessionCoordinatorSetting(String object) {
	super(object);
    }

    public SessionCoordinatorSetting() {
	setName("Session coordinator settings");
	enableCompactMode(false);
	setCanBeDisabled(false);
	setShowHeader(false);

	Option<BooleanChoice> distributedOption = BooleanChoiceOptionBuilder.get().//
		withKey(DISTRIBUTED_SESSION_COORDINATOR_OPTION_KEY).//
		withLabel("Use distributed session coordinator (Redis)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(distributedOption);

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
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(usernameOption);

	Option<String> passwordOption = StringOptionBuilder.get().//
		withKey(REDIS_PASSWORD_OPTION_KEY).//
		withLabel("Redis password").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(passwordOption);
    }

    public static class DescriptorProvider {

	private final TabContentDescriptor descriptor;

	public DescriptorProvider() {
	    descriptor = TabContentDescriptorBuilder.get(SessionCoordinatorSetting.class).//
		    withLabel("Session coordinator").//
		    build();
	}

	public TabContentDescriptor get() {
	    return descriptor;
	}
    }

    public boolean isDistributedSessionCoordinator() {
	return getOption(DISTRIBUTED_SESSION_COORDINATOR_OPTION_KEY, BooleanChoice.class).//
		map(opt -> BooleanChoice.toBoolean(opt.getSelectedValue())).//
		orElse(false);
    }

    public void setDistributedSessionCoordinator(boolean value) {
	getOption(DISTRIBUTED_SESSION_COORDINATOR_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(value));
    }

    public String getRedisEndpoint() {
	return getOption(REDIS_ENDPOINT_OPTION_KEY, String.class).get().getValue();
    }

    public void setRedisEndpoint(String endpoint) {
	getOption(REDIS_ENDPOINT_OPTION_KEY, String.class).get().setValue(endpoint != null ? endpoint : "");
    }

    public Optional<String> getRedisUsername() {
	String v = getOption(REDIS_USERNAME_OPTION_KEY, String.class).get().getValue();
	return (v == null || v.isEmpty()) ? Optional.empty() : Optional.of(v);
    }

    public void setRedisUsername(String username) {
	getOption(REDIS_USERNAME_OPTION_KEY, String.class).get().setValue(username != null ? username : "");
    }

    public Optional<String> getRedisPassword() {
	String v = getOption(REDIS_PASSWORD_OPTION_KEY, String.class).get().getValue();
	return (v == null || v.isEmpty()) ? Optional.empty() : Optional.of(v);
    }

    public void setRedisPassword(String password) {
	getOption(REDIS_PASSWORD_OPTION_KEY, String.class).get().setValue(password != null ? password : "");
    }
}
