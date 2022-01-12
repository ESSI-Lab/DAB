package eu.essi_lab.shared.driver.es.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.mail.MessagingException;

import eu.essi_lab.lib.net.smtp.GmailClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
public class GSMailSenderElasticsearch {
    public static final String MAIL_REPORT_SUBJECT = "[GS-REPORT]";
    public static final String MAIL_REPORT_STATISTICS = "[STATISTICS]";
    public static final String MAIL_ERROR_SUBJECT = "[ERROR]";
    public static final String MAIL_WARNING_SUBJECT = "[WARNING]";
    public static final String MAIL_HARVESTING_SUBJECT = "[HARVESTING]";

    public static String RECIPIENTS = null;
    public static String SMTP_HOST = null;
    public static String SMTP_PORT = null;
    public static String SMTP_USER = null;
    public static String SMTP_PASSWORD = null;

    /**
     * @param subject
     * @param message
     * @throws GSException
     */
    public static void sendEmail(String subject, String message) {

	printMailParameters();

	if (SMTP_HOST == null || SMTP_PORT == null || SMTP_USER == null || SMTP_PASSWORD == null || RECIPIENTS == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing e-mail parameters. Unable to send mail.");
	    return;
	}

	GmailClient client = new GmailClient(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD);

	try {

	    if (RECIPIENTS != null) {

		String[] recipients = new String[] { RECIPIENTS };
		if (RECIPIENTS.contains(",")) {
		    recipients = RECIPIENTS.split(",");
		}
		client.send(subject, message, recipients);
	    }

	} catch (MessagingException e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).error(e.getMessage(), e);
	}
    }

    public static void printMailParameters() {
	if (SMTP_HOST == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing smtp host");
	} else {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).info("smtp host: {}", SMTP_HOST);
	}
	if (SMTP_PORT == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing smtp port");
	} else {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).info("smtp port: {}", SMTP_PORT);
	}
	if (SMTP_USER == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing smtp email");
	} else {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).info("smtp email: {}", SMTP_USER);
	}
	if (SMTP_PASSWORD == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing smtp password");
	} else {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).info("smtp password: {}", SMTP_PASSWORD);
	}
	if (RECIPIENTS == null) {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).warn("Missing smtp recipients");
	} else {
	    GSLoggerFactory.getLogger(GSMailSenderElasticsearch.class).info("smtp recipients: {}", RECIPIENTS);
	}

    }
}
