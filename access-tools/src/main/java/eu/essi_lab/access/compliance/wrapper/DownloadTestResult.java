package eu.essi_lab.access.compliance.wrapper;

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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class DownloadTestResult {

    @XmlElement(name = "downloadable", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String downloadable;

    @XmlElement(required = false, name = "downloadTimeLong", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Long downloadTimeLong;

    @XmlElement(required = false, name = "downloadTime", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String downloadTime;

    /**
     * @return the downloadable
     */
    @XmlTransient
    public String isDownloadable() {

	return downloadable;
    }

    /**
     * @param downloadable the downloadable to set
     */
    public void setDownloadable(String downloadable) {

	this.downloadable = downloadable;
    }

    /**
     * @return the downloadTimeLong
     */
    @XmlTransient
    public long getDownloadTimeLong() {

	return downloadTimeLong;
    }

    /**
     * @param downloadTimeLong the downloadTimeLong to set
     */
    public void setDownloadTimeLong(long downloadTimeLong) {

	this.downloadTimeLong = downloadTimeLong;
    }

    /**
     * @return the downloadTime
     */
    @XmlTransient
    public String getDownloadTime() {

	return downloadTime;
    }

    /**
     * @param downloadTime the downloadTime to set
     */
    public void setDownloadTime(String downloadTime) {

	this.downloadTime = downloadTime;
    }
}
