package eu.essi_lab.cfga.gs.setting;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class EmailSetting extends Setting {

    private static final String SMTP_HOST_OPTION_KEY = "smtpHost";
    private static final String SMTP_PORT_OPTION_KEY = "smtpPort";
    private static final String SMTP_USER_OPTION_KEY = "smtpUser";
    private static final String SMTP_PASSWORD_OPTION_KEY = "smtpPassword";
    private static final String RECIPIENTS_OPTION_KEY = "emailRecipients";

    /**
     * 
     */
    public EmailSetting() {

	setName("E-mail Settings");
	setDescription("Settings required by the DAB to send e-mail messages. E-mail are sent during harvesting"+
	"(if the related option is enabled) and if errors occur during statistics storing (if statistics gathering is enabled)");
	enableCompactMode(false);
	setEditable(false);
	setEnabled(false);

	Option<String> smtpHost = StringOptionBuilder.get().//
		withKey(SMTP_HOST_OPTION_KEY).//
		withLabel("SMTP host").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(smtpHost);

	Option<Integer> smtpPort = IntegerOptionBuilder.get().//
		withKey(SMTP_PORT_OPTION_KEY).//
		withLabel("SMTP port").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(smtpPort);

	Option<String> smtpUser = StringOptionBuilder.get().//
		withKey(SMTP_USER_OPTION_KEY).//
		withLabel("SMTP user").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(smtpUser);

	Option<String> smtpPassword = StringOptionBuilder.get().//
		withKey(SMTP_PASSWORD_OPTION_KEY).//
		withLabel("SMTP password").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(smtpPassword);

	Option<String> emailRecipients = StringOptionBuilder.get().//
		withKey(RECIPIENTS_OPTION_KEY).//
		withLabel("E-mail recipients").//
		withDescription("Comma separated list of e-mail recipients").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(emailRecipients);
    }

    /**
     * @param object
     */
    public EmailSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public EmailSetting(String object) {

	super(object);
    }

    /**
     * @param host
     */
    public void setSMTPHost(String host) {

	getOption(SMTP_HOST_OPTION_KEY, String.class).get().setValue(host);
    }

    /**
     * @param port
     */
    public void setSMTPPort(int port) {

	getOption(SMTP_PORT_OPTION_KEY, Integer.class).get().setValue(port);

    }

    /**
     * @param user
     */
    public void setSMTPUser(String user) {

	getOption(SMTP_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * @param password
     */
    public void setSMTPPassword(String password) {

	getOption(SMTP_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @param user
     */
    public void setRecipients(String... recipients) {

	String joined = String.join(",", Arrays.asList(recipients));
	getOption(RECIPIENTS_OPTION_KEY, String.class).get().setValue(joined);
    }

    /**
     * @return
     */
    public Optional<String> getSMTPHost() {

	return getOption(SMTP_HOST_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<Integer> getSMTPPort() {

	return getOption(SMTP_PORT_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<String> getSMTPUser() {

	return getOption(SMTP_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<String> getSMTPPassword() {

	return getOption(SMTP_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();

    }

    /**
     * @return
     */
    public List<String> getRecipients() {

	Optional<String> optValue = getOption(RECIPIENTS_OPTION_KEY, String.class).get().getOptionalValue();

	if (optValue.isPresent()) {

	    String value = optValue.get();
	    return Arrays.asList(value.split(","));
	}

	return new ArrayList<>();
    }
}
