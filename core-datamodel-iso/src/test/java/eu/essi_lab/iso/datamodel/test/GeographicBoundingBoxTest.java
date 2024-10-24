package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import net.opengis.iso19139.gmd.v_20060504.EXGeographicBoundingBoxType;

public class GeographicBoundingBoxTest extends MetadataTest<GeographicBoundingBox, EXGeographicBoundingBoxType> {

    public GeographicBoundingBoxTest() {
	super(GeographicBoundingBox.class, EXGeographicBoundingBoxType.class);
    }

    @Override
    public void setProperties(GeographicBoundingBox metadata) {
	metadata.setWest(-180.);
	metadata.setEast(180.);
	metadata.setSouth(-90.);
	metadata.setNorth(90.);
	metadata.setId("my-id");

    }

    @Override
    public void checkProperties(GeographicBoundingBox metadata) {
	Assert.assertEquals(-180, metadata.getWest(), 10E-13);
	Assert.assertEquals(180, metadata.getEast(), 10E-13);
	Assert.assertEquals(-90, metadata.getSouth(), 10E-13);
	Assert.assertEquals(90, metadata.getNorth(), 10E-13);
	Assert.assertEquals("my-id", metadata.getId());

    }

    @Override
    public void clearProperties(GeographicBoundingBox metadata) {
	metadata.setBigDecimalWest(null);
	metadata.setBigDecimalEast(null);
	metadata.setBigDecimalSouth(null);
	metadata.setBigDecimalNorth(null);
	metadata.setId(null);

    }

    @Override
    public void checkNullProperties(GeographicBoundingBox metadata) {
	Assert.assertNull(metadata.getWest());
	Assert.assertNull(metadata.getEast());
	Assert.assertNull(metadata.getSouth());
	Assert.assertNull(metadata.getNorth());
	Assert.assertNull(metadata.getId());

    }
}
