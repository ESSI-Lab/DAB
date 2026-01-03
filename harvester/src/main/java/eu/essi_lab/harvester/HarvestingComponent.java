package eu.essi_lab.harvester;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public abstract class HarvestingComponent {

    /**
     * 
     */
    private String resumptionToken;
    @SuppressWarnings("rawtypes")
    private IHarvestedAccessor accessor;
    private SourceStorage sourceStorage;
    private HarvestingProperties harvestingProperties;
    private boolean isRecovering;
    private boolean isFirstHarvesting;
    private boolean isIncrementalHarvesting;
    private ListRecordsRequest request;

    /**
     * @return
     */
    public ListRecordsRequest getRequest() {

	return request;
    }

    /**
     * @param request
     */
    public void setRequest(ListRecordsRequest request) {

	this.request = request;
    }

    /**
     * @return
     */
    public boolean isIncrementalHarvesting() {

	return isIncrementalHarvesting;
    }

    /**
     * @param isIncrementalHarvesting
     */
    public void setIsIncrementalHarvesting(boolean isIncrementalHarvesting) {

	this.isIncrementalHarvesting = isIncrementalHarvesting;
    }

    /**
     * @return
     */
    public boolean isFirstHarvesting() {

	return isFirstHarvesting;
    }

    /**
     * @param isFirstHarvesting
     */
    public void setIsFirstHarvesting(boolean isFirstHarvesting) {

	this.isFirstHarvesting = isFirstHarvesting;
    }

    /**
     * @return
     */
    public boolean isRecovering() {

	return isRecovering;
    }

    /**
     * @param isRecovering
     */
    public void setIsRecovering(boolean isRecovering) {

	this.isRecovering = isRecovering;
    }

    /**
     * @return
     */
    public HarvestingProperties getHarvestingProperties() {

	return harvestingProperties;
    }

    /**
     * @param harvestingProperties
     */
    public void setHarvestingProperties(HarvestingProperties harvestingProperties) {

	this.harvestingProperties = harvestingProperties;
    }

    /**
     * @param resumptionToken
     */
    public void setResumptionToken(String resumptionToken) {

	this.resumptionToken = resumptionToken;
    }

    /**
     * @param accessor
     */
    @SuppressWarnings("rawtypes")
    public void setAccessor(IHarvestedAccessor accessor) {

	this.accessor = accessor;
    }

    /**
     * @param sourceStorage
     */
    public void setSourceStorage(SourceStorage sourceStorage) {

	this.sourceStorage = sourceStorage;
    }

    /**
     * @return
     */
    public String getResumptionToken() {

	return resumptionToken;
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public IHarvestedAccessor getAccessor() {

	return accessor;
    }

    /**
     * @return
     */
    public SourceStorage getSourceStorage() {

	return sourceStorage;
    }

    /**
     * @param resource
     * @throws HarvestingComponentException
     * @throws DuplicatedResourceException
     * @throws ConflictingResourceException
     */
    public abstract void apply(GSResource resource)
	    throws HarvestingComponentException, DuplicatedResourceException, ConflictingResourceException;
}
