package eu.essi_lab.pdk.handler.selector;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.messages.web.WebRequest;

/**
 * When a path is set, it <b>MUST NOT</b> include:
 * <ul>
 * <li>the base path (e.g: 'gs-service/services/essi')</li>
 * <li>the user token 'token' path (and its value)</li>
 * <li>the view 'view' path (and its value)</li>
 * <li>initial '/'</li>
 * <li>trailing '/'</li>
 * </ul>
 * It is compared with {@link WebRequest#getRequestPath()}, current request path which follows the base path, deprived
 * of the last '/' and deprived of the 'token' and 'view' path segments and values. This means that user tokens and
 * views are <i>never</i>
 * included in the comparison.<br>
 * The comparison is done by comparing the path segments of both paths at the same position (that is
 * acceptPath.segment[i] with requestPath.segment[i]).<br>
 * If the paths have the same number of segments, and they match according to their position, the request path is
 * accepted.<br>
 * The accepted path can also include character '*'.<br>
 * E.g.:
 * <ul>
 * <li>accepted path: opensearch/description</li>
 * <li>request path: http://localhost/gs-service/services/essi/opensearch/description</li>
 * </ul>
 * The path can end with the '*' character to indicate a path parameter, e.g.: 'csw/pubsub/subscription/*'. In this case
 * it is compared with
 * the current request path which follows the base path deprived of the last path segment and of the last '/'. E.g:
 * <ul>
 * <li>set path: csw/pubsub/subscription/*</li>
 * <li>current path: http://localhost/gs-service/services/essi/csw/pubsub/subscription/ko83fd</li>
 * <li>comparison path: csw/pubsub/subscription</li>
 * </ul>
 *
 * @author Fabrizio
 */
public abstract class PathRequestFilter implements WebRequestFilter {

    private String expectedPath;
    private String servicesPath;

    protected PathRequestFilter() {
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     */
    public PathRequestFilter(String path) {

	setPath(path);
    }

    /**
     * Set the path where the filter accepts the requests.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     */
    public void setPath(String path) {

	this.expectedPath = path;
    }

    /**
     * Overrides the services path provided by {@link WebRequest#getServicesPath()}.<br>
     * 
     * @param servicesPath
     */
    public void overrideServicesPath(String servicesPath) {

	this.servicesPath = servicesPath;
    }

    /**
     * Returns an {@link Optional} of {@link Boolean} which is empty if no path is set and
     * {@link WebRequest#getRequestPath()} is
     * <code>null</code>, otherwise returns <code>true</code> according to the description provided in the constructor
     */
    protected Optional<Boolean> acceptPath(WebRequest webRequest) {

	//
	// overrides the default services path
	//
	if (this.servicesPath != null) {

	    webRequest.setServicesPath(this.servicesPath);
	}

	String requestPath = webRequest.getRequestPath();

	if (expectedPath != null && requestPath != null) {

	    List<String> expPaths = Arrays.asList(expectedPath.split("/")).//
		    stream().//
		    filter(s -> !s.isEmpty()).// removes the initial /
		    collect(Collectors.toList());

	    List<String> reqPaths = new ArrayList<String>(Arrays.asList(requestPath.split("/")));

	    //
	    // 1) replaces the 'token' and the 'view' segments and their values with a # char
	    // that will be removed
	    //
	    List<String> reqPaths_ = new ArrayList<>(reqPaths);

	    for (int i = 0; i < reqPaths_.size(); i++) {

		String reqPath = reqPaths_.get(i);
		if (reqPath.equals("#")) {
		    continue;
		}

		if (reqPath.equals("token") || reqPath.equals("view")) {

		    reqPaths.set(i, "#");
		    reqPaths.set(i + 1, "#");
		}
	    }

	    reqPaths = reqPaths.stream().filter(v -> !v.equals("#")).collect(Collectors.toList());

	    //
	    // 2) segments length check
	    //

	    if (expPaths.size() != reqPaths.size()) {

		return Optional.of(false);
	    }

	    //
	    // 3) segments check
	    //

	    boolean accepts = true;

	    for (int i = 0; i < expPaths.size(); i++) {

		String expPath = expPaths.get(i);
		String reqPath = reqPaths.get(i);

		accepts &= expPath.equals(reqPath) || expPath.equals("*");
	    }

	    return Optional.of(accepts);
	}

	return Optional.empty();
    }
}
