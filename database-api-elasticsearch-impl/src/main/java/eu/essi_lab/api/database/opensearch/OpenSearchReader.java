/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.Arrays;

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchReader implements DatabaseReader {

    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchWrapper(this.database);
    }

    @Override
    public OpenSearchDatabase getDatabase() {

	return database;
    }

    @Override
    public boolean supports(StorageInfo info) {

	return OpenSearchDatabase.isSupported(info);
    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	Query query = OpenSearchQueryBuilder.buildSearchQuery(getDatabase().getIdentifier());

	try {
	    return wrapper.searchBinaries(UsersMapping.get().getIndex(), query).//
		    stream().//
		    map(GSUser::createOrNull).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetUsersError", ex);
	}
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {

	Query query = OpenSearchQueryBuilder.buildGetViewQuery(//
		getDatabase().getIdentifier(), //
		viewId);

	try {
	    return wrapper.searchBinaries(ViewsMapping.get().getIndex(), query).//
		    stream().//
		    map(View::createOrNull).//
		    filter(Objects::nonNull).//
		    findFirst();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetViewByIdError", ex);
	}
    }

    @Override
    public List<View> getViews() throws GSException {

	Query query = OpenSearchQueryBuilder.buildSearchQuery(//
		getDatabase().getIdentifier(), //
		ViewsMapping.VIEW_ID //
	);

	try {
	    return wrapper.searchBinaries(ViewsMapping.get().getIndex(), query).//
		    stream().//
		    map(View::createOrNull).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetViewsError", ex);
	}
    }

    @Override
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException {

	Query query = OpenSearchQueryBuilder.buildSearchViewsQuery(//
		database.getIdentifier(), //
		request.getCreator(), //
		request.getOwner(), //
		request.getVisibility()//
	);
	try {

	    List<String> list = wrapper.searchField(//
		    ViewsMapping.get().getIndex(), //
		    query, //
		    ViewsMapping.VIEW_ID);

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
    public List<GSResource> getResources(IdentifierType type, String identifier) throws GSException {

	Queryable property = switch (type) {
	    case OAI_HEADER -> ResourceProperty.OAI_PMH_HEADER_ID;
	    case ORIGINAL -> ResourceProperty.ORIGINAL_ID;
	    case PRIVATE -> ResourceProperty.PRIVATE_ID;
	    case PUBLIC -> MetadataElement.IDENTIFIER;
	};

	Query query = OpenSearchQueryBuilder.buildFilterQuery(//
		Arrays.asList(//
			OpenSearchQueryBuilder.buildDatabaseIdQuery(database.getIdentifier()), //
			OpenSearchQueryBuilder.buildTermQuery(IndexMapping.toKeywordField(property.getName()), identifier)));

	try {
	    return wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetResourcesError", ex);
	}
    }

    @Override
    public List<GSResource> getResources(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {

	Query query = OpenSearchQueryBuilder.buildFilterQuery(//
		Arrays.asList(//
			OpenSearchQueryBuilder.buildDatabaseIdQuery(database.getIdentifier()), //
			OpenSearchQueryBuilder.buildTermQuery(IndexMapping.toKeywordField(ResourceProperty.ORIGINAL_ID.getName()),
				originalIdentifier),
			OpenSearchQueryBuilder.buildTermQuery(IndexMapping.toKeywordField(ResourceProperty.SOURCE_ID.getName()),
				source.getUniqueIdentifier())

		));

	try {
	    return wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchReaderGetResourcesError", ex);
	}
    }
}
