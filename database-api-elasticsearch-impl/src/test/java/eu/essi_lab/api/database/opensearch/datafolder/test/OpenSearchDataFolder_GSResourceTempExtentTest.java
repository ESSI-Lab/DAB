/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio  
 */
public class OpenSearchDataFolder_GSResourceTempExtentTest {

    @Test
    public void lineStringFromBoundingPolygonTest() throws Exception {

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

	BoundingPolygon boundingPolygon = new BoundingPolygon();

	List<List<Double>> list = new ArrayList<>();

	List<Double> list1 = Arrays.asList(10.0, 20.0);
	List<Double> list2 = Arrays.asList(15.0, 25.0);
	List<Double> list3 = Arrays.asList(25.0, 35.0);

	list.add(list1);
	list.add(list2);
	list.add(list3);

	boundingPolygon.setMultiPoints(list);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addBoundingPolygon(boundingPolygon);

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

	System.out.println(wrapper.toStringHideBinary());

	//
	//
	//

	List<String> bboxes = wrapper.getGSResourceProperties(MetadataElement.BOUNDING_BOX);

	Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(bboxes.size()));

	String shape = bboxes.get(0);

	Assert.assertEquals("LINESTRING (10.0 20.0, 15.0 25.0, 25.0 35.0)", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }
}
