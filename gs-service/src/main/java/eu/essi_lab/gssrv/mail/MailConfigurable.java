package eu.essi_lab.gssrv.mail;

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

import java.util.HashMap;
import java.util.Map;

import eu.essi_lab.harvester.GSMailSenderHarvesting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.driver.es.stats.GSMailSenderElasticsearch;

public class MailConfigurable extends AbstractGSconfigurableComposed implements IGSMainConfigurable {
    private static final long serialVersionUID = -7091478348973867072L;

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public static final String E_MAIL = "E_MAIL";

    private static final String SMTP_HOST = "SMTP_HOST";

    private static final String SMTP_PORT = "SMTP_PORT";

    private static final String SMTP_USER = "SMTP_USER";

    private static final String SMTP_PASSWORD = "SMTP_PASSWORD";

    private static final String E_MAIL_RECIPIENTS = "E_MAIL_RECIPIENTS";

    @Override
    public String getLabel() {
	return "E-mail settings";
    }

    public MailConfigurable() {

	setKey(E_MAIL);

	addStringOption(SMTP_HOST, "The SMTP host used by the DAB to send e-mail messages");
	addStringOption(SMTP_PORT, "The SMTP port used by the DAB to send e-mail messages");
	addStringOption(SMTP_USER, "The SMTP user used by the DAB to send e-mail messages");
	addStringOption(SMTP_PASSWORD, "The SMTP password used by the DAB to send e-mail messages");
	addStringOption(E_MAIL_RECIPIENTS, "The e-mail recipients for DAB mail notifications, comma separated");

    }

    private void addStringOption(String key, String label) {
	GSConfOptionString option = new GSConfOptionString();
	option.setKey(key);
	option.setLabel(label);
	getSupportedOptions().put(key, option);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	String value = opt.getValue() == null ? null : opt.getValue().toString();
	if (value != null && !value.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Setting mail setting: {} {}", opt.getKey(), value);
	    switch (opt.getKey()) {
	    case E_MAIL_RECIPIENTS:
		GSMailSenderHarvesting.RECIPIENTS = value;
		GSMailSenderElasticsearch.RECIPIENTS = value;
		break;
	    case SMTP_HOST:
		GSMailSenderHarvesting.SMTP_HOST = value;
		GSMailSenderElasticsearch.SMTP_HOST = value;
		break;
	    case SMTP_PORT:
		GSMailSenderHarvesting.SMTP_PORT = value;
		GSMailSenderElasticsearch.SMTP_PORT = value;
		break;
	    case SMTP_USER:
		GSMailSenderHarvesting.SMTP_USER = value;
		GSMailSenderElasticsearch.SMTP_USER = value;
		break;
	    case SMTP_PASSWORD:
		GSMailSenderHarvesting.SMTP_PASSWORD = value;
		GSMailSenderElasticsearch.SMTP_PASSWORD = value;
		break;
	    default:
		break;
	    }
	    GSMailSenderHarvesting.printMailParameters();
	    GSMailSenderElasticsearch.printMailParameters();
	}

    }

    @Override
    public void onFlush() throws GSException {
	// TODO Auto-generated method stub

    }

}
