package eu.essi_lab.gssrv.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author ilsanto
 */
public class CORSDelegatorFilter implements Filter {

    private Filter delegated;

    @Override
    public void init(FilterConfig arg0) throws ServletException {

	GSLoggerFactory.getLogger(getClass()).info("Initializing filter {}", this);

	try {

	    Class<?> delegatedClass = Class.forName("org.apache.catalina.filters.CorsFilter");

	    delegated = (Filter) delegatedClass.newInstance();

	    delegated.init(arg0);

		GSLoggerFactory.getLogger(getClass()).info("Initialized CORS filter: {}", delegated);

	} catch (Exception e) {

	    try {

		Class<?> delegatedClass = Class.forName("org.eclipse.jetty.servlets.CrossOriginFilter");

		delegated = (Filter) delegatedClass.newInstance();

		delegated.init(arg0);

		GSLoggerFactory.getLogger(getClass()).info("Initialized CORS filter: {}", delegated);

	    } catch (Exception e2) {

		if (e2 instanceof ClassNotFoundException) {

		    GSLoggerFactory.getLogger(getClass()).error(e2.getMessage());
		    return;
		}

		GSLoggerFactory.getLogger(getClass()).error(e2.getMessage(), e2);
	    }
	}
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {

	if (delegated != null) {
	    delegated.doFilter(arg0, arg1, arg2);
	} else {
	    arg2.doFilter(arg0, arg1);
	}
    }

    @Override
    public void destroy() {
	if (delegated != null) {
	    delegated.destroy();
	}
    }

}
