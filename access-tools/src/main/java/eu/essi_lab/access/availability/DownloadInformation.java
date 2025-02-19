package eu.essi_lab.access.availability;

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

import java.util.Date;

import eu.essi_lab.model.GSSource;

public class DownloadInformation {
    private boolean success = false;
    private Date date = null;
    private String platformId = null;    
    private GSSource source = null;

    public GSSource getSource() {
        return source;
    }

    public void setSource(GSSource source) {
        this.source = source;
    }

    public DownloadInformation(boolean success, Date date, String platformId) {
	super();
	this.success = success;
	this.date = date;
	this.platformId = platformId;
    }

    public boolean isSuccess() {
	return success;
    }

    public void setSuccess(boolean success) {
	this.success = success;
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public String getPlatformId() {
	return platformId;
    }

    public void setPlatformId(String platformId) {
	this.platformId = platformId;
    }
}
