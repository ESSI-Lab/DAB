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
import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchClientWrapper;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchWriter;
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
public class OpenSearchDataFolder_GSResourceTest extends OpenSearchTest {

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

    @Test
    public void removeResourcesTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//
	
	int TOTAL = 14;

	int RESOURCE_A = 5;
	int RESOURCE_B = 3;
	int RESOURCE_C = 4;
	int RESOURCE_D = 2;


	storeResources(folder, "resourceA", "titleA", "abstractA", RESOURCE_A);

	storeResources(folder, "resourceB", "titleB", "abstractB", RESOURCE_B);

	storeResources(folder, "resourceC", "titleC", "abstractC", RESOURCE_C);

	storeResources(folder, "resourceD", "titleD", "abstractD", RESOURCE_D);

	OpenSearchClientWrapper wrapper = new OpenSearchClientWrapper(database.getClient());

	
	int folderSize = folder.size();
	Assert.assertEquals(TOTAL, folderSize);

	OpenSearchWriter writer = new OpenSearchWriter();
	writer.setDatabase(database);
	
	//
	// removes the resourceA_0
	//

	{
	    Node node_resourceA_0 = folder.get("resourceA_0");
	    Assert.assertNotNull(node_resourceA_0);

	    Node node_resourceA_1 = folder.get("resourceA_1");
	    Assert.assertNotNull(node_resourceA_1);

	    Node node_resourceA_2 = folder.get("resourceA_2");
	    Assert.assertNotNull(node_resourceA_2);

	    Node node_resourceA_3 = folder.get("resourceA_3");
	    Assert.assertNotNull(node_resourceA_3);

	    Node node_resourceA_4 = folder.get("resourceA_4");
	    Assert.assertNotNull(node_resourceA_4);

	    GSResource resourceA_0 = GSResource.create(node_resourceA_0);

	    writer.remove(resourceA_0);

	    folderSize = folder.size();
	    Assert.assertEquals(TOTAL - 1, folderSize);

	    Assert.assertNull(folder.get("resourceA_0"));

	    Assert.assertNotNull(folder.get("resourceA_1"));
	    Assert.assertNotNull(folder.get("resourceA_2"));
	    Assert.assertNotNull(folder.get("resourceA_3"));
	    Assert.assertNotNull(folder.get("resourceA_4"));
	}

	
	//
	// removes the resourceC_2
	//

	{
	    Node node_resourceC_0 = folder.get("resourceC_0");
	    Assert.assertNotNull(node_resourceC_0);

	    Node node_resourceC_1 = folder.get("resourceC_1");
	    Assert.assertNotNull(node_resourceC_1);

	    Node node_resourceC_2 = folder.get("resourceC_2");
	    Assert.assertNotNull(node_resourceC_2);

	    Node node_resourceC_3 = folder.get("resourceC_3");
	    Assert.assertNotNull(node_resourceC_3);

	    GSResource resourceC_2 = GSResource.create(node_resourceC_2);

	    writer.remove(resourceC_2);

	    folderSize = folder.size();
	    Assert.assertEquals(TOTAL - 2, folderSize);

	    Assert.assertNull(folder.get("resourceC_2"));

	    Assert.assertNotNull(folder.get("resourceC_0"));
	    Assert.assertNotNull(folder.get("resourceC_1"));
	    Assert.assertNotNull(folder.get("resourceC_3"));

	}

	//
	//
	//

	RESOURCE_A = RESOURCE_A - 1;
	RESOURCE_C = RESOURCE_C - 1;
	TOTAL = TOTAL - 2;

	//
	// removes all resourceA by title
	//

	{
	    writer.remove(MetadataElement.TITLE.getName(), "titleA");

	    folderSize = folder.size();
	    Assert.assertEquals(TOTAL - RESOURCE_A, folderSize);

	    //
	    //
	    //

	    Query titleAquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleA");

	    List<GSResource> titleAresources = wrapper.searchBinaries(titleAquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(0, titleAresources.size());

	    //
	    //
	    //

	    Query titleBquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleB");

	    List<GSResource> titleBresources = wrapper.searchBinaries(titleBquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_B, titleBresources.size());

	    //
	    //
	    //

	    Query titleCquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleC");

	    List<GSResource> titleCresources = wrapper.searchBinaries(titleCquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_C, titleCresources.size());

	    //
	    //
	    //

	    Query titleDquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleD");

	    List<GSResource> titleDresources = wrapper.searchBinaries(titleDquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_D, titleDresources.size());
	}

	TOTAL = TOTAL - RESOURCE_A;

	//
	// removes all resourceC by abstract
	// 
	
	{
	    writer.remove(MetadataElement.ABSTRACT.getName(), "abstractC");

	    folderSize = folder.size();
	    Assert.assertEquals(TOTAL - RESOURCE_C, folderSize);

	    //
	    //
	    //

	    Query titleAquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleA");

	    List<GSResource> titleAresources = wrapper.searchBinaries(titleAquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(0, titleAresources.size());

	    //
	    //
	    //

	    Query titleBquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleB");

	    List<GSResource> titleBresources = wrapper.searchBinaries(titleBquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_B, titleBresources.size());
	
	    //
	    //
	    //

	    Query titleDquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleD");

	    List<GSResource> titleDresources = wrapper.searchBinaries(titleDquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_D, titleDresources.size());
	    
	    //
	    //
	    //

	    Query titleCquery = wrapper.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleC");

	    List<GSResource> titleCresources = wrapper.searchBinaries(titleCquery).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(0, titleCresources.size());
	}

    }

    /**
     * @param folder
     * @param title
     * @param _abstract
     * @param count
     * @throws Exception
     */
    private void storeResources(OpenSearchFolder folder, String privateIdPrefix, String title, String _abstract, int count)
	    throws Exception {

	for (int i = 0; i < count; i++) {

	    Dataset dataset = new Dataset();
	    dataset.setPrivateId(privateIdPrefix + "_" + i);
	    dataset.setOriginalId(UUID.randomUUID().toString());
	    dataset.setPublicId(UUID.randomUUID().toString());

	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);
	    dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract(_abstract);

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}
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
