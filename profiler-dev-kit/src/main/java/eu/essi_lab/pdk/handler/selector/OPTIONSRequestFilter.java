package eu.essi_lab.pdk.handler.selector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.HashMap;

import eu.essi_lab.messages.web.WebRequest;

/**
 * @author Fabrizio
 */
public class OPTIONSRequestFilter extends GETRequestFilter {
    
    /**
     * Creates an undefined filter
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #addParameterCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @see #GETRequestFilter(String)
     */
    public OPTIONSRequestFilter() {

	queryConditions = new HashMap<>();
	parameterConditions = new HashMap<>();
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @param queryString a non <code>null</code> string representing the query part of a OPTIONS request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public OPTIONSRequestFilter(String queryString, InspectionStrategy strategy) {

	this();
	addQueryCondition(queryString, strategy);
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     */
    public OPTIONSRequestFilter(String path) {

	this();
	setPath(path);
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>. The filter accepts only
     * requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     * @param queryString a non <code>null</code> string representing the query part of a OPTIONS request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public OPTIONSRequestFilter(String path, String queryString, InspectionStrategy strategy) {

	this();
	addQueryCondition(queryString, strategy);
	setPath(path);
    }


    /**
     * @param request
     * @return
     */
    @Override
    protected boolean isSupported(WebRequest request) {

	return request.isOptionsRequest();
    }
}
