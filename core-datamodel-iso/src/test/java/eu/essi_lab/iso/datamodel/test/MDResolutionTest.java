package eu.essi_lab.iso.datamodel.test;

import java.math.BigInteger;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import net.opengis.iso19139.gmd.v_20060504.MDResolutionType;

public class MDResolutionTest extends MetadataTest<MDResolution, MDResolutionType> {

    public MDResolutionTest() {
	super(MDResolution.class, MDResolutionType.class);
    }

    @Override
    public void setProperties(MDResolution resolution) {
	resolution.setDistance("m", 10.);
	resolution.setEquivalentScale(BigInteger.TEN);
    }

    @Override
    public void checkProperties(MDResolution resolution) {

	Assert.assertEquals("m", resolution.getDistanceUOM());
	Assert.assertEquals(10., resolution.getDistanceValue(), 10 ^ -7);
	Assert.assertEquals(BigInteger.TEN, resolution.getEquivalentScale());

    }

    @Override
    public void clearProperties(MDResolution resolution) {

	resolution.clearDistance();
	resolution.clearEquivalentScale();

    }

    @Override
    public void checkNullProperties(MDResolution resolution) {

	Assert.assertNull(resolution.getDistanceUOM());
	Assert.assertNull(resolution.getDistanceValue());
	Assert.assertNull(resolution.getEquivalentScale());

    }
}
