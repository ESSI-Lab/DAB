package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import net.opengis.iso19139.gmd.v_20060504.MDReferenceSystemType;

public class ReferenceSystemTest extends MetadataTest<ReferenceSystem, MDReferenceSystemType> {

    public ReferenceSystemTest() {
	super(ReferenceSystem.class, MDReferenceSystemType.class);
    }

    @Override
    public void setProperties(ReferenceSystem metadata) {
	metadata.setCodeSpace("EPSG");
	metadata.setCode("4326");
	metadata.setVersion("1984");

    }

    @Override
    public void checkProperties(ReferenceSystem metadata) {
	Assert.assertEquals("EPSG", metadata.getCodeSpace());
	Assert.assertEquals("4326", metadata.getCode());
	Assert.assertEquals("1984", metadata.getVersion());

    }

    @Override
    public void checkNullProperties(ReferenceSystem metadata) {
	Assert.assertNull(metadata.getCodeSpace());
	Assert.assertNull(metadata.getCode());
	Assert.assertNull(metadata.getVersion());

    }

    @Override
    public void clearProperties(ReferenceSystem metadata) {
	metadata.setCodeSpace(null);
	metadata.setCode(null);
	metadata.setVersion(null);

    }

}
