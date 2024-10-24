package eu.essi_lab.harvester;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.LinkedList;
import java.util.List;

import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.model.exceptions.SkipProcessStepException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class HarvesterPlan extends HarvestingComponent {

    private LinkedList<HarvestingComponent> components;

    /**
     * 
     */
    HarvesterPlan() {

	components = new LinkedList<HarvestingComponent>();
    }

    /**
     * @return the components
     */
    public List<HarvestingComponent> getComponents() {

	return components;
    }

    @Override
    public void apply(GSResource resource) throws HarvestingComponentException, DuplicatedResourceException, ConflictingResourceException {

	List<HarvestingComponent> harvestingComponents = getComponents();

	HarvestingComponent current = null;

	try {
	    for (HarvestingComponent component : harvestingComponents) {

		current = component;

//		GSLoggerFactory.getLogger(getClass()).trace(//
//			"Application of {} on resource {} STARTED", //
//			component.getClass().getSimpleName(), //
//			resource);

		component.setAccessor(getAccessor());
		component.setSourceStorage(getSourceStorage());

		component.setResumptionToken(getResumptionToken());
		component.setHarvestingProperties(getHarvestingProperties());
		component.setIsRecovering(isRecovering());
		component.setIsFirstHarvesting(isFirstHarvesting());
		component.setIsIncrementalHarvesting(isIncrementalHarvesting());

		component.apply(resource);
	    }
	} catch (SkipProcessStepException spse) {
	    //
	    // next harvesting component application is skipped and Harvester
	    // continues with the next resource
	    //
	    throw spse;

	} finally {

//	    GSLoggerFactory.getLogger(getClass()).trace( //
//		    "Application of {} on resource {} ENDED", //
//		    current.getClass().getSimpleName(), //
//		    resource);
	}
    }
}
