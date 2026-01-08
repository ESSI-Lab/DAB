package eu.essi_lab.accessor.cdi;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;

public abstract class CDIValidator {

    Logger logger = GSLoggerFactory.getLogger(getClass());
    private Validator schemaValidator;

    private static URIResolver resolver = new CDIURIResolver();

    public CDIValidator() {

	try {
	    URL schemaURL = getSchemaURL();
	    // XSD validation
	    SchemaFactory schemaFactory = XMLFactories.newSchemaFactory();

	    Schema schema = schemaFactory.newSchema(schemaURL);

	    this.schemaValidator = schema.newValidator();
	    this.schemaValidator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");

	} catch (Exception e) {
	    // This should not happen
	    e.printStackTrace();
	}

    }

    public ValidationResult validate(byte[] array) {

	ValidationResult response = new ValidationResult();
	response.setPassed(false);
	try {
	    ByteArrayInputStream bais = new ByteArrayInputStream(array);
	    StreamSource source = new StreamSource(bais);
	    schemaValidator.validate(source);
	    bais.close();
	    logger.info("Valid SeaDataNet CDI document according to XML schema");
	} catch (Exception e) {
	    // validation failed
	    e.printStackTrace();

	    response.setErrorMessage("XML schema error");
	    return response;
	}

	// Schematron validation

	InputStream xsltStream = getSchematronXSLT();

	Transformer transformer;
	try {
	    transformer = XMLFactories.newTransformerFactory().newTransformer(new StreamSource(xsltStream));
	} catch (TransformerConfigurationException e) {
	    e.printStackTrace();
	    response.setErrorMessage("transformer configuration exception");
	    return response;
	}

	transformer.setURIResolver(resolver);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	StreamResult result = new StreamResult(baos);
	try {
	    ByteArrayInputStream bais = new ByteArrayInputStream(array);
	    StreamSource source = new StreamSource(bais);
	    transformer.transform(source, result);
	    bais.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    response.setErrorMessage("XSLT exception");
	    return response;
	}
	XMLDocumentReader reader;
	try {
	    reader = new XMLDocumentReader(new ByteArrayInputStream(baos.toByteArray()));
	} catch (SAXException | IOException e) {
	    e.printStackTrace();
	    response.setErrorMessage("XML reader exception");
	    return response;
	}
	try {
	    Boolean ret = reader.evaluateBoolean("count(//*:failed-assert)=0");
	    if (ret) {
		logger.info("Valid SeaDataNet CDI document according to Schematron rules");
		response.setPassed(true);
	    } else {
		Node[] nodes = reader.evaluateNodes("//*:failed-assert");
		TreeSet<String> uniqueErrors = new TreeSet<String>();		
		for (Node node : nodes) {
		    uniqueErrors.add(reader.evaluateString(node, "*:text"));
		}
		String errorMessage = "";
		for (String uniqueError : uniqueErrors) {
		    errorMessage = errorMessage + uniqueError + "\n";
		}
		response.setErrorMessage(errorMessage);
	    }
	    return response;
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    response.setErrorMessage("XPath exception");
	    return response;
	}

    }

    public abstract URL getSchemaURL();

    /**
     * Schematron schemata can be translated to XSLT using the plugin found in the pom!
     * 
     * @return
     */
    public abstract InputStream getSchematronXSLT();

}
