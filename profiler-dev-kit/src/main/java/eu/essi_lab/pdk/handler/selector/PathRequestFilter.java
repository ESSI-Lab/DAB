package eu.essi_lab.pdk.handler.selector;

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

import java.util.Optional;

import eu.essi_lab.messages.web.WebRequest;
public abstract class PathRequestFilter implements WebRequestFilter {

    private String expectedPath;

    protected PathRequestFilter() {
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     */
    public PathRequestFilter(String path) {

	setPath(path, false);
    }

    /**
     * Creates a filter which accepts only requests on the supplied <code>path</code>.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     * @param view if <code>true</code> the provided path will be accepted also if preceded by the 'view/viewId/' path
     */
    public PathRequestFilter(String path, boolean view) {

	setPath(path, view);
    }

    /**
     * Set the path where the filter accepts the requests.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     */
    public void setPath(String path) {

	setPath(path, false);
    }

    /**
     * Set the path where the filter accepts the requests.<br>
     * See constructor docs for path usage info
     *
     * @param path the request path
     * @param view if <code>true</code> the provided path will be accepted also if preceded by the 'view/viewId/' path
     */
    public void setPath(String path, boolean view) {

	this.expectedPath = path;
	if (view) {
	    this.expectedPath = "view/*/" + path;
	}
    }

    /**
     * Returns an {@link Optional} of {@link Boolean} which is empty if no path is set and
     * {@link WebRequest#getRequestPath()} is
     * <code>null</code>, otherwise returns <code>true</code> according to the description provided in the constructor
     */
    protected Optional<Boolean> acceptPath(WebRequest webRequest) {

	String requestPath = webRequest.getRequestPath();

	if (expectedPath != null && requestPath != null) {

	    String expectedPath_ = new String(expectedPath);
	    String viewId = null;

	    if (expectedPath_.startsWith("view")) {

		String[] reqSplit = requestPath.split("/");
		String[] expSplit = expectedPath_.split("/");

		if (reqSplit.length == expSplit.length) {

		    for (int i = 0; i < reqSplit.length; i++) {

			String reqPath = reqSplit[i];
			String expPath = expSplit[i];

			if (expPath.equals("*") || reqPath.equals(expPath)) {
			    if (expPath.equals("*") && viewId == null) {
				viewId = reqPath;
			    }
			    // OK!

			} else {
			    return Optional.of(false);
			}
		    }

		    expectedPath_ = expectedPath_.replace("view/*/", "view/" + viewId + "/");

		} else {

		    return Optional.of(false);
		}
	    }

	    if (expectedPath_.equals("*")) {
		return Optional.of(true);
	    } else if (expectedPath_.endsWith("/*")) {

		String expectedPathTruncated = expectedPath_.replace("/*", "");

		String requestPathTruncated = requestPath;

		if (requestPath.endsWith("/")) {
		    requestPathTruncated = requestPath.substring(0, requestPath.length() - 1);
		}

		if (requestPathTruncated.startsWith(expectedPathTruncated) && !requestPathTruncated.equals(expectedPathTruncated)) {

		    return Optional.of(true);
		}

		return Optional.of(false);

	    } else if (requestPath.equals(expectedPath_) //

		    || expectedPath_.endsWith("/") && requestPath.equals(expectedPath_.substring(0, expectedPath_.length() - 1))
		    || requestPath.endsWith("/") && expectedPath_.equals(requestPath.substring(0, requestPath.length() - 1))

	    ) {

		return Optional.of(true);
	    }

	    return Optional.of(false);
	}

	return Optional.empty();
    }
}
