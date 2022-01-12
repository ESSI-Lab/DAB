package eu.essi_lab.lib.net.utils;

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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This wrapper allows to put headers to the request
 * 
 * @author Fabrizio
 */
public class WritableHTTPServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders;

    /**
     * @param request
     */
    public WritableHTTPServletRequest(HttpServletRequest request) {
	super(request);
	this.customHeaders = new HashMap<String, String>();
    }

    /**
     * @param name
     * @param value
     */
    public void putHeader(String name, String value) {

	this.customHeaders.put(name, value);
    }

    public String getHeader(String name) {
	// check the custom headers first
	String headerValue = customHeaders.get(name);

	if (headerValue != null) {
	    return headerValue;
	}
	// else return from into the original wrapped object
	return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
	// create a set of the custom header names
	Set<String> set = new HashSet<String>(customHeaders.keySet());

	// now add the headers from the wrapped request object
	Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
	while (e.hasMoreElements()) {
	    // add the names of the request headers into the list
	    String n = e.nextElement();
	    set.add(n);
	}

	// create an enumeration from the set and return
	return Collections.enumeration(set);
    }
}
