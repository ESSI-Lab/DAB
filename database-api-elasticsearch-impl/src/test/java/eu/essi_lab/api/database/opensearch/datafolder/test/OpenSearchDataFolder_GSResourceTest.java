/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.OpenSearchWriter;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_GSResourceTest extends OpenSearchTest {

    /*
     * 
     */
    private OpenSearchFolder createDataFolder(//
	    OpenSearchDatabase database, //
	    String sourceId) throws Exception {

	String folderName = sourceId + SourceStorageWorker.DATA_1_POSTFIX;//

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	return folder;
    }

    /**
     * @param folder
     * @param folderEntries
     * @throws Exception
     */
    private void storeDatasets(OpenSearchFolder folder, int folderEntries) throws Exception {

	for (int i = 1; i <= folderEntries; i++) {

	    Dataset dataset = new Dataset();
	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("TITLE_" + i);
	    dataset.setPrivateId(folder.getName() + "_PRIVATE_ID_" + i);
	    dataset.setOriginalId(folder.getName() + "_ORIGINAL_ID_" + i);
	    dataset.setPublicId(folder.getName() + "_PUBLIC_ID_" + i);
	    dataset.getPropertyHandler().setOAIPMHHeaderIdentifier(folder.getName() + "_OAI_ID_" + i);
	    dataset.setSource(new GSSource("sourceId"));

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}
    }

    @Test
    public void listIdentifiersTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	OpenSearchFolder folder1 = createDataFolder(database, "sourceId_1");
	OpenSearchFolder folder2 = createDataFolder(database, "sourceId_2");
	OpenSearchFolder folder3 = createDataFolder(database, "sourceId_3");

	//
	//
	//

	int folder1Entries = 5;
	int folder2Entries = 9;
	int folder3Entries = 3;

	storeDatasets(folder1, folder1Entries);
	storeDatasets(folder2, folder2Entries);
	storeDatasets(folder3, folder3Entries);

	//
	//
	//

	test(folder1, folder1Entries);
	test(folder2, folder2Entries);
	test(folder3, folder3Entries);
	
	//
	//
	//
	
	folder1.clear();
	Assert.assertEquals(0, folder1.listIdentifiers(IdentifierType.ORIGINAL).size());
	Assert.assertEquals(0, folder1.listIdentifiers(IdentifierType.PRIVATE).size());
	Assert.assertEquals(0, folder1.listIdentifiers(IdentifierType.PUBLIC).size());
	Assert.assertEquals(0, folder1.listIdentifiers(IdentifierType.OAI_HEADER).size());
    }

    /**
     * @param folder
     * @param entries
     * @throws Exception
     */
    private void test(OpenSearchFolder folder, int entries) throws Exception {

	List<String> privateIds = folder.listIdentifiers(IdentifierType.PRIVATE);
	List<String> originalIds = folder.listIdentifiers(IdentifierType.ORIGINAL);
	List<String> oaiIds = folder.listIdentifiers(IdentifierType.OAI_HEADER);
	List<String> publicIds = folder.listIdentifiers(IdentifierType.PUBLIC);

	Assert.assertEquals(entries, privateIds.size());
	Assert.assertEquals(entries, originalIds.size());
	Assert.assertEquals(entries, oaiIds.size());
	Assert.assertEquals(entries, publicIds.size());

	Assert.assertTrue(privateIds.stream().allMatch(id -> id.startsWith(folder.getName() + "_PRIVATE_ID_")));
	Assert.assertTrue(originalIds.stream().allMatch(id -> id.startsWith(folder.getName() + "_ORIGINAL_ID_")));
	Assert.assertTrue(oaiIds.stream().allMatch(id -> id.startsWith(folder.getName() + "_OAI_ID_")));
	Assert.assertTrue(publicIds.stream().allMatch(id -> id.startsWith(folder.getName() + "_PUBLIC_ID_")));
    }

    @Test
    public void getByIdentifierTest() throws ParserConfigurationException, JAXBException, SAXException, IOException, Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	for (int i = 1; i <= 3; i++) {

	    Dataset dataset = new Dataset();
	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("TITLE_" + i);
	    dataset.setPrivateId("PRIVATE_ID_" + i);
	    dataset.setOriginalId("ORIGINAL_ID_" + i);
	    dataset.setPublicId("PUBLIC_ID_" + i);
	    dataset.getPropertyHandler().setOAIPMHHeaderIdentifier("OAI_ID_" + i);
	    dataset.setSource(new GSSource("sourceId"));

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}

	//
	//
	//

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "PRIVATE_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "PUBLIC_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "OAI_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "ORIGINAL_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "PUBLIC_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "OAI_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "ORIGINAL_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "PRIVATE_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "OAI_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.OAI_HEADER, "ORIGINAL_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.OAI_HEADER, "PRIVATE_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.OAI_HEADER, "PUBLIC_ID_1");
	    Assert.assertFalse(optional.isPresent());
	}

	//
	//
	//

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "ORIGINAL_ID_1");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_1", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_1", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_1", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_1", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "PRIVATE_ID_1");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_1", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_1", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_1", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_1", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "PUBLIC_ID_1");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_1", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_1", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_1", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_1", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	//
	//
	//

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "ORIGINAL_ID_2");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_2", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_2", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_2", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_2", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "PRIVATE_ID_2");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_2", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_2", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_2", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_2", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "PUBLIC_ID_2");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_2", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_2", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_2", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_2", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	//
	//

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.ORIGINAL, "ORIGINAL_ID_3");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_3", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_3", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_3", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_3", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PRIVATE, "PRIVATE_ID_3");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_3", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_3", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_3", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_3", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

	{
	    Optional<GSResource> optional = folder.get(IdentifierType.PUBLIC, "PUBLIC_ID_3");
	    Assert.assertTrue(optional.isPresent());

	    Assert.assertEquals("ORIGINAL_ID_3", optional.get().getOriginalId().get());
	    Assert.assertEquals("PRIVATE_ID_3", optional.get().getPrivateId());
	    Assert.assertEquals("PUBLIC_ID_3", optional.get().getPublicId());
	    Assert.assertEquals("TITLE_3", optional.get().getHarmonizedMetadata().getCoreMetadata().getTitle());

	}

    }

    @Test
    public void minMaxTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	for (int i = 1; i <= 10; i++) {

	    Dataset dataset = new Dataset();

	    dataset.setSource(new GSSource());

	    dataset.setPrivateId(UUID.randomUUID().toString());

	    dataset.getPropertyHandler().setMetadataQuality(i);

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}

	Query query = OpenSearchQueryBuilder.buildMatchAllQuery();

	//
	//
	//

	int maxQuality = (int) folder.getWrapper().findMinMaxValue(query, ResourceProperty.METADATA_QUALITY.getName(), true);
	int minQuality = (int) folder.getWrapper().findMinMaxValue(query, ResourceProperty.METADATA_QUALITY.getName(), false);

	Assert.assertEquals(10, maxQuality);
	Assert.assertEquals(1, minQuality);
    }

    @Test
    public void removeTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	String privateId = UUID.randomUUID().toString();

	Dataset dataset = new Dataset();
	dataset.setPrivateId(privateId);
	dataset.setOriginalId(UUID.randomUUID().toString());
	dataset.setPublicId(UUID.randomUUID().toString());
	dataset.setSource(new GSSource("sourceId"));

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

	Assert.assertTrue(folder.exists(key));

	Assert.assertTrue(folder.remove(key));

	Assert.assertFalse(folder.exists(key));

	Assert.assertNull(folder.get(key));
    }

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

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
	dataset.setSource(new GSSource("sourceId"));

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

	Assert.assertEquals(folder.getName(), wrapper.getFolderName());

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

	//
	// binary is always available, also for GS resources
	//

	InputStream binary = folder.getBinary(key);
	Assert.assertNotNull(binary);

	GSResource gsResource = GSResource.create(binary);
	Assert.assertEquals(gsResource.getPrivateId(), dataset.getPrivateId());
    }

    @Test
    public void searchResourcesTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = TestUtils.getDataFolderName(database);

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	storeResources(folder, 10);

	//
	//
	//

	OpenSearchWrapper wrapper = new OpenSearchWrapper(database);
	//
	//
	//

	Query query = OpenSearchQueryBuilder.buildSearchQuery(database.getIdentifier(), DataFolderMapping.get().getIndex());

	List<GSResource> resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(10, resources.size());

	//
	//
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		ResourceProperty.PRIVATE_ID.getName(), //
		"GSResource_0");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, resources.size());

	Assert.assertEquals("GSResource_0", resources.get(0).getPrivateId());

	//
	//
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"Title_5");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, resources.size());

	Assert.assertEquals("Title_5", resources.get(0).getHarmonizedMetadata().getCoreMetadata().getTitle());

	//
	// wrong property
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.ABSTRACT.getName(), //
		"Title_5");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());

	//
	// wrong index
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"Title_5");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());

	//
	// undefined property
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		"undefined_property", //
		"Title_5");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());

	//
	// undefined value
	//

	query = OpenSearchQueryBuilder.buildSearchQuery(//
		database.getIdentifier(), //
		DataFolderMapping.get().getIndex(), //
		MetadataElement.TITLE.getName(), //
		"unknown");

	resources = wrapper.searchSources(DataFolderMapping.get().getIndex(), query).//
		stream().//
		map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, resources.size());

    }

    @Test
    public void removeResourcesTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

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

	OpenSearchWrapper wrapper = new OpenSearchWrapper(database);

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

	    Query titleAquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleA");

	    List<GSResource> titleAresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleAquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(0, titleAresources.size());

	    //
	    //
	    //

	    Query titleBquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleB");

	    List<GSResource> titleBresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleBquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_B, titleBresources.size());

	    //
	    //
	    //

	    Query titleCquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleC");

	    List<GSResource> titleCresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleCquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_C, titleCresources.size());

	    //
	    //
	    //

	    Query titleDquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleD");

	    List<GSResource> titleDresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleDquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
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

	    Query titleAquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleA");

	    List<GSResource> titleAresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleAquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(0, titleAresources.size());

	    //
	    //
	    //

	    Query titleBquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleB");

	    List<GSResource> titleBresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleBquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_B, titleBresources.size());

	    //
	    //
	    //

	    Query titleDquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleD");

	    List<GSResource> titleDresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleDquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    Assert.assertEquals(RESOURCE_D, titleDresources.size());

	    //
	    //
	    //

	    Query titleCquery = OpenSearchQueryBuilder.buildSearchQuery(//
		    database.getIdentifier(), //
		    DataFolderMapping.get().getIndex(), //
		    MetadataElement.TITLE.getName(), //
		    "titleC");

	    List<GSResource> titleCresources = wrapper.searchSources(DataFolderMapping.get().getIndex(), titleCquery).//
		    stream().//
		    map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
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
	    dataset.setSource(new GSSource("sourceId"));

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
	    dataset.setSource(new GSSource("sourceId"));

	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle("Title_" + i);

	    IndexedElementsWriter.write(dataset);

	    Assert.assertTrue(folder.store(dataset.getPrivateId(), //
		    FolderEntry.of(dataset.asDocument(true)), //
		    EntryType.GS_RESOURCE));
	}
    }

}
