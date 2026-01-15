/**
 *
 */
package eu.essi_lab.lib.net.smtp;

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

import java.io.*;
import java.net.http.*;
import java.time.*;
import java.util.Properties;
import java.util.concurrent.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import dev.failsafe.*;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SMTPClient {

    private String user;
    private String password;
    private Properties properties;

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(15);

    private static final int RETRY_DELAY_SECONDS = 60;

    private static final int MAX_RETRY_ATTEMTPS = 5;

    /**
     *
     */
    public SMTPClient() {
	this(//
		System.getProperty("smtpHost"), //
		System.getProperty("smtpPort"), //
		System.getProperty("smtpUser"), //
		System.getProperty("smtpPassword"));
    }

    /**
     * @param host
     * @param port
     * @param user
     * @param password
     */
    public SMTPClient(String host, String port, String user, String password) {

	if (host == null || port == null) {
	    throw new IllegalArgumentException("SMTP host and port should be specified at least");
	}

	this.user = user;
	this.password = password;

	properties = new Properties();

	properties.put("mail.smtp.auth", "true");
	properties.put("mail.smtp.port", port);
	properties.put("mail.smtp.host", host);

	properties.put("mail.smtp.connectiontimeout", String.valueOf(TIMEOUT)); 
	properties.put("mail.smtp.timeout", String.valueOf(TIMEOUT));
	properties.put("mail.smtp.writetimeout", String.valueOf(TIMEOUT));

	enableSSL(true);
    }

    /**
     * @param enable
     */
    public void enableSTARTTLS(boolean enable) {

	properties.put("mail.smtp.starttls.enable", String.valueOf(enable));
    }

    /**
     * @param enable
     */
    public void enableSSL(boolean enable) {

	properties.put("mail.smtp.ssl.enable", String.valueOf(enable));
    }

    /**
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String subject, String message, String... recipients) throws AddressException, MessagingException {

	for (String recipient : recipients) {

	    Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

		protected PasswordAuthentication getPasswordAuthentication() {

		    return new PasswordAuthentication(user, password);
		}
	    });

	    session.setDebug(false);

	    MimeMessage mimeMessage = new MimeMessage(session);

	    mimeMessage.setFrom(new InternetAddress(user));

	    mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

	    mimeMessage.setSubject(subject);

	    mimeMessage.setText(message);

	    GSLoggerFactory.getLogger(getClass()).trace("Sending email STARTED");

	    RetryPolicy<Boolean> retryPolicy = RetryPolicy.<Boolean> //
		    builder().//
		    handleResultIf(sent -> !sent).//
		    withDelay(Duration.ofSeconds(RETRY_DELAY_SECONDS)).//
		    withMaxAttempts(MAX_RETRY_ATTEMTPS).//
		    onRetry(e -> {
		GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
	    }).//
		    onRetriesExceeded(e -> {
		GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
	    }).//
		    build();

	    Boolean sent = Failsafe.with(retryPolicy).get(() -> send(mimeMessage));

	    GSLoggerFactory.getLogger(getClass()).trace("Sending email ENDED");
	}
    }

    /**
     * @param mimeMessage
     * @return
     */
    private boolean send(MimeMessage mimeMessage) {

	try {
	    Transport.send(mimeMessage);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage());
	    return false;
	}

	return true;
    }

}
