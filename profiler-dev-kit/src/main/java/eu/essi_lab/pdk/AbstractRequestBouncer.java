package eu.essi_lab.pdk;

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

import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public abstract class AbstractRequestBouncer {
    private int maxConcurrentRequests;
    private int maxConcurrentRequestsPerIP;
    private int maxOverallRequestsPerIp;

    public int getMaximumConcurrentRequests() {
	return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxTotalRequests) {
	this.maxConcurrentRequests = maxTotalRequests;
    }

    public int getMaximumConcurrentRequestsPerIP() {
	return maxConcurrentRequestsPerIP;
    }

    public void setMaxConcurrentRequestsPerIP(int maxRequestsPerIP) {
	this.maxConcurrentRequestsPerIP = maxRequestsPerIP;
    }

    public int getMaximumOverallRequestsPerIp() {
	return maxOverallRequestsPerIp;
    }

    public void setMaximumOverallRequestsPerIp(int overallMaxRequestsPerIp) {
	this.maxOverallRequestsPerIp = overallMaxRequestsPerIp;
    }

    public AbstractRequestBouncer(int maxConcurrentRequests, int maxConcurrentRequestsPerIP, int overallMaxRequestsPerIp) {
	if (maxConcurrentRequestsPerIP > maxConcurrentRequests) {
	    GSLoggerFactory.getLogger(getClass())
		    .error("maximum concurrent requests per IP can't exceed the maximum concurrent requests, reducing");
	    maxConcurrentRequestsPerIP = maxConcurrentRequests;
	}
	this.maxConcurrentRequests = maxConcurrentRequests;
	this.maxConcurrentRequestsPerIP = maxConcurrentRequestsPerIP;
	this.maxOverallRequestsPerIp = overallMaxRequestsPerIp;
    }

    public abstract boolean askForExecutionAndWait(String ipAddress, String requestId, long timeout, TimeUnit unit)
	    throws InterruptedException;

    public abstract boolean notifyExecutionEnded(String ipAddress, String requestId);

    protected String validate(String ipAddress) {
	if (ipAddress == null || ipAddress.isEmpty()) {
	    return "unknown";
	} else {
	    return ipAddress;
	}
    }
}
