package eu.essi_lab.messages.listrecords;

import java.util.ArrayList;
import java.util.List;

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

import java.util.Objects;
import java.util.Optional;

import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class ListRecordsRequest {

    private String resumptionToken;
    private String fromDateStamp;
    private String untilDateStamp;
    private HarvestingProperties properties;
    private boolean isRecovered;
    private boolean isResumed;
    private boolean isFirst;
    private JobStatus status;
    private Integer expectedRecords;
    private GSPropertyHandler additionalInfo;
    private List<GSResource> modifiedResources;
    private List<GSResource> deletedResources;

    /**
     * 
     */
    public static String SOURCE_STORAGE_WORKER_PROPERTY = "sourceStorageWorker";

    /**
     * 
     */
    public ListRecordsRequest() {

	setFirst(true);

	modifiedResources = new ArrayList<>();
	deletedResources = new ArrayList<>();
    }

    /**
     * @param status
     */
    public ListRecordsRequest(JobStatus status) {

	this.status = status;

	setFirst(true);

	modifiedResources = new ArrayList<GSResource>();
	deletedResources = new ArrayList<>();
    }

    /**
     * @return
     */
    public Optional<Integer> getExpectedRecords() {

	return Optional.ofNullable(expectedRecords);
    }

    /**
     * @param expectedRecords
     */
    public void setExpectedRecords(int expectedRecords) {

	this.expectedRecords = expectedRecords;
    }

    /**
     * @return
     */
    public boolean isFirst() {

	return isFirst;
    }

    /**
     * @param isFirst
     */
    public void setFirst(boolean isFirst) {

	this.isFirst = isFirst;
    }

    /**
     * @return
     */
    public boolean isFirstHarvesting() {

	return Objects.isNull(getHarvestingProperties()) || //
		getHarvestingProperties().isEmpty() || //
		Objects.isNull(getHarvestingProperties().getEndHarvestingTimestamp());
    }

    /**
     * @return
     */
    public HarvestingProperties getHarvestingProperties() {

	return properties;
    }

    /**
     * @param properties
     */
    public void setHarvestingProperties(HarvestingProperties properties) {

	this.properties = properties;
    }

    /**
     * @return
     */
    public String getUntilDateStamp() {

	return untilDateStamp;
    }

    /**
     * @param untilDateStamp
     */
    public void setUntilDateStamp(String untilDateStamp) {

	this.untilDateStamp = untilDateStamp;
    }

    /**
     * @return
     */
    public String getResumptionToken() {

	return resumptionToken;
    }

    /**
     * @param resumptionToken
     */
    public void setResumptionToken(String resumptionToken) {

	this.resumptionToken = resumptionToken;
    }

    /**
     * @return
     */
    public String getFromDateStamp() {

	return fromDateStamp;
    }

    /**
     * @param fromDateStamp
     */
    public void setFromDateStamp(String fromDateStamp) {

	this.fromDateStamp = fromDateStamp;
    }

    /**
     * @return
     */
    public boolean isResumed() {

	return isResumed;
    }

    /**
     * @param recovering
     */
    public void setResumed(Boolean resumed) {

	isResumed = resumed;
    }

    /**
     * @return
     */
    public boolean isRecovered() {

	return isRecovered;
    }

    /**
     * @param recovering
     */
    public void setRecovered(Boolean recovered) {

	isRecovered = recovered;
    }

    /**
     * @return
     */
    public Optional<JobStatus> getStatus() {

	return Optional.ofNullable(status);
    }

    /**
     * @return
     */
    public GSPropertyHandler getAdditionalInfo() {

	return additionalInfo;
    }

    /**
     * @param additionalInfo
     */
    public void setAdditionalInfo(GSPropertyHandler additionalInfo) {

	this.additionalInfo = additionalInfo;
    }

    /**
     * @param resource
     */
    public void addIncrementalModifiedResource(GSResource resource) {

	this.modifiedResources.add(resource);
    }

    /**
     * @return
     */
    public List<GSResource> getIncrementalModifiedResources() {

	return modifiedResources;
    }

    /**
     * @param gsResource
     */
    public void addIncrementalDeletedResource(GSResource resource) {

	this.deletedResources.add(resource);
    }

    /**
     * @return
     */
    public List<GSResource> getIncrementalDeletedResources() {

	return deletedResources;
    }
}
