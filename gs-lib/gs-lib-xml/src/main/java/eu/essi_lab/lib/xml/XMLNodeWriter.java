package eu.essi_lab.lib.xml;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extends {@link XMLDocumentReader} by adding writing functionalities
 * 
 * @author Fabrizio
 */
public class XMLNodeWriter {

    private XMLNodeReader xmlNodeReader;

    /**
     * Creates a new <code>XMLDocumentWriter</code> from the supplied {@link XMLDocumentReader}
     * 
     * @param xmlNodeReader
     * @throws SAXException
     * @throws IOException
     */
    public XMLNodeWriter(XMLNodeReader xmlNodeReader) {

	this.xmlNodeReader = xmlNodeReader;
    }

    /**
     * Removes the nodes resulting from the supplied <code>xPath</code>
     * 
     * @param xPath
     * @return <code>true</code> if the the nodes list resulting from the supplied <code>xPath</code> is not empty,
     *         <code>false</code> otherwise
     * @throws XPathExpressionException
     */
    public boolean remove(String xPath) throws XPathExpressionException {

	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	if (list.isEmpty()) {
	    return false;
	}
	for (Node node : list) {
	    remove(node);
	}
	return true;
    }

    private void remove(Node node) {

	Node prev = node.getPreviousSibling();
	//
	// removes also the "indent" before the element and the "carriage return"
	// see GIP-293
	//
	if (prev != null && prev.getNodeType() == Node.TEXT_NODE && prev.getNodeValue().trim().length() == 0) {
	    node.getParentNode().removeChild(prev);
	}

	node.getParentNode().removeChild(node);
    }

    public void removeAllAttributesFromNode(Node node) {

	while (node.getAttributes().getLength() > 0) {
	    Node att = node.getAttributes().item(0);
	    node.getAttributes().removeNamedItem(att.getNodeName());
	}
    }

    /**
     * Renames the nodes resulting from the supplied <code>xPath</code> with the given name
     * 
     * @param xPath
     * @param qualifiedName the new qualified name, such as gco:CharacterString or CharacterString in case of no
     *        namespace is provided
     * @throws XPathExpressionException
     */
    public void rename(String xPath, String qualifiedName) throws XPathExpressionException {

	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	for (Node node : list) {
	    rename(node, qualifiedName);

	}
    }

    public void rename(Node targetNode, String xPath, String qualifiedName) throws XPathExpressionException {

	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(targetNode, xPath);
	for (Node node : list) {
	    rename(node, qualifiedName);
	}
    }

    /**
     * Removes all the attributes matching the qualified name from the elements result of the xPath
     * 
     * @param xPath
     * @param qualifiedName
     * @return
     * @throws XPathExpressionException
     */
    public boolean remove(String xPath, String localName) throws XPathExpressionException {

	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	if (list.isEmpty()) {
	    return false;
	}
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		element.removeAttribute(localName);
	    }
	}
	return true;
    }
    
    public boolean remove(String xPath, String namespace, String localName) throws XPathExpressionException {

	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	if (list.isEmpty()) {
	    return false;
	}
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		element.removeAttributeNS(namespace, localName);
	    }
	}
	return true;
    }

    private void rename(Node node, String qualifiedName) {
	if (node instanceof Element) {
	    Element element = (Element) node;
	    if (qualifiedName.contains(":")) {
		String[] split = qualifiedName.split(":");
		String prefix = split[0];
		NamespaceContext namespaceContext = this.xmlNodeReader.getNamespaceContext();
		if (namespaceContext != null) {
		    String namespace = namespaceContext.getNamespaceURI(prefix);
		    if (namespace != null) {
			element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace);
			Document ownerDocument = this.xmlNodeReader.targetNode.getOwnerDocument();
			if (ownerDocument == null) {
			    ownerDocument = (Document) xmlNodeReader.targetNode;
			}
			ownerDocument.renameNode(element, namespace, qualifiedName);
		    }
		}
	    } else {
		Document ownerDocument = this.xmlNodeReader.targetNode.getOwnerDocument();
		if (ownerDocument == null) {
		    ownerDocument = (Document) xmlNodeReader.targetNode;
		}
		ownerDocument.renameNode(element, element.getNamespaceURI(), qualifiedName);
	    }
	} else if (node instanceof Attr) {
	    Attr attr = (Attr) node;
	    String value = attr.getNodeValue();
	    Element ele = attr.getOwnerElement();
	    ele.removeAttributeNode(attr);
	    ele.setAttribute(qualifiedName, value);
	}

    }

    public void addAttributes(String xPath, String... nameValuePairs) throws XPathExpressionException {
	addAttributes(null, xPath, nameValuePairs);
    }

    /**
     * Adds the attributes specified by the given name value pairs to the nodes resulting from the supplied
     * <code>xPath</code>
     * 
     * @param xPath
     * @param nameValuePairs
     * @throws XPathExpressionException
     */
    public void addAttributes(Node target, String xPath, String... nameValuePairs) throws XPathExpressionException {
	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(target, xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		for (int i = 0; i < nameValuePairs.length; i = i + 2) {
		    element.setAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
		}
	    }
	}
    }

    public void addAttributesNS(String xPath, String... namespaceURINameValueTriples) throws XPathExpressionException {
	addAttributesNS(null, xPath, namespaceURINameValueTriples);
    }

    /**
     * Adds the attributes specified by the attribute namespace plus qualified name plus value triples to the nodes
     * resulting from the supplied <code>xPath</code>
     * 
     * @param xPath
     * @param namespaceURINameValueTriples the namespaceURI + qualified name + value triples
     * @throws XPathExpressionException
     */
    public void addAttributesNS(Node target, String xPath, String... namespaceURINameValueTriples) throws XPathExpressionException {
	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		for (int i = 0; i < namespaceURINameValueTriples.length; i = i + 3) {
		    element.setAttributeNS(namespaceURINameValueTriples[i], namespaceURINameValueTriples[i + 1],
			    namespaceURINameValueTriples[i + 2]);
		}
	    }
	}
    }

    /**
     * Sets the specified text to the elements resulting from the supplied <code>xPath</code>
     * 
     * @param xPath
     * @param text
     * @throws XPathExpressionException
     */
    public void setText(String xPath, String text) throws XPathExpressionException {
	setText(null, xPath, text);
    }

    /**
     * Sets the specified text to the elements resulting from the supplied <code>xPath</code>
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xPath
     * @param text
     * @throws XPathExpressionException
     */
    public void setText(Node target, String xPath, String text) throws XPathExpressionException {
	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(target, xPath);
	for (Node node : list) {
	    if (node instanceof Element) {
		Element element = (Element) node;
		element.setTextContent(text);
	    } else if (node instanceof Attr) {
		Attr attr = (Attr) node;
		attr.setTextContent(text);
	    }
	}
    }

    /**
     * Adds the specified node to the nodes resulting from the supplied
     * <code>xPath</code>
     * 
     * @param xPath
     * @param node
     * @throws XPathExpressionException
     */
    public void addNode(String xPath, Node node) throws XPathExpressionException {
	List<Node> list = this.xmlNodeReader.evaluateOriginalNodesList(xPath);
	for (Node originalNode : list) {
	    Node importedNode = originalNode.getOwnerDocument().importNode(node, true);
	    originalNode.appendChild(importedNode);
	}
    }

    /**
     * Removes prefixes from this node and recursively on children nodes
     */
    public void removePrefixes() {
	Node node = this.xmlNodeReader.getNode();
	removePrefixes(node);
    }

    private void removePrefixes(Node node) {
	rename(node, node.getLocalName());
	NamedNodeMap attributes = node.getAttributes();
	if (attributes != null) {
	    for (int i = 0; i < attributes.getLength(); i++) {
		Node attribute = attributes.item(i);
		if (attribute != null && attribute.getLocalName() != null) {
		    rename(attribute, attribute.getLocalName());
		}
	    }
	}
	NodeList nodes = node.getChildNodes();
	for (int i = 0; i < nodes.getLength(); i++) {
	    Node child = nodes.item(i);
	    removePrefixes(child);
	}
    }

}
