package eu.essi_lab.harvester.component;

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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.DescriptorProtos.MethodOptions.IdempotencyLevel;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseReader.IdentifierType;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.harvester.HarvestingComponentException;
import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class DatabaseComponent extends HarvestingComponent {

    /**
    * 
    */
    private static final int MAX_TRIES = 3;
    /**
     * 
     */
    private static final long SLEEP_TIME = TimeUnit.MINUTES.toMillis(1);

    /**
    * 
    */
    private DatabaseWriter dBWriter;
    private DatabaseReader dBReader;

    /**
     * 
     */
    public DatabaseComponent() {
    }

    /**
     * @param dBWriter
     */
    public DatabaseComponent(DatabaseWriter dBWriter, DatabaseReader dBReader) {

	this.dBWriter = dBWriter;
	this.dBReader = dBReader;
    }

    @Override
    public void apply(GSResource resource) throws HarvestingComponentException {

	int tries = 1;

	while (tries <= MAX_TRIES) {

	    try {

		boolean deleted = resource.getPropertyHandler().isDeleted();

		//
		// deleted records are not stored
		//

		if (deleted) {

		    Optional<String> headerIdentifier = resource.getPropertyHandler().getOAIPMHHeaderIdentifier();

		    if (headerIdentifier.isPresent()) {

			GSLoggerFactory.getLogger(getClass()).debug("Deleted record found with OAI id: {}", headerIdentifier.get());

			//
			// finds resources with given headerIdentifier
			//

			List<GSResource> resources = dBReader.getResources(IdentifierType.OAI_HEADER, headerIdentifier.get());

			if (!resources.isEmpty()) {

			    for (GSResource gsResource : resources) {

				GSLoggerFactory.getLogger(getClass()).debug("Removing deleted dataset: {}", gsResource.toString());

				//
				// removes resources with given headerIdentifier since they have been deleted from the
				// origin OAI-PMH service
				//

				dBWriter.remove(gsResource);
			    }
			}
		    }

		} else {

		    dBWriter.store(resource);
		}

		return;

	    } catch (GSException e) {

		if (tries < MAX_TRIES) {

		    GSLoggerFactory.getLogger(getClass()).warn("Try #" + tries + " failed, waiting a while before the next try");

		    try {
			Thread.sleep(SLEEP_TIME);
		    } catch (InterruptedException ex) {
		    }

		    tries++;

		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to store the record after " + MAX_TRIES + " tries");

		    throw new HarvestingComponentException(e);
		}
	    }
	}
    }
}
