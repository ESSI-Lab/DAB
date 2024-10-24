package eu.essi_lab.accessor.cdi.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.xml.sax.SAXParseException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import junit.framework.TestCase;

/**
 * Validates valid and invalid CDI and CSR documents against the latest SeaDataNet schemas (both XSD schema and
 * Schematron rules).
 * 
 * @author boldrini
 */
public class CDI11ValidatorExternalTestIT {

    org.slf4j.Logger logger = GSLoggerFactory.getLogger(CDI11ValidatorExternalTestIT.class);

    @Test
    public void testCDI() throws Exception {
	StreamSource cdiGood = new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("cdiValidationTest-good.xml"));
	StreamSource cdiWrong = new StreamSource(
		CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("cdiValidationTest-wrong-xsd.xml"));

	URL schemaURL = new URL("https://s3.amazonaws.com/gi-project-test-it/cdi-11.0.0.xsd");

	// XSD validation

	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	Schema schema = schemaFactory.newSchema(schemaURL);
	Validator validator = schema.newValidator();
	validator.validate(cdiGood);
	logger.info("Valid SeaDataNet CDI document");
	try {
	    validator.validate(cdiWrong);
	    TestCase.fail("Expected failed validation");
	} catch (SAXParseException e) {
	    // validation failed as expected
	}

	// Schematron validation

	InputStream xsltStream = CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("xslt/cdi-11.0.0.xslt");
	// DocumentBuilder documentBuilder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();

	// StreamSource stylesource = new StreamSource(stylesheet);
	Transformer transformer = XMLFactories.newTransformerFactory().newTransformer(new StreamSource(xsltStream));

	Downloader downloader = new Downloader();

	URIResolver resolver = new URIResolver() {

	    @Override
	    public Source resolve(String href, String base) throws TransformerException {
		logger.info("Resolving href: " + href + "  base: " + base);
		if (base == null || base.isEmpty()) {
		    Optional<InputStream> stream = downloader.downloadOptionalStream(href);
		    if (stream.isPresent()) {
			return new StreamSource(stream.get());
		    }
		}
		return null;
	    }
	};
	transformer.setURIResolver(resolver);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	StreamResult result = new StreamResult(baos);
	transformer.transform(new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("cdiValidationTest-good.xml")),
		result);
	XMLDocumentReader reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));
	TestCase.assertTrue(reader.evaluateBoolean("count(//*:failed-assert)=0"));

	baos = new ByteArrayOutputStream();
	result = new StreamResult(baos);
	transformer.transform(
		new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("cdiValidationTest-wrong-schematron.xml")),
		result);
	reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));

	TestCase.assertTrue(reader.evaluateBoolean("count(//*:failed-assert)=2"));

    }

    @Test
    public void testCSR() throws Exception {
	StreamSource csrGood = new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("csrValidationTest-good.xml"));
	StreamSource csrWrong = new StreamSource(
		CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("csrValidationTest-wrong-xsd.xml"));

	URL schemaURL = new URL("https://s3.amazonaws.com/gi-project-test-it/csr-4.0.0.xsd");

	// XSD validation

	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	Schema schema = schemaFactory.newSchema(schemaURL);
	Validator validator = schema.newValidator();
	validator.validate(csrGood);
	logger.info("Valid SeaDataNet CSR document");
	try {
	    validator.validate(csrWrong);
	    TestCase.fail("Expected failed validation");
	} catch (SAXParseException e) {
	    // validation failed as expected
	}

	// Schematron validation

	InputStream xsltStream = CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("xslt/csr-4.0.0.xslt");
	// DocumentBuilder documentBuilder = XMLFactories.newDocumentBuilderFactory().newDocumentBuilder();

	// StreamSource stylesource = new StreamSource(stylesheet);
	Transformer transformer = XMLFactories.newTransformerFactory().newTransformer(new StreamSource(xsltStream));

	Downloader downloader = new Downloader();

	URIResolver resolver = new URIResolver() {

	    @Override
	    public Source resolve(String href, String base) throws TransformerException {
		logger.info("Resolving href: " + href + "  base: " + base);
		if (base == null || base.isEmpty()) {
		    Optional<InputStream> stream = downloader.downloadOptionalStream(href);
		    if (stream.isPresent()) {
			return new StreamSource(stream.get());
		    }
		}
		return null;
	    }
	};
	transformer.setURIResolver(resolver);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	StreamResult result = new StreamResult(baos);
	transformer.transform(new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("csrValidationTest-good.xml")),
		result);
	XMLDocumentReader reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));
	TestCase.assertTrue(reader.evaluateBoolean("count(//*:failed-assert)=0"));

	baos = new ByteArrayOutputStream();
	result = new StreamResult(baos);
	transformer.transform(
		new StreamSource(CDI11ValidatorExternalTestIT.class.getClassLoader().getResourceAsStream("csrValidationTest-wrong-schematron.xml")),
		result);
	reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));

	TestCase.assertTrue(reader.evaluateBoolean("count(//*:failed-assert)=1"));
    }

}
