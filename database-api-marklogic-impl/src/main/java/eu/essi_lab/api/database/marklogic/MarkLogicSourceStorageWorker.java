/**
 * 
 */
package eu.essi_lab.api.database.marklogic;

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

import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class MarkLogicSourceStorageWorker extends SourceStorageWorker {

    /**
     * @param sourceId
     * @param database
     * @throws GSException
     */
    public MarkLogicSourceStorageWorker(String sourceId, Database database) throws GSException {

	super(sourceId, database);
    }

    /**
     * @param indexName
     * @param status
     * @throws Exception
     */
    @Override
    protected void checkDataFolderIndex(String indexName, Optional<SchedulerJobStatus> status) throws Exception {

	MarkLogicIndexesManager idxManager = new MarkLogicIndexesManager((MarkLogicDatabase) getDatabase(), false);

	if (!idxManager.rangeIndexExists(//
		indexName, //
		MarkLogicScalarType.STRING)) {

	    debug("Adding data folder range index STARTED", status);

	    idxManager.addRangeIndex(indexName, //
		    MarkLogicScalarType.STRING.getType());

	    debug("Adding data folder range index ENDED", status);
	}
    }
}
