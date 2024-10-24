package eu.essi_lab.iso.datamodel.test;

import java.math.BigInteger;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import net.opengis.iso19139.gmd.v_20060504.MDDimensionType;

public class DimensionTest extends MetadataTest<Dimension, MDDimensionType> {

    public DimensionTest() {
	super(Dimension.class, MDDimensionType.class);
    }

    @Override
    public void setProperties(Dimension dimension) {
	dimension.setDimensionNameTypeCode("time");
	dimension.setDimensionSize(BigInteger.TEN);
	dimension.setResolution("m", 10.);

    }

    @Override
    public void checkProperties(Dimension dimension) {
	Assert.assertEquals("time", dimension.getDimensionNameTypeCode());
	Assert.assertEquals(BigInteger.TEN, dimension.getDimensionSize());
	Assert.assertEquals("m", dimension.getResolutionUOM());
	Assert.assertEquals(10., dimension.getResolutionValue(), 10 ^ -7);

    }

    @Override
    public void clearProperties(Dimension dimension) {
	dimension.setDimensionNameTypeCode(null);
	dimension.setDimensionSize(null);
	dimension.clearResolution();
    }

    @Override
    public void checkNullProperties(Dimension dimension) {
	Assert.assertNull(dimension.getDimensionNameTypeCode());
	Assert.assertNull(dimension.getDimensionSize());
	Assert.assertNull(dimension.getResolutionUOM());
	Assert.assertNull(dimension.getResolutionValue());
    }
}
