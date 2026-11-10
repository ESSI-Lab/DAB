package eu.essi_lab.access.datacache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

/**
 * Summary of the cached data of a time series, and of its last update (as recorded in the statistics)
 *
 * @author boldrini
 */
public class SeriesCacheInfo {

    private String dataIdentifier;
    private String observedPropertyURI;
    private String observedProperty;
    private long valuesCount;
    private Date firstObservation;
    private Date lastObservation;
    private Date nextExpected;
    private Date lastInsert;

    private Date lastUpdate;
    private Integer lastUpdateValues;
    private String lastUpdateError;

    public String getDataIdentifier() {
	return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
	this.dataIdentifier = dataIdentifier;
    }

    public String getObservedPropertyURI() {
	return observedPropertyURI;
    }

    public void setObservedPropertyURI(String observedPropertyURI) {
	this.observedPropertyURI = observedPropertyURI;
    }

    public String getObservedProperty() {
	return observedProperty;
    }

    public void setObservedProperty(String observedProperty) {
	this.observedProperty = observedProperty;
    }

    public long getValuesCount() {
	return valuesCount;
    }

    public void setValuesCount(long valuesCount) {
	this.valuesCount = valuesCount;
    }

    public Date getFirstObservation() {
	return firstObservation;
    }

    public void setFirstObservation(Date firstObservation) {
	this.firstObservation = firstObservation;
    }

    public Date getLastObservation() {
	return lastObservation;
    }

    public void setLastObservation(Date lastObservation) {
	this.lastObservation = lastObservation;
    }

    /**
     * @return the next record expected time, if the series is scheduled for update
     */
    public Date getNextExpected() {
	return nextExpected;
    }

    public void setNextExpected(Date nextExpected) {
	this.nextExpected = nextExpected;
    }

    /**
     * @return the time of the most recent insertion of a value in the cache
     */
    public Date getLastInsert() {
	return lastInsert;
    }

    public void setLastInsert(Date lastInsert) {
	this.lastInsert = lastInsert;
    }

    /**
     * @return the time of the last update attempt recorded in the statistics, if any
     */
    public Date getLastUpdate() {
	return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
	this.lastUpdate = lastUpdate;
    }

    /**
     * @return the number of values retrieved by the last update attempt
     */
    public Integer getLastUpdateValues() {
	return lastUpdateValues;
    }

    public void setLastUpdateValues(Integer lastUpdateValues) {
	this.lastUpdateValues = lastUpdateValues;
    }

    /**
     * @return the error of the last update attempt, if it failed
     */
    public String getLastUpdateError() {
	return lastUpdateError;
    }

    public void setLastUpdateError(String lastUpdateError) {
	this.lastUpdateError = lastUpdateError;
    }
}
