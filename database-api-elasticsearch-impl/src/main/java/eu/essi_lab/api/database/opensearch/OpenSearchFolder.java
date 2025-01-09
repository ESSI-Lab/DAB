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

import java.io.InputStream;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.opensearch.client.opensearch.indices.PutMappingResponse;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;

/**
 * @author Fabrizio
 */
public class OpenSearchFolder implements DatabaseFolder {

    private String name;
    private OpenSearchDatabase database;

    /**
     * @param database
     * @param name
     */
    public OpenSearchFolder(OpenSearchDatabase database, String name) {

	this.database = database;
	this.name = name;
    }

    @Override
    public String getName() {

	return name;
    }

    @Override
    public boolean store(String key, FolderEntry entry, EntryType type) throws Exception {

	OpenSearchClient client = database.getClient();

	IndexData indexData = IndexData.of(this, key, entry, type);

	PutMappingRequest mappingRequest = indexData.getMappingRequest();

	PutMappingResponse putMappingResponse = client.indices().putMapping(mappingRequest);

	IndexRequest<String> indexRequest = indexData.getIndexRequest();

	IndexResponse indexResponse = client.index(indexRequest);

	return false;
    }

    @Override
    public boolean replace(String key, FolderEntry entry, EntryType type) throws Exception {

	return false;
    }

    @Override
    public Node get(String key) throws Exception {

	return null;
    }

    @Override
    public InputStream getBinary(String key) throws Exception {

	return null;
    }

    @Override
    public boolean remove(String key) throws Exception {

	return false;
    }

    @Override
    public boolean exists(String key) throws Exception {

	return false;
    }

    @Override
    public String[] listKeys() throws Exception {

	return null;
    }

    @Override
    public int size() throws Exception {

	return 0;
    }

    @Override
    public void clear() throws Exception {

    }

    /**
     * @return
     */
    @Override
    public Database getDatabase() {

	return database;
    }

}
