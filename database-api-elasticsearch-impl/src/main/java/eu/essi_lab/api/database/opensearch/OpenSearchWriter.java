/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import org.apache.commons.lang3.NotImplementedException;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchWriter extends DatabaseWriter {

    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchWrapper(this.database);
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
    public void remove(GSResource resource) throws GSException {

	remove(IndexData.ENTRY_NAME, resource.getPrivateId());
    }

    @Override
    public void remove(String propertyName, String propertyValue) throws GSException {

	Query query = OpenSearchQueryBuilder.buildSearchQuery(//
		getDatabase().getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		propertyName, //
		propertyValue);

	DeleteByQueryRequest request = wrapper.buildDeleteByQueryRequest(DataFolderMapping.get().getIndex(), query);

	try {
	    DeleteByQueryResponse response = wrapper.deleteByQuery(request);

	    Long deleted = response.deleted();

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).debug("Deleted count: {}", deleted);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchWriterRemoveByPropertiesError", ex);
	}
    }

    //
    // NOT IMPLEMENTED AT THE MOMENT
    //
    @Override
    public void storeRDF(Node rdf) throws GSException {

	throw new NotImplementedException();
    }
}
