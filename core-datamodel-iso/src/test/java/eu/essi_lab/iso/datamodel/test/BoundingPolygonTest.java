package eu.essi_lab.iso.datamodel.test;

import java.util.ArrayList;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import net.opengis.iso19139.gmd.v_20060504.EXBoundingPolygonType;

public class BoundingPolygonTest extends MetadataTest<BoundingPolygon, EXBoundingPolygonType> {

    public BoundingPolygonTest() {
	super(BoundingPolygon.class, EXBoundingPolygonType.class);
    }

    private ArrayList<Double> coordinates;

    public void init() {
	this.coordinates = new ArrayList<>();
	coordinates.add(-90.);
	coordinates.add(-180.);
	coordinates.add(90.);
	coordinates.add(-180.);
	coordinates.add(0.);
	coordinates.add(0.);
	coordinates.add(-90.);
	coordinates.add(-180.);
    }

    @Override
    public void setProperties(BoundingPolygon metadata) {

	metadata.setCoordinates(coordinates);
	metadata.setId("my-id");

    }

    @Override
    public void checkProperties(BoundingPolygon metadata) {
	ArrayList<Double> actualArray = Lists.newArrayList(metadata.getCoordinates());
	Assert.assertArrayEquals(coordinates.toArray(new Double[] {}), actualArray.toArray(new Double[] {}));
	Assert.assertEquals("my-id", metadata.getId());

    }

    @Override
    public void clearProperties(BoundingPolygon metadata) {
	metadata.clearCoordinates();
	metadata.setId(null);

    }

    @Override
    public void checkNullProperties(BoundingPolygon metadata) {
	Assert.assertFalse(metadata.getCoordinates().hasNext());
	Assert.assertNull(metadata.getId());

    }
}
