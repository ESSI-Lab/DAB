/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch.core.IndexRequest;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.CacheMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ConfigurationMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class IndexData {

    /**
     * @author Fabrizio
     */
    public enum DataType {

	/**
	 * 
	 */
	BINARY("binary"),

	/**
	 * 
	 */
	DOC("doc");

	private String type;

	/**
	 * @param type
	 */
	private DataType(String type) {

	    this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {

	    return type;
	}

	/**
	 * @param type
	 * @return
	 */
	public static DataType decode(String type) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(t -> t.getType().equals(type)).//
		    findFirst().//
		    orElseThrow();
	}
    }

    public static final String BINARY_DATA_TYPE = "binary";

    //
    // base source properties
    //
    public static final String INDEX = "index"; // see wrapper.decorateSource
    public static final String ENTRY_ID = "entryId"; // see wrapper.decorateSource

    public static final String DATABASE_ID = "databaseId";
    public static final String FOLDER_NAME = "folderName";
    public static final String FOLDER_ID = "folderId";
    public static final String ENTRY_NAME = "entryName";
    public static final String BINARY_PROPERTY = "binaryProperty";
    public static final String DATA_TYPE = "dataType";

    //
    //
    //

    public static final String _INDEX = "_index";

    private JSONObject object;
    private IndexMapping mapping;
    private String entryId;
    private String index;

    /**
     * @param folder
     * @param key
     */
    public static IndexData of(DatabaseFolder folder) {

	IndexData indexData = new IndexData();

	String entryName = FolderRegistryMapping.getEntryName(folder);

	indexData.put(ENTRY_NAME, entryName);
	indexData.put(DATABASE_ID, folder.getDatabase().getIdentifier());

	indexData.put(FOLDER_NAME, folder.getName());
	indexData.put(FOLDER_ID, OpenSearchFolder.getFolderId(folder));

	indexData.put(DATA_TYPE, EntryType.REGISTERED_FOLDER);

	indexData.entryId = FolderRegistryMapping.getEntryId(folder);

	indexData.mapping = FolderRegistryMapping.get();

	indexData.mapping.setEntryType(EntryType.REGISTERED_FOLDER);

	indexData.index = FolderRegistryMapping.get().getIndex();

	return indexData;
    }

    /**
     * @param folder
     * @param key
     * @param entry
     * @param type
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    @SuppressWarnings("incomplete-switch")
    public static IndexData of(//
	    DatabaseFolder folder, //
	    String key, //
	    FolderEntry entry, //
	    EntryType type)

	    throws IOException, TransformerException {

	IndexData indexData = new IndexData();

	//
	// put the base properties
	//

	indexData.put(ENTRY_NAME, key);
	indexData.put(DATABASE_ID, folder.getDatabase().getIdentifier());
	indexData.put(FOLDER_NAME, folder.getName());
	indexData.put(FOLDER_ID, OpenSearchFolder.getFolderId(folder));

	indexData.put(DATA_TYPE, entry.getDataType());

	indexData.entryId = OpenSearchFolder.getEntryId(folder, key);

	//
	// encodes the binary property
	//

	ClonableInputStream stream = null;

	if (entry.getStream().isPresent()) {

	    stream = new ClonableInputStream(entry.getStream().get());
	}

	String encodedString = ConversionUtils.encode(entry.getDocument().orElse(null), stream);

	//
	//
	//

	switch (type) {

	case GS_RESOURCE:

	    indexData.put(BINARY_PROPERTY, DataFolderMapping.GS_RESOURCE);
	    indexData.put(DataFolderMapping.GS_RESOURCE, encodedString);

	    String dataFolder = folder.getName().endsWith(SourceStorageWorker.DATA_1_SHORT_POSTFIX) //
		    ? SourceStorageWorker.DATA_1_SHORT_POSTFIX //
		    : SourceStorageWorker.DATA_2_SHORT_POSTFIX; //

	    indexData.put(MetaFolderMapping.DATA_FOLDER, dataFolder);

	    GSResource gsResource = GSResource.createOrNull(entry.getDocument().get());

	    IndexesMetadata indexesMd = gsResource.getIndexesMetadata();

	    //
	    // shape
	    //

	    Optional<Shape> shape = Optional.empty();

	    List<BoundingPolygon> boundingPolygons = gsResource.getHarmonizedMetadata().//
		    getCoreMetadata().//
		    getDataIdentification().//
		    getBoundingPolygonsList();

	    if (!boundingPolygons.isEmpty()) {

		shape = Shape.of(boundingPolygons.get(0));
	    }

	    if (shape.isEmpty() && indexesMd.readBoundingBox().isPresent()) {

		shape = Shape.of(indexesMd.readBoundingBox().get());
	    }

	    if (!shape.isEmpty()) {

		indexData.put(MetadataElement.BOUNDING_BOX.getName(), shape.get().getShape());
		indexData.put(BoundingBox.AREA_ELEMENT_NAME, shape.get().getArea());
	    }

	    //
	    // temp extent begin
	    //

	    put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_BEGIN, DateTime.class);

	    if (!indexesMd.read(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName()).isEmpty()) {

		put(indexesMd, indexData, IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName(), Boolean.class);
	    }

	    //
	    // temp extent end
	    //
	    put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_END, DateTime.class);

	    if (!indexesMd.read(IndexedElements.TEMP_EXTENT_END_NOW.getElementName()).isEmpty()) {

		put(indexesMd, indexData, IndexedElements.TEMP_EXTENT_END_NOW.getElementName(), Boolean.class);
	    }

	    //
	    // other metadata elements
	    //
	    MetadataElement.listValues().forEach(el -> {

		put(el, indexesMd, indexData);
	    });

	    //
	    // resource properties
	    //
	    ResourceProperty.listValues().forEach(rp -> {

		put(rp, indexesMd, indexData);
	    });

	    //
	    // keyword fields for aggregation
	    //
	    DataFolderMapping.AGGREGABLE_FIELDS.forEach(field -> {

		put(indexesMd, indexData, field, KeywordProperty.class);
	    });

	    indexData.mapping = DataFolderMapping.get();

	    break;

	case WRITING_FOLDER_TAG:

	    indexData.put(BINARY_PROPERTY, DataFolderMapping.WRITING_FOLDER_TAG);
	    indexData.put(DataFolderMapping.WRITING_FOLDER_TAG, encodedString);

	    indexData.mapping = DataFolderMapping.get();

	    break;

	case AUGMENTER_PROPERTIES:

	    indexData.put(BINARY_PROPERTY, AugmentersMapping.AUGMENTER_PROPERTIES);
	    indexData.put(AugmentersMapping.AUGMENTER_PROPERTIES, encodedString);

	    indexData.mapping = AugmentersMapping.get();

	    break;

	case CONFIGURATION:

	    indexData.put(BINARY_PROPERTY, ConfigurationMapping.CONFIGURATION);
	    indexData.put(ConfigurationMapping.CONFIGURATION, encodedString);

	    indexData.put(ConfigurationMapping.CONFIGURATION_NAME, key);

	    indexData.mapping = ConfigurationMapping.get();

	    break;

	case CONFIGURATION_LOCK:

	    indexData.put(BINARY_PROPERTY, ConfigurationMapping.CONFIGURATION_LOCK);
	    indexData.put(ConfigurationMapping.CONFIGURATION_LOCK, encodedString);

	    indexData.mapping = ConfigurationMapping.get();

	    break;

	case USER:

	    indexData.put(BINARY_PROPERTY, UsersMapping.USER);
	    indexData.put(UsersMapping.USER, encodedString);

	    GSUser user = GSUser.createOrNull(entry.getDocument().get());

	    indexData.put(UsersMapping.USER_ID, user.getIdentifier());
	    user.getUserIdentifierType().ifPresent(t -> indexData.put(UsersMapping.USER_ID_TYPE, t.getType()));

	    indexData.put(UsersMapping.ENABLED, user.isEnabled());
	    indexData.put(UsersMapping.USER_ROLE, user.getRole());

	    indexData.mapping = UsersMapping.get();

	    break;

	case VIEW:

	    indexData.put(BINARY_PROPERTY, ViewsMapping.VIEW);
	    indexData.put(ViewsMapping.VIEW, encodedString);

	    try {

		View view = View.fromStream(stream.clone());

		indexData.put(ViewsMapping.VIEW_ID, view.getId());
		indexData.put(ViewsMapping.VIEW_LABEL, view.getLabel());
		indexData.put(ViewsMapping.VIEW_OWNER, view.getOwner());
		indexData.put(ViewsMapping.VIEW_CREATOR, view.getCreator());
		indexData.put(ViewsMapping.VIEW_VISIBILITY, view.getVisibility().name());

	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(IndexData.class).error(e);
	    }

	    indexData.mapping = ViewsMapping.get();

	    break;

	case DATA_FOLDER_INDEX_DOC:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.INDEX_DOC);
	    indexData.put(MetaFolderMapping.INDEX_DOC, encodedString);

	    DataFolderIndexDocument doc = new DataFolderIndexDocument(entry.getDocument().get());

	    indexData.put(MetaFolderMapping.DATA_FOLDER, doc.getShortDataFolderPostfix());

	    String sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_ERROR_REPORT:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.ERRORS_REPORT);
	    indexData.put(MetaFolderMapping.ERRORS_REPORT, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_WARN_REPORT:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.WARN_REPORT);
	    indexData.put(MetaFolderMapping.WARN_REPORT, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_PROPERTIES:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.HARVESTING_PROPERTIES);
	    indexData.put(MetaFolderMapping.HARVESTING_PROPERTIES, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;
	case CACHE_ENTRY:

	    indexData.put(BINARY_PROPERTY, CacheMapping.CACHED_ENTRY);
	    indexData.put(CacheMapping.CACHED_ENTRY, encodedString);

	    indexData.mapping = CacheMapping.get();

	    break;
	}

	indexData.index = indexData.mapping.getIndex();

	indexData.mapping.setEntryType(type);

	return indexData;
    }

    /**
     * @param folder
     */
    public static String detectIndex(OpenSearchFolder folder) {

	String name = folder.getName();

	if (name.endsWith(FolderRegistryMapping.ENTRY_POSTFIX)) {

	    return FolderRegistryMapping.get().getIndex();
	}

	if (name.contains(SourceStorageWorker.META_PREFIX)) {

	    return MetaFolderMapping.get().getIndex();
	}

	else if (name.contains(SourceStorageWorker.DATA_1_POSTFIX) || name.contains(SourceStorageWorker.DATA_2_POSTFIX)) {

	    return DataFolderMapping.get().getIndex();
	}

	else if (name.contains(Database.USERS_FOLDER)) {

	    return UsersMapping.get().getIndex();
	}

	else if (name.contains(Database.VIEWS_FOLDER)) {

	    return ViewsMapping.get().getIndex();
	}

	else if (name.contains(Database.AUGMENTERS_FOLDER)) {

	    return AugmentersMapping.get().getIndex();
	}

	else if (name.contains(Database.CACHE_FOLDER)) {

	    return CacheMapping.get().getIndex();

	} else {// name.contains(Database.CONFIGURATION_FOLDER

	    return ConfigurationMapping.get().getIndex();
	}
    }

    /**
     * 
     */
    private IndexData() {

	object = new JSONObject();
    }

    /**
     * @param field
     * @param value
     */
    private void put(String field, Object value) {

	object.put(field, value);
    }

    /**
     * @return the request
     */
    public IndexMapping getMapping() {

	return mapping;
    }

    /**
     * @return
     */
    public IndexRequest<JSONObject> getRequest() {

	return new IndexRequest.Builder<JSONObject>().//
		index(mapping.getIndex()).//
		document(object).//
		id(entryId).//
		build();
    }

    /**
     * @return the index
     */
    public String getIndex() {

	return index;
    }

    /**
     * @return the
     */
    public String getEntryId() {

	return entryId;
    }

    /**
     * @return
     */
    public String getData() {

	return object.toString();
    }

    @Override
    public String toString() {

	return object.toString(3);
    }

    /**
     * @param quer
     * @param indexesMd
     * @param indexData
     */
    @SuppressWarnings("incomplete-switch")
    private static void put(Queryable quer, IndexesMetadata indexesMd, IndexData indexData) {

	switch (quer.getContentType()) {
	case BOOLEAN:
	    put(indexesMd, indexData, quer.getName(), Boolean.class);
	    break;
	case DOUBLE:
	    put(indexesMd, indexData, quer.getName(), Double.class);
	    break;
	case INTEGER:
	    put(indexesMd, indexData, quer.getName(), Integer.class);
	    break;
	case LONG:
	    put(indexesMd, indexData, quer.getName(), Long.class);
	    break;
	case TEXTUAL:
	    put(indexesMd, indexData, quer.getName(), String.class);
	    break;
	case ISO8601_DATE:
	case ISO8601_DATE_TIME:
	    put(indexesMd, indexData, quer.getName(), DateTime.class);
	    break;
	}

    }

    /**
     * @param indexesMd
     * @param indexData
     * @param elName
     * @param valueClass
     */
    private static void put(IndexesMetadata indexesMd, IndexData indexData, String elName, Class<?> valueClass) {

	JSONArray array = new JSONArray();

	indexesMd.read(elName).forEach(v -> { //

	    if (valueClass.equals(String.class) || valueClass.equals(KeywordProperty.class)) {

		array.put(String.valueOf(v));

	    } else if (valueClass.equals(Integer.class)) {

		array.put(Integer.valueOf(v));

	    } else if (valueClass.equals(Double.class)) {

		array.put(Double.valueOf(v));

	    } else if (valueClass.equals(Long.class)) {

		array.put(Long.valueOf(v));

	    } else if (valueClass.equals(Boolean.class)) {

		boolean val = Boolean.valueOf(v);

		//
		// particular case to support tmpExtentEnd_Now and tmpExtentBegin_Now elements that in the
		// GSResource indexed elements, if present, they have no value but they indicates 'true'
		//
		if (v.isEmpty()) {

		    val = true;
		}

		array.put(val);

	    } else if (valueClass.equals(DateTime.class)) {

		ConversionUtils.parseToLong(v).ifPresent(dt -> array.put(dt));
	    }
	});

	if (array.length() > 0) {

	    // keyword fields used for aggregation
	    String name = valueClass.equals(KeywordProperty.class) ? DataFolderMapping.toAggField(elName) : elName;

	    indexData.put(name, array);
	}
    }

    /**
     * @param metadata
     * @param indexData
     * @param mel
     * @param valueClass
     */
    private static void put(IndexesMetadata metadata, IndexData indexData, MetadataElement mel, Class<?> valueClass) {

	put(metadata, indexData, mel.getName(), valueClass);
    }

}
