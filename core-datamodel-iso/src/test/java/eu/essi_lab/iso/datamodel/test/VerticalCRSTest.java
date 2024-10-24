package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import net.opengis.gml.v_3_2_0.VerticalCRSType;

public class VerticalCRSTest extends MetadataTest<VerticalCRS, VerticalCRSType> {

    public VerticalCRSTest() {
	super(VerticalCRS.class, VerticalCRSType.class);
    }

    @Override
    public void setProperties(VerticalCRS vertical) {
	vertical.setId("ID");

    }

    @Override
    public void checkProperties(VerticalCRS vertical) {
	Assert.assertEquals("ID", vertical.getId());

    }

    @Override
    public void clearProperties(VerticalCRS vertical) {
	vertical.setId(null);
    }

    @Override
    public void checkNullProperties(VerticalCRS vertical) {
	Assert.assertNull(vertical.getId());
    }
}
