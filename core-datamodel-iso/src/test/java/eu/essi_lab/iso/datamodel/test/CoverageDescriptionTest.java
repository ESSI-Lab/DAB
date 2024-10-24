package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import net.opengis.iso19139.gmd.v_20060504.MDCoverageDescriptionType;

public class CoverageDescriptionTest extends MetadataTest<CoverageDescription, MDCoverageDescriptionType> {

    public CoverageDescriptionTest() {
	super(CoverageDescription.class, MDCoverageDescriptionType.class);
    }

    @Override
    public void setProperties(CoverageDescription metadata) {
	metadata.setAttributeDescription("attrDesc");
	metadata.setAttributeIdentifier("attrId");
    }

    @Override
    public void checkProperties(CoverageDescription metadata) {
	Assert.assertEquals("attrDesc", metadata.getAttributeDescription());
	Assert.assertEquals("attrId", metadata.getAttributeIdentifier());
    }

    @Override
    public void clearProperties(CoverageDescription metadata) {
	metadata.setAttributeDescription(null);
	metadata.setAttributeIdentifier(null);

    }

    @Override
    public void checkNullProperties(CoverageDescription metadata) {
	Assert.assertNull(metadata.getAttributeDescription());
	Assert.assertNull(metadata.getAttributeIdentifier());

    }

}
