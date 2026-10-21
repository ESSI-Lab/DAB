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
 * Data cache statistics of the values of a source with a given observed property (name and URI)
 * 
 * @author boldrini
 */
public class PropertyCacheStats {

    private String observedProperty;
    private String observedPropertyURI;
    private Long uniqueDatasetCount;
    private Long recordCount;
    private Date newestObservation;
    private Date newestInsert;

    public String getObservedProperty() {
	return observedProperty;
    }

    public void setObservedProperty(String observedProperty) {
	this.observedProperty = observedProperty;
    }

    /**
     * @return the observed property URI, or an empty string if the values have no URI
     */
    public String getObservedPropertyURI() {
	return observedPropertyURI;
    }

    public void setObservedPropertyURI(String observedPropertyURI) {
	this.observedPropertyURI = observedPropertyURI;
    }

    public Long getUniqueDatasetCount() {
	return uniqueDatasetCount;
    }

    public void setUniqueDatasetCount(Long uniqueDatasetCount) {
	this.uniqueDatasetCount = uniqueDatasetCount;
    }

    public Long getRecordCount() {
	return recordCount;
    }

    public void setRecordCount(Long recordCount) {
	this.recordCount = recordCount;
    }

    /**
     * @return the date of the most recent observation (the observation time, not the insertion time)
     */
    public Date getNewestObservation() {
	return newestObservation;
    }

    public void setNewestObservation(Date newestObservation) {
	this.newestObservation = newestObservation;
    }

    /**
     * @return the most recent insertion time in the cache
     */
    public Date getNewestInsert() {
	return newestInsert;
    }

    public void setNewestInsert(Date newestInsert) {
	this.newestInsert = newestInsert;
    }
}
