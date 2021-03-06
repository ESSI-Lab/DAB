package eu.essi_lab.gssrv.starter;

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
import java.util.List;
import java.util.Map;

import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.ProfilerConfigurable;
public class ProfilerConfigurableComposed extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    /**
     * 
     */
    private static final long serialVersionUID = -7638876215497937302L;

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public ProfilerConfigurableComposed() {

	setLabel("Profilers");
	setKey(GSConfiguration.PROFILERS_KEY);

	PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);

	for (Profiler profiler : profilers) {

	    ProfilerConfigurable configurable = profiler.getConfigurable();

	    getConfigurableComponents().put(configurable.getKey(), configurable);
	}
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {

    }
}
