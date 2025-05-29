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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A utility class to execute XPaths on a node
 * 
 * @author boldrini
 */
public class XMLNodeReader {

    protected Node targetNode;
    private XPath xpath;

    private static XPathFactory factory;
    private static final TransformerFactory TRANSFORMER_FACTORY = XMLFactories.newTransformerFactory();

    /**
     * The lock object is needed, as the underlying library (Saxon-HE) is not multithread-safe
     */
    final protected static Object LOCK = new Object();

    /**
     * @return
     */
    public Node getNode() {
	return (Node) targetNode;
    }

    static {
	try {
	    synchronized (LOCK) {

		factory = XMLFactories.newXPathFactory();
	    }
	} catch (XPathFactoryConfigurationException e) {
	    // Saxon HE library not found.. this should not happen!
	    e.printStackTrace();
	    System.err.println("Saxon HE libraries not found in the classpath");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	} catch (FactoryConfigurationError e) {
	    // Factory instantiation error.. this should not happen!
	    e.printStackTrace();
	    System.err.println("The default XML document builder factory could not be instantiated");
	    System.err.println("Application will exit");
	    // not possible to continue
	    System.exit(1);
	}

    }

    /*
     * CONSTRUCTOR
     */

    /**
     * Creates a new <code>XMLDocumentReader</code> from the supplied <code>node</code>
     * 
     * @param node a non <code>null</code> W3C node
     * @throws SAXException
     * @throws IOException
     */
    public XMLNodeReader(Node node) {

	this.targetNode = node;
	xpath = factory.newXPath();
    }

    protected XMLNodeReader() {
	synchronized (LOCK) {
	    xpath = factory.newXPath();
	}
    }

    /*
     * NAMESPACE CONTEXT METHODS
     */

    private static class NamespaceContextImpl implements NamespaceContext {

	private Map<String, String> namespaces;

	NamespaceContextImpl(Map<String, String> namespaces) {
	    this.namespaces = namespaces;
	}

	public String getNamespaceURI(String prefix) {
	    return namespaces.get(prefix);
	}

	public String getPrefix(String namespace) {
	    for (Map.Entry<String, String> entry : namespaces.entrySet()) {
		if (entry.getValue().equals(namespace)) {
		    return entry.getKey();
		}
	    }
	    return null;
	}

	public Iterator<String> getPrefixes(String namespace) {
	    String prefix = namespaces.get(namespace);
	    if (prefix == null) {
		return null;
	    }
	    return Collections.singletonList(prefix).iterator();
	}
    }

    /**
     * Gets the namespace context used evaluating XPaths
     */
    public NamespaceContext getNamespaceContext() {
	synchronized (LOCK) {
	    return xpath.getNamespaceContext();
	}
    }

    /**
     * Sets the namespace context to be used evaluating XPaths
     * 
     * @param context
     */
    public void setNamespaceContext(NamespaceContext context) {
	synchronized (LOCK) {
	    xpath.setNamespaceContext(context);
	}
    }

    /**
     * Sets the namespaces context to be used evaluating XPaths
     * 
     * @param namespaces
     */
    public void setNamespaces(Map<String, String> namespaces) {

	setNamespaceContext(new NamespaceContextImpl(namespaces));
    }

    /*
     * EVALUATE METHODS
     */

    /**
     * Evaluates the given XPath expression on the document to get a {@link Number} result.
     * 
     * @param xpathExpression
     * @return a {@link Number} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Number evaluateNumber(String xpathExpression) throws XPathExpressionException {
	return evaluateNumber(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Number} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Number} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Number evaluateNumber(Node target, String xpathExpression) throws XPathExpressionException {
	return (Number) this.evaluate(xpathExpression, target, XPathResultType.NUMBER);
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link Boolean} result.
     * 
     * @param xpathExpression
     * @return a {@link Boolean} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Boolean evaluateBoolean(String xpathExpression) throws XPathExpressionException {
	return evaluateBoolean(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Boolean} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Boolean} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Boolean evaluateBoolean(Node target, String xpathExpression) throws XPathExpressionException {
	return (Boolean) this.evaluate(xpathExpression, target, XPathResultType.BOOLEAN);
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link String} result.
     * 
     * @param xpathExpression
     * @return a String result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public String evaluateString(String xpathExpression) throws XPathExpressionException {
	return evaluateString(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link String} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a String result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public String evaluateString(Node target, String xpathExpression) throws XPathExpressionException {
	return (String) this.evaluate(xpathExpression, target, XPathResultType.STRING);
    }

    /**
     * Evaluates the given XPath expression to get a list of the resulting text content. In order to obtain the text
     * content
     * of the target nodes, the xPath must ends with the "/text()" function. If the target nodes have no text content,
     * the returned list is empty
     * 
     * @param xpathExpression
     * @return a {@link List} of text content
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public List<String> evaluateTextContent(String xpathExpression) throws XPathExpressionException {

	Node[] nodes = evaluateNodes(xpathExpression);
	return Arrays.asList(nodes).//
		stream().//
		map(n -> n.getNodeValue()).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    /**
     * Evaluates the given XPath expression on the document to get a {@link Node} result.
     * 
     * @param xpathExpression
     * @return a {@link XMLResultNode} result or null if the expression evaluated to null
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Node evaluateNode(String xpathExpression) throws XPathExpressionException {
	return evaluateNode(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get a {@link Node} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return a {@link Node} result or null if the expression evaluated to null
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Node evaluateNode(Node target, String xpathExpression) throws XPathExpressionException {
	Object ret = this.evaluate(xpathExpression, target, XPathResultType.NODE);
	if (ret == null) {
	    return null;
	}
	return (Node) ret;
    }

    /**
     * Evaluates the given XPath expression on the document to get an array {@link XMLResultNode} result.
     * 
     * @param xpathExpression
     * @return an array of {@link XMLResultNode} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Node[] evaluateNodes(String xpathExpression) throws XPathExpressionException {
	return evaluateNodes(null, xpathExpression);
    }

    /**
     * Evaluates the given XPath expression on the given target to get an array {@link Node} result.
     * 
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return an array of {@link Node} result
     * @throws XPathExpressionException If expression cannot be evaluated.
     */
    public Node[] evaluateNodes(Node target, String xpathExpression) throws XPathExpressionException {
	Object ret = this.evaluate(xpathExpression, target, XPathResultType.NODESET);
	if (ret instanceof NodeList) {
	    NodeList nodes = (NodeList) ret;
	    Node[] result = new Node[nodes.getLength()];
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		if (item == null) {
		    result[i] = null;
		} else {
		    result[i] = item;
		}
	    }
	    return result;

	}
	// it should never happen!
	return null;
    }

    /**
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    public List<Node> evaluateOriginalNodesList(String xpathExpression) throws XPathExpressionException {

	return evaluateOriginalNodesList(null, xpathExpression);
	
    }

    /**
     * @param target a {@link Node} that is the result of a previous evaluateNode operation. If null, evaluation
     *        will be performed on the entire document.
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    public List<Node> evaluateOriginalNodesList(Node target, String xpathExpression) throws XPathExpressionException {

	ArrayList<Node> out = new ArrayList<>();

	Object ret = this.evaluate(xpathExpression, target, XPathResultType.NODESET);
	if (ret instanceof NodeList) {
	    NodeList nodes = (NodeList) ret;

	    for (int i = 0; i < nodes.getLength(); i++) {
		Node item = nodes.item(i);
		out.add(item);
	    }
	}
	return out;
    }

    protected Object evaluate(String xpathExpression, Node target, XPathResultType resultType) throws XPathExpressionException {
	Object item = targetNode;
	if (target != null) {
	    item = target;
	}
	Object result;
	synchronized (LOCK) {
	    result = xpath.evaluate(xpathExpression, item, resultType.getResultType());
	}
	return result;
    }

    /**
     * @param node
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Node node, boolean omitXMLdeclaration) throws TransformerException, UnsupportedEncodingException {

	return asOutputStream(node, omitXMLdeclaration).toString("UTF-8");
    }

    /**
     * @param node
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String asString(Node node) throws TransformerException, UnsupportedEncodingException {

	return asString(node, false);
    }

    /**
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public String asString(boolean omitXMLdeclaration) throws TransformerException, UnsupportedEncodingException {

	return asString(targetNode, omitXMLdeclaration);
    }

    /**
     * @return
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public String asString() throws TransformerException, UnsupportedEncodingException {

	return asString(targetNode, false);
    }

    /**
     * @return
     * @throws TransformerException
     */
    public ByteArrayInputStream asStream() throws TransformerException {

	return asStream(false);
    }

    /**
     * @return
     * @throws TransformerException
     */
    public ByteArrayInputStream asStream(boolean omitXMLdeclaration) throws TransformerException {

	ByteArrayOutputStream outputStream = asOutputStream(targetNode, omitXMLdeclaration);
	return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * @param node
     * @param omitXMLdeclaration
     * @return
     * @throws TransformerException
     */
    public static ByteArrayOutputStream asOutputStream(Node node, boolean omitXMLdeclaration) throws TransformerException {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	DOMSource xmlSource = new DOMSource(node);
	Result outputTarget = new StreamResult(outputStream);

	Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	String omit = omitXMLdeclaration ? "yes" : "no";
	transformer.setOutputProperty("omit-xml-declaration", omit);

	transformer.transform(xmlSource, outputTarget);
	return outputStream;
    }

    @Override
    public String toString() {
	try {
	    return asString();
	} catch (Exception e) {
	    return "Error converting to string: " + e.getMessage();
	}
    }
}
