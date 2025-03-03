/**
 * 
 */
package eu.essi_lab.api.database.opensearch.shape.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.opensearch.index.Shape;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 */
public class ShapeTest {

    //
    // LINESTRING from BoundingPolygon
    //

    @Test
    public void lineStringFromBoundingPolygonTest() {

	BoundingPolygon boundingPolygon = new BoundingPolygon();

	List<List<Double>> list = new ArrayList<>();

	List<Double> list1 = Arrays.asList(10.0, 20.0);
	List<Double> list2 = Arrays.asList(15.0, 25.0);
	List<Double> list3 = Arrays.asList(25.0, 35.0);

	list.add(list1);
	list.add(list2);
	list.add(list3);

	boundingPolygon.setMultiPoints(list);

	//
	//
	//

	Optional<Shape> optShape = Shape.of(boundingPolygon);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();
	Assert.assertEquals("LINESTRING (10.0 20.0, 15.0 25.0, 25.0 35.0)", shape);

	double area = optShape.get().getArea();
	Assert.assertEquals(Double.valueOf(0), Double.valueOf(area));
    }

    //
    // POLYGON from BoundingPolygon
    //

    @Test
    public void polygonFromBoundingPolygonTest() {

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

	//
	//
	//

	Optional<Shape> optShape = Shape.of(boundingPolygon);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();
	Assert.assertEquals("POLYGON ((30.0 10.0, 40.0 40.0, 20.0 40.0, 10.0 20.0, 30.0 10.0))", shape);

	double area = optShape.get().getArea();
	Assert.assertEquals(Double.valueOf(550), Double.valueOf(area));
    }

    //
    // POINT
    //
    @Test
    public void pointTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(10));
	box.setBigDecimalEast(new BigDecimal(10));

	box.setBigDecimalSouth(new BigDecimal(10));
	box.setBigDecimalWest(new BigDecimal(10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	Assert.assertEquals("POINT (10.0 10.0)", shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(0), Double.valueOf(area));
    }

    //
    // MULTIPOINT
    //
    @Test
    public void multiPointTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

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

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	Assert.assertEquals("MULTIPOINT (10.0 10.0, 20.0 20.0, 30.0 30.0)", shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(0), Double.valueOf(area));
    }

    //
    // POLYGON
    //
    @Test
    public void polygonTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(10));
	box.setBigDecimalEast(new BigDecimal(10));

	box.setBigDecimalSouth(new BigDecimal(-10));
	box.setBigDecimalWest(new BigDecimal(-10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	Assert.assertEquals("POLYGON ((-10.0 -10.0, -10.0 10.0, 10.0 10.0, 10.0 -10.0, -10.0 -10.0))", shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(400), Double.valueOf(area));
    }

    //
    // POLYGON with south > north. SpatialIndexHelper in this case switches north with south.
    // It is the right behavior?
    //
    @Test
    public void southGTNorthPolygonTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

	GeographicBoundingBox box = new GeographicBoundingBox();

	box.setBigDecimalNorth(new BigDecimal(-10));
	box.setBigDecimalSouth(new BigDecimal(10));

	box.setBigDecimalEast(new BigDecimal(10));
	box.setBigDecimalWest(new BigDecimal(-10));

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	Assert.assertEquals("POLYGON ((-10.0 -10.0, -10.0 10.0, 10.0 10.0, 10.0 -10.0, -10.0 -10.0))", shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(400), Double.valueOf(area));
    }

    //
    // POLYGON with south > north. SpatialIndexHelper in this case switches north with south.
    // It is the right behavior?
    //
    @Test
    public void outOfScaleValuesPolygonTest() {

	//
	// west < -180
	//

	{

	    Dataset dataset = new Dataset();
	    dataset.setSource(new GSSource("sourceId"));

	    GeographicBoundingBox box = new GeographicBoundingBox();

	    box.setBigDecimalNorth(new BigDecimal(10));
	    box.setBigDecimalSouth(new BigDecimal(-10));

	    box.setBigDecimalEast(new BigDecimal(10));

	    box.setBigDecimalWest(new BigDecimal(-190));

	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	    IndexedElementsWriter.write(dataset);

	    BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	    Assert.assertFalse(Shape.of(boundingBox).isPresent());
	}

	//
	// west > 180
	//

	{

	    Dataset dataset = new Dataset();
	    dataset.setSource(new GSSource("sourceId"));

	    GeographicBoundingBox box = new GeographicBoundingBox();

	    box.setBigDecimalNorth(new BigDecimal(10));
	    box.setBigDecimalSouth(new BigDecimal(-10));

	    box.setBigDecimalEast(new BigDecimal(10));

	    box.setBigDecimalWest(new BigDecimal(190));

	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	    IndexedElementsWriter.write(dataset);

	    BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	    Assert.assertFalse(Shape.of(boundingBox).isPresent());
	}

	//
	// east > 180. in this case (e > (180 + TOL)) east is reduced of 180
	//

	{

	    Dataset dataset = new Dataset();
	    dataset.setSource(new GSSource("sourceId"));

	    GeographicBoundingBox box = new GeographicBoundingBox();

	    box.setBigDecimalNorth(new BigDecimal(10));
	    box.setBigDecimalSouth(new BigDecimal(-10));

	    box.setBigDecimalEast(new BigDecimal(190));

	    box.setBigDecimalWest(new BigDecimal(10));

	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	    IndexedElementsWriter.write(dataset);

	    BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	    Optional<Shape> optShape = Shape.of(boundingBox);

	    Assert.assertTrue(optShape.isPresent());

	    Shape shape = optShape.get();

	    Assert.assertEquals("POLYGON ((-170.0 -10.0, -170.0 10.0, 10.0 10.0, 10.0 -10.0, -170.0 -10.0))", shape.getShape());
	}

	//
	// east < -180
	//

	{
	    Dataset dataset = new Dataset();
	    dataset.setSource(new GSSource("sourceId"));

	    GeographicBoundingBox box = new GeographicBoundingBox();

	    box.setBigDecimalNorth(new BigDecimal(10));
	    box.setBigDecimalSouth(new BigDecimal(-10));

	    box.setBigDecimalEast(new BigDecimal(-190));

	    box.setBigDecimalWest(new BigDecimal(10));

	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addGeographicBoundingBox(box);

	    IndexedElementsWriter.write(dataset);

	    BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	    Assert.assertFalse(Shape.of(boundingBox).isPresent());
	}
    }

    //
    // MULTIPOLYGON
    //
    @Test
    public void multiPolygonTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

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

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	String multi = "MULTIPOLYGON (((-2.0 43.0, -2.0 48.0, 5.0 48.0, 5.0 43.0, -2.0 43.0)), ((16.0 44.0, 16.0 50.0, 27.0 50.0, 27.0 44.0, 16.0 44.0)), ((6.0 31.0, 6.0 34.0, 13.0 34.0, 13.0 31.0, 6.0 31.0)))";

	Assert.assertEquals(multi, shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(122), Double.valueOf(area));
    }

    //
    // GEOMETRYCOLLECTION
    //
    @Test
    public void geometryCollectionTest() {

	Dataset dataset = new Dataset();
	dataset.setSource(new GSSource("sourceId"));

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

	IndexedElementsWriter.write(dataset);

	//
	//
	//

	BoundingBox boundingBox = dataset.getIndexesMetadata().readBoundingBox().get();

	Optional<Shape> optShape = Shape.of(boundingBox);

	Assert.assertTrue(optShape.isPresent());

	String shape = optShape.get().getShape();

	String collection = "GEOMETRYCOLLECTION (POINT (10.0 10.0), POLYGON ((-20.0 -20.0, -20.0 20.0, 20.0 20.0, 20.0 -20.0, -20.0 -20.0)), POLYGON ((-30.0 -30.0, -30.0 30.0, 30.0 30.0, 30.0 -30.0, -30.0 -30.0)))";

	Assert.assertEquals(collection, shape);

	double area = optShape.get().getArea();

	Assert.assertEquals(Double.valueOf(5200), Double.valueOf(area));
    }

}
