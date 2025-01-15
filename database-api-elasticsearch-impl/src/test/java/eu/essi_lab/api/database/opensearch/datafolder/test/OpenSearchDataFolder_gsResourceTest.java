/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.InputStream;
import java.math.BigDecimal;
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
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_gsResourceTest extends OpenSearchTest {

    @Test
    public void test1() throws Exception {

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
	
	dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
		new BigDecimal(50),//
		new BigDecimal(-10),//
		new BigDecimal(-30),//
		new BigDecimal(40));

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

	//
	//
	//

	SourceWrapper wrapper = folder.getSourceWrapper(key);

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

	OpenSearchDataFolder_gsResourceSourceTest.compareProperties(wrapper, dataset, gsResource1);
	OpenSearchDataFolder_gsResourceSourceTest.compareProperties(wrapper, dataset, gsResource2);
    }
}
