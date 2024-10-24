package eu.essi_lab.iso.datamodel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import net.opengis.iso19139.gmd.v_20060504.EXVerticalExtentType;

public class VerticalExtentTest extends MetadataTest<VerticalExtent, EXVerticalExtentType> {

    public VerticalExtentTest() {
	super(VerticalExtent.class, EXVerticalExtentType.class);
    }

    private static final double TOL = 10E-13;

    @Test
    public void test() {
	try {

	    VerticalExtent vertical = new VerticalExtent();

	    test(vertical);

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    vertical.toStream(outputStream);

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

	    VerticalExtent vertical2 = new VerticalExtent(inputStream);
	    test(vertical2);
	    test(vertical2.getElementType());

	} catch (Exception ex) {

	    ex.printStackTrace();

	    fail("Exception thrown");
	}

    }

    private void test(EXVerticalExtentType elementType) {

	test(new VerticalExtent(elementType));
    }

    private void test(VerticalExtent vertical) {

    }

    @Override
    public void setProperties(VerticalExtent vertical) {
	vertical.setMinimumValue(-18.);
	vertical.setMaximumValue(23.);

    }

    @Override
    public void checkProperties(VerticalExtent vertical) {
	Assert.assertEquals(vertical.getMinimumValue(), -18.0, TOL);
	Assert.assertEquals(vertical.getMaximumValue(), 23.0, TOL);

    }

    @Override
    public void clearProperties(VerticalExtent vertical) {
	vertical.setMinimumValue(null);
	vertical.setMaximumValue(null);
    }

    @Override
    public void checkNullProperties(VerticalExtent vertical) {
	Assert.assertNull(vertical.getMinimumValue());
	Assert.assertNull(vertical.getMaximumValue());
    }
}
