/**
 * 
 */
package eu.essi_lab.model.resource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.MD_MetadataPatch;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.jaxb.common.schemas.BooleanValidationHandler;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.common.schemas.SchemaValidator;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * See GIP-412
 * 
 * @author Fabrizio
 */
public class PangaeaMetadataTest {

    @Test
    public void test() throws JAXBException, UnsupportedEncodingException {

	SchemaValidator sv = new SchemaValidator();

	InputStream stream = getClass().getClassLoader().getResourceAsStream("pangaea-md.xml");

	MDMetadata mdMetadata = new MDMetadata(stream);

	try {
	    BooleanValidationHandler handler = sv.validate( //
		    mdMetadata.asStream(), //
		    CommonSchemas.GMD());

	    Assert.assertFalse(handler.isValid());

	    MD_MetadataPatch.applyDateTimePatch(mdMetadata);

	    handler = sv.validate( //
		    mdMetadata.asStream(), //
		    CommonSchemas.GMD());

	    Assert.assertTrue(handler.isValid());

	} catch (UnsupportedEncodingException | JAXBException e) {

	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	}
    }
}
