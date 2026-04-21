/**
 * 
 */
package eu.essi_lab.api.database.opensearch.shapefilesfolder.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;

/**
 * @author Fabrizio
 */
public class ShapeFilesFolderInternalTestIT extends OpenSearchTest {

    @Test
    public void test() throws Exception {

	InputStream stream = ShapeFilesFolderInternalTestIT.class.getClassLoader().getResourceAsStream("shape.zip");

OpenSearchDatabase database = createDataBase();

	OpenSearchFolder folder = new OpenSearchFolder(database, OpenSearchDatabase.SHAPE_FILES_FOLDER);

	boolean stored = folder.store("UOMIT20181025", FolderEntry.of(stream), EntryType.SHAPE_FILE);

	//
	//
	//

	Assert.assertTrue(stored);

	Assert.assertEquals(47, folder.size());

	//
	//
	//

	List<String> keys = Arrays.asList(folder.listKeys());

	OpenSearchWrapper wrapper = new OpenSearchWrapper(database);

	Optional<JSONArray> shapeCoordinates = wrapper.getShapeFeatureCoordinates(keys.get(0));

	Assert.assertTrue(shapeCoordinates.isPresent());
    }

}
