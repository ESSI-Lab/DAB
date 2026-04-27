package eu.essi_lab.request.executor.impl.discover.submitter;

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

import eu.essi_lab.adk.distributed.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.identifierdecorator.*;
import eu.essi_lab.lib.servlet.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.parser.*;
import eu.essi_lab.messages.count.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.request.executor.query.*;
import org.w3c.dom.*;

import java.io.*;
import java.util.AbstractMap.*;
import java.util.*;

public class DistributedQueryExecutorImpl implements DistributedQueryExecutor {

    @SuppressWarnings("rawtypes")
    private IDistributedAccessor accessor;
    private String sourceIdentifier;

    @SuppressWarnings("rawtypes")
    public DistributedQueryExecutorImpl(IDistributedAccessor accessor, String id) {

	this.accessor = accessor;
	this.sourceIdentifier = id;
    }

    @Override
    public String getSourceIdentifier() {

	return sourceIdentifier;
    }

    @Override
    public SimpleEntry<String, DiscoveryCountResponse> count(ReducedDiscoveryMessage message) throws GSException {

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	DiscoveryCountResponse countResult;

	if (parser.isCanonicalQueryByIdentifiers()) {

	    ResultSet<GSResource> result = retrieve(message, null);

	    countResult = new DiscoveryCountResponse();
	    countResult.setCount(result.getResultsList().size());

	} else {

	    countResult = accessor.count(message);
	}

	return new SimpleEntry<>(getSourceIdentifier(), countResult);
    }

    @Override
    public ResultSet<Node> retrieveNodes(ReducedDiscoveryMessage message, Page page) throws GSException {

	ResultSet<GSResource> gsResources = retrieve(message, page);
	ResultSet<Node> ret = new ResultSet<>();

	for (GSResource resource : gsResources.getResultsList()) {

	    try {
		Document node = resource.asDocument(true);
		ret.getResultsList().add(node);

	    } catch (Exception e) {

		throw GSException.createException(getClass(), "ResourceNodeMarshallError", e);
	    }
	}

	return ret;
    }

    @Override
    public ResultSet<String> retrieveStrings(ReducedDiscoveryMessage message, Page page) throws GSException {

	ResultSet<GSResource> gsResources = retrieve(message, page);
	ResultSet<String> ret = new ResultSet<>();

	for (GSResource resource : gsResources.getResultsList()) {

	    try {

		String node = resource.asString(true);
		ret.getResultsList().add(node);

	    } catch (Exception e) {

		throw GSException.createException(getClass(), "ResourceStringMarshallError", e);
	    }
	}

	return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResultSet<GSResource> retrieve(ReducedDiscoveryMessage message, Page page) throws GSException {

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	if (parser.isCanonicalQueryByIdentifiers()) {

	    return retrieveCached(message);
	}

	ResultSet<GSResource> ret = accessor.query(message, page);

	List<GSResource> results = ret.getResultsList();

	DatabaseFolder cacheFolder = getCacheFolder();

	IdentifierDecorator decorator = new IdentifierDecorator();

	for (GSResource result : results) {

	    decorator.decorateDistributedIdentifier(result);

	    try {

		cacheFolder.store(//
			result.getPrivateId(), //
			DatabaseFolder.FolderEntry.of(result.asStream()),//
			DatabaseFolder.EntryType.CACHE_ENTRY);//

	    } catch (Exception e) {

		throw GSException.createException(getClass(), "DatabaseStoreCachedResourceError", e);
	    }
	}

	return ret;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private ResultSet<GSResource> retrieveCached(ReducedDiscoveryMessage message) throws GSException {

	Bond reducedBond = message.getReducedBond();

	IdentifierBondHandler parser = new IdentifierBondHandler(reducedBond);

	List<String> identifiers = parser.getIdentifiers();

	ResultSet<GSResource> ret = new ResultSet<>();

	DatabaseFolder cacheFolder = getCacheFolder();

	for (String identifier : identifiers) {

	    try {

		InputStream stream = cacheFolder.getBinary(identifier);

		if (stream != null) {

		    ret.getResultsList().add(GSResource.create(stream));
		}

	    } catch (Exception e) {

		throw GSException.createException(getClass(), "DatabaseGetCachedResourceError", e);
	    }
	}

	return ret;
    }

    /**
     * @return
     * @throws GSException
     */
    private DatabaseFolder getCacheFolder() throws GSException {

	StorageInfo storageInfo = ConfigurationWrapper.getStorageInfo();

	return DatabaseFactory.get(storageInfo).getCacheFolder();
    }

    @Override
    public Type getType() {

	return Type.DISTRIBUTED;
    }

}
