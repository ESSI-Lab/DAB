package eu.essi_lab.lib.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class RequestInfo {

    private String requestId;
    private Set<String> threadNames;
    private Date start;

    /**
     * @param requestId
     */
    public RequestInfo(String requestId) {
	this.threadNames = new HashSet<>();
	this.requestId = requestId;
	this.start = new Date();
	addThreadName();
    }

    public Set<String> getThreadNames() {
	return new HashSet<String>(threadNames);
    }

    public Date getStart() {
	return start;
    }

    public void setStart(Date start) {
	this.start = start;
    }

    public void addThreadName() {
	addThreadName(Thread.currentThread().getName());
    }

    public void addThreadName(String threadName) {
	boolean print = false;
	synchronized (threadNames) {
	    int size = threadNames.size();
	    threadNames.add(threadName);
	    int newSize = threadNames.size();
	    if (newSize > size) {
		print = true;
	    }
	}
	if (print) {
	    printLogQuery();
	}
    }

    public String getLogQuery() {

	String aws = "AWS Logs Insights\n";
	aws += "fields @message\n";

	String sublime = "Sublime\n";

	int items = 0;

	synchronized (threadNames) {
	    Iterator<String> iterator = threadNames.iterator();
	    while (iterator.hasNext()) {
		items++;
		String threadName = (String) iterator.next();
		String msg = "@message like \"[" + threadName + "]\"";
		String sublimeChild = "\\[" + threadName + "\\]";
		if (items == 1) {
		    aws += "| filter " + msg;
		    sublime += sublimeChild;
		} else {
		    aws += " or " + msg;
		    sublime += " | " + sublimeChild;
		}
	    }
	}

	if (items > 0) {
	    aws += "\n";
	    sublime += "\n";
	}

	String hostname = "";
	try {
	    hostname = InetAddress.getLocalHost().getHostName();
	    hostname = "| filter @message like \"" + hostname + "\"\n"; //
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	}

	aws += "| filter toMillis(@timestamp) > " + start.getTime() + "\n" + //
	// "| filter toMillis(@timestamp) < " + new Date().getTime() + "\n" + //
		hostname + //
		"| sort @timestamp asc\n" + //
		"| limit 2000";

	String ret = "[LOG_QUERY][" + items + "] for request: " + requestId + "\n";
	ret += "\n";
	ret += aws;
	ret += "\n";
	ret += sublime;
	ret = ret.replace("\n", " ");
	return ret;
    }

    /**
     * 
     */
    public void printLogQuery() {

//	GSLoggerFactory.getLogger(getClass()).info(getLogQuery());
    }
}
