package eu.essi_lab.harvester.component;

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

import eu.essi_lab.api.database.DatabaseWriter;
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
    private DatabaseWriter dBWriter;

    /**
     * 
     */
    public DatabaseComponent() {
    }

    /**
     * @param dBWriter
     */
    public DatabaseComponent(DatabaseWriter dBWriter) {

	this.dBWriter = dBWriter;
    }

    @Override
    public void apply(GSResource resource) throws HarvesterComponentException {

	int tries = 1;
	int maxTries = 3;
	long sleep = 60000;

	while (tries <= maxTries) {

	    try {

		// GSLoggerFactory.getLogger(getClass()).trace("Try #" + tries + " STARTED");

		dBWriter.store(resource);

		// GSLoggerFactory.getLogger(getClass()).trace("Try #" + tries + " ENDED");

		return;

	    } catch (GSException e) {

		if (tries < maxTries) {

		    GSLoggerFactory.getLogger(getClass()).warn("Try #" + tries + " failed, waiting a while before the next try");

		    try {
			Thread.sleep(sleep);
		    } catch (InterruptedException ex) {
		    }

		    tries++;

		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to store the record after " + maxTries + " tries");

		    throw new HarvesterComponentException(e);
		}
	    }
	}
    }
}
