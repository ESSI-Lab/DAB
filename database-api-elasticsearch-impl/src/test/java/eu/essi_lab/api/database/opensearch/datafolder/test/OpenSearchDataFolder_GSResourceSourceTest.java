/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchClientWrapper;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

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

	Assert.assertTrue(folder.store(key, //
		FolderEntry.of(dataset.asDocument(true)), //
		EntryType.GS_RESOURCE));

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(DataFolderMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(DataFolderMapping.GS_RESOURCE, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());

	Assert.assertEquals(wrapper.getGSResource().get(), wrapper.getBinaryValue());

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void searchResourcesTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	storeResources(folder, 10);

	//
	//
	//

	OpenSearchClientWrapper wrapper = new OpenSearchClientWrapper(database.getClient());
	//
	//
	//

	Query query = wrapper.buildSearchQuery(database.getIdentifier(), DataFolderMapping.get().getIndex());

	List<GSResource> resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(10, resources.size());

	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		ResourceProperty.PRIVATE_ID.getName(), //
		"GSResource_0");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, resources.size());

	Assert.assertEquals("GSResource_0", resources.get(0).getPrivateId());
	
	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"Title_5");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, resources.size());

	Assert.assertEquals("Title_5", resources.get(0).getHarmonizedMetadata().getCoreMetadata().getTitle());
	
	//
	// wrong property
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.ABSTRACT.getName(), //
		"Title_5");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());
	
	//
	// wrong index
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"Title_5");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());
	
	//
	// undefined property
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		"undefined_property", //
		"Title_5");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());
	
	//
	// undefined value
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"unknown");

	resources = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSResource.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());
	
	
    }

    /**
     * @param folder
     * @param count
     * @throws Exception
     */
    private void storeResources(OpenSearchFolder folder, int count) throws Exception {

	for (int i = 0; i < count; i++) {

	    Dataset dataset = new Dataset();
	    dataset.setPrivateId("GSResource_" + i);
	    dataset.setOriginalId(UUID.randomUUID().toString());
	    dataset.setPublicId(UUID.randomUUID().toString());

	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("Title_" + i);

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}
    }

}
