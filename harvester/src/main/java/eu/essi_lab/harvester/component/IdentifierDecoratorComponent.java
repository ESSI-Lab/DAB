package eu.essi_lab.harvester.component;

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

import eu.essi_lab.harvester.HarvestingComponentException;
import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class IdentifierDecoratorComponent extends HarvestingComponent {

    private IdentifierDecorator identifierDecorator;

    /**
     * 
     */
    public IdentifierDecoratorComponent() {
    }

    /**
     * @param identifierDecorator
     */
    public IdentifierDecoratorComponent(IdentifierDecorator identifierDecorator) {

	this.identifierDecorator = identifierDecorator;
    }

    @Override
    public void apply(GSResource resource) throws HarvestingComponentException, DuplicatedResourceException, ConflictingResourceException {

	try {

	    this.identifierDecorator.setListRecordsRequest(getRequest());

	    this.identifierDecorator.decorateHarvestedIdentifier(//
		    resource, //
		    getHarvestingProperties(), //
		    getSourceStorage(), //
		    isFirstHarvesting(), //
		    isRecovering(), //
		    isIncrementalHarvesting());

	} catch (GSException e) {

	    throw new HarvestingComponentException(e);
	}
    }
}
