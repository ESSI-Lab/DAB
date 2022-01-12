package eu.essi_lab.harvester.component;

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

import java.util.HashMap;
import java.util.Map;

import eu.essi_lab.harvester.Harvester;
import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.identifierdecorator.ConflictingResourceException;
import eu.essi_lab.identifierdecorator.DuplicatedResourceException;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
public class IdentifierDecoratorComponent extends HarvestingComponent {

    /**
     * 
     */
    private static final long serialVersionUID = -1866300158216492576L;
    public static final String DECORATOR_KEY = "DECORATOR_KEY";

    public IdentifierDecoratorComponent() {
    }

    public IdentifierDecoratorComponent(IdentifierDecorator identifierDecorator) {
	getConfigurableComponents().put(DECORATOR_KEY, identifierDecorator);
    }

    @Override
    public void apply(GSResource resource) throws HarvesterComponentException, DuplicatedResourceException, ConflictingResourceException {

	IdentifierDecorator decorator = (IdentifierDecorator) getConfigurableComponents().get(DECORATOR_KEY);

	try {

	    decorator.decorateHarvestedIdentifier(//
		    resource, //
		    getHarvestingProperties(), //
		    getSourceStorage(),//
		    isFirstHarvesting(), //
		    isRecovering(), //
		    isIncrementalHarvesting());

	} catch (GSException e) {

	    throw new HarvesterComponentException(e);
	}
    }

    @Override
    public String getLabel() {
	return "Identifier component";
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return new HashMap<>();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	/**
	 * It does not hold any option.
	 */
	return;
    }

    @Override
    public void onFlush() throws GSException {
    }
}
