/**
 * 
 */
package eu.essi_lab.api.database.opensearch.augmentersfolder.test;

import java.io.InputStream;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.messages.AugmenterProperties;

/**
 * @author Fabrizio
 */
public class OpenSearchAugmentersFolderTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.AUGMENTERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	AugmenterProperties properties = new AugmenterProperties();
	properties.setStart(5);
	properties.setTimestamp();
	properties.setBeginTimestamp();

	String key = UUID.randomUUID().toString() + ".properties";

	//
	//
	//

	folder.store(key, //
		FolderEntry.of(properties.asStream()), //
		EntryType.AUGMENTER_PROPERTIES);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(AugmentersMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(AugmentersMapping.AUGMENTER_PROPERTIES, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	//
	// augmenters-folder-index property
	//

	//
	// binary data test
	//

	String binaryData = wrapper.getAugmenterProperties().get();
	InputStream stream = IndexData.decode(binaryData);
	AugmenterProperties fromStream = AugmenterProperties.fromStream(stream);

	Assert.assertEquals(properties, fromStream);

	Assert.assertEquals(properties.toString(), fromStream.toString());

	//
	// low level test omitted since it is not guaranteed that the base 64 encoded strings
	// are always equals
	//
	// Assert.assertEquals(//
	// IndexData.encode(FolderEntry.of(properties.asStream())), //
	// wrapper.getAugmenterProperties().get() //
	// );
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.AUGMENTERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	AugmenterProperties properties = new AugmenterProperties();
	properties.setStart(5);
	properties.setTimestamp();
	properties.setBeginTimestamp();

	String key = UUID.randomUUID().toString() + ".properties";

	//
	//
	//

	folder.store(key, FolderEntry.of(properties.asStream()), EntryType.AUGMENTER_PROPERTIES);

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	AugmenterProperties fromStream = AugmenterProperties.fromStream(binary);

	Assert.assertEquals(properties, fromStream);
	Assert.assertEquals(properties.toString(), fromStream.toString());

	//
	// trying to get a binary as a doc return null
	//

	Assert.assertNull(folder.get(key));
    }
}
