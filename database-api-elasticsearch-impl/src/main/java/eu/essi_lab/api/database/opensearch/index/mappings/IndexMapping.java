/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.mapping.BinaryProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.indices.ExistsAliasRequest;
import org.opensearch.client.opensearch.indices.PutAliasRequest;
import org.opensearch.client.opensearch.indices.PutMappingRequest;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public abstract class IndexMapping {

    private JSONObject mapping;
    private String index;

    /**
     * 
     */
    public static final List<IndexMapping> MAPPINGS = new ArrayList<>();
    protected EntryType entryType;

    static {

	MAPPINGS.add(AugmentersMapping.get());
	MAPPINGS.add(ConfigurationMapping.get());
	MAPPINGS.add(DataFolderMapping.get());
	MAPPINGS.add(MetaFolderMapping.get());
	MAPPINGS.add(UsersMapping.get());
	MAPPINGS.add(ViewsMapping.get());
	MAPPINGS.add(FolderRegistryMapping.get());
	MAPPINGS.add(CacheMapping.get());
    }

    /**
     * 
     */
    public static final String ALL_INDEXES = "*";

    //
    // Lucene doesn't allow terms that contain more than 32k bytes
    // Elasticsearch suggests to use ignore_above = 32766 / 4 = 8191 since UTF-8 characters may occupy at most 4 bytes.
    //
    public static final int MAX_KEYWORD_LENGTH = 32766 / 4;
    private boolean indexAlias;

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

	return MAPPINGS.stream().//
		map(i -> i.getIndex(indexAlias)).//
		collect(Collectors.toList());
    }

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
     * Return the index associated to this mapping, or if present and <code>alias</code> is <code>true</code>, the index
     * alias
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
    public String getMapping() {

	return mapping.toString();
    }

    /**
     * @return
     */
    public InputStream getMappingStream() {

	return IOStreamUtils.asStream(getMapping());
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

	JSONObject property = new JSONObject();
	property.put("type", type);
	if (ignoreMalformed) {
	    property.put("ignore_malformed", true);
	}

	mapping.getJSONObject("mappings").//
		getJSONObject("properties").//
		put(key, property);
    }

    /**
     * @return
     */
    private Optional<String> getIndexAlias() {

	return indexAlias ? Optional.ofNullable(toAlias(index)) : Optional.empty();
    }

    /**
     * @param indexName
     * @return
     */
    private static String toAlias(String indexName) {

	return indexName + "_alias";
    }

    /**
     * @return
     */
    private String getBaseMapping() {

	try {
	    return IOStreamUtils.asUTF8String(

		    getClass().getClassLoader().getResourceAsStream("mappings/base-mapping.json"));
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
}
