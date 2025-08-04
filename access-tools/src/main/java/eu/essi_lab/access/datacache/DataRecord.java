package eu.essi_lab.access.datacache;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;

import javax.xml.datatype.Duration;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * A class holding the observed value and the main related metadata elements.
 * 
 * @author boldrini
 */
public class DataRecord {
    // mandatory
    private String dataIdentifier;
    private Date date;
    private BigDecimal value;
    //
    private String uom;
    private String observedProperty;
    private LatitudeLongitude latitudeLongitude;

    private Date timestamp; // the time the record was inserted in the cache
    private Date nextRecordExpectedTime; // the time next record is expected to be available
    private Long verifiedPublicationGap; // the verified publication gap in ms

    // recommended
    private Boolean active; // indicates that this station is actively acquiring data
    private String samplingPoint;
    private String interpolationType;
    private Duration aggregationDuration;
    private String sourceIdentifier;

    // optional
    private BigDecimal altitude;
    private BigDecimal accuracy;
    private String processType;
    private String quality;
    private String status;

    public DataRecord(Date date, BigDecimal value, String uom, String observedProperty,
	    LatitudeLongitude latitudeLongitude, String dataIdentifier) {
	this();
	this.date = date;
	this.value = value;
	this.uom = uom;
	this.latitudeLongitude = latitudeLongitude;
	this.observedProperty = observedProperty;
	this.dataIdentifier = dataIdentifier;
    }

    public DataRecord() {
	this.timestamp = new Date();
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public BigDecimal getValue() {
	return value;
    }

    public void setValue(BigDecimal value) {
	this.value = value;
    }

    public String getUom() {
	return uom;
    }

    public void setUom(String uom) {
	this.uom = uom;
    }

    public Date getTimestamp() {
	return timestamp;
    }

    public void setTimestamp(Date timestamp) {
	this.timestamp = timestamp;
    }

    public String getSourceIdentifier() {
	return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
	this.sourceIdentifier = sourceIdentifier;
    }

    public String getObservedProperty() {
	return observedProperty;
    }

    public void setObservedProperty(String observedProperty) {
	this.observedProperty = observedProperty;
    }

    public LatitudeLongitude getLatitudeLongitude() {
	return latitudeLongitude;
    }

    public void setLatitudeLongitude(LatitudeLongitude latitudeLongitude) {
	this.latitudeLongitude = latitudeLongitude;
    }

    public BigDecimal getAltitude() {
	return altitude;
    }

    public void setAltitude(BigDecimal altitude) {
	this.altitude = altitude;
    }

    public String getDataIdentifier() {
	return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
	this.dataIdentifier = dataIdentifier;
    }

    public String getSamplingPoint() {
	return samplingPoint;
    }

    public void setSamplingPoint(String samplingPoint) {
	this.samplingPoint = samplingPoint;
    }

    public String getInterpolationType() {
	return interpolationType;
    }

    public void setInterpolationType(String interpolationType) {
	this.interpolationType = interpolationType;
    }

    public Duration getAggregationDuration() {
	return aggregationDuration;
    }

    public void setAggregationDuration(Duration aggregationDuration) {
	this.aggregationDuration = aggregationDuration;
    }

    public BigDecimal getAccuracy() {
	return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
	this.accuracy = accuracy;
    }

    public String getProcessType() {
	return processType;
    }

    public void setProcessType(String processType) {
	this.processType = processType;
    }

    public String getQuality() {
	return quality;
    }

    public void setQuality(String quality) {
	this.quality = quality;
    }

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
    }

    public Boolean isActive() {
	return active;
    }

    public void setActive(Boolean active) {
	this.active = active;
    }

    @Override
    public String toString() {
	String ret = "";
	if (dataIdentifier != null) {
	    ret += dataIdentifier;
	}
	if (date != null) {
	    ret += " " + ISO8601DateTimeUtils.getISO8601DateTime(date);
	}
	if (value != null) {
	    ret += " " + value.toString();
	}
	return ret;
    }

    public Date getNextRecordExpectedTime() {
	return nextRecordExpectedTime;
    }

    public void setNextRecordExpectedTime(Date nextRecordExpectedTime) {
	this.nextRecordExpectedTime = nextRecordExpectedTime;
    }

    public Long getVerifiedPublicationGap() {
	return verifiedPublicationGap;
    }

    public void setVerifiedPublicationGap(Long verifiedPublicationGap) {
	this.verifiedPublicationGap = verifiedPublicationGap;
    }

}
