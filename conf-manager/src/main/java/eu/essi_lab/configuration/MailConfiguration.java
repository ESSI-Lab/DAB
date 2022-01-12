package eu.essi_lab.configuration;

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

public class MailConfiguration {

    private String smtpHost;

    private String smtpPort;

    private String smtpUser;

    private String smtpPassword;

    private String eMailRecipients;

    public String getSmtpHost() {
	return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
	this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
	return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
	this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
	return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
	this.smtpUser = smtpUser;
    }

    public String getSmtpPassword() {
	return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
	this.smtpPassword = smtpPassword;
    }

    public String geteMailRecipients() {
	return eMailRecipients;
    }

    public void seteMailRecipients(String eMailRecipients) {
	this.eMailRecipients = eMailRecipients;
    }

    @Override
    public String toString() {
	String ret = "";

	if (smtpHost != null) {
	    ret += " smtpHost: " + smtpHost;
	}

	if (smtpPort != null) {
	    ret += " smtpPort: " + smtpPort;
	}

	if (smtpUser != null) {
	    ret += " smtpUser: " + smtpUser;
	}

	if (smtpPassword != null) {
	    ret += " smtpPassword: " + smtpPassword;
	}

	if (eMailRecipients != null) {
	    ret += " eMailRecipients: " + eMailRecipients;
	}

	return "[" + ret + "]";
    }

}
