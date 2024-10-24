/**
 * 
 */
package eu.essi_lab.jaxb.common.test;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.schemas.BooleanValidationHandler;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.common.schemas.SchemaValidator;
import eu.essi_lab.lib.utils.ClonableInputStream;

/**
 * @author Fabrizio
 */
public class SchemaValidatorTest {

    @Test
    public void test1() throws JAXBException {

	SchemaValidator schemaValidator = new SchemaValidator();
	InputStream stream = getClass().getClassLoader().getResourceAsStream("getRecords1.xml");
	BooleanValidationHandler handler = schemaValidator.validate(stream, CommonSchemas.CSW_Discovery());

	Assert.assertTrue(handler.isValid());
	Assert.assertNull(handler.getEvent());
    }

    @Test
    public void test2() throws JAXBException {

	SchemaValidator schemaValidator = new SchemaValidator();

	InputStream stream = getClass().getClassLoader().getResourceAsStream("getRecords2.xml");
	BooleanValidationHandler handler = schemaValidator.validate(stream, CommonSchemas.CSW_Discovery());

	Assert.assertFalse(handler.isValid());
	Assert.assertNotNull(handler.getEvent());
    }

    @Test
    public void test3() throws JAXBException, IOException {

	SchemaValidator schemaValidator = new SchemaValidator();

	ClonableInputStream clone = new ClonableInputStream(getClass().getClassLoader().getResourceAsStream("testMiMetadata.xml"));

	BooleanValidationHandler handler = schemaValidator.validate(clone.clone(), CommonSchemas.GMI());

	Assert.assertFalse(handler.isValid());
	Assert.assertNotNull(handler.getEvent());
    }
}
