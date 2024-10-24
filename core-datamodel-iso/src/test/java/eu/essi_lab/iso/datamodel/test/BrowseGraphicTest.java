package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import net.opengis.iso19139.gmd.v_20060504.MDBrowseGraphicType;

public class BrowseGraphicTest extends MetadataTest<BrowseGraphic, MDBrowseGraphicType> {

    public BrowseGraphicTest() {
	super(BrowseGraphic.class, MDBrowseGraphicType.class);
    }

    @Override
    public void setProperties(BrowseGraphic metadata) {
	metadata.setFileDescription("description");
	metadata.setFileName("name");
	metadata.setFileType("type");
    }

    @Override
    public void checkProperties(BrowseGraphic metadata) {
	Assert.assertEquals("description", metadata.getFileDescription());
	Assert.assertEquals("name", metadata.getFileName());
	Assert.assertEquals("type", metadata.getFileType());
    }

    @Override
    public void clearProperties(BrowseGraphic metadata) {
	metadata.setFileDescription(null);
	metadata.setFileName(null);
	metadata.setFileType(null);

    }

    @Override
    public void checkNullProperties(BrowseGraphic metadata) {
	Assert.assertNull(metadata.getFileDescription());
	Assert.assertNull(metadata.getFileName());
	Assert.assertNull(metadata.getFileType());

    }

}
