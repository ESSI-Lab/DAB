package eu.essi_lab.gssrv.credentials;

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

public class CredentialsConfigurable extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public static final String CREDENTIALS = "CREDENTIALS";
    public static final String SENTINEL_USER = "SENTINEL_USER";
    public static final String SENTINEL_PASSWORD = "SENTINEL_PASSWORD";
    public static final String SENTINEL_DOWNLOADER_TOKEN = "SENTINEL_DOWNLOADER_TOKEN";
    public static final String NVE_TOKEN = "NVE_TOKEN";
    public static final String DINAGUA_USER = "DINAGUA_USER";
    public static final String DINAGUA_USER_TOKEN = "DINAGUA_USER_TOKEN";
    public static final String DINAGUA_PASSWORD = "DINAGUA_PASSWORD";
    public static final String INUMET_USER = "INUMET_USER";
    public static final String INUMET_PASSWORD = "INUMET_PASSWORD";

    @Override
    public String getLabel() {
	return "Credentials settings";
    }

    public CredentialsConfigurable() {

	setKey(CREDENTIALS);

	addStringOption(SENTINEL_USER, "The username used by Sentinel connector and mapper");
	addStringOption(SENTINEL_PASSWORD, "The password used by Sentinel connector and mapper");
	addStringOption(SENTINEL_DOWNLOADER_TOKEN, "The token used by Sentinel downloader for generating previews");
	addStringOption(NVE_TOKEN, "The token used by the Norwegian Water Resources and Energy Directorat connector and downloader");
	addStringOption(DINAGUA_USER, "The username used by the DINAGUA connector and downloader");
	addStringOption(DINAGUA_USER_TOKEN, "The token used by the DINAGUA connector and downloader");
	addStringOption(DINAGUA_PASSWORD, "The password used by the DINAGUA connector and downloader");
	addStringOption(INUMET_USER, "The username used by the INUMET connector and downloader");
	addStringOption(INUMET_PASSWORD, "The password used by the INUMET connector and downloader");
	

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
	    GSLoggerFactory.getLogger(getClass()).info("Setting credentials: {} {}", opt.getKey(), value);
	}

    }

    @Override
    public void onFlush() throws GSException {
	// TODO Auto-generated method stub

    }

}
