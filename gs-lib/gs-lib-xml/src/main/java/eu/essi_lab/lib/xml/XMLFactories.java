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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

/**
 * @author Fabrizio
 */
public class XMLFactories {

    /**
     * SAXON is used ads default, see <code>TransformerFactoryTest</code> for info
     */
    public static TransformerFactoryImpl defaultTransformerFactoryImpl = TransformerFactoryImpl.SAXON;

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
	 * It seems to be more slowly for unmarshalling/marshalling than XALAN, but it ensures that children nodes
	 * have all the name space declarations of the parent node
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

	return newTransformerFactory(defaultTransformerFactoryImpl);
    }

    /**
     * @param impl
     * @return
     */
    public static TransformerFactory newTransformerFactory(TransformerFactoryImpl impl) {

	TransformerFactory factory = TransformerFactory.newInstance(//
		impl.getImpl(), //
		TransformerFactory.class.getClassLoader());//

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
}
