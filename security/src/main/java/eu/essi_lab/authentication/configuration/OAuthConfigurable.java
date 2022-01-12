package eu.essi_lab.authentication.configuration;

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
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class OAuthConfigurable extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    private static final long serialVersionUID = 6857422095983144674L;
    private Map<String, GSConfOption<?>> supported = new HashMap<>();
    public static final String AUTH_MAIN_COMPONENT_KEY = "AUTH_MAIN_COMPONENT_KEY";
    private transient Logger logger = GSLoggerFactory.getLogger(OAuthConfigurable.class);
    private static final String BAD_AUTHENTICATOR_KEY_ERR_ID = "BAD_AUTHENTICATOR_KEY_ERR_ID";

    public OAuthConfigurable() {
	setKey(AUTH_MAIN_COMPONENT_KEY);

	setLabel("OAuth");

	Iterator<IOAuthAuthenticatorConfigurable> it = getLoaderIterator();

	while (it.hasNext()) {
	    IOAuthAuthenticatorConfigurable authenticator = it.next();

	    String key = getEnableOptionKey(authenticator.getKey());

	    GSConfOptionBoolean opt = new GSConfOptionBoolean();
	    opt.setKey(key);
	    opt.setLabel("Enable " + authenticator.getLabel());

	    getSupportedOptions().put(key, opt);

	}

    }

    @JsonIgnore
    @XmlTransient
    Iterator<IOAuthAuthenticatorConfigurable> getLoaderIterator() {
	return ServiceLoader.load(IOAuthAuthenticatorConfigurable.class).iterator();

    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	logger.debug("Received option {} with value {}", opt.getKey(), opt.getValue());

	IOAuthAuthenticatorConfigurable authenticator = loadAuthenticatorConfigurable(opt.getKey());

	if (getConfigurableComponents().get(getSubcomponentKey(authenticator.getKey())) == null && ((GSConfOptionBoolean) opt).getValue()) {

	    getConfigurableComponents().put(getSubcomponentKey(authenticator.getKey()), authenticator);

	} else {
	    getConfigurableComponents().remove(getSubcomponentKey(authenticator.getKey()));
	}
    }

    IOAuthAuthenticatorConfigurable loadAuthenticatorConfigurable(String key) throws GSException {

	String authKey = getAuthenticatorKeyFromOptionKey(key);

	Iterator<IOAuthAuthenticatorConfigurable> it = getLoaderIterator();

	while (it.hasNext()) {

	    IOAuthAuthenticatorConfigurable authenticator = it.next();

	    if (authenticator.getKey().equalsIgnoreCase(authKey))
		return authenticator;

	}

	throw GSException.createException(this.getClass(), "Can't find authenticator configurable with key " + authKey, null, null,
		ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, BAD_AUTHENTICATOR_KEY_ERR_ID);
    }

    private String getEnableOptionKey(String authKey) {
	return "enable-" + authKey;
    }

    private String getAuthenticatorKeyFromOptionKey(String optKey) {
	return optKey.replace("enable-", "");
    }

    private String getSubcomponentKey(String authKey) {
	return "subcompnent-" + authKey;
    }

    @Override
    public void onFlush() throws GSException {
	//nothing to be done here
    }
}
