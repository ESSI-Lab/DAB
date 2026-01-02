package eu.essi_lab.gssrv.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;

/**
 * A filter which blocks the request and returns a 404 error code in case the request path owns to a offline profiler
 *
 * @author Fabrizio
 */
public class ProfilerServiceFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

	HttpServletRequest httpRequest = (HttpServletRequest) request;

//	WebRequest webRequest = new WebRequest(httpRequest, false);
//
//	boolean fromPresent = webRequest.readFromHeader().//
//		filter(header -> header.toLowerCase().contains("bingbot") || header.toLowerCase().contains("microsoft")).//
//		isPresent();
//
//	boolean agentPresent = webRequest.readUserAgentHeader().//
//		filter(header -> header.toLowerCase().contains("microsoftpreview") || header.toLowerCase().contains("skypeuripreview")).//
//		isPresent();
//
//	if (fromPresent || agentPresent) {
//
//	    HttpServletResponse httpResponse = (HttpServletResponse) response;
//	    httpResponse.setStatus(404);
//
//	    return;
//	}

	String pathInfo = httpRequest.getPathInfo(); // e.g: /essi/oaipmh or /essi/opensearch/query

	Optional<Boolean> online = pathInfo == null ? Optional.empty() : ConfigurationWrapper.isProfilerOnline(pathInfo);

	if (online.isEmpty() || online.isPresent() && online.get()) {

	    filterChain.doFilter(request, response);

	} else {

	    HttpServletResponse httpResponse = (HttpServletResponse) response;
	    httpResponse.setStatus(404);
	}
    }

    public void destroy() {
    }
}
