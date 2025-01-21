/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchReader implements DatabaseReader {

    private OpenSearchDatabase database;
    private OpenSearchClientWrapper wrapper;

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchClientWrapper(this.database.getClient());
    }

    @Override
    public OpenSearchDatabase getDatabase() {

	return (OpenSearchDatabase) database;
    }

    @Override
    public boolean supports(StorageInfo info) {

	return OpenSearchDatabase.isSupported(info);
    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	Query query = wrapper.buildSearchQuery(getDatabase().getIdentifier(), UsersMapping.get().getIndex());

	try {
	    return wrapper.searchBinaries(query).//
		    stream().//
		    map(binary -> GSUser.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetUsersError", ex);
	}
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {

	Query query = wrapper.buildSearchQuery(//
		getDatabase().getIdentifier(), //
		ViewsMapping.get().getIndex(), //
		ViewsMapping.VIEW_ID, //
		viewId);

	try {
	    return wrapper.searchBinaries(query).//
		    stream().//
		    map(binary -> View.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    findFirst();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetViewByIdError", ex);
	}
    }

    @Override
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException {

	Query query = wrapper.buildSearchViewsQuery(//
		database.getIdentifier(), //
		request.getCreator(), //
		request.getOwner(), //
		request.getVisibility()//
	);
	try {

	    List<String> list = wrapper.searchProperty(query, ViewsMapping.VIEW_ID);

	    int fromIndex = Math.min(list.size(), request.getStart());
	    int toIndex = Math.min(list.size(), request.getStart() + request.getCount());

	    list = list.subList(fromIndex, toIndex);

	    return list;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetViewIdentifiersError", ex);
	}
    }

    @Override
    public boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException {

	return false;
    }

    @Override
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException {

	return null;
    }

    @Override
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {

	return false;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException {

	return null;
    }

    @Override
    public List<GSResource> getResources(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {
	// TODO Auto-generated method stub
	return null;
    }
}
