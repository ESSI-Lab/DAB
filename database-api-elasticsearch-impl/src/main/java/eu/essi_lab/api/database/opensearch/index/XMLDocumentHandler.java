/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.lib.xml.XPathResultType;

/**
 * @author Fabrizio
 */
public class XMLDocumentHandler {

    private Document targetDoc;
    private XPath xpath;

    /**
     * @param targetDoc
     * @throws XPathFactoryConfigurationException
     */
    public XMLDocumentHandler(Document targetDoc) throws XPathFactoryConfigurationException {

	this.targetDoc = targetDoc;
	this.xpath = XMLFactories.newXPathFactory().newXPath();
    }

    /**
     * @param xPath
     * @return
     * @throws XPathExpressionException
     */
    public Node evaluateNode(String xPath) throws XPathExpressionException {

	Object ret = this.evaluate(xPath, XPathResultType.NODE);

	if (ret == null) {
	    return null;
	}

	return (Node) ret;
    }

    /**
     * @param xPath
     * @return
     * @throws XPathExpressionException
     */
    public List<Node> evaluateNodes(String xPath) throws XPathExpressionException {

	ArrayList<Node> result = new ArrayList<Node>();

	Object ret = this.evaluate(xPath, XPathResultType.NODESET);

	if (ret instanceof NodeList) {

	    NodeList nodes = (NodeList) ret;

	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		if (item != null) {
		    result.add(item);
		}
	    }

	    return result;
	}

	return result;
    }

    /**
     * @param xPath
     * @return
     * @throws XPathExpressionException
     */
    public boolean evaluateBoolean(String xPath) throws XPathExpressionException {

	return (Boolean) this.evaluate(xPath, XPathResultType.BOOLEAN);
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public List<String> evaluateTextContent(String xPath) throws XPathExpressionException {

	return evaluateNodes(xPath).//
		stream().//
		map(n -> n.getNodeValue()).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    /**
     * @param xpath
     * @throws XPathExpressionException
     */
    public void remove(String xPath) throws XPathExpressionException {

	List<Node> list = evaluateOriginalNodesList(xPath);
	if (!list.isEmpty()) {

	    for (Node node : list) {
		remove(node);
	    }
	}
    }

    /**
     * @return
     */
    public Document getDocument() {
    
        return targetDoc;
    }

    /**
     * @param target
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    private List<Node> evaluateOriginalNodesList(String xpathExpression) throws XPathExpressionException {

	ArrayList<Node> out = new ArrayList<>();

	Object ret = this.evaluate(xpathExpression, XPathResultType.NODESET);

	if (ret instanceof NodeList) {

	    NodeList nodes = (NodeList) ret;

	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		out.add(item);
	    }
	}

	return out;
    }

    /**
     * @param xpathExpression
     * @param target
     * @param resultType
     * @return
     * @throws XPathExpressionException
     */
    private Object evaluate(String xpathExpression, XPathResultType resultType) throws XPathExpressionException {

	return xpath.evaluate(xpathExpression, targetDoc, resultType.getResultType());
    }

    /**
     * @param node
     */
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
}
