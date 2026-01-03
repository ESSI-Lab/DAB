package eu.essi_lab.pdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;
import java.util.List;
import java.util.Set;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class BouncerTool {

    private RedisTool tool;

    public BouncerTool(RedisTool tool) {
	this.tool = tool;
    }

    public List<String> getRequests(String hash) {
	return tool.getListMembers(hash + "." + DistributedRequestBouncer.REQUESTS_KEY);
    }

    public List<String> getExecutingRequests(String hash) {
	return tool.getListMembers(hash + "." + DistributedRequestBouncer.EXECUTING_KEY);
    }

    public Set<String> getHosts() {
	return tool.getSetMembers(DistributedRequestBouncer.HOSTS_KEY);
    }

    public Date getHostInformation(String hostname) {
	String ret = tool.getValue(DistributedRequestBouncer.HOST_KEY + hostname);
	if (ret == null) {
	    return null;
	}
	return ISO8601DateTimeUtils.parseISO8601ToDate(ret).get();
    }

    public String getRequestIp(String hash, String requestId) {
	String key = hash + ".request#" + requestId;
	return  tool.getHashValue(key, DistributedRequestBouncer.REQUEST_IP_PROPERTY);
    }
    
    public String getRequestHostname(String hash, String requestId) {
	String key = hash + ".request#" + requestId;
	return  tool.getHashValue(key, DistributedRequestBouncer.REQUEST_HOSTNAME_PROPERTY);
    }
    
    public String getRequestDatestamp(String hash, String requestId) {
	String key = hash + ".request#" + requestId;
	return  tool.getHashValue(key, DistributedRequestBouncer.REQUEST_DATESTAMP_PROPERTY);
    }
    
    
    public BouncerRequest getRequest(String hash, String requestId) {
	String key = hash + ".request#" + requestId;
	String ip = tool.getHashValue(key, DistributedRequestBouncer.REQUEST_IP_PROPERTY);
	String hostname = tool.getHashValue(key, DistributedRequestBouncer.REQUEST_HOSTNAME_PROPERTY);
	String datestamp = tool.getHashValue(key, DistributedRequestBouncer.REQUEST_DATESTAMP_PROPERTY);
	if (ip == null || hostname == null || datestamp == null) {
	    return null;
	}
	BouncerRequest req = new BouncerRequest(ip, hostname, datestamp, requestId);
	return req;
    }

    public void close() {
	tool.close();
    }

}
