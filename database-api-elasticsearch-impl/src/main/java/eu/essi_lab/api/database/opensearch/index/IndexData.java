/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

import java.io.File;
import java.io.FileInputStream;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.CacheMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ConfigurationMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.zip.Unzipper;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.composed.ComposedElement;

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
     * @param resourceDocument
     * @return
     * @throws Exception
     */
    public static IndexData of(OpenSearchFolder folder, Document resourceDoc) throws Exception {

	IndexData indexData = new IndexData();

	XMLDocumentHandler handler = new XMLDocumentHandler(resourceDoc);

	String privateId = readValues(handler, ResourceProperty.PRIVATE_ID.getName()).get(0);

	//
	// put the base properties
	//

	indexData.put(ENTRY_NAME, privateId);
	indexData.put(DATABASE_ID, folder.getDatabase().getIdentifier());
	indexData.put(FOLDER_NAME, folder.getName());
	indexData.put(FOLDER_ID, OpenSearchFolder.getFolderId(folder));

	indexData.put(DATA_TYPE, "doc");

	indexData.entryId = OpenSearchFolder.getEntryId(folder, privateId);

	String dataFolder = folder.getName().endsWith(SourceStorageWorker.DATA_1_SHORT_POSTFIX) //
		? SourceStorageWorker.DATA_1_SHORT_POSTFIX //
		: SourceStorageWorker.DATA_2_SHORT_POSTFIX; //

	indexData.put(MetaFolderMapping.DATA_FOLDER, dataFolder);

	handleResource(indexData, handler);

	String encodedString = OpenSearchUtils.encode(handler.getDocument());

	indexData.put(BINARY_PROPERTY, DataFolderMapping.GS_RESOURCE);
	indexData.put(DataFolderMapping.GS_RESOURCE, encodedString);

	indexData.mapping = DataFolderMapping.get();

	indexData.index = indexData.mapping.getIndex();

	indexData.mapping.setEntryType(EntryType.GS_RESOURCE);

	return indexData;
    }

    /**
     * @param folder
     * @param resourceStream
     * @return
     * @throws Exception
     */
    public static IndexData of(OpenSearchFolder folder, InputStream resourceStream) throws Exception {

	return IndexData.of(folder, (Document) OpenSearchUtils.toNode(resourceStream));
    }

    /**
     * @param folder
     * @param resourcefile
     * @return
     * @throws Exception
     */
    public static IndexData of(OpenSearchFolder folder, File resourcefile) throws Exception {

	return of(folder, new FileInputStream(resourcefile));
    }

    /**
     * @param folder
     * @param gsResource
     * @return
     * @throws Exception
     */
    public static IndexData of(OpenSearchFolder folder, GSResource gsResource) throws Exception {

	return IndexData.of(folder, gsResource.asDocument(false));
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

	    throws Exception {

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
	//
	//

	switch (type) {

	case GS_RESOURCE:

	    return IndexData.of((OpenSearchFolder) folder, entry.getDocument().get());

	case AUGMENTER_PROPERTIES:

	    indexData.put(BINARY_PROPERTY, AugmentersMapping.AUGMENTER_PROPERTIES);
	    indexData.put(AugmentersMapping.AUGMENTER_PROPERTIES, encodeString(entry));

	    indexData.mapping = AugmentersMapping.get();

	    break;

	case CONFIGURATION:

	    indexData.put(BINARY_PROPERTY, ConfigurationMapping.CONFIGURATION);
	    indexData.put(ConfigurationMapping.CONFIGURATION, encodeString(entry));

	    indexData.put(ConfigurationMapping.CONFIGURATION_NAME, key);

	    indexData.mapping = ConfigurationMapping.get();

	    break;

	case CONFIGURATION_LOCK:

	    indexData.put(BINARY_PROPERTY, ConfigurationMapping.CONFIGURATION_LOCK);
	    indexData.put(ConfigurationMapping.CONFIGURATION_LOCK, encodeString(entry));

	    indexData.mapping = ConfigurationMapping.get();

	    break;

	case USER:

	    indexData.put(BINARY_PROPERTY, UsersMapping.USER);
	    indexData.put(UsersMapping.USER, encodeString(entry));

	    GSUser user = GSUser.createOrNull(entry.getDocument().get());

	    indexData.put(UsersMapping.USER_ID, user.getIdentifier());
	    user.getUserIdentifierType().ifPresent(t -> indexData.put(UsersMapping.USER_ID_TYPE, t.getType()));

	    indexData.put(UsersMapping.ENABLED, user.isEnabled());
	    indexData.put(UsersMapping.USER_ROLE, user.getRole());

	    indexData.put(IndexMapping.toKeywordField(UsersMapping.USER_ID), user.getIdentifier());
	    user.getUserIdentifierType().ifPresent(t -> indexData.put(

		    IndexMapping.toKeywordField(UsersMapping.USER_ID_TYPE), t.getType()));

	    indexData.put(IndexMapping.toKeywordField(UsersMapping.USER_ROLE), user.getRole());

	    indexData.mapping = UsersMapping.get();

	    break;

	case VIEW:

	    ClonableInputStream stream = new ClonableInputStream(entry.getStream().get());

	    indexData.put(BINARY_PROPERTY, ViewsMapping.VIEW);
	    indexData.put(ViewsMapping.VIEW, OpenSearchUtils.encode(stream.clone()));

	    try {

		View view = View.fromStream(stream.clone());

		indexData.put(ViewsMapping.VIEW_ID, view.getId());
		indexData.put(ViewsMapping.VIEW_LABEL, view.getLabel());
		indexData.put(ViewsMapping.VIEW_OWNER, view.getOwner());
		indexData.put(ViewsMapping.VIEW_CREATOR, view.getCreator());
		indexData.put(ViewsMapping.VIEW_VISIBILITY, view.getVisibility().name());

		if (view.getSourceDeployment() != null) {

		    indexData.put(ViewsMapping.SOURCE_DEPLOYMENT, view.getSourceDeployment());
		}

		indexData.put(IndexMapping.toKeywordField(ViewsMapping.VIEW_ID), view.getId());
		indexData.put(IndexMapping.toKeywordField(ViewsMapping.VIEW_LABEL), view.getLabel());
		indexData.put(IndexMapping.toKeywordField(ViewsMapping.VIEW_OWNER), view.getOwner());
		indexData.put(IndexMapping.toKeywordField(ViewsMapping.VIEW_CREATOR), view.getCreator());
		indexData.put(IndexMapping.toKeywordField(ViewsMapping.VIEW_VISIBILITY), view.getVisibility().name());

		if (view.getSourceDeployment() != null) {

		    indexData.put(IndexMapping.toKeywordField(ViewsMapping.SOURCE_DEPLOYMENT), view.getSourceDeployment());
		}

	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(IndexData.class).error(e);
	    }

	    indexData.mapping = ViewsMapping.get();

	    break;

	case WRITING_FOLDER_TAG:

	    String dataFolder = folder.getName().endsWith(SourceStorageWorker.DATA_1_SHORT_POSTFIX) //
		    ? SourceStorageWorker.DATA_1_SHORT_POSTFIX //
		    : SourceStorageWorker.DATA_2_SHORT_POSTFIX; //

	    indexData.put(MetaFolderMapping.DATA_FOLDER, dataFolder);

	    String sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);

	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.SOURCE_ID), sourceId);

	    indexData.put(BINARY_PROPERTY, DataFolderMapping.WRITING_FOLDER_TAG);
	    indexData.put(DataFolderMapping.WRITING_FOLDER_TAG, "");

	    indexData.mapping = DataFolderMapping.get();

	    break;

	case DATA_FOLDER_INDEX_DOC:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.INDEX_DOC);
	    indexData.put(MetaFolderMapping.INDEX_DOC, encodeString(entry));

	    DataFolderIndexDocument doc = new DataFolderIndexDocument(entry.getDocument().get());

	    indexData.put(MetaFolderMapping.DATA_FOLDER, doc.getShortDataFolderPostfix());
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.DATA_FOLDER), doc.getShortDataFolderPostfix());

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);

	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.SOURCE_ID), sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_ERROR_REPORT:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.ERRORS_REPORT);
	    indexData.put(MetaFolderMapping.ERRORS_REPORT, encodeString(entry));

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);

	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.SOURCE_ID), sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_WARN_REPORT:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.WARN_REPORT);
	    indexData.put(MetaFolderMapping.WARN_REPORT, encodeString(entry));

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);

	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.SOURCE_ID), sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case HARVESTING_PROPERTIES:

	    indexData.put(BINARY_PROPERTY, MetaFolderMapping.HARVESTING_PROPERTIES);
	    indexData.put(MetaFolderMapping.HARVESTING_PROPERTIES, encodeString(entry));

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);

	    indexData.put(MetaFolderMapping.SOURCE_ID, sourceId);
	    indexData.put(IndexMapping.toKeywordField(MetaFolderMapping.SOURCE_ID), sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    break;

	case CACHE_ENTRY:

	    indexData.put(BINARY_PROPERTY, CacheMapping.CACHED_ENTRY);
	    indexData.put(CacheMapping.CACHED_ENTRY, encodeString(entry));

	    indexData.mapping = CacheMapping.get();

	    break;
	}

	indexData.index = indexData.mapping.getIndex();

	indexData.mapping.setEntryType(type);

	return indexData;
    }

    /**
     * @param key
     * @param entry
     * @return
     */
    public static List<IndexData> ofShapeFile(OpenSearchFolder folder, String key, FolderEntry entry) throws Exception {

	ClonableInputStream inputStream = new ClonableInputStream(entry.getStream().get());

	Unzipper unzipper = new Unzipper(inputStream.clone());

	File shapeFile = unzipper.unzipAll().stream().filter(f -> f.getName().endsWith(".shp")).findFirst().get();

	List<JSONObject> objects = ShapeFileMapper.map(shapeFile);

	ArrayList<IndexData> out = new ArrayList<IndexData>();

	for (JSONObject object : objects) {

	    IndexData indexData = new IndexData();

	    indexData.mapping = ShapeFileMapping.get();

	    indexData.index = indexData.mapping.getIndex();

	    indexData.mapping.setEntryType(EntryType.SHAPE_FILE);

	    //
	    //
	    //

	    String entryName = key + "_" + object.getString(IndexData.ENTRY_NAME);

	    String userId = object.optString(ShapeFileMapping.USER_ID);

	    JSONObject shape = object.getJSONObject(ShapeFileMapping.SHAPE);

	    //
	    // put the base properties
	    //

	    indexData.put(ENTRY_NAME, entryName);
	    indexData.put(DATABASE_ID, folder.getDatabase().getIdentifier());
	    indexData.put(FOLDER_NAME, folder.getName());
	    indexData.put(FOLDER_ID, OpenSearchFolder.getFolderId(folder));

	    indexData.put(DATA_TYPE, entry.getDataType());

	    indexData.entryId = OpenSearchFolder.getEntryId(folder, entryName);

	    //
	    //
	    //

	    indexData.put(BINARY_PROPERTY, ShapeFileMapping.SHAPE_FILE);
	    indexData.put(ShapeFileMapping.SHAPE_FILE, Base64.getEncoder().encodeToString(object.toString().getBytes("UTF-8")));

	    indexData.put(ShapeFileMapping.SHAPE, shape);

	    out.add(indexData);
	}

	// clears the temp folder
	Arrays.asList(unzipper.getOutputFolder().listFiles()).forEach(f -> f.delete());

	return out;
    }

    /**
     * @param folder
     */
    public static String detectIndex(OpenSearchFolder folder) {

	String name = folder.getName();

	if (name.endsWith(FolderRegistryMapping.ENTRY_POSTFIX)) {

	    return FolderRegistryMapping.get().getIndex();
	}

	if (name.contains(SourceStorageWorker.META_POSTFIX)) {

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

	} else if (name.contains(Database.SHAPE_FILES_FOLDER)) {

	    return ShapeFileMapping.get().getIndex();

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
     * @return the request
     */
    public IndexMapping getMapping() {

	return mapping;
    }

    /**
     * @return
     */
    public IndexRequest<Map<String, Object>> getIndexRequest() {

	return new IndexRequest.Builder<Map<String, Object>>().//
		index(mapping.getIndex()).//
		document(object.toMap()).//
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
    public String getDataString() {

	return object.toString();
    }

    /**
     * @return
     */
    public JSONObject getDataObject() {

	return object;
    }

    @Override
    public String toString() {

	return object.toString(3);
    }

    /**
     * @param entry
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    private static String encodeString(FolderEntry entry) throws IOException, TransformerException {

	ClonableInputStream stream = null;

	if (entry.getStream().isPresent()) {

	    stream = new ClonableInputStream(entry.getStream().get());
	}

	return OpenSearchUtils.encode(entry.getDocument().orElse(null), stream);
    }

    /**
     * @param field
     * @param value
     */
    private void put(String field, Object value) {

	object.put(field, value);
    }

    /**
     * @param indexData
     * @param gsResource
     * @throws XPathExpressionException
     * @throws JAXBException
     */
    private static void handleResource(IndexData indexData, XMLDocumentHandler handler) throws XPathExpressionException, JAXBException {

	//
	// shape
	//

	Optional<Shape> shape = Optional.empty();

	Optional<BoundingPolygon> polygon = handler.evaluateNodes("//*:EX_BoundingPolygon").//
		stream().//
		map(n -> BoundingPolygon.createOrNull(n)).//
		findFirst();

	if (polygon.isPresent()) {

	    shape = Shape.of(polygon.get());
	}

	Node bboxNode = handler.evaluateNode("//*:indexesMetadata/*:bbox");

	if (bboxNode != null) {

	    BoundingBox boundingBox = BoundingBox.create(bboxNode);

	    shape = Shape.of(boundingBox);
	}

	if (!shape.isEmpty()) {

	    indexData.put(MetadataElement.BOUNDING_BOX.getName(), shape.get().getShape());
	    indexData.put(BoundingBox.AREA_ELEMENT_NAME, shape.get().getArea());
	    indexData.put(DataFolderMapping.CENTROID, shape.get().getCentroid());
	}

	//
	// composed elements
	//

	List<Node> composedNodes = handler.evaluateNodes("//*:indexesMetadata/*:composedElement");

	if (!composedNodes.isEmpty()) {

	    List<ComposedElement> composedList = composedNodes.//
		    stream().//
		    map(el -> create(el)).//
		    filter(Objects::nonNull).//
		    distinct().//
		    collect(Collectors.toList());

	    composedList.stream().map(el -> el.getName()).distinct().forEach(elName -> {

		JSONArray nested = new JSONArray();

		composedList.//
			stream().//
			filter(el -> el.getName().equals(elName)).//
			forEach(composed -> {

			    JSONObject item = new JSONObject();
			    nested.put(item);

			    composed.getProperties().//
				    forEach(prop -> {

					item.put(prop.getName(), prop.getValue());

					if (prop.getType() == ContentType.TEXTUAL) {

					    item.put(IndexMapping.toKeywordField(prop.getName()), prop.getValue());
					}
				    });
			});

		indexData.put(elName, nested);
	    });
	}

	//
	// temp extent begin
	//

	List<String> values = readValues(handler, MetadataElement.TEMP_EXTENT_BEGIN.getName());

	if (!values.isEmpty()) {

	    put(values, indexData, MetadataElement.TEMP_EXTENT_BEGIN.getName(), DateTime.class);
	}

	//
	// temp extent begin now
	//

	if (exists(handler, MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName())) {

	    put(Arrays.asList("true"), indexData, MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName(), Boolean.class);
	}

	//
	// temp extent end
	//

	values = readValues(handler, MetadataElement.TEMP_EXTENT_END.getName());

	if (!values.isEmpty()) {

	    put(values, indexData, MetadataElement.TEMP_EXTENT_END.getName(), DateTime.class);
	}

	//
	// temp extent end now
	//

	if (exists(handler, MetadataElement.TEMP_EXTENT_END_NOW.getName())) {

	    put(Arrays.asList("true"), indexData, MetadataElement.TEMP_EXTENT_END_NOW.getName(), Boolean.class);
	}

	//
	// other metadata elements
	//
	MetadataElement.listValues().forEach(el -> {

	    try {
		List<String> v = readValues(handler, el.getName());
		put(el, v, indexData);

	    } catch (XPathExpressionException ex) {

		GSLoggerFactory.getLogger(IndexData.class).error(ex);
	    }
	});

	//
	// resource properties
	//
	ResourceProperty.listValues().forEach(rp -> {

	    try {
		List<String> v = readValues(handler, rp.getName());
		put(rp, v, indexData);

	    } catch (XPathExpressionException ex) {

		GSLoggerFactory.getLogger(IndexData.class).error(ex);
	    }
	});

	// clear the indexes (all but the bbox) before storing the binary property
	handler.remove("//*:indexesMetadata");
    }

    /**
     * @param node
     * @return
     */
    private static ComposedElement create(Node node) {

	try {
	    return ComposedElement.of(node);
	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(IndexData.class).error(e);
	}

	return null;
    }

    /**
     * @param handler
     * @param elName
     * @return
     * @throws XPathExpressionException
     */
    private static boolean exists(XMLDocumentHandler handler, String elName) {

	try {
	    return handler.evaluateBoolean("exists(//*:indexesMetadata/*:" + elName + ")");
	} catch (XPathExpressionException ex) {

	    GSLoggerFactory.getLogger(IndexData.class).error(ex);
	}

	return false;
    }

    /**
     * @param indexData
     * @param gsResource
     */
    @Deprecated
    private static void handleResource(IndexData indexData, GSResource gsResource) {

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
	    indexData.put(DataFolderMapping.CENTROID, shape.get().getCentroid());
	}

	//
	// temp extent begin
	//

	put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_BEGIN, DateTime.class);

	//
	// temp extent begin now
	//

	if (!indexesMd.read(MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName()).isEmpty()) {

	    // this element has the empty string value!
	    put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_BEGIN_NOW.getName(), Boolean.class);
	}

	//
	// temp extent end
	//

	put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_END, DateTime.class);

	//
	// temp extent end now
	//

	if (!indexesMd.read(MetadataElement.TEMP_EXTENT_END_NOW.getName()).isEmpty()) {

	    // this element has the empty string value!
	    put(indexesMd, indexData, MetadataElement.TEMP_EXTENT_END_NOW.getName(), Boolean.class);
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

	// clear the indexes (all but the bbox) before storing the binary property
	indexesMd.clear(false);
    }

    /**
     * @param handler
     * @param el
     * @return
     * @throws XPathExpressionException
     */
    private static List<String> readValues(XMLDocumentHandler handler, String el) throws XPathExpressionException {

	return handler.evaluateTextContent("//*:indexesMetadata/*:" + el + "/text()");
    }

    /**
     * @param quer
     * @param indexesMd
     * @param indexData
     */
    private static void put(Queryable quer, IndexesMetadata indexesMd, IndexData indexData) {

	put(quer, indexesMd.read(quer.getName()), indexData);
    }

    /**
     * @param indexesMd
     * @param indexData
     * @param elName
     * @param valueClass
     */
    private static void put(IndexesMetadata indexesMd, IndexData indexData, String elName, Class<?> valueClass) {

	put(indexesMd.read(elName), indexData, elName, valueClass);
    }

    /**
     * @param indexesMd
     * @param indexData
     * @param mel
     * @param valueClass
     */
    private static void put(IndexesMetadata indexesMd, IndexData indexData, MetadataElement mel, Class<?> valueClass) {

	put(indexesMd, indexData, mel.getName(), valueClass);
    }

    /**
     * @param quer
     * @param values
     * @param indexData
     */
    @SuppressWarnings("incomplete-switch")
    private static void put(Queryable quer, List<String> values, IndexData indexData) {

	switch (quer.getContentType()) {
	case BOOLEAN -> put(values, indexData, quer.getName(), Boolean.class);
	case DOUBLE -> put(values, indexData, quer.getName(), Double.class);
	case INTEGER -> put(values, indexData, quer.getName(), Integer.class);
	case LONG -> put(values, indexData, quer.getName(), Long.class);
	case TEXTUAL -> {
	    put(values, indexData, quer.getName(), String.class);
	    // keyword fields used for aggregation and wildcard queries
	    put(values, indexData, quer.getName(), KeywordProperty.class);
	}
	case ISO8601_DATE, ISO8601_DATE_TIME -> put(values, indexData, quer.getName(), DateTime.class);
	}
    }

    /**
     * @param values
     * @param indexData
     * @param elName
     * @param valueClass
     */
    private static void put(List<String> values, IndexData indexData, String elName, Class<?> valueClass) {

	JSONArray array = new JSONArray();

	// only distinct values
	values = values.stream().distinct().collect(Collectors.toList());

	values.forEach(value -> { //

	    if (valueClass.equals(String.class) || valueClass.equals(KeywordProperty.class)) {

		value = value.trim().strip();

		if (valueClass.equals(KeywordProperty.class) && value.length() > IndexMapping.MAX_KEYWORD_LENGTH) {

		    value = value.substring(0, IndexMapping.MAX_KEYWORD_LENGTH);
		}

		if (value.length() > 1) {

		    array.put(String.valueOf(value));
		}

	    } else if (valueClass.equals(Integer.class)) {

		array.put(Integer.valueOf(value));

	    } else if (valueClass.equals(Double.class)) {

		array.put(Double.valueOf(value));

	    } else if (valueClass.equals(Long.class)) {

		array.put(Long.valueOf(value));

	    } else if (valueClass.equals(Boolean.class)) {

		boolean val = Boolean.valueOf(value);

		//
		// particular case to support tmpExtentEnd_Now and tmpExtentBegin_Now elements that in the
		// GSResource indexed elements, if present, they have no value but they indicates 'true'
		//
		if (value.isEmpty()) {

		    val = true;
		}

		array.put(val);

	    } else if (valueClass.equals(DateTime.class)) {

		OpenSearchUtils.parseToLong(value).ifPresent(dt -> array.put(dt));
	    }
	});

	if (array.length() > 0) {

	    if (valueClass.equals(DateTime.class)) {

		if (!array.isEmpty()) {

		    // dates always indexed as long
		    indexData.put(elName, array);
		}

		JSONArray dateTimeStringArray = new JSONArray();

		array.toList().//
			stream().//

			// mapping to ISO-8601 string
			map(v -> ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(Long.valueOf(v.toString())))).

			forEach(date -> dateTimeStringArray.put(date));

		if (!dateTimeStringArray.isEmpty()) {

		    String dateField = DataFolderMapping.toDateField(elName);

		    indexData.put(dateField, dateTimeStringArray);
		}

	    } else {

		// keyword fields used for aggregation and wildcard queries
		String name = valueClass.equals(KeywordProperty.class) ? DataFolderMapping.toKeywordField(elName) : elName;

		indexData.put(name, array);
	    }
	}
    }

}
