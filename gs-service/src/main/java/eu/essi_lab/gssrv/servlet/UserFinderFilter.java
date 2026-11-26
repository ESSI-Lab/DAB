package eu.essi_lab.gssrv.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import eu.essi_lab.authorization.*;
import eu.essi_lab.authorization.userfinder.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.auth.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;

/**
 * @author Fabrizio
 */
public class UserFinderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	HttpServletRequest httpRequest = (HttpServletRequest) request;

	if (isVaadinRequest(httpRequest)) {

	    chain.doFilter(request, response);
	    return;
	}

	GSUser user = BasicRole.createAnonymousUser();

	try {

	    user = UserFinder.findCurrentUser(httpRequest);

	} catch (InvalidBearerTokenException e) {

	    sendInvalidToken((HttpServletResponse) response, e.getMessage());
	    return;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	WebRequest.setCurrentUser(user, httpRequest);

	chain.doFilter(request, response);
    }

    /**
     * Replies with HTTP 401, as defined by RFC 6750 for invalid bearer tokens
     *
     * @param response
     * @param reason
     * @throws IOException
     */
    static void sendInvalidToken(HttpServletResponse response, String reason) throws IOException {

	String description = reason == null ? "Invalid token" : reason.replaceAll("[\"\\\\\\r\\n]", " ").trim();

	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"" + description + "\"");
	response.setHeader("Cache-Control", "no-store");
	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");

	response.getWriter().write("{\"error\":\"invalid_token\",\"error_description\":\"" + description + "\"}");
    }

    /**
     * @param httpRequest
     * @return
     */
    static boolean isVaadinRequest(HttpServletRequest httpRequest) {

	String queryString = httpRequest.getQueryString();
	String requestURI = httpRequest.getRequestURI();

	return (queryString != null && queryString.contains("v-r=")) || //
		requestURI.contains("VAADIN") || //
		requestURI.contains("sw.js") || //
		requestURI.contains("sw-runtime-resources-precache.js");

    }

    @Override
    public void destroy() {
    }
}
