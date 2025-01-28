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
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.mapping.BinaryProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
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

    /**
     * @return
     */
    public static List<String> getIndexes() {

	return MAPPINGS.stream().//
		map(i -> i.getIndex()).//
		collect(Collectors.toList());
    }

    /**
     * @param index
     */
    protected IndexMapping(String index) {

	this.index = index;
	this.mapping = new JSONObject(getBaseMapping());
    }

    /**
     * @return
     */
    public String getIndex() {

	return index;
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
     * @param key
     * @param type
     */
    protected void addProperty(String key, String type) {

	JSONObject property = new JSONObject();
	property.put("type", type);

	mapping.getJSONObject("mappings").//
		getJSONObject("properties").//
		put(key, property);
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
}
