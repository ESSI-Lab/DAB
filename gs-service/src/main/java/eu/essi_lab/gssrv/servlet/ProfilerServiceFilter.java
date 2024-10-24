package eu.essi_lab.gssrv.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ServiceLoader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.pdk.Profiler;

/**
 * A filter which blocks the request and returns a 404 error code in case the request path owns to a disabled profiler
 *
 * @author Fabrizio
 */
public class ProfilerServiceFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

	HttpServletRequest httpRequest = (HttpServletRequest) request;
	String pathInfo = httpRequest.getPathInfo(); // e.g: /essi/oaipmh

	ServiceLoader<Profiler> profilers = ServiceLoader.load(Profiler.class);

	boolean isProfilerPath = StreamUtils.iteratorToStream(profilers.iterator()).//
		map(p -> p.getSetting()).//
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

	Optional<ProfilerSetting> setting = ConfigurationWrapper.getProfilerSettings().//
		stream().//
		filter(c -> pathInfo.contains(c.getServicePath())).//
		findFirst();

	return (setting.isPresent() && !setting.get().isEnabled());
    }

    public void destroy() {
	// nothing to do here
    }
}
