/**
 * 
 */
package eu.essi_lab.api.database.vol;

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

import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class VolatileDatabaseExecutor extends VolatileDatabaseReader implements DatabaseExecutor {

    @Override
    public List<String> retrieveEiffelIds(DiscoveryMessage message, int start, int count) throws GSException {

	return null;
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {
	return null;
    }

    @Override
    public ResultSet<String> discoverDistinctStrings(DiscoveryMessage message) throws Exception {
        return null;
    }
    
    @Override
    public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled) throws GSException {

	return null;
    }

    @Override
    public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count) throws GSException {

	return null;
    }

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	return null;
    }

    @Override
    public void clearDeletedRecords() throws GSException {
    }

    @Override
    public int countDeletedRecords() throws GSException {

	return 0;
    }

    @Override
    public List<WMSClusterResponse> execute(WMSClusterRequest request) throws GSException {

	return null;
    }

}
