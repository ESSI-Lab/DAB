package eu.essi_lab.gssrv.conf.task.bluecloud;

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

public class DownloadInfo {

    
    Integer protocolCount;
    String onlineLinkage;
    String getRecordById;
    String onlineName;
    
    public DownloadInfo(Integer protocolCount, String onlineLinkage, String getRecordById, String onlineName) {
	this.protocolCount = protocolCount;
	this.onlineLinkage = onlineLinkage;
	this.getRecordById = getRecordById;
	this.onlineName = onlineName;
    }
    
    public String getOnlineName() {
        return onlineName;
    }

    public void setOnlineName(String onlineName) {
        this.onlineName = onlineName;
    }

    public Integer getProtocolCount() {
        return protocolCount;
    }
    public void setProtocolCount(Integer protocolCount) {
        this.protocolCount = protocolCount;
    }
    public String getOnlineLinkage() {
        return onlineLinkage;
    }
    public void setOnlineLinkage(String identifier) {
        this.onlineLinkage = identifier;
    }
    public String getGetRecordById() {
        return getRecordById;
    }
    public void setGetRecordById(String getRecordById) {
        this.getRecordById = getRecordById;
    }
    
    
}
