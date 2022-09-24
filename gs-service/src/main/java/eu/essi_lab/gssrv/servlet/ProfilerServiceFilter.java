package eu.essi_lab.gssrv.servlet;

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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.configuration.reader.GSConfigurationReader;
import eu.essi_lab.gssrv.starter.ProfilerConfigurableComposed;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.ProfilerConfigurable;
public class ProfilerServiceFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

	GSLoggerFactory.getLogger(ProfilerServiceFilter.class).trace("Executing filter {}", this);

	HttpServletRequest httpRequest = (HttpServletRequest) request;
	String pathInfo = httpRequest.getPathInfo(); // e.g: /essi/oaipmh

	PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);

	boolean isProfilerPath = profilers.//
		stream().//
		map(p -> p.getProfilerInfo()).//
		anyMatch(i -> pathInfo != null && pathInfo.contains(i.getServicePath()));

	if (isProfilerPath && isDisabled(pathInfo)) {
	    
	    HttpServletResponse httpResponse = (HttpServletResponse) response;
	    httpResponse.setStatus(404);

	} else {
	    
	    filterChain.doFilter(request, response);
	    return;
	}
    }

    /**
     * Calls the configuration manager component in order to know if the given path owns to a disabled profiler
     *
     * @param pathInfo
     * @return
     */
    private boolean isDisabled(String pathInfo) {

	try {
	    GSConfigurationReader reader = ConfigurationUtils.createConfigurationReader();

	    GSConfiguration configuration = reader.getConfiguration();

	    Map<String, IGSConfigurable> components = configuration.getConfigurableComponents();

	    ProfilerConfigurableComposed profConf = (ProfilerConfigurableComposed) components.get(GSConfiguration.PROFILERS_KEY);

	    Map<String, IGSConfigurable> configurableComponents = profConf.getConfigurableComponents();

	    Optional<ProfilerConfigurable> configurable = configurableComponents.values().//
		    stream().//
		    map(c -> (ProfilerConfigurable) c).//
		    filter(c -> pathInfo.contains(c.getProfilerPath())).//
		    findFirst();

	    return (configurable.isPresent() && !configurable.get().isProfilerEnabled());

	} catch (GSException e) {

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

	return false;
    }

    public void destroy() {
	// nothing to do here
    }
}
