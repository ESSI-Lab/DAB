package eu.essi_lab.cfga.gs;

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

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.setting.EmailSetting;
import eu.essi_lab.cfga.source.*;
import eu.essi_lab.lib.net.smtp.SMTPClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class ConfiguredSMTPClient {

    public static final String MAIL_ALARM = "[ALARM]";
    public static final String MAIL_REPORT_SUBJECT = "[GS-REPORT]";
    public static final String MAIL_REPORT_STATISTICS = "[STATISTICS]";
    public static final String MAIL_ERROR_SUBJECT = "[ERROR]";
    public static final String MAIL_WARNING_SUBJECT = "[WARNING]";
    public static final String MAIL_HARVESTING_SUBJECT = "[HARVESTING]";
    public static final String MAIL_AUGMENTATION_SUBJECT = "[AUGMENTATION]";

    private static List<String> recipients;
    private static String smtpHost;
    private static String smtpPort;
    private static String smtpUser;
    private static String smtpPassword;

    /**
     * @param subject
     * @param message
     * @param recipient
     */
    public static boolean sendEmail(String subject, String message, String... recipient) {

	Optional<EmailSetting> optEmailSetting = ConfigurationWrapper.getSystemSettings().getEmailSetting();

	if (optEmailSetting.isPresent()) {

	    EmailSetting emailSetting = optEmailSetting.get();

	    smtpHost = emailSetting.getSMTPHost().orElse(null);
	    smtpPort = emailSetting.getSMTPPort().map(String::valueOf).orElse(null);
	    smtpUser = emailSetting.getSMTPUser().orElse(null);
	    smtpPassword = emailSetting.getSMTPPassword().orElse(null);

	    if (recipient != null && recipient.length > 0) {
		recipients = Arrays.asList(recipient);
	    } else {
		recipients = emailSetting.getRecipients();
	    }
	}

	logMailParameters();

	if (smtpHost == null || smtpPort == null || smtpUser == null || smtpPassword == null || recipients.isEmpty()) {

	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing e-mail parameters. Unable to send mail.");
	    return false;
	}

	SMTPClient client = new SMTPClient(smtpHost, smtpPort, smtpUser, smtpPassword);
	client.enableSSL(true);

	try {

	    client.send(subject, message, recipients.toArray(new String[] {}));

	    return true;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).error(e.getMessage(), e);
	}

	return false;
    }

    /**
     * @param subject
     * @param message
     */
    public static void sendEmail(String subject, String message) {

	sendEmail(subject, message, new String[] {});
    }

    /**
     *
     */
    public static void logMailParameters() {

	if (smtpHost == null) {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing smtp host");
	} else {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).info("smtp host: {}", smtpHost);
	}

	if (smtpPort == null) {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing smtp port");
	} else {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).info("smtp port: {}", smtpPort);
	}

	if (smtpUser == null) {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing smtp email");
	} else {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).info("smtp email: {}", smtpUser);
	}

	if (smtpPassword == null) {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing smtp password");
	} else {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).info("smtp password: {}", smtpPassword);
	}

	if (recipients == null || recipients.isEmpty()) {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).warn("Missing smtp recipients");
	} else {
	    GSLoggerFactory.getLogger(ConfiguredSMTPClient.class).info("smtp recipients: {}", recipients);
	}
    }
}
