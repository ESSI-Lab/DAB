package eu.essi_lab.lib.xml;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Used to eradicate from a document a given set of nodes, identified by the the given xPaths
 * 
 * @author Fabrizio
 */
public class XMLPruner {

    private List<String> xpaths;
    private NamespaceContext context;
    private XMLDocumentReader reader;

    /**
     * Creates a pruner with the given set of xPaths and context
     * 
     * @param xpaths
     * @param context
     */
    public XMLPruner(List<String> xpaths, NamespaceContext context) {
	this.xpaths = xpaths;
	this.context = context;

    }

    public void setXpaths(List<String> xpaths) {
	this.xpaths = xpaths;
    }

    public void setContext(NamespaceContext context) {
	this.context = context;
    }

    public List<String> getXpaths() {
	return xpaths;
    }

    public NamespaceContext getContext() {
	return context;
    }

    /**
     * Prunes a clone of the given document applying the current set of xPaths and returns it
     * 
     * @param document
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public Document prune(Document document) throws XPathExpressionException, SAXException, IOException, TransformerException {

	document = (Document) document.cloneNode(true);

	reader = new XMLDocumentReader(document);
	reader.setNamespaceContext(context);

	HashSet<Node> nodesToSave = new HashSet<Node>();

	for (String xPath : xpaths) {

	    xPath = "//" + xPath + "/ancestor-or-self::*";
	    List<Node> nodes = reader.evaluateOriginalNodesList(xPath);
	    // when the xPath involves gmd:dateStamp, the reader returns
	    // a gco:Date also if it not exists but with the gco:nil='true' attribute
	    // so in that case, we remove it
	    Iterator<Node> iterator = nodes.iterator();
	    while (iterator.hasNext()) {
		Node next = iterator.next();
		NamedNodeMap attributes = next.getAttributes();
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
		    Attr item = (Attr) attributes.item(i);
		    String value = item.getValue();
		    String name = item.getName();
		    if (name.equals("xsi:nil") && value.equals("true")) {
			iterator.remove();
		    }
		}
	    }
	    nodesToSave.addAll(nodes);
	}

	prune(document, nodesToSave);

	String docString = reader.asString();

	// replaces all the sequences of 2 or more white spaces with a single one
	docString = docString.trim().replaceAll(" {2,}", "");
	// replaces all the sequences of 2 or more \n\r spaces with a single one
	docString = docString.replaceAll("[\r\n]+", "\n");

	return XMLDocumentReader.builder.parse(new ByteArrayInputStream(docString.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Prunes a clone of the given document applying the supplied set of xPaths with the supplied context and returns it
     * 
     * @param doc
     * @param xpaths
     * @param context
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public Document prune(Document doc, List<String> xpaths, NamespaceContext context)
	    throws XPathExpressionException, SAXException, IOException, TransformerException {
	setContext(context);
	setXpaths(xpaths);
	return prune(doc);
    }

    private Node prune(Node node, HashSet<Node> nodesToSave) {
	List<Node> toRemove = new ArrayList<Node>();
	// Determine action based on node type
	switch (node.getNodeType()) {
	case Node.DOCUMENT_NODE:
	    // recurse on each top-level node
	    NodeList nodes = node.getChildNodes();
	    if (nodes != null) {
		for (int i = 0; i < nodes.getLength(); i++) {
		    Node prune = prune(nodes.item(i), nodesToSave);
		    if (prune != null) {
			toRemove.add(prune);
		    }
		}
	    }
	    break;
	case Node.ELEMENT_NODE:
	    if (nodesToSave.contains(node)) {
		// System.out.println("OK " + name);
		NodeList children = node.getChildNodes();
		if (children != null) {
		    for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			Node prune = prune(child, nodesToSave);
			if (prune != null) {
			    toRemove.add(prune);
			}
		    }
		}
	    } else {
		return node;
	    }

	    break;
	case Node.TEXT_NODE:
	    break;
	case Node.CDATA_SECTION_NODE:
	    break;
	case Node.COMMENT_NODE:
	    break;
	case Node.PROCESSING_INSTRUCTION_NODE:
	    break;
	case Node.ENTITY_REFERENCE_NODE:
	    break;
	case Node.DOCUMENT_TYPE_NODE:
	    break;

	}
	for (Node badNode : toRemove) {
	    Node parent = badNode.getParentNode();
	    parent.removeChild(badNode);
	}

	return null;
    }
}
