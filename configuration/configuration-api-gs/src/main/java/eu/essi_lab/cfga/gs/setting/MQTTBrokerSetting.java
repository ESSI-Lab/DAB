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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * Connection parameters of one MQTT broker. A disabled setting means that the publishers bound to it do not publish.
 *
 * @author Fabrizio
 */
public class MQTTBrokerSetting extends Setting {

    private static final String HOST_OPTION_KEY = "mqttHost";
    private static final String PORT_OPTION_KEY = "mqttPort";
    private static final String USER_OPTION_KEY = "mqttUser";
    private static final String PASSWORD_OPTION_KEY = "mqttPassword";

    /**
     * 
     */
    public MQTTBrokerSetting() {

	setCanBeDisabled(true);
	setEditable(false);
	setEnabled(false);
	enableCompactMode(false);
	setShowHeader(true);

	Option<String> host = StringOptionBuilder.get().//
		withKey(HOST_OPTION_KEY).//
		withLabel("Host").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(host);

	Option<Integer> port = IntegerOptionBuilder.get().//
		withKey(PORT_OPTION_KEY).//
		withLabel("Port").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(port);

	Option<String> user = StringOptionBuilder.get().//
		withKey(USER_OPTION_KEY).//
		withLabel("User").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(user);

	Option<String> password = StringOptionBuilder.get().//
		withKey(PASSWORD_OPTION_KEY).//
		withLabel("Password").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(password);
    }

    /**
     * @param object
     */
    public MQTTBrokerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public MQTTBrokerSetting(String object) {

	super(object);
    }

    /**
     * @param host
     */
    public void setHost(String host) {

	getOption(HOST_OPTION_KEY, String.class).get().setValue(host);
    }

    /**
     * @param port
     */
    public void setPort(int port) {

	getOption(PORT_OPTION_KEY, Integer.class).get().setValue(port);
    }

    /**
     * @param user
     */
    public void setUser(String user) {

	getOption(USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * @param password
     */
    public void setPassword(String password) {

	getOption(PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public Optional<String> getHost() {

	return getOption(HOST_OPTION_KEY, String.class).get().getOptionalValue().filter(value -> !value.isBlank());
    }

    /**
     * @return
     */
    public Optional<Integer> getPort() {

	return getOption(PORT_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<String> getUser() {

	return getOption(USER_OPTION_KEY, String.class).get().getOptionalValue().filter(value -> !value.isBlank());
    }

    /**
     * @return
     */
    public Optional<String> getPassword() {

	return getOption(PASSWORD_OPTION_KEY, String.class).get().getOptionalValue().filter(value -> !value.isBlank());
    }

    /**
     * @return
     */
    public boolean isComplete() {

	return getHost().isPresent() && getPort().isPresent() && getUser().isPresent() && getPassword().isPresent();
    }
}
