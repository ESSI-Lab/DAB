/**
 * 
 */
package eu.essi_lab.request.executor.statistics;

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

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.IStatisticsExecutor;
import eu.essi_lab.request.executor.discover.QueryInitializer;

/**
 * @author Fabrizio
 */
public class StatisticsExecutor implements IStatisticsExecutor {

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	new QueryInitializer().initializeQuery(message);

	//
	// for the DB the is preferred the use of the short form instead of the
	// normalized (longer) form
	//
	message.setNormalizedBond(message.getPermittedBond());

	StorageInfo uri = message.getDataBaseURI();

	DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(uri);

	GSLoggerFactory.getLogger(getClass()).info("Computation STARTED");

	StatisticsResponse response = executor.compute(message);

	GSLoggerFactory.getLogger(getClass()).info("Computation ENDED");

	return response;
    }
}
