package eu.essi_lab.messages.listrecords;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author ilsanto
 */
public class ListRecordsRequest {

    private String resumptionToken;
    private String fromDateStamp;
    private String untilDateStamp;
    private HarvestingProperties properties;
    private Boolean isRecovering;
    private boolean isFirst;
    private JobStatus status;

    /**
     * 
     */
    public ListRecordsRequest() {

	setFirst(true);
	setRecovering(false);
    }

    /**
     * @param status
     */
    public ListRecordsRequest(JobStatus status) {

	this.status = status;
	
	setFirst(true);
	setRecovering(false);
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

    public String getUntilDateStamp() {

	return untilDateStamp;
    }

    public void setUntilDateStamp(String untilDateStamp) {

	this.untilDateStamp = untilDateStamp;
    }

    public String getResumptionToken() {

	return resumptionToken;
    }

    public void setResumptionToken(String resumptionToken) {

	this.resumptionToken = resumptionToken;
    }

    public String getFromDateStamp() {

	return fromDateStamp;
    }

    public void setFromDateStamp(String fromDateStamp) {

	this.fromDateStamp = fromDateStamp;
    }

    public Boolean getRecovering() {

	return isRecovering;
    }

    public void setRecovering(Boolean recovering) {

	isRecovering = recovering;
    }

    public Optional<JobStatus> getStatus() {

	return Optional.ofNullable(status);
    }
}
