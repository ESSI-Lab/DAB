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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data cache statistics of the values of a source with a given observed property
 * 
 * @author boldrini
 */
public class PropertyCacheStats {

    private String observedProperty;
    private List<String> observedPropertyURIs = new ArrayList<>();
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
     * @return the observed property URIs found in the values with this observed property
     */
    public List<String> getObservedPropertyURIs() {
	return observedPropertyURIs;
    }

    public void setObservedPropertyURIs(List<String> observedPropertyURIs) {
	this.observedPropertyURIs = observedPropertyURIs;
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
