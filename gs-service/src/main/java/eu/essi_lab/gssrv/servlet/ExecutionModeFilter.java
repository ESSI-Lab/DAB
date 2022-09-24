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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.LoggerFactory;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * This class filters out all incoming requests if {@link eu.essi_lab.configuration.ExecutionMode} is {@link
 * eu.essi_lab.configuration.ExecutionMode#BATCH}
 *
 * @author ilsanto
 */
public class ExecutionModeFilter implements Filter {

    private ExecutionMode mode;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
	mode = GIProjectExecutionMode.getMode();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain)
	    throws IOException, ServletException {

	GSLoggerFactory.getLogger(ProfilerServiceFilter.class).trace("Executing filter {}", this);

	if (mode.equals(ExecutionMode.BATCH)) {

	    LoggerFactory.getLogger(ExecutionModeFilter.class).error("Received incoming request altough I'm running in batch mode");

	    return;
	}

	filterChain.doFilter(servletRequest, response);
    }

    @Override
    public void destroy() {

	// nothing to do here
    }
}
