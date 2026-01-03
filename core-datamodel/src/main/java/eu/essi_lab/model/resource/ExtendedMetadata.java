package eu.essi_lab.model.resource;

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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLFactories;

/**
 * An extension element <code>&lt;gs:extension&gt;</code> for any type of additional metadatas
 * 
 * @author Fabrizio
 */
public class ExtendedMetadata {

    @XmlTransient
    private Document document;

    @XmlTransient
    private XMLDocumentReader xmlDocReader;

    @XmlTransient
    private XMLDocumentWriter xmlDocWriter;

    @XmlAnyElement
    private Element extension;

    ExtendedMetadata() {
    }

    @XmlTransient
    public XMLDocumentReader getReader() {
	init();
	return xmlDocReader;
    }

    private void init() {

	if (xmlDocReader == null) {

	    try {
		DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
		factory.setNamespaceAware(true);

		if (extension == null) {

		    String ext = "<gs:extension xmlns:gs=\"" + NameSpace.GS_DATA_MODEL_SCHEMA_URI + "\"></gs:extension>";
		    xmlDocReader = new XMLDocumentReader(
			    factory.newDocumentBuilder().parse(new ByteArrayInputStream(ext.getBytes(StandardCharsets.UTF_8))));

		} else {

		    xmlDocReader = new XMLDocumentReader(extension.getOwnerDocument());
		}

		xmlDocReader.setNamespaceContext(new CommonNameSpaceContext());

		document = xmlDocReader.getDocument();

		xmlDocWriter = new XMLDocumentWriter(xmlDocReader);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(GSResource.class).error("Fatal initialization error!");
		GSLoggerFactory.getLogger(GSResource.class).error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Adds the supplied <code>node</code>
     * 
     * @param node a non <code>null</code> node
     */
    public void add(Node node) {

	init();

	document.adoptNode(node);
	document.getDocumentElement().appendChild(node);

	extension = document.getDocumentElement();
    }

    /**
     * Creates an xml element with the supplied <code>prefix</code>, <code>nameSpaceURI</code>, <code>elementName</code>
     * and <code>textContent</code> and, if an an xml element with such <code>elementName</code> do not already exists,
     * adds it to the extension element
     * 
     * @param prefix a non <code>null</code> prefix
     * @param nameSpaceURI a non <code>null</code> uri
     * @param elementName a non <code>null</code> element name
     * @param textContent a non <code>null</code> text content
     * @return
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws IOException
     */
    public void add(String prefix, String nameSpaceURI, String elementName, String textContent)
	    throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

	init();

	Element element = document.createElementNS(nameSpaceURI, prefix + ":" + elementName);
	if (textContent != null) {
	    element.setTextContent(textContent);
	}

	document.getDocumentElement().appendChild(element);
	extension = document.getDocumentElement();

    }

    /**
     * Creates an xml element with with the "gs"
     * prefix and GI-suite name space URI and <code>elementName</code> and, if an an xml
     * element with such <code>elementName</code> do not already exists,
     * adds it to the extension element
     * 
     * @param elementName a non <code>null</code> element name
     * @return
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws IOException
     */
    public void add(String elementName) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

	add("gs", NameSpace.GS_DATA_MODEL_SCHEMA_URI, elementName, null);
    }

    /**
     * Creates an xml element with the supplied <code>elementName</code> and <code>textContent</code> with the "gs"
     * prefix and GI-suite
     * name space URI ( {@link NameSpace#GS_DATA_MODEL_SCHEMA_URI} ) and, if an an xml
     * element with such <code>elementName</code> do not already exists, adds it to the extension element
     * 
     * @see #getTextContent(String)
     * @param elementName a non <code>null</code> element name
     * @param textContent a non <code>null</code> text content
     * @return
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     */
    public void add(String elementName, String textContent)
	    throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

	add("gs", NameSpace.GS_DATA_MODEL_SCHEMA_URI, elementName, textContent);
    }

    /**
     * Set the text content to the xml element with the supplied <code>prefix</code> and <code>elementName</code>
     * 
     * @param prefix a non <code>null</code> prefix
     * @param elementName a non <code>null</code> element name
     * @param textContent a non <code>null</code> text content
     * @return <code>true</code> if the xml element with the supplied <code>elementName</code> exists,
     *         <code>false</code> otherwise
     * @throws XPathExpressionException
     */
    public boolean setTextContent(String prefix, String elementName, String textContent) throws XPathExpressionException {

	init();

	Node[] nodes = xmlDocReader.evaluateNodes("//" + prefix + ":" + elementName);
	if (nodes.length == 0) {
	    return false;
	}
	Node node = nodes[0];
	node.setTextContent(textContent);

	extension = document.getDocumentElement();

	return true;
    }

    /**
     * Set the text content to the xml element with the supplied <code>prefix</code> and <code>elementName</code> and
     * the "gs" prefix
     * 
     * @param elementName a non <code>null</code> element name
     * @param textContent a non <code>null</code> text content
     * @return <code>true</code> if the xml element exists, <code>false</code> otherwise
     * @throws XPathExpressionException
     */
    public boolean setTextContent(String elementName, String textContent) throws XPathExpressionException {

	return setTextContent("gs", elementName, textContent);
    }

    /**
     * Gets the text content of the xml element with the supplied <code>prefix</code> and <code>elementName</code>
     * 
     * @param prefix a non <code>null</code> prefix
     * @param elementName a non <code>null</code> element name
     * @return the text content of the xml element or
     *         <code>null</code> if such element do not exists or has no text content
     * @throws XPathExpressionException
     */
    public String getTextContent(String prefix, String elementName) throws XPathExpressionException {

	init();

	String textContent = xmlDocReader.evaluateString("//" + prefix + ":" + elementName);
	if (textContent != null && textContent.equals("")) {
	    return null;
	}

	return textContent;
    }

    /**
     * Gets the text content of the first xml element with the supplied <code>elementName</code> and the "gs" prefix
     * 
     * @param elementName a non <code>null</code> element name
     * @see #add(String, String)
     * @return the text content of the xml element resulting from the evaluation of the supplied <code>xPath</code> or
     *         <code>null</code> if such element do not exists or has no text content
     * @throws XPathExpressionException
     */
    public String getTextContent(String elementName) throws XPathExpressionException {

	List<String> textContents = getTextContents(elementName);
	if (textContents.isEmpty()) {
	    return null;
	}
	return textContents.get(0);

    }

    /**
     * Gets the text contents of the xml element with the supplied <code>elementName</code> and the "gs" prefix
     * 
     * @param elementName a non <code>null</code> element name
     * @see #add(String, String)
     * @return a list of text content of the xml elements resulting from the evaluation of the supplied
     *         <code>xPath</code> or
     *         <code>null</code> if such element do not exists or has no text content
     * @throws XPathExpressionException
     */
    public List<String> getTextContents(String elementName) throws XPathExpressionException {

	init();

	List<String> ret = new ArrayList<>();

	Node[] nodes = xmlDocReader.evaluateNodes("//gs:" + elementName);

	for (Node node : nodes) {
	    String textContent = node.getTextContent();
	    if (textContent != null && !textContent.equals("")) {
		ret.add(textContent);
	    }
	}

	return ret;
    }

    /**
     * Evaluates the supplied <code>xPath</code> and retrieves the list (possible empty) of resulting nodes
     * 
     * @param xPath a non <code>null</code>, valid xPath
     * @return the list of nodes (possible empty) resulting from the evaluation of the supplied <code>xPath</code>
     * @throws XPathExpressionException
     */
    public List<Node> get(String xPath) throws XPathExpressionException {

	init();

	Node[] nodes = xmlDocReader.evaluateNodes(xPath);
	return Arrays.asList(nodes);
    }

    /**
     * Removes from the extension the node resulting from the evaluation of the supplied <code>xPath</code>
     * 
     * @param xPath a non <code>null</code>, valid xPath
     * @return <code>true</code> if the xml element exists and has been removed, <code>false</code> otherwise
     * @throws XPathExpressionException
     */
    public boolean remove(String xPath) throws XPathExpressionException {

	init();

	boolean remove = xmlDocWriter.remove(xPath);

	extension = document.getDocumentElement();
	return remove;
    }

    /**
     * Removes all the xml elements from the extension
     */
    public void clear() {

	init();

	try {
	    xmlDocWriter.remove("//gs:extension/*");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    // it should not happen
	}

	extension = document.getDocumentElement();
    }

    public String toString() {

	init();

	XMLDocumentReader reader;
	try {
	    reader = new XMLDocumentReader(document);
	    return reader.asString();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "Error occurred during element transformation";
    }
}
