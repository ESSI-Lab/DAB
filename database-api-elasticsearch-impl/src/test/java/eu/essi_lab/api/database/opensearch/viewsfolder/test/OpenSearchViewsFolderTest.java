/**
 * 
 */
package eu.essi_lab.api.database.opensearch.viewsfolder.test;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.api.database.opensearch.OpenSearchClientWrapper;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 */
public class OpenSearchViewsFolderTest extends OpenSearchTest {

    @Test
    public void sourceTest1() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	View view = new View();
	view.setId("viewId");
	view.setLabel("View label");
	view.setOwner("View owner");
	view.setCreator("View creator");

	String key = view.getId();

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW));

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(ViewsMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(ViewsMapping.VIEW, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	Assert.assertEquals(wrapper.getView().get(), wrapper.getBinaryValue());

	//
	// viws-index property
	//

	Assert.assertEquals(view.getId(), wrapper.getViewId().get());
	Assert.assertEquals(view.getOwner(), wrapper.getViewOwner().get());
	Assert.assertEquals(view.getCreator(), wrapper.getViewCreator().get());
	Assert.assertEquals(view.getLabel(), wrapper.getViewLabel().get());
	Assert.assertEquals(view.getVisibility(), wrapper.getViewVisibility().get());

	Assert.assertEquals(//
		IndexData.encode(FolderEntry.of(view.toStream())), wrapper.getView().get());
    }

    @Test
    public void sourceTest2() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	View view = new View();
	view.setId("viewId");
	view.setVisibility(ViewVisibility.PUBLIC);

	String key = view.getId();

	folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(ViewsMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(ViewsMapping.VIEW, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.BINARY, wrapper.getDataType());

	//
	// viws-index property
	//

	Assert.assertEquals(view.getId(), wrapper.getViewId().get());

	Assert.assertFalse(wrapper.getViewOwner().isPresent());
	Assert.assertFalse(wrapper.getViewCreator().isPresent());
	Assert.assertFalse(wrapper.getViewLabel().isPresent());

	Assert.assertEquals(view.getVisibility(), wrapper.getViewVisibility().get());

	Assert.assertEquals(//
		IndexData.encode(FolderEntry.of(view.toStream())), wrapper.getView().get());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	View view = new View();
	view.setId("viewId");
	view.setLabel("View label");
	view.setOwner("View owner");
	view.setCreator("View creator");
	view.setVisibility(ViewVisibility.PRIVATE);

	String key = view.getId();

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW));

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	View view2 = View.fromStream(binary);

	Assert.assertEquals(view, view2);
    }

    @Test
    public void searchViewsTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	storeViews(folder, 10);

	//
	//
	//

	OpenSearchClientWrapper wrapper = new OpenSearchClientWrapper(database.getClient());

	//
	//
	//

	Query query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		ViewsMapping.get().getIndex(), //
		ViewsMapping.VIEW_ID, //
		"View_Id_0");

	Optional<View> view = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		findFirst();

	Assert.assertTrue(view.isPresent());

	Assert.assertEquals("View_Id_0", view.get().getId());

	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		ViewsMapping.get().getIndex(), //
		ViewsMapping.VIEW_LABEL, //
		"View_Label_1");

	view = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		findFirst();

	Assert.assertTrue(view.isPresent());

	Assert.assertEquals("View_Label_1", view.get().getLabel());

	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		ViewsMapping.get().getIndex(), //
		ViewsMapping.VIEW_CREATOR, //
		"View_Label_1");

	view = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		findFirst();

	Assert.assertFalse(view.isPresent());

	//
	// wrong index
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		ViewsMapping.VIEW_LABEL, //
		"View_Label_1");

	view = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		findFirst();

	Assert.assertFalse(view.isPresent());

	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		ViewsMapping.get().getIndex());

	List<View> list = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(10, list.size());

	//
	// wrong index
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex());

	list = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> View.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, list.size());

    }

    @Test
    public void getViewIdentifiersSublistTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	storeViews(folder, 10);

	//
	//
	//

	OpenSearchClientWrapper wrapper = new OpenSearchClientWrapper(database.getClient());

	Query query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		ViewsMapping.get().getIndex());

	//
	// all
	//

	GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create();

	List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	ids = subList(ids, request);

	Assert.assertEquals(10, ids.size());

	//
	// first 3 (indexes 0,1,2)
	//

	request = GetViewIdentifiersRequest.create(0, 3);

	ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	ids = subList(ids, request);

	Assert.assertEquals(3, ids.size());

	Assert.assertEquals("View_Id_0", ids.get(0));
	Assert.assertEquals("View_Id_1", ids.get(1));
	Assert.assertEquals("View_Id_2", ids.get(2));

	//
	// indexes 1,2,3
	//

	request = GetViewIdentifiersRequest.create(1, 3);

	ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	ids = subList(ids, request);

	Assert.assertEquals(3, ids.size());

	Assert.assertEquals("View_Id_1", ids.get(0));
	Assert.assertEquals("View_Id_2", ids.get(1));
	Assert.assertEquals("View_Id_3", ids.get(2));

	//
	// index 1
	//

	request = GetViewIdentifiersRequest.create(1, 1);

	ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	ids = subList(ids, request);

	Assert.assertEquals(1, ids.size());

	Assert.assertEquals("View_Id_1", ids.get(0));

	//
	// start bigger that list size, set to 0. to index also set to 0
	//

	request = GetViewIdentifiersRequest.create(15, 3);

	ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID).//
		stream().//
		sorted().//
		collect(Collectors.toList());

	ids = subList(ids, request);

	Assert.assertEquals(0, ids.size());
    }

    @Test
    public void getViewIdentifiersFilterTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	String folderName = Database.VIEWS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	// creator1 > 7
	// creator2 > 8
	// creator3 > 3
	//
	// ownerA > 13
	// ownerB > 5
	//
	// private > 10
	// public > 8
	//
	// total > 18
	//

	storeViews(folder, "creator1", "ownerA", ViewVisibility.PRIVATE, 5);
	storeViews(folder, "creator1", "ownerB", ViewVisibility.PUBLIC, 2);

	storeViews(folder, "creator2", "ownerA", ViewVisibility.PRIVATE, 3);
	storeViews(folder, "creator2", "ownerA", ViewVisibility.PUBLIC, 5);

	storeViews(folder, "creator3", "ownerB", ViewVisibility.PRIVATE, 2);
	storeViews(folder, "creator3", "ownerB", ViewVisibility.PUBLIC, 1);

	final int TOTAL = 18;

	final int CREATOR_1 = 7;
	final int CREATOR_1_PUBLIC = 2;
	final int CREATOR_1_PRIVATE = 5;

	final int CREATOR_2 = 8;
	final int CREATOR_2_PUBLIC = 5;
	final int CREATOR_2_PRIVATE = 3;

	final int CREATOR_3 = 3;
	final int CREATOR_3_PUBLIC = 1;
	final int CREATOR_3_PRIVATE = 2;

	final int CREATOR_1_OWNER_A = 5;
	final int CREATOR_1_OWNER_B = 2;

	final int PRIVATE = 10;
	final int PUBLIC = 8;

	//
	//
	//

	OpenSearchClientWrapper wrapper = new OpenSearchClientWrapper(database.getClient());

	//
	// all
	//

	{

	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create();

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(TOTAL, ids.size());
	}

	//
	// all, with size greater than 18 (TOTAL + 10)
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create();

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL + 10);

	    ids = subList(ids, request);

	    Assert.assertEquals(TOTAL, ids.size());
	}

	//
	// start bigger than total (TOTAL + 10)
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create();

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, TOTAL + 10, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(0, ids.size());
	}

	//
	// "creator1" and PUBLIC
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator1", //
		    ViewVisibility.PUBLIC//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_1_PUBLIC, ids.size());
	}

	//
	// "creator1" and PRIVATE
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator1", //
		    ViewVisibility.PRIVATE//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_1_PRIVATE, ids.size());
	}

	//
	// "creator2" and PUBLIC
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator2", //
		    ViewVisibility.PUBLIC//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_2_PUBLIC, ids.size());
	}

	//
	// "creator2" and PRIVATE
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator2", //
		    ViewVisibility.PRIVATE//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_2_PRIVATE, ids.size());
	}

	//
	// "creator3" and PUBLIC
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator3", //
		    ViewVisibility.PUBLIC//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_3_PUBLIC, ids.size());
	}

	//
	// "creator3" and PRIVATE
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator3", //
		    ViewVisibility.PRIVATE//
	    );

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_3_PRIVATE, ids.size());
	}

	//
	// creator1
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator1");

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_1, ids.size());
	}

	//
	// creator2
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator2");

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_2, ids.size());
	}

	//
	// creator3
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator3");

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_3, ids.size());
	}

	//
	// public
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    ViewVisibility.PUBLIC);

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(PUBLIC, ids.size());
	}

	//
	// private
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    ViewVisibility.PRIVATE);

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(PRIVATE, ids.size());
	}

	//
	// creator1 ownerA
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator1", "ownerA");

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_1_OWNER_A, ids.size());
	}

	//
	// creator1 ownerB
	//

	{
	    GetViewIdentifiersRequest request = GetViewIdentifiersRequest.create(//
		    "creator1", "ownerB");

	    Query query = wrapper.buildSearchViewsQuery(//
		    database.getIdentifier(), //
		    request.getCreator(), //
		    request.getOwner(), //
		    request.getVisibility()//
	    );

	    List<String> ids = wrapper.searchProperty(query, ViewsMapping.VIEW_ID, 0, TOTAL);

	    ids = subList(ids, request);

	    Assert.assertEquals(CREATOR_1_OWNER_B, ids.size());
	}

    }

    /**
     * @param ids
     * @param request
     * @return
     */
    private List<String> subList(List<String> ids, GetViewIdentifiersRequest request) {

	int fromIndex = Math.min(ids.size(), request.getStart());
	int toIndex = Math.min(ids.size(), request.getStart() + request.getCount());

	return ids.subList(fromIndex, toIndex);
    }

    /**
     * @param folder
     * @param count
     * @throws JAXBException
     * @throws Exception
     */
    private void storeViews(OpenSearchFolder folder, int count) throws JAXBException, Exception {

	for (int i = 0; i < count; i++) {

	    View view = new View();

	    view.setId("View_Id_" + i);
	    view.setLabel("View_Label_" + i);
	    view.setOwner("View owner");
	    view.setCreator("View creator_" + i);
	    view.setVisibility(i % 2 == 0 ? ViewVisibility.PRIVATE : ViewVisibility.PUBLIC);

	    Assert.assertTrue(folder.store(//
		    view.getId(), //
		    FolderEntry.of(view.toStream()), //
		    EntryType.VIEW));
	}
    }

    /**
     * @param folder
     * @param creator
     * @param visibility
     * @param count
     * @throws JAXBException
     * @throws Exception
     */
    private void storeViews(OpenSearchFolder folder, String creator, String owner, ViewVisibility visibility, int count)
	    throws JAXBException, Exception {

	for (int i = 0; i < count; i++) {

	    View view = new View();

	    view.setId(UUID.randomUUID().toString());
	    view.setLabel(UUID.randomUUID().toString());
	    view.setOwner(owner);
	    view.setCreator(creator);
	    view.setVisibility(visibility);

	    Assert.assertTrue(folder.store(//
		    view.getId(), //
		    FolderEntry.of(view.toStream()), //
		    EntryType.VIEW));
	}
    }
}
