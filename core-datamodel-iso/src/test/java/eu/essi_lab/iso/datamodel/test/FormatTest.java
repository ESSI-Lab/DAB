package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Format;
import net.opengis.iso19139.gmd.v_20060504.MDFormatType;

public class FormatTest extends MetadataTest<Format, MDFormatType> {

    public FormatTest() {
	super(Format.class, MDFormatType.class);
    }

    @Override
    public void setProperties(Format format) {
	format.setName("name");
	format.setVersion("version");

    }

    @Override
    public void checkProperties(Format format) {
	Assert.assertEquals("name", format.getName());
	Assert.assertEquals("version", format.getVersion());

    }

    @Override
    public void clearProperties(Format format) {
	format.setName(null);
	format.setVersion(null);

    }

    @Override
    public void checkNullProperties(Format format) {
	Assert.assertNull(format.getName());
	Assert.assertNull(format.getVersion());

    }
}
