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
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class InitializationFilter implements Filter {

    private String confFile = "conf/initfilter.properties";
    private Set<String> excludedPaths = new HashSet<>();
    private boolean alreadyInitialized = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

	Properties props = new Properties();

	try (InputStream stream = InitializationFilter.class.getClassLoader().getResourceAsStream(confFile)) {

	    props.load(stream);

	    props.values().forEach(value -> excludedPaths.add((String) value));

	} catch (IOException e) {

	    LoggerFactory.getLogger(InitializationFilter.class).warn("Unable to load properties from {}", confFile, e);
	}

	return;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	GSLoggerFactory.getLogger(ProfilerServiceFilter.class).trace("Executing filter {}", this);

	if (isInitPath((HttpServletRequest) request) || gsInitialized()) {

	    chain.doFilter(request, response);

	    return;

	}

	((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "GI-suite has not been initialized yet");
    }

    private boolean isInitPath(HttpServletRequest request) {

	String uri = request.getRequestURI();

	return excludedPaths.stream().anyMatch(uri::contains);

    }

    GSConfigurationManager getManager() throws GSException {
	return new GSConfigurationManager();
    }

    private boolean gsInitialized() {

	if (alreadyInitialized)
	    return true;

	try {

	    GSConfigurationManager manager = getManager();

	    alreadyInitialized = manager.getConfiguration() != null;

	    return alreadyInitialized;

	} catch (GSException e) {

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

	return false;
    }

    @Override
    public void destroy() {
    }

    Set<String> getExcludedPaths() {
	return excludedPaths;
    }

}
