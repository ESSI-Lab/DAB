package eu.essi_lab.accessor.smhi;

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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class SMHIStation {

    @XmlElement
    private String key;
    @XmlElement
    private String title;
    @XmlElement
    private String summary;
    @XmlElement
    private String name;
    @XmlElement
    private Integer id;
    @XmlElement
    private String owner;
    @XmlElement
    private String measuringStations;
    @XmlElement
    private Boolean active;
    @XmlElement
    private Long from;
    @XmlElement
    private Long to;
    @XmlElement
    private BigDecimal latitude;
    @XmlElement
    private BigDecimal longitude;
    @XmlElement
    private Integer region;
    @XmlElement
    private String catchmentName;
    @XmlElement
    private Integer catchmentNumber;
    @XmlElement
    private Integer catchmentsize;
    @XmlElement
    private String stationLink;

    @XmlTransient
    public String getStationLink() {
	return stationLink;
    }

    public void setStationLink(String stationLink) {
	this.stationLink = stationLink;
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
    public String getSummary() {
	return summary;
    }

    public void setSummary(String summary) {
	this.summary = summary;
    }

    @XmlTransient
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    @XmlTransient
    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    @XmlTransient
    public String getOwner() {
	return owner;
    }

    public void setOwner(String owner) {
	this.owner = owner;
    }

    @XmlTransient
    public String getMeasuringStations() {
	return measuringStations;
    }

    public void setMeasuringStations(String measuringStations) {
	this.measuringStations = measuringStations;
    }

    @XmlTransient
    public Boolean getActive() {
	return active;
    }

    public void setActive(Boolean active) {
	this.active = active;
    }

    @XmlTransient
    public Long getFrom() {
	return from;
    }

    public void setFrom(Long from) {
	this.from = from;
    }

    @XmlTransient
    public Long getTo() {
	return to;
    }

    public void setTo(Long to) {
	this.to = to;
    }

    @XmlTransient
    public BigDecimal getLatitude() {
	return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
    }

    @XmlTransient
    public BigDecimal getLongitude() {
	return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
    }

    @XmlTransient
    public Integer getRegion() {
	return region;
    }

    public void setRegion(Integer region) {
	this.region = region;
    }

    @XmlTransient
    public String getCatchmentName() {
	return catchmentName;
    }

    public void setCatchmentName(String catchmentName) {
	this.catchmentName = catchmentName;
    }

    @XmlTransient
    public Integer getCatchmentNumber() {
	return catchmentNumber;
    }

    public void setCatchmentNumber(Integer catchmentNumber) {
	this.catchmentNumber = catchmentNumber;
    }

    @XmlTransient
    public Integer getCatchmentsize() {
	return catchmentsize;
    }

    public void setCatchmentsize(Integer catchmentsize) {
	this.catchmentsize = catchmentsize;
    }

}
