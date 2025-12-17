package eu.essi_lab.lib.xml;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import net.sf.saxon.lib.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.*;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * @author Fabrizio
 */
public class XMLFactories {

    /**
     * SAXON is used as default, see <code>TransformerFactoryTest</code> for info
     */
    public static TransformerFactoryImpl DEFAULT_IMPL = TransformerFactoryImpl.SAXON;

    /**
     * See <code>TransformerFactoryTest</code> for info
     *
     * @author Fabrizio
     */
    public enum TransformerFactoryImpl {
	/**
	 * It seems to be faster for unmarshalling/marshalling than SAXON, but there is a problem with the attributes
	 */
	XALAN("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"),
	/**
	 * It seems to be more slowly for unmarshalling/marshalling than XALAN, but it ensures that children nodes have all the name space
	 * declarations of the parent node
	 */
	SAXON("net.sf.saxon.TransformerFactoryImpl");

	private String impl;

	private TransformerFactoryImpl(String impl) {

	    this.impl = impl;
	}

	/**
	 * @return the impl
	 */
	public String getImpl() {

	    return impl;
	}
    }

    /**
     * @return
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() {

	DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
	documentFactory.setNamespaceAware(true);
	documentFactory.setValidating(false);
	documentFactory.setIgnoringComments(false);
	documentFactory.setExpandEntityReferences(false);
	documentFactory.setIgnoringElementContentWhitespace(false);
	documentFactory.setCoalescing(false);

	return documentFactory;
    }

    /**
     * @return
     */
    public static TransformerFactory newTransformerFactory() {

	return newTransformerFactory(DEFAULT_IMPL);
    }

    /**
     * @param impl
     * @return
     */
    public static TransformerFactory newTransformerFactory(TransformerFactoryImpl impl) {

	TransformerFactory factory = TransformerFactory.newInstance(//
		impl.getImpl(), //
		TransformerFactory.class.getClassLoader());//

	factory.setAttribute(FeatureKeys.DTD_VALIDATION, false);
	factory.setAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);
	factory.setAttribute(FeatureKeys.RECOVERY_POLICY, 0);

	return factory;
    }

    /**
     * @return
     * @throws XPathFactoryConfigurationException
     */
    public static XPathFactory newXPathFactory() throws XPathFactoryConfigurationException {

	XPathFactory xpathFactory = XPathFactory.newInstance(//
		XPathFactory.DEFAULT_OBJECT_MODEL_URI, //
		"net.sf.saxon.xpath.XPathFactoryImpl", //
		XPathFactory.class.getClassLoader());

	return xpathFactory;
    }

    static {

	//
	// This is required in order to load the SUN implementation of the factory instead of the woodstox
	// implementation
	//

	System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.internal.stream.XMLInputFactoryImpl");
    }

    /**
     * @return
     */
    public static XMLInputFactory newXMLInputFactory() {

	XMLInputFactory inputFactory = XMLInputFactory.newFactory("javax.xml.stream.XMLInputFactory", null);

	inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

	return inputFactory;
    }

    /**
     * @return
     */
    public static SchemaFactory newSchemaFactory() throws SAXNotSupportedException, SAXNotRecognizedException {

	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
	schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

	return schemaFactory;
    }
}
