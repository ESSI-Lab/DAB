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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchFolder implements DatabaseFolder {

    private String name;
    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    /**
     * @param database
     * @param name
     */
    public OpenSearchFolder(OpenSearchDatabase database, String name) {

	this.database = database;
	//
	// for compatibility with MarkLogic, the returned name includes also the database id
	//
	this.name = name.startsWith(database.getIdentifier()) ? name : database.getIdentifier() + "_" + name;
	this.wrapper = new OpenSearchWrapper(database.getClient());
    }

    @Override
    public String getName() {

	return name;
    }

    @Override
    public boolean store(String key, FolderEntry entry, EntryType type) throws Exception {

	IndexData indexData = IndexData.of(this, key, entry, type);

	boolean stored = wrapper.storeWithOpenSearchClient(indexData);

	wrapper.synch();

	return stored;
    }

    @Override
    public boolean replace(String key, FolderEntry entry, EntryType type) throws Exception {

	if (remove(key)) {
	    return store(key, entry, type);
	}

	return false;
    }

    @Override
    public Node get(String key) throws Exception {

	Optional<JSONObject> source = _getSource(key);

	if (source.isEmpty()) {

	    return null;
	}

	SourceWrapper wrapper = new SourceWrapper(source.get());

	if (wrapper.getDataType() == DataType.BINARY) {

	    return null;
	}

	if (wrapper.getBinaryProperty().equals(DataFolderMapping.GS_RESOURCE)) {

	    return ConversionUtils.toGSResource(source.get()).get().asDocument(true);
	}

	return ConversionUtils.toNode(ConversionUtils.toStream(source.get()));
    }

    @Override
    public Optional<GSResource> get(IdentifierType type, String identifier) throws Exception {

	OpenSearchQueryBuilder builder = new OpenSearchQueryBuilder(wrapper, new RankingStrategy(), new HashMap<String, String>(), false);

	Bond bond = null;
	Query query = null;
	switch (type) {
	case OAI_HEADER:
	    bond = BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.OAI_PMH_HEADER_ID, identifier);
	    query = builder.buildResourcePropertyQuery((ResourcePropertyBond) bond);
	    break;
	case ORIGINAL:
	    bond = BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.ORIGINAL_ID, identifier);
	    query = builder.buildResourcePropertyQuery((ResourcePropertyBond) bond);
	    break;
	case PRIVATE:
	    Node node = get(identifier);
	    if (node != null) {
		return Optional.of(GSResource.create(node));
	    }
	    return Optional.empty();
	case PUBLIC:
	    bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, identifier);
	    query = builder.buildMetadataElementQuery(MetadataElement.IDENTIFIER, BondOperator.EQUAL, identifier);
	    break;
	}

	Query folderEntriesQuery = OpenSearchQueryBuilder.buildFolderEntriesQuery(this);
	Query boolQuery = OpenSearchQueryBuilder.buildBoolQuery(//
		Arrays.asList(query, folderEntriesQuery), //
		Arrays.asList(), //
		Arrays.asList());

	List<JSONObject> sources = wrapper.searchSources(DataFolderMapping.get().getIndex(), boolQuery, 0, 1);

	if (!sources.isEmpty()) {

	    return ConversionUtils.toGSResource(sources.get(0));
	}

	return Optional.empty();
    }

    @Override
    public InputStream getBinary(String key) throws Exception {

	Optional<JSONObject> source = _getSource(key);

	if (source.isEmpty()) {

	    return null;
	}

	SourceWrapper wrapper = new SourceWrapper(source.get());

	if (wrapper.getBinaryProperty().equals(DataFolderMapping.GS_RESOURCE)) {

	    return ConversionUtils.toGSResource(source.get()).get().asStream();
	}

	return ConversionUtils.toStream(source.get());
    }

    @Override
    public boolean remove(String key) throws Exception {

	String index = IndexData.detectIndex(this);

	String id = getEntryId(this, key);

	boolean deleted = wrapper.delete(index, id);

	wrapper.synch();

	return deleted;
    }

    @Override
    public boolean exists(String key) throws Exception {

	String index = IndexData.detectIndex(this);

	String id = getEntryId(this, key);

	return wrapper.entryExists(index, id);
    }

    @Override
    public String[] listKeys() throws Exception {

	String index = IndexData.detectIndex(this);

	Query searchQuery = OpenSearchQueryBuilder.buildFolderEntriesQuery(this);

	return wrapper.searchField(//
		index, //
		searchQuery, //
		IndexData.ENTRY_NAME).//
		toArray(new String[] {});
    }

    /**
     * @return
     * @throws Exception
     */
    public List<String> listIds() throws Exception {

	String index = IndexData.detectIndex(this);

	Query searchQuery = OpenSearchQueryBuilder.buildFolderEntriesQuery(this);

	return wrapper.searchField(//
		index, //
		searchQuery, //
		IndexData.ENTRY_ID);//

    }

    @Override
    public int size() throws Exception {

	String index = IndexData.detectIndex(this);

	Query query = OpenSearchQueryBuilder.buildFolderEntriesQuery(this);

	return (int) wrapper.count(index, query);
    }

    @Override
    public void clear() throws Exception {

	String index = IndexData.detectIndex(this);

	Query query = OpenSearchQueryBuilder.buildFolderEntriesQuery(this);

	DeleteByQueryRequest queryRequest = wrapper.buildDeleteByQueryRequest(index, query);

	wrapper.deleteByQuery(queryRequest);

	wrapper.synch();
    }

    /**
     * @return
     */
    @Override
    public Database getDatabase() {

	return database;
    }

    /**
     * The folder is formed by the database id followed by '_' and the folder name.<br>
     * E.g.: database id = 'test'; folder name = 'acronet'; folder id = 'test_acronet'.
     * 
     * @param folder
     */
    public static String getFolderId(DatabaseFolder folder) {

	return folder.getDatabase().getIdentifier() + "_" + folder.getName();
    }

    /**
     * The resource id, used as source id for the stored items, is formed by the folder id followed by '_' and
     * the resource key.<br>
     * E.g.: E.g.: database id = 'test'; folder name = 'acronet'; resource key = 'key'; resource key =
     * 'test_acronet_key'.
     * 
     * @param key
     * @return
     */
    public static String getEntryId(DatabaseFolder folder, String resourceKey) {

	return getFolderId(folder) + "_" + resourceKey;
    }

    /**
     * @param key
     * @return
     * @throws Exception
     */
    public SourceWrapper getSourceWrapper(String key) throws Exception {

	return new SourceWrapper(_getSource(key).orElse(null));
    }

    /**
     * @param key
     * @return
     * @throws Exception
     */
    private Optional<JSONObject> _getSource(String key) throws Exception {

	String index = IndexData.detectIndex(this);

	String entryId = getEntryId(this, key);

	return wrapper.getSource(index, entryId);
    }
}
