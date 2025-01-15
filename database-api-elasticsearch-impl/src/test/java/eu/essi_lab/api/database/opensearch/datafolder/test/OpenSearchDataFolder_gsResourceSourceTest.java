/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_gsResourceSourceTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = OpenSearchDataFolder_writingFolderTagTest.getFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	
	
	//
	//
	//
	IndexedElementsWriter.write(dataset);
	//
	//
	//

	String key = privateId;

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(DataFolderMapping.DATA_FOLDER_INDEX, wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(DataFolderMapping.GS_RESOURCE, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());

	//
	// binary data test
	//

	Optional<String> optResource = wrapper.getGSResource();
	Assert.assertTrue(optResource.isPresent());

	String base64resource = optResource.get();
	InputStream decoded = IndexData.decode(base64resource);

	GSResource gsResource1 = GSResource.create(decoded);

	Node binary = folder.get(key);

	GSResource gsResource2 = GSResource.create(binary);

	//
	//
	//

	compareProperties(wrapper, dataset, gsResource1);
	compareProperties(wrapper, dataset, gsResource2);
    }

    /**
     * @param wrapper 
     * @param res1
     * @param res2
     * @return
     */
    static boolean compareProperties(SourceWrapper wrapper, GSResource res1, GSResource res2) {

	IndexesMetadata indexesMetadata1 = res1.getIndexesMetadata();
	IndexesMetadata indexesMetadata2 = res2.getIndexesMetadata();

	boolean equals = false;

	equals &= indexesMetadata1.getProperties().equals(indexesMetadata2.getProperties());

	for (String prop : indexesMetadata1.getProperties()) {

	    equals &= indexesMetadata1.read(prop).equals(indexesMetadata2.read(prop));
	    	    
	    equals &= wrapper.getGSResourceProperties(prop).equals(indexesMetadata1.read(prop));
	}

	return equals;
    }
}
