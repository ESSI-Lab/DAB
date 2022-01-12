package eu.essi_lab.harvester;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.harvester.component.HarvesterComponentException;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.exceptions.SkipProcessStepException;
import eu.essi_lab.model.resource.GSResource;
public abstract class HarvestingComponent extends AbstractGSconfigurableComposed {

    /**
     * 
     */
    private static final long serialVersionUID = -6116781346299838771L;
    @JsonIgnore
    private String resumptionToken;
    @JsonIgnore
    private IHarvestedAccessor accessor;
    @JsonIgnore
    private SourceStorage sourceStorage;
    @JsonIgnore
    private HarvestingProperties harvestingProperties;
    @JsonIgnore
    private boolean isRecovering;
    @JsonIgnore
    private boolean isFirstHarvesting;
    @JsonIgnore
    private boolean isIncrementalHarvesting;

    /**
     * @return
     */
    @JsonIgnore
    public boolean isIncrementalHarvesting() {

	return isIncrementalHarvesting;
    }

    /**
     * @param isIncrementalHarvesting
     */
    @JsonIgnore
    public void setIsIncrementalHarvesting(boolean isIncrementalHarvesting) {

	this.isIncrementalHarvesting = isIncrementalHarvesting;
    }

    /**
     * @return
     */
    @JsonIgnore
    public boolean isFirstHarvesting() {

	return isFirstHarvesting;
    }

    /**
     * @param isFirstHarvesting
     */
    @JsonIgnore
    public void setIsFirstHarvesting(boolean isFirstHarvesting) {

	this.isFirstHarvesting = isFirstHarvesting;
    }

    /**
     * @return
     */
    @JsonIgnore
    public boolean isRecovering() {

	return isRecovering;
    }

    /**
     * @param isRecovering
     */
    @JsonIgnore
    public void setIsRecovering(boolean isRecovering) {

	this.isRecovering = isRecovering;
    }

    /**
     * @return
     */
    @JsonIgnore
    public HarvestingProperties getHarvestingProperties() {

	return harvestingProperties;
    }

    /**
     * @param harvestingProperties
     */
    @JsonIgnore
    public void setHarvestingProperties(HarvestingProperties harvestingProperties) {

	this.harvestingProperties = harvestingProperties;
    }

    /**
     * @param resumptionToken
     */
    @JsonIgnore
    public void setResumptionToken(String resumptionToken) {

	this.resumptionToken = resumptionToken;
    }

    /**
     * @param accessor
     */
    @JsonIgnore
    public void setAccessor(IHarvestedAccessor accessor) {

	this.accessor = accessor;
    }

    /**
     * @param sourceStorage
     */
    @JsonIgnore
    public void setSourceStorage(SourceStorage sourceStorage) {

	this.sourceStorage = sourceStorage;
    }

    /**
     * @return
     */
    @JsonIgnore
    public String getResumptionToken() {

	return resumptionToken;
    }

    /**
     * @return
     */
    @JsonIgnore
    public IHarvestedAccessor getAccessor() {

	return accessor;
    }

    /**
     * @return
     */
    @JsonIgnore
    public SourceStorage getSourceStorage() {

	return sourceStorage;
    }

    /**
     * Applies its task to the supplied resource
     * 
     * @param resource containing metadata set to elaborate
     * @throws
     * @throws {@link SkipProcessStepException}
     */
    public abstract void apply(GSResource resource)
	    throws HarvesterComponentException, DuplicatedResourceException, ConflictingResourceException;
}
