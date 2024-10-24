package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Citation;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;

public class CitationTest extends MetadataTest<Citation, CICitationType> {

    public CitationTest() {
	super(Citation.class, CICitationType.class);
    }

    @Override
    public void setProperties(Citation citation) {
	citation.setTitle("title");

    }

    @Override
    public void checkProperties(Citation citation) {
	org.junit.Assert.assertEquals("title", citation.getTitle());

    }

    @Override
    public void clearProperties(Citation citation) {
	citation.setTitle(null);
    }

    @Override
    public void checkNullProperties(Citation citation) {

	Assert.assertNull(citation.getTitle());
    }
}
