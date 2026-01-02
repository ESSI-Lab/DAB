/**
 *
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.JavaOptions;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import org.json.JSONObject;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.mapping.BinaryProperty;
import org.opensearch.client.opensearch._types.mapping.FieldType;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Fabrizio
 */
public abstract class IndexMapping {

    /**
     *
     */
    public static final String ALL_INDEXES = "*";
    //
    // Lucene doesn't allow terms that contain more than 32k bytes
    // Elasticsearch suggests to use ignore_above = 32766 / 4 = 8191 since UTF-8 characters may occupy at most 4 bytes.
    //
    public static final int MAX_KEYWORD_LENGTH = 32766 / 4;
    /**
     *
     */
    private static List<IndexMapping> MAPPINGS;
    private final JSONObject mapping;
    private final String index;
    private final boolean indexAlias;
    protected EntryType entryType;

    /**
     * @param index
     */
    protected IndexMapping(String index) {

	this(index, false);
    }

    /**
     * @param index
     * @param indexAlias <code>true</code> to use an index alias
     */
    protected IndexMapping(String index, boolean indexAlias) {

	this.index = index;
	this.indexAlias = indexAlias;
	this.mapping = new JSONObject(getBaseMapping());
    }

    /**
     * @return
     */
    public static List<IndexMapping> getMappings() {

	if (MAPPINGS == null) {

	    MAPPINGS = new ArrayList<>();
	}

	MAPPINGS.add(AugmentersMapping.get());
	MAPPINGS.add(ConfigurationMapping.get());
	MAPPINGS.add(DataFolderMapping.get());
	MAPPINGS.add(MetaFolderMapping.get());
	MAPPINGS.add(UsersMapping.get());
	MAPPINGS.add(ViewsMapping.get());
	MAPPINGS.add(FolderRegistryMapping.get());
	MAPPINGS.add(CacheMapping.get());
	MAPPINGS.add(ShapeFileMapping.get());

	return MAPPINGS;
    }

    /**
     * @return
     */
    public static List<String> getIndexes() {

	return getIndexes(true);
    }

    /**
     * @param indexAlias indexAlias <code>true</code> to get the index alias, if present
     * @return
     */
    public static List<String> getIndexes(boolean indexAlias) {

	return getMappings().stream().//
		map(i -> i.getIndex(indexAlias)).//
		collect(Collectors.toList());
    }

    /**
     * @param field
     * @return
     */
    public static String toKeywordField(String field) {

	return field + "_keyword";
    }

    /**
     * @param field
     * @return
     */
    public static String toTextField(String field) {

	return field.replace("_keyword", "");
    }

    /**
     * @param indexName
     * @return
     */
    private static String toAlias(String indexName) {

	return indexName + "_alias";
    }

    /**
     * @param client
     * @throws GSException
     */
    public static void initializeIndexes(OpenSearchClient client) throws GSException {

	GSLoggerFactory.getLogger(IndexMapping.class).info("Indexes init STARTED");

	final ArrayList<String> indexes = new ArrayList<>();

	for (IndexMapping mapping : IndexMapping.getMappings()) {

	    boolean exists = checkIndex(client, mapping.getIndex(false));

	    PutAliasRequest putAliasRequest = null;

	    if (!exists) {

		indexes.add(mapping.getIndex());

		GSLoggerFactory.getLogger(IndexMapping.class).info("Creating index {} STARTED", mapping.getIndex());

		createIndex(client, mapping);

		GSLoggerFactory.getLogger(IndexMapping.class).info("Creating index {} ENDED", mapping.getIndex());

		if (mapping.hasIndexAlias()) {

		    putAliasRequest = mapping.createPutAliasRequest();
		}
	    }

	    if (putAliasRequest != null) {

		GSLoggerFactory.getLogger(IndexMapping.class).info("Put alias {} STARTED", mapping.getIndex());

		try {
		    client.indices().putAlias(putAliasRequest);

		} catch (OpenSearchException | IOException e) {

		    throw GSException.createException(IndexMapping.class, "OpenSearchDatabasePutAliasError", e);
		}

		GSLoggerFactory.getLogger(IndexMapping.class).info("Put alias {} ENDED", mapping.getIndex());
	    }
	}

	if (indexes.isEmpty()) {

	    GSLoggerFactory.getLogger(IndexMapping.class).debug("No new index created");

	} else {

	    GSLoggerFactory.getLogger(IndexMapping.class).debug("Created indexes: {}",

		    indexes.stream().collect(Collectors.joining(",")));
	}

	GSLoggerFactory.getLogger(IndexMapping.class).info("Indexes init ENDED");
    }

    /**
     * @param client
     * @param indexName
     * @return
     * @throws GSException
     */
    public static boolean checkIndex(OpenSearchClient client, String indexName) throws GSException {

	ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(indexName).build();

	try {

	    return client.indices().exists(existsIndexRequest).value();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(OpenSearchDatabase.class, "OpenSearchDatabaseCheckIndexError", ex);
	}
    }

    /**
     * Return the index associated to this mapping, or if present and <code>alias</code> is <code>true</code>, the index alias
     *
     * @param alias
     * @return
     */
    public String getIndex(boolean alias) {

	if (alias) {

	    return getIndexAlias().orElse(index);
	}

	return index;
    }

    /**
     * Return the index associated to this mapping, or if present, the index alias
     *
     * @return
     */
    public String getIndex() {

	return getIndex(true);
    }

    /**
     * @return
     */
    public JSONObject getMapping() {

	return mapping.getJSONObject("mappings");
    }

    /**
     * @return
     */
    public InputStream getMappingStream() {

	return IOStreamUtils.asStream(getMapping().toString());
    }

    /**
     *
     */
    @Override
    public String toString() {

	return mapping.toString(3);
    }

    /**
     * @param type
     */
    public void setEntryType(EntryType type) {

	this.entryType = type;
    }

    /**
     * @return
     */
    public boolean hasIndexAlias() {

	return indexAlias;
    }

    /**
     * @param key
     * @param type
     */
    protected void addProperty(String key, String type) {

	addProperty(key, type, false);
    }

    /**
     * @param key
     * @param type
     * @param ignoreMalformed
     */
    protected void addProperty(String key, String type, boolean ignoreMalformed) {

	JSONObject property = OpenSearchUtils.toJSONObject(createProperty(type, ignoreMalformed));

	mapping.getJSONObject("mappings").//
		getJSONObject("properties").//
		put(key, property);
    }

    /**
     * @param key
     * @param type
     * @return
     */
    protected Property createProperty(String type) {

	return createProperty(type, false);
    }

    /**
     * @param type
     * @param ignoreMalformed
     * @return
     */
    public Property createProperty(String type, boolean ignoreMalformed) {

	JSONObject property = new JSONObject();
	property.put("type", type);

	if (ignoreMalformed) {
	    property.put("ignore_malformed", true);
	}

	JsonpMapper mapper = new JacksonJsonpMapper();
	JsonParser parser = Json.createParser(new StringReader(property.toString()));

	return Property._DESERIALIZER.deserialize(parser, mapper);
    }

    /**
     * @param type
     * @param ignoreMalformed
     * @return
     */
    protected Property createNestedProperty(JSONObject properties) {

	JSONObject nested = new JSONObject();
	nested.put("type", "nested");

	nested.put("properties", properties);

	JsonpMapper mapper = new JacksonJsonpMapper();
	JsonParser parser = Json.createParser(new StringReader(nested.toString()));

	return Property._DESERIALIZER.deserialize(parser, mapper);
    }

    /**
     * @param key
     * @param properties
     */
    protected void addNested(String key, JSONObject properties) {

	JSONObject nested = OpenSearchUtils.toJSONObject(createNestedProperty(properties));

	mapping.getJSONObject("mappings").//
		getJSONObject("properties").//
		put(key, nested);
    }

    /**
     * @param type
     * @return
     */
    protected JSONObject createTypeObject(FieldType type) {

	JSONObject out = new JSONObject();
	out.put("type", type.jsonValue());

	return out;
    }

    /**
     * @return
     */
    public ExistsAliasRequest createExistsAliasRequest() {

	return new ExistsAliasRequest.Builder().index(index).name(toAlias(index)).build();
    }

    /**
     * @return
     */
    public PutAliasRequest createPutAliasRequest() {

	return new PutAliasRequest.Builder().index(index).name(toAlias(index)).build();
    }

    /**
     * @return
     */
    private Optional<String> getIndexAlias() {

	return indexAlias ? Optional.ofNullable(toAlias(index)) : Optional.empty();
    }

    /**
     * @return
     */
    private String getBaseMapping() {

	try {
	    InputStream stream = getClass().getClassLoader().getResourceAsStream("mappings/base-mapping.json");
	    String ret = IOStreamUtils.asUTF8String(stream);
	    stream.close();
	    return ret;
	} catch (IOException e) {
	}

	return null;
    }

    /**
     * @param key
     * @param index
     * @return
     */
    @SuppressWarnings("unused")
    private PutMappingRequest createPutMappingRequest(String key, String index) {

	Property property = new Property.Builder().//
		binary(new BinaryProperty.Builder().build()).//
		build();

	return new PutMappingRequest.Builder().//
		properties(key, property).//
		index(index).//
		build();
    }

    /**
     * @param mapping
     * @param client
     * @throws GSException
     */
    private static void createIndex(OpenSearchClient client, IndexMapping mapping) throws GSException {

	TypeMapping typeMapping = new TypeMapping.Builder().//
		withJson(mapping.getMappingStream()).//
		build();

	CreateIndexRequest.Builder createIndexBuilder = new CreateIndexRequest.Builder().//
		index(mapping.getIndex(false)).//
		mappings(typeMapping);

	Optional<String> shards = JavaOptions.getValue(JavaOptions.NUMBER_OF_DATA_FOLDER_INDEX_SHARDS);

	if (mapping.getIndex().equals(DataFolderMapping.get().getIndex()) && shards.isPresent()) {

	    GSLoggerFactory.getLogger(IndexMapping.class).debug("Number of data-folder index shards: {}", shards.get());

	    createIndexBuilder.settings(new IndexSettings.Builder()//
		    .numberOfShards(shards.get()).numberOfReplicas("0")//
		    .build()); //
	}

	CreateIndexRequest createIndexRequest = createIndexBuilder.build();

	try {

	    CreateIndexResponse response = client.indices().create(createIndexRequest);

	    if (Boolean.FALSE.equals(response.acknowledged())) {

		throw GSException.createException(//
			IndexMapping.class, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_FATAL, //
			"OpenSearchDatabaseCreate" + mapping.getIndex() + "NotAcknowledgedError");
	    }

	    // synch
	    client.indices().refresh();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(IndexMapping.class).error(ex);

	    throw GSException.createException(IndexMapping.class, "OpenSearchDatabaseCreate" + mapping.getIndex() + "Error", ex);
	}
    }

    /**
     * @throws GSException
     */
    private void createIndexWithGenericCLient(OpenSearchClient client, IndexMapping mapping) throws GSException {

	try {

	    Response response = client.generic().execute(//
		    Requests.builder().//
			    endpoint(mapping.getIndex(false)).//
			    method("PUT").//
			    json(mapping.getMapping().toString()).build());

	    // synch
	    client.indices().refresh();

	    String bodyAsString = response.getBody().//
		    get().//
		    bodyAsString();

	    JSONObject responseObject = new JSONObject(bodyAsString);

	    if (!responseObject.getBoolean("acknowledged")) {

		throw GSException.createException(//
			getClass(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_FATAL, //
			"OpenSearchDatabaseCreate" + mapping.getIndex() + "NotAcknowledgedError");
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchDatabaseCreate" + mapping.getIndex() + "Error", ex);
	}
    }

}
