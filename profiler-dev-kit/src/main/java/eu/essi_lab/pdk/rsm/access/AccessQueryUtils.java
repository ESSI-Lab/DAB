package eu.essi_lab.pdk.rsm.access;

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
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class AccessQueryUtils {

    private static ExpiringCache<ResultSet<GSResource>> resultCache;

    static {
	resultCache = new ExpiringCache<>();
	resultCache.setDuration(TimeUnit.MINUTES.toMillis(30));
	resultCache.setMaxSize(50);
    }

    public static ResultSet<GSResource> findResource(String requestId, List<GSSource> sources,
	    String onlineIdentifier, StorageInfo databaseURI) throws GSException {

	if (requestId != null) {
	    synchronized (resultCache) {
		ResultSet<GSResource> result = resultCache.get(requestId);
		if (result != null) {
		    return result;
		}
	    }
	}

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId(requestId);
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(sources);
	discoveryMessage.setDataBaseURI(databaseURI);

	SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.ONLINE_ID, //
		onlineIdentifier);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	synchronized (resultCache) {
	    resultCache.put(requestId, resultSet);
	}

	return resultSet;

    }

}
