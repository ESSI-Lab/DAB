package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIPlatformType;

public class MIPlatformTest extends MetadataTest<MIPlatform, MIPlatformType> {

    public MIPlatformTest() {
	super(MIPlatform.class, MIPlatformType.class);
    }

    @Override
    public void setProperties(MIPlatform metadata) {
	metadata.setDescription("desc");
	metadata.setMDIdentifierCode("my-id");
	
	Citation citation = new Citation();
	citation.setTitle("EPSG Organization");
	metadata.setMDIdentifierAuthority(citation);
	Citation cit = new Citation();
	cit.setTitle("My platform");
	metadata.setCitation(cit);

    }

    @Override
    public void checkProperties(MIPlatform metadata) {
	Assert.assertEquals("desc", metadata.getDescription());
	Assert.assertEquals("EPSG Organization", metadata.getMDIdentifierAuthority().getTitle());
	Assert.assertEquals("my-id", metadata.getMDIdentifierCode());
	
	Assert.assertEquals("My platform", metadata.getCitation().getTitle());
    }

    @Override
    public void clearProperties(MIPlatform metadata) {
	metadata.setDescription(null);
	metadata.setMDIdentifierCode(null);
	metadata.setMDIdentifierAuthority(null);
	
	metadata.setCitation(null);
    }

    @Override
    public void checkNullProperties(MIPlatform metadata) {
	Assert.assertNull(metadata.getDescription());
	Assert.assertNull(metadata.getMDIdentifierCode());
	
	Assert.assertNull(metadata.getMDIdentifierAuthority());
	Assert.assertNull(metadata.getCitation());
    }

}
