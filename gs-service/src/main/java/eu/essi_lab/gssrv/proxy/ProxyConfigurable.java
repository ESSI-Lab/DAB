package eu.essi_lab.gssrv.proxy;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;

public class ProxyConfigurable extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public static final String GI_PROXY = "GI_PROXY";
    public static final String GI_PROXY_ENDPOINT = "GI_PROXY_ENDPOINT";

    @Override
    public String getLabel() {
	return "GI-proxy settings";
    }

    public ProxyConfigurable() {

	setKey(GI_PROXY);

	addStringOption(GI_PROXY_ENDPOINT, "The GI-proxy endpoint used by accessors and downloaders (e.g. to circumvent firewalls)");

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
	    GSLoggerFactory.getLogger(getClass()).info("Setting GI-proxy setting: {} {}", opt.getKey(), value);
	}

    }

    @Override
    public void onFlush() throws GSException {
	// TODO Auto-generated method stub

    }

}
