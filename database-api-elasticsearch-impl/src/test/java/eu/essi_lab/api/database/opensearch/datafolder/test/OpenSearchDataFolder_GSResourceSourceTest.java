/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_GSResourceSourceTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = TestUtils.getDataFolderName(database);

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
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

}
