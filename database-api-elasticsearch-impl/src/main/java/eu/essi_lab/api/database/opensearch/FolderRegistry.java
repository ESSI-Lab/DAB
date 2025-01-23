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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;

/**
 * @author Fabrizio
 */
public class FolderRegistry {

    private OpenSearchWrapper wrapper;
    private OpenSearchDatabase database;

    /**
     * @param database
     * @return
     */
    public static FolderRegistry get(OpenSearchDatabase database) {

	return new FolderRegistry(database);
    }

    /**
     * @return
     */
    private FolderRegistry(OpenSearchDatabase database) {

	this.database = database;
	this.wrapper = new OpenSearchWrapper(database.getClient());
    }

    /**
     * @param folder
     * @throws IOException
     */
    public boolean register(OpenSearchFolder folder) throws IOException {

	IndexData indexData = IndexData.of(folder);

	boolean stored = wrapper.storeWithGenericClient(indexData);

	wrapper.synch();

	return stored;
    }

    /**
     * @param folder
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public boolean deregister(OpenSearchFolder folder) throws OpenSearchException, IOException {

	String index = FolderRegistryMapping.get().getIndex();

	String id = FolderRegistryMapping.getEntryId(folder);

	boolean deleted = wrapper.delete(index, id);

	wrapper.synch();

	return deleted;
    }

    /**
     * @param folder
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public boolean isRegistered(OpenSearchFolder folder) throws OpenSearchException, IOException {

	String index = FolderRegistryMapping.get().getIndex();

	String id = FolderRegistryMapping.getEntryId(folder);

	return wrapper.entryExists(index, id);
    }

    /**
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public List<OpenSearchFolder> getRegisteredFolders() throws OpenSearchException, IOException {

	Query query = wrapper.buildSearchRegistryQuery(database.getIdentifier());

	List<String> names = wrapper.searchProperty(query, IndexData.FOLDER_NAME);

	return names.stream().//
		map(name -> new OpenSearchFolder(this.database, name)).//
		collect(Collectors.toList());
    }
}
