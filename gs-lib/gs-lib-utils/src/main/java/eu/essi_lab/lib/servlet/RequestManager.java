package eu.essi_lab.lib.servlet;

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

import java.util.concurrent.ConcurrentHashMap;

public class RequestManager {

    private static RequestManager instance = new RequestManager();

    private RequestManager() {
    }

    public static RequestManager getInstance() {
	return instance;
    }

    private ConcurrentHashMap<String, RequestInfo> infos = new ConcurrentHashMap<>();

    public RequestInfo getRequestInfo(String requestId) {
	return infos.get(requestId);
    }

    private RequestInfo retrieveRequestInfo(String requestId) {
	RequestInfo info;
	synchronized (infos) {
	    info = infos.get(requestId);
	    if (info == null) {
		info = new RequestInfo(requestId);
		infos.put(requestId, info);
	    }
	}
	return info;
    }

    public void addThreadName(String requestId) {
	RequestInfo info = retrieveRequestInfo(requestId);
	info.addThreadName();
    }

    public void addThreadName(String requestId, String threadName) {
	RequestInfo info = retrieveRequestInfo(requestId);
	info.addThreadName(threadName);
    }

    public void removeRequest(String requestId) {
	synchronized (infos) {
	    infos.remove(requestId);
	}
    }

    public void printRequestInfo(String requestId) {
	RequestInfo info = infos.get(requestId);
	if (info != null) {
	    info.printLogQuery();
	}
    }

}
