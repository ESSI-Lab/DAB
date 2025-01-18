/**
 * 
 */
package eu.essi_lab.api.database.opensearch.viewsfolder.test;

import java.io.InputStream;

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
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
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

	folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(ViewsMapping.VIEWS_INDEX, wrapper.getIndex());

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

	Assert.assertEquals(ViewsMapping.VIEWS_INDEX, wrapper.getIndex());

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

	folder.store(//
		key, //
		FolderEntry.of(view.toStream()), //
		EntryType.VIEW);

	//
	//
	//

	InputStream binary = folder.getBinary(key);

	View view2 = View.fromStream(binary);

	Assert.assertEquals(view, view2);
    }
}
