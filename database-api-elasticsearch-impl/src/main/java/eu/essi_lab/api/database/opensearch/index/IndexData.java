/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

import java.io.ByteArrayInputStream;

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
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ConfigurationMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MiscMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;
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
    // base properties
    //
    public static final String INDEX = "index";
    public static final String DATABASE_ID = "databaseId";
    public static final String FOLDER_NAME = "folderName";
    public static final String FOLDER_ID = "folderId";
    public static final String ENTRY_NAME = "entryName";
    public static final String ENTRY_ID = "entryId";
    public static final String BINARY_PROPERTY = "binaryProperty";
    public static final String DATA_TYPE = "dataType";

    //
    //
    //

    public static final String ALL_INDEXES = "_all";

    private JSONObject object;
    private IndexMapping mapping;
    private String indexId;
    private String index;

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

	indexData.object.put(ENTRY_NAME, key);
	indexData.object.put(DATABASE_ID, folder.getDatabase().getIdentifier());
	indexData.object.put(FOLDER_NAME, folder.getName());
	indexData.object.put(FOLDER_ID, OpenSearchFolder.getFolderId(folder));

	indexData.object.put(DATA_TYPE, entry.getDataType());

	indexData.indexId = OpenSearchFolder.getEntryId(folder, key);

	//
	// encodes the binary property
	//

	ClonableInputStream stream = null;

	if (entry.getStream().isPresent()) {

	    stream = new ClonableInputStream(entry.getStream().get());
	}

	String encodedString = encode(entry.getDocument().orElse(null), stream);

	//
	//
	//

	switch (type) {

	case GS_RESOURCE:

	    indexData.object.put(BINARY_PROPERTY, DataFolderMapping.GS_RESOURCE);
	    indexData.object.put(DataFolderMapping.GS_RESOURCE, encodedString);

	    GSResource gsResource = GSResource.createOrNull(entry.getDocument().get());

	    IndexesMetadata metadata = gsResource.getIndexesMetadata();

	    //
	    // bbox
	    //
	    if (metadata.readBoundingBox().isPresent()) {

		BoundingBox bbox = metadata.readBoundingBox().get();

		indexData.object.put(BoundingBox.AREA_ELEMENT_NAME, Double.valueOf(bbox.getArea()));
		indexData.object.put(BoundingBox.IS_CROSSED_ELEMENT_NAME, Boolean.valueOf(bbox.getIsCrossed()));

		bbox.getCardinalValues().forEach(cv -> { //

		    indexData.object.put(BoundingBox.SOUTH_ELEMENT_NAME, Double.valueOf(cv.getSouth()));
		    indexData.object.put(BoundingBox.WEST_ELEMENT_NAME, Double.valueOf(cv.getWest()));
		    indexData.object.put(BoundingBox.NORTH_ELEMENT_NAME, Double.valueOf(cv.getNorth()));
		    indexData.object.put(BoundingBox.EAST_ELEMENT_NAME, Double.valueOf(cv.getEast()));

		    indexData.object.put(MetadataElement.BOUNDING_BOX.getName(), asShape(cv));
		});

	    } else {

		indexData.object.put(IndexedElements.BOUNDING_BOX_NULL.getElementName(), true);
	    }

	    //
	    // temp extent begin
	    //
	    metadata.read(MetadataElement.TEMP_EXTENT_BEGIN).//
		    forEach(v -> parseDateTime(v).//
			    ifPresent(dt -> indexData.object.put(MetadataElement.TEMP_EXTENT_BEGIN.getName(), dt)));

	    if (!metadata.read(IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName()).isEmpty()) {

		put(metadata, indexData, IndexedElements.TEMP_EXTENT_BEGIN_NOW.getElementName(), Boolean.class);
	    }

	    if (!metadata.read(IndexedElements.TEMP_EXTENT_BEGIN_NULL.getElementName()).isEmpty()) {

		put(metadata, indexData, IndexedElements.TEMP_EXTENT_BEGIN_NULL.getElementName(), Boolean.class);
	    }

	    //
	    // temp extent end
	    //
	    metadata.read(MetadataElement.TEMP_EXTENT_END).//
		    forEach(v -> parseDateTime(v).//
			    ifPresent(dt -> indexData.object.put(MetadataElement.TEMP_EXTENT_END.getName(), dt)));

	    if (!metadata.read(IndexedElements.TEMP_EXTENT_END_NOW.getElementName()).isEmpty()) {

		put(metadata, indexData, IndexedElements.TEMP_EXTENT_END_NOW.getElementName(), Boolean.class);
	    }

	    if (!metadata.read(IndexedElements.TEMP_EXTENT_END_NULL.getElementName()).isEmpty()) {

		put(metadata, indexData, IndexedElements.TEMP_EXTENT_END_NULL.getElementName(), Boolean.class);
	    }

	    //
	    // other metadata elements
	    //
	    MetadataElement.listValues().forEach(el -> {

		switch (el.getContentType()) {
		case BOOLEAN:
		    put(metadata, indexData, el, Boolean.class);
		    break;
		case DOUBLE:
		    put(metadata, indexData, el, Double.class);
		    break;
		case INTEGER:
		    put(metadata, indexData, el, Integer.class);
		    break;
		case LONG:
		    put(metadata, indexData, el, Long.class);
		    break;
		case TEXTUAL:
		    put(metadata, indexData, el, String.class);
		    break;
		}
	    });

	    //
	    // resource properties
	    //
	    ResourceProperty.listValues().forEach(rp -> {

		switch (rp.getContentType()) {
		case BOOLEAN:
		    put(metadata, indexData, rp, Boolean.class);
		    break;
		case DOUBLE:
		    put(metadata, indexData, rp, Double.class);
		    break;
		case INTEGER:
		    put(metadata, indexData, rp, Integer.class);
		    break;
		case LONG:
		    put(metadata, indexData, rp, Long.class);
		    break;
		case TEXTUAL:
		    put(metadata, indexData, rp, String.class);
		    break;
		}
	    });

	    indexData.mapping = DataFolderMapping.get();

	    indexData.index = DataFolderMapping.get().getIndex();

	    break;

	case WRITING_FOLDER_TAG:

	    indexData.object.put(BINARY_PROPERTY, DataFolderMapping.WRITING_FOLDER_TAG);
	    indexData.object.put(DataFolderMapping.WRITING_FOLDER_TAG, encodedString);

	    indexData.mapping = DataFolderMapping.get();

	    indexData.index = DataFolderMapping.get().getIndex();

	    break;

	case AUGMENTER_PROPERTIES:

	    indexData.object.put(BINARY_PROPERTY, AugmentersMapping.AUGMENTER_PROPERTIES);
	    indexData.object.put(AugmentersMapping.AUGMENTER_PROPERTIES, encodedString);

	    indexData.mapping = AugmentersMapping.get();

	    indexData.index = AugmentersMapping.get().getIndex();

	    break;

	case CONFIGURATION:

	    indexData.mapping = ConfigurationMapping.get();

	    indexData.index = ConfigurationMapping.get().getIndex();

	    break;

	case MISC:

	    indexData.mapping = MiscMapping.get();

	    indexData.index = MiscMapping.get().getIndex();

	    break;

	case USER:

	    indexData.object.put(BINARY_PROPERTY, UsersMapping.USER);
	    indexData.object.put(UsersMapping.USER, encodedString);

	    GSUser user = GSUser.createOrNull(entry.getDocument().get());

	    indexData.object.put(UsersMapping.USER_ID, user.getIdentifier());
	    user.getUserIdentifierType().ifPresent(t -> indexData.object.put(UsersMapping.USER_ID_TYPE, t.getType()));

	    indexData.object.put(UsersMapping.ENABLED, user.isEnabled());
	    indexData.object.put(UsersMapping.USER_ROLE, user.getRole());

	    indexData.mapping = UsersMapping.get();

	    indexData.index = UsersMapping.get().getIndex();

	    break;

	case VIEW:

	    indexData.object.put(BINARY_PROPERTY, ViewsMapping.VIEW);
	    indexData.object.put(ViewsMapping.VIEW, encodedString);

	    try {

		View view = View.fromStream(stream.clone());

		indexData.object.put(ViewsMapping.VIEW_ID, view.getId());
		indexData.object.put(ViewsMapping.VIEW_LABEL, view.getLabel());
		indexData.object.put(ViewsMapping.VIEW_OWNER, view.getOwner());
		indexData.object.put(ViewsMapping.VIEW_CREATOR, view.getCreator());
		indexData.object.put(ViewsMapping.VIEW_VISIBILITY, view.getVisibility().name());

	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(IndexData.class).error(e);
	    }

	    indexData.mapping = ViewsMapping.get();

	    indexData.index = ViewsMapping.get().getIndex();

	    break;

	case DATA_FOLDER_INDEX_DOC:

	    indexData.object.put(BINARY_PROPERTY, MetaFolderMapping.INDEX_DOC);
	    indexData.object.put(MetaFolderMapping.INDEX_DOC, encodedString);

	    DataFolderIndexDocument doc = new DataFolderIndexDocument(entry.getDocument().get());

	    indexData.object.put(MetaFolderMapping.DATA_FOLDER, doc.getDataFolder());

	    String sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.object.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    indexData.index = MetaFolderMapping.get().getIndex();

	    break;

	case HARVESTING_ERROR_REPORT:

	    indexData.object.put(BINARY_PROPERTY, MetaFolderMapping.ERRORS_REPORT);
	    indexData.object.put(MetaFolderMapping.ERRORS_REPORT, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.object.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    indexData.index = MetaFolderMapping.get().getIndex();

	    break;

	case HARVESTING_WARN_REPORT:

	    indexData.object.put(BINARY_PROPERTY, MetaFolderMapping.WARN_REPORT);
	    indexData.object.put(MetaFolderMapping.WARN_REPORT, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.object.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    indexData.index = MetaFolderMapping.get().getIndex();

	    break;

	case HARVESTING_PROPERTIES:

	    indexData.object.put(BINARY_PROPERTY, MetaFolderMapping.HARVESTING_PROPERTIES);
	    indexData.object.put(MetaFolderMapping.HARVESTING_PROPERTIES, encodedString);

	    sourceId = DatabaseFolder.computeSourceId(folder.getDatabase(), folder);
	    indexData.object.put(MetaFolderMapping.SOURCE_ID, sourceId);

	    indexData.mapping = MetaFolderMapping.get();

	    indexData.index = MetaFolderMapping.get().getIndex();

	    break;
	}

	indexData.mapping.setEntryType(type);

	return indexData;
    }

    /**
     * @param folder
     */
    public static Optional<String> detectIndex(OpenSearchFolder folder) {

	String name = folder.getName();

	if (name.contains(SourceStorageWorker.META_PREFIX)) {

	    return Optional.of(MetaFolderMapping.META_FOLDER_INDEX);
	}

	if (name.contains(SourceStorageWorker.DATA_1_PREFIX) || name.contains(SourceStorageWorker.DATA_2_PREFIX)) {

	    return Optional.of(DataFolderMapping.DATA_FOLDER_INDEX);
	}

	if (name.contains(Database.USERS_FOLDER)) {

	    return Optional.of(UsersMapping.USERS_INDEX);
	}

	if (name.contains(Database.VIEWS_FOLDER)) {

	    return Optional.of(ViewsMapping.VIEWS_INDEX);
	}

	if (name.contains(Database.AUGMENTERS_FOLDER)) {

	    return Optional.of(AugmentersMapping.AUGMENTERS_INDEX);
	}

	//
	// misc-index and configuration-index cannot be
	// detected from the folder name
	//
	return Optional.empty();
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
    public IndexRequest<JSONObject> getRequest() {

	return new IndexRequest.Builder<JSONObject>().//
		index(mapping.getIndex()).//
		document(object).//
		id(indexId).//
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

	return indexId;
    }

    /**
     * @return
     */
    public String getData() {

	return object.toString();
    }

    /**
     * @param entry
     * @param stream
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    private static String encode(Document doc, ClonableInputStream stream) throws IOException, TransformerException {

	byte[] bytes = null;

	if (doc != null) {

	    bytes = toString(doc).getBytes();

	} else {

	    bytes = IOStreamUtils.getBytes(stream.clone());
	}

	return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @param entry
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    public static String encode(FolderEntry entry) throws IOException, TransformerException {

	byte[] bytes = null;

	if (entry.getDocument().isPresent()) {

	    Document doc = entry.getDocument().get();

	    bytes = toString(doc).getBytes();

	} else {

	    InputStream stream = entry.getStream().get();
	    bytes = IOStreamUtils.getBytes(stream);
	}

	return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @param binaryData
     * @return
     */
    public static InputStream decode(String binaryData) {

	byte[] decoded = Base64.getDecoder().decode(binaryData);
	return new ByteArrayInputStream(decoded);
    }

    /**
     * @param document
     * @return
     * @throws TransformerException
     */
    private static String toString(Document document) throws TransformerException {

	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();

	StringWriter stringWriter = new StringWriter();
	transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

	return stringWriter.toString();
    }

    /**
     * @param source
     * @return
     */
    @SuppressWarnings("unchecked")
    public static JSONObject toJSONObject(Object source) {

	return new JSONObject((HashMap<String, String>) source);
    }

    /**
     * @param source
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document toDocument(InputStream source) throws SAXException, IOException, ParserConfigurationException {

	DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
	DocumentBuilder builder = factory.newDocumentBuilder();

	return builder.parse(source);
    }

    /**
     * @param source
     * @return
     */
    public static InputStream toStream(JSONObject source) {

	String binaryProperty = source.getString("binaryProperty");
	String binaryData = source.getString(binaryProperty);

	return decode(binaryData);
    }

    /**
     * @param value
     * @return
     */
    private static Optional<Long> parseDateTime(String value) {

	if (value.equals("now")) {
	    return Optional.of(new Date().getTime());
	}

	Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(value);
	if (date.isEmpty()) {

	    date = ISO8601DateTimeUtils.parseNotStandardToDate(value);

	    if (date.isEmpty()) {

		date = ISO8601DateTimeUtils.parseNotStandard2ToDate(value);
	    }
	}

	return date.map(d -> d.getTime());
    }

    /**
     * 
     */
    private static final Double TOL = Math.pow(10, -4); // 11 meters

    /**
     * @param cardinalValues
     * @return
     */
    private static Optional<String> asShape(CardinalValues cardinalValues) {

	String north = cardinalValues.getNorth();
	String east = cardinalValues.getEast();
	String south = cardinalValues.getSouth();
	String west = cardinalValues.getWest();

	double e = Double.parseDouble(east);
	double w = Double.parseDouble(west);
	double s = Double.parseDouble(south);
	double n = Double.parseDouble(north);

	String es = e + " " + s;
	String ws = w + " " + s;
	String en = e + " " + n;
	String wn = w + " " + n;

	if (e > (180 + TOL)) {

	    e = e - 180;
	    w = w - 180;
	}

	if (n <= 90 && s >= -90 && e >= -180 && e <= 180 && w <= 180 && w >= -180) {

	    if (n >= s) {

		boolean snEqual = (Math.abs(n - s) < TOL);
		boolean weEqual = (Math.abs(w - e) < TOL);

		if (snEqual && weEqual) {
		    String shape = "POINT (" + ws + ")";
		    return Optional.of(shape);

		} else if (!snEqual && !weEqual) {
		    String shape = "POLYGON ((" + ws + "," + wn + "," + en + "," + es + "," + ws + "))";
		    return Optional.of(shape);

		} else {
		    if (snEqual && !weEqual) {
			// line: let's give a width
			double length = Math.abs(w - e);
			double width = length / 10.0;
			s = s - width;
			n = n + width;
		    } else if (!snEqual && weEqual) {
			// line: let's give a width
			double length = Math.abs(s - n);
			double width = length / 10.0;
			w = w - width;
			e = e + width;
		    }

		    es = "180 " + s;
		    ws = "-180 " + s;
		    en = "180 " + n;
		    wn = "-180 " + n;
		    String shape = "POLYGON ((" + ws + "," + wn + "," + en + "," + es + "," + ws + "))";

		    return Optional.of(shape);
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * @param metadata
     * @param indexData
     * @param elName
     * @param valueClass
     */
    private static void put(IndexesMetadata metadata, IndexData indexData, String elName, Class<?> valueClass) {

	metadata.read(elName).forEach(v -> { //

	    if (valueClass.equals(Integer.class)) {

		indexData.object.put(elName, Integer.valueOf(v));
	    }

	    if (valueClass.equals(Double.class)) {

		indexData.object.put(elName, Double.valueOf(v));
	    }

	    if (valueClass.equals(Boolean.class)) {

		indexData.object.put(elName, Boolean.valueOf(v));
	    }
	});
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

    /**
     * @param metadata
     * @param indexData
     * @param rp
     * @param valueClass
     */
    private static void put(IndexesMetadata metadata, IndexData indexData, ResourceProperty rp, Class<?> valueClass) {

	put(metadata, indexData, rp.getName(), valueClass);
    }
}
