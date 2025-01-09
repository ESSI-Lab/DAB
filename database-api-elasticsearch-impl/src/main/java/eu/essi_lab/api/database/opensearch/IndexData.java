/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Base64;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.mapping.BinaryProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.w3c.dom.Document;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class IndexData {

    private JSONObject object;
    private String index;
    private PutMappingRequest request;

    /**
     * @param folder
     * @param key
     * @param entry
     * @param type
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    public static IndexData of(DatabaseFolder folder, String key, FolderEntry entry, EntryType type)
	    throws IOException, TransformerException {

	IndexData indexData = new IndexData();

	//
	// put the base properties
	//

	indexData.object.put("databaseId", folder.getDatabase().getIdentifier());
	indexData.object.put("folderName", folder.getName());

	//
	// put the binary property
	//

	byte[] bytes = null;

	if (entry.getDocument().isPresent()) {

	    Document doc = entry.getDocument().get();

	    bytes = toString(doc).getBytes();

	} else {

	    InputStream stream = entry.getInputStream().get();
	    bytes = IOStreamUtils.getBytes(stream);
	}

	String encodedString = Base64.getEncoder().encodeToString(bytes);

	indexData.object.put(key, encodedString);

	//
	//
	//

	switch (type) {

	case DATA_FOLDER_ENTRY:

	    indexData.index = OpenSearchDatabase.DATA_FOLDER_INDEX;

	    indexData.request = createDataFolderRequest(key, indexData.index);

	    break;

	case AUGMENTER_PROPERTIES:

	    indexData.index = OpenSearchDatabase.AUGMENTER_PROPERTIES_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;

	case CONFIGURATION:

	    indexData.index = OpenSearchDatabase.CONFIGURATION_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;

	case META_FOLDER_ENTRY:

	    indexData.index = OpenSearchDatabase.META_FOLDER_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;
	case MISC:

	    indexData.index = OpenSearchDatabase.MISC_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;
	case USER:

	    indexData.index = OpenSearchDatabase.USERS_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;
	case VIEW:

	    indexData.index = OpenSearchDatabase.VIEWS_INDEX;

	    indexData.request = createBinaryRequest(key, indexData.index);

	    break;
	}

	return null;
    }

    /**
     * @param key
     * @param index2
     * @return
     */
    private static PutMappingRequest createDataFolderRequest(String key, String index2) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @param key
     * @param index
     * @return
     */
    private static PutMappingRequest createBinaryRequest(String key, String index) {

	Property property = new Property.Builder().//
		binary(new BinaryProperty.Builder().build()).//
		build();

	return new PutMappingRequest.Builder().//
		properties(key, property).//
		index(index).//
		build();

    }

    /**
     * 
     */
    private IndexData() {

	object = new JSONObject();
    }

    /**
     * @return
     */
    public String getIndex() {

	return index;
    }

    /**
     * @return the request
     */
    public PutMappingRequest getMappingRequest() {

	return request;
    }

    /**
     * @return
     */
    public IndexRequest<String> getIndexRequest() {
    
        return new IndexRequest.Builder<String>().index(getIndex()).//
        	document(getData()).//
        	build();
    }

    /**
     * @return
     */
    public String getData() {

	return object.toString(3);
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

}
