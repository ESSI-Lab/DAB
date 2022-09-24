package eu.essi_lab.gssrv.statistics;

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

public class StatisticsConfigurable extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public static final String GI_STATS = "GI_STATS";
    public static final String GI_STATS_ENDPOINT = "GI_STATS_ENDPOINT";
    public static final String GI_STATS_DB_NAME = "GI_STATS_DB_NAME";
    public static final String GI_STATS_USER = "GI_STATS_USER";
    public static final String GI_STATS_PASSWORD = "GI_STATS_PASSWORD";

    @Override
    public String getLabel() {
	return "GI-stats settings";
    }

    public StatisticsConfigurable() {

	setKey(GI_STATS);

	addStringOption(GI_STATS_ENDPOINT, "The Elasticsearch endpoint where the statistics are stored");
	addStringOption(GI_STATS_DB_NAME, "Elasticsearch db name (prefix for GI-stats indexes)");
	addStringOption(GI_STATS_USER, "Elasticsearch username");
	addStringOption(GI_STATS_PASSWORD, "Elasticsearch password");


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
	    GSLoggerFactory.getLogger(getClass()).info("Setting GI-stats setting: {} {}", opt.getKey(), value);
	}

    }

    @Override
    public void onFlush() throws GSException {
	// TODO Auto-generated method stub

    }

}
