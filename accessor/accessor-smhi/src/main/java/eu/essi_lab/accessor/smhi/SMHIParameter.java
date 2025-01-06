package eu.essi_lab.accessor.smhi;

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

import java.io.InputStream;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.net.downloader.Downloader;

@XmlRootElement
public class SMHIParameter {
    @XmlElement
    private String key;
    @XmlElement
    private String title;
    @XmlElement
    private String units;
    @XmlElement
    private String isoLink;
    @XmlElement
    private String stationLink;

    @XmlTransient
    public Optional<InputStream> getIsoDocument() {
	Downloader downloader = new Downloader();
	return downloader.downloadOptionalStream(isoLink);	
    }
    @XmlTransient
    public String getIsoLink() {
	return isoLink;
    }

    public void setIsoLink(String isoLink) {
	this.isoLink = isoLink;
    }
    @XmlTransient
    public String getStationLink() {
	return stationLink;
    }

    public void setStationLink(String jsonLink) {
	this.stationLink = jsonLink;
    }
    @XmlTransient
    public String getKey() {
	return key;
    }

    public void setKey(String key) {
	this.key = key;
    }
    @XmlTransient
    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }
    @XmlTransient
    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

}
