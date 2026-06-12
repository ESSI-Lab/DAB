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

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Prevents long-lived browser caching of portal HTML/JS/CSS/JSON assets.
 */
public class PortalAssetCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
	    throws IOException, ServletException {

	HttpServletRequest request = (HttpServletRequest) servletRequest;
	HttpServletResponse response = (HttpServletResponse) servletResponse;

	if (!UserFinderFilter.isVaadinRequest(request) && isPortalStaticAsset(request.getRequestURI())) {
	    applyNoCacheHeaders(response);
	}

	filterChain.doFilter(servletRequest, servletResponse);
    }

    static boolean isPortalStaticAsset(String requestUri) {
	if (requestUri == null || requestUri.isEmpty()) {
	    return false;
	}

	String lower = requestUri.toLowerCase();

	if (lower.contains("/services/") || lower.contains("/configuration/") || lower.contains("/auth/")
		|| lower.contains("/mcp")) {
	    return false;
	}

	if (lower.endsWith("/search") || lower.endsWith("/search.html")) {
	    return true;
	}

	return lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".json") || lower.endsWith(".html");
    }

    static void applyNoCacheHeaders(HttpServletResponse response) {
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
    }

    @Override
    public void destroy() {
    }
}
