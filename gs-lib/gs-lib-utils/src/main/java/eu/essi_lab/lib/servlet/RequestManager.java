package eu.essi_lab.lib.servlet;

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

public class RequestManager {

    private static RequestManager instance = new RequestManager();

    private RequestManager() {
    }

    /**
     * @return
     */
    public static RequestManager getInstance() {
	return instance;
    }

    /**
     * @param callerClass
     * @param requestId
     */
    public void updateThreadName(Class<?> callerClass, String requestId) {

	if (requestId.contains("@")) {

	    requestId = requestId.substring(requestId.indexOf("@") + 1, requestId.length());
	}

	String name = callerClass.getSimpleName() + "@" + requestId;

	Thread.currentThread().setName(name);
    }
}
