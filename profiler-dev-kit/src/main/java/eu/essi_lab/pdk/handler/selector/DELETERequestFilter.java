package eu.essi_lab.pdk.handler.selector;

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

import eu.essi_lab.messages.web.WebRequest;

/**
 * Implementation of {@link WebRequestFilter} specific for {@link WebRequest} with DELETE method.<br>
 * When a path is set, it is compared with the current request path which follows from the profiler path, not including
 * the '/'. E.g.: http://localhost/gs-service/services/essi/opensearch/description -> opensearch/description
 * 
 * @see WebRequest#isGetRequest()
 * @see WebRequest#isPostRequest()
 * @author Fabrizio
 */
public class DELETERequestFilter extends GETRequestFilter {

    /**
     * Creates an undefined filter
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @see #GETRequestFilter(String)
     */
    public DELETERequestFilter() {

	super();
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @see #setPath(String)
     * @param queryString a non <code>null</code> string representing the query part of a GET request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public DELETERequestFilter(String queryString, InspectionStrategy strategy) {

	super(queryString, strategy);
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * The path is compared with the current request path which follows from the profiler path, not including
     * the '/'. E.g.: http://localhost/gs-service/services/essi/opensearch/description -> opensearch/description
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     */
    public DELETERequestFilter(String path) {

	super(path);
    }

    /**
     * Creates a filter with a new condition defined by the supplied <code>queryString</code> to compare with
     * {@link WebRequest#getQueryString()} according to the given <code>strategy</code>. The filter accepts only
     * requests on the supplied <code>path</code>.<br>
     * The path is compared with the current request path which follows from the profiler path, not including
     * the '/'. E.g.: http://localhost/gs-service/services/essi/opensearch/description -> opensearch/description
     * 
     * @see #addQueryCondition(String, InspectionStrategy)
     * @param path the request path
     * @param queryString a non <code>null</code> string representing the query part of a GET request which is compared
     *        with {@link WebRequest#getQueryString()}
     *        according to the given <code>strategy</code>
     * @param strategy a non <code>null</code> {@link InspectionStrategy}
     */
    public DELETERequestFilter(String path, String queryString, InspectionStrategy strategy) {

	super(path, queryString, strategy);
    }

    /**
     * @param request
     * @return
     */
    protected boolean isSupported(WebRequest request) {

	return request.isDeleteRequest();
    }
}
