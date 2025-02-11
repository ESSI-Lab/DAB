/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.math.BigDecimal;
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
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchDataFolder_GSResourceBboxTest extends OpenSearchTest {

    @Test
    public void lineStringFromBoundingPolygonTest() throws Exception {

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

    @Test
    public void polygonFromBoundingPolygonTest() throws Exception {

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

	BoundingPolygon boundingPolygon = new BoundingPolygon();

	List<List<Double>> list = new ArrayList<>();

	List<Double> list1 = Arrays.asList(30.0, 10.0);
	List<Double> list2 = Arrays.asList(40.0, 40.0);
	List<Double> list3 = Arrays.asList(20.0, 40.0);
	List<Double> list4 = Arrays.asList(10.0, 20.0);
	List<Double> list5 = Arrays.asList(30.0, 10.0);

	list.add(list1);
	list.add(list2);
	list.add(list3);
	list.add(list4);
	list.add(list5);

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

	Assert.assertEquals("POLYGON ((30.0 10.0, 40.0 40.0, 20.0 40.0, 10.0 20.0, 30.0 10.0))", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void pointTest() throws Exception {

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

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(10));
	box.setBigDecimalEast(new BigDecimal(10));

	box.setBigDecimalSouth(new BigDecimal(10));
	box.setBigDecimalWest(new BigDecimal(10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

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

	Assert.assertEquals("POINT (10.0 10.0)", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void multiPointTest() throws Exception {

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

	GeographicBoundingBox box1 = new GeographicBoundingBox();

	box1.setBigDecimalNorth(new BigDecimal(10));
	box1.setBigDecimalEast(new BigDecimal(10));

	box1.setBigDecimalSouth(new BigDecimal(10));
	box1.setBigDecimalWest(new BigDecimal(10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box1);

	GeographicBoundingBox box2 = new GeographicBoundingBox();

	box2.setBigDecimalNorth(new BigDecimal(20));
	box2.setBigDecimalEast(new BigDecimal(20));

	box2.setBigDecimalSouth(new BigDecimal(20));
	box2.setBigDecimalWest(new BigDecimal(20));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box2);

	GeographicBoundingBox box3 = new GeographicBoundingBox();

	box3.setBigDecimalNorth(new BigDecimal(30));
	box3.setBigDecimalEast(new BigDecimal(30));

	box3.setBigDecimalSouth(new BigDecimal(30));
	box3.setBigDecimalWest(new BigDecimal(30));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box3);

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

	Assert.assertEquals("MULTIPOINT (10.0 10.0, 20.0 20.0, 30.0 30.0)", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void polygonTest() throws Exception {

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

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(10));
	box.setBigDecimalEast(new BigDecimal(10));

	box.setBigDecimalSouth(new BigDecimal(-10));
	box.setBigDecimalWest(new BigDecimal(-10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

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

	Assert.assertEquals("POLYGON ((-10.0 -10.0, -10.0 10.0, 10.0 10.0, 10.0 -10.0, -10.0 -10.0))", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void invalidBboxToPointTest() throws Exception {

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

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(65.583));
	box.setBigDecimalEast(new BigDecimal(188.983)); // value is normalized

	box.setBigDecimalSouth(new BigDecimal(65.583));
	box.setBigDecimalWest(new BigDecimal(188.983)); // value is normalized

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

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

	Assert.assertEquals("POINT (8.983000000000004 65.583)", shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    
    @Test
    public void multiPolygonTest() throws Exception {

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

	GeographicBoundingBox box1 = new GeographicBoundingBox();

	box1.setBigDecimalNorth(new BigDecimal(48));
	box1.setBigDecimalEast(new BigDecimal(5));

	box1.setBigDecimalSouth(new BigDecimal(43));
	box1.setBigDecimalWest(new BigDecimal(-2));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box1);

	GeographicBoundingBox box2 = new GeographicBoundingBox();

	box2.setBigDecimalNorth(new BigDecimal(50));
	box2.setBigDecimalEast(new BigDecimal(27));

	box2.setBigDecimalSouth(new BigDecimal(44));
	box2.setBigDecimalWest(new BigDecimal(16));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box2);

	GeographicBoundingBox box3 = new GeographicBoundingBox();

	box3.setBigDecimalNorth(new BigDecimal(34));
	box3.setBigDecimalEast(new BigDecimal(13));

	box3.setBigDecimalSouth(new BigDecimal(31));
	box3.setBigDecimalWest(new BigDecimal(6));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box3);

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

	String multi = "MULTIPOLYGON (((-2.0 43.0, -2.0 48.0, 5.0 48.0, 5.0 43.0, -2.0 43.0)), ((16.0 44.0, 16.0 50.0, 27.0 50.0, 27.0 44.0, 16.0 44.0)), ((6.0 31.0, 6.0 34.0, 13.0 34.0, 13.0 31.0, 6.0 31.0)))";

	Assert.assertEquals(multi, shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }

    @Test
    public void geometryCollectionTest() throws Exception {

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

	GeographicBoundingBox box1 = new GeographicBoundingBox();

	box1.setBigDecimalNorth(new BigDecimal(10));
	box1.setBigDecimalEast(new BigDecimal(10));

	box1.setBigDecimalSouth(new BigDecimal(10));
	box1.setBigDecimalWest(new BigDecimal(10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box1);

	GeographicBoundingBox box2 = new GeographicBoundingBox();

	box2.setBigDecimalNorth(new BigDecimal(20));
	box2.setBigDecimalEast(new BigDecimal(20));

	box2.setBigDecimalSouth(new BigDecimal(-20));
	box2.setBigDecimalWest(new BigDecimal(-20));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box2);

	GeographicBoundingBox box3 = new GeographicBoundingBox();

	box3.setBigDecimalNorth(new BigDecimal(30));
	box3.setBigDecimalEast(new BigDecimal(30));

	box3.setBigDecimalSouth(new BigDecimal(-30));
	box3.setBigDecimalWest(new BigDecimal(-30));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box3);

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

	String collection = "GEOMETRYCOLLECTION (POINT (10.0 10.0), POLYGON ((-20.0 -20.0, -20.0 20.0, 20.0 20.0, 20.0 -20.0, -20.0 -20.0)), POLYGON ((-30.0 -30.0, -30.0 30.0, 30.0 30.0, 30.0 -30.0, -30.0 -30.0)))";

	Assert.assertEquals(collection, shape);

	//
	//
	//

	TestUtils.compareResources(wrapper, dataset, folder, key);
    }
}
