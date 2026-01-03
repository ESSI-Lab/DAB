package eu.essi_lab.lib.xml.stax;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import eu.essi_lab.lib.xml.*;
import org.apache.commons.io.IOUtils;

import com.google.common.xml.XmlEscapers;

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class StAXDocumentParser {

    public static boolean debug;

    /**
     *
     */
    private static final XMLInputFactory FACTORY = XMLFactories.newXMLInputFactory();

    private HashMap<List<QName>, Consumer<String>> pathsMap;
    private HashMap<List<QName>, Consumer<String>> parentsMap;
    private List<HashMap<QName, Consumer<String>>> targetsList;
    private List<HashMap<Predicate<Stack<XMLEvent>>, Consumer<Stack<XMLEvent>>>> conditionsList;

    private ClonableInputStream stream;

    private boolean addNewLine;

    /**
     * @param document
     * @throws XMLStreamException
     * @throws IOException
     */
    public StAXDocumentParser(String document) throws XMLStreamException, IOException {

	this(IOUtils.toInputStream(document, StandardCharsets.UTF_8));
    }

    /**
     * @param document
     * @throws XMLStreamException
     * @throws IOException
     */
    public StAXDocumentParser(InputStream document) throws XMLStreamException, IOException {

	stream = new ClonableInputStream(document);

	pathsMap = new HashMap<>();
	parentsMap = new HashMap<>();
	targetsList = new ArrayList<>();
	conditionsList = new ArrayList<>();
    }

    /**
     * The elements returned from the <code>find</code> methods are not formatted. If set to <code>true</code>, adds the new line character
     * on the closing tags of the elements found with the
     * <code>find</code> methods thus allowing further parsing by reading them line by line (for example by means
     * of the {@link BufferedReader#lines()} method).<br>
     *
     * @param addNewLine
     */
    public void addNewLineOnCloseTags(boolean addNewLine) {

	this.addNewLine = addNewLine;
    }

    /**
     * Defines an XML element as <i>parsing target</i> according to <code>targetPath</code> and performs <code>action</code> with its text
     * content.<br> The given <code>targetPath</code> represents a <i>precise</i> path of XML elements in the document, from the
     * <i>root</i> element to the <i>parsing target</i>.<br>
     * So, the first {@link QName} in the list <i>must</i> be related to the <i>root</i> document, and the last is related to the <i>parsing
     * target</i>.<br> The symbol "*" can be used as local part of the {@link QName} taking care not to define an ambiguous path<br>
     * <br>
     * E.g.:<br>
     * <br>
     * Defines the XML element 'a' as a parsing target and when found, adds its text content to the given list.<br> The element 'c' is the
     * root of the document, so this path corresponds to the xPath: <code>c/b/a/text()</code><br>
     * <br>
     * <code>parser.add(Arrays.asList(new QName("c"), new QName("b"), new QName("a")), v -> list.add(v));</code>
     *
     * @param targetPath the path to the <i>parsing target</i>
     * @param action the action to perform with the text content of the XML element of the <i>parsing target</i>
     */
    public void add(List<QName> targetPath, Consumer<String> action) {

	pathsMap.put(targetPath, action);
    }

    /**
     * Defines an XML element as <i>parsing target</i> according to <code>parent</code> and <code>target</code> and performs
     * <code>action</code> with its text content.<br> The <i>parsing target</i> can be located in any position of the document.<br> The
     * xPath corresponding to the <i>parsing target</i> is:
     * <code>&#47;&#47;*&#47;parent&#47;target/text()</code><br>
     * <br>
     * E.g.:<br>
     * <br>
     * Defines the XML element 'b' as a <i>parsing target</i> with the element 'a' as parent and when found, adds its text content to the
     * given list.<br> The xPath corresponding to the <i>parsing target</i> is: <code>&#47;&#47;*&#47;a&#47;b/text()</code>
     * <br>
     * <br>
     * <code>parser.add(new QName("a"), new QName("b"), v -> list.add(v));</code>
     *
     * @param parent the parent element of the <i>parsing target</i>
     * @param target the <i>parsing target</i>
     * @param action the action to perform with the text content of the XML element of the <i>parsing target</i>
     */
    public void add(QName parent, QName target, Consumer<String> action) {

	List<QName> list = Arrays.asList(parent, target);

	parentsMap.put(list, action);
    }

    /**
     * Defines an XML element as <i>parsing target</i> according to <code>target</code> and performs <code>action</code> with its text
     * content.<br> The <i>parsing target</i> can be located in any position of the document<br> The xPath corresponding to the <i>parsing
     * target</i> is: <code>&#47;&#47;*&#47;target/text()</code><br>
     * <br>
     * E.g.:<br>
     * <br>
     * Defines the XML element 'a' as a <i>parsing target</i> and when found, adds its text content to the given list.<br> The xPath
     * corresponding to the <i>parsing target</i> is: <code>&#47;&#47;*&#47;a/text()</code>
     * <br>
     * <br>
     * <code>parser.add(new QName("a"), v -> list.add(v));</code>
     *
     * @param target the <i>parsing target</i>
     * @param action the action to perform with the text content of the XML element of the <i>parsing target</i>
     */
    public void add(QName target, Consumer<String> action) {

	HashMap<QName, Consumer<String>> map = new HashMap<QName, Consumer<String>>();
	map.put(target, action);

	targetsList.add(map);
    }

    /**
     * Defines one or more XML element as <i>parsing targets</i> according to the given <code>condition</code> and performs
     * <code>action</code> on the current {@link XMLEvent} stack.<br> The {@link XMLEvent} at the top of the stack is the last parsed
     * {@link XMLEvent} while the first is the 'start document' event<br> E.g::<br>
     * <br>
     * Defines as <i>parsing targets</i> all the XML elements having non empty text content, and prints the text content:<br>
     * <br>
     * <code>parser.add( <br>
     * stack -> stack.peek().isCharacters() && !stack.peek().asCharacters().getData().trim().isEmpty(),  <br> stack ->
     * System.out.println("[" + stack.peek().asCharacters().getData().trim() + "]"));</code><br>
     * <br>
     * Defines as <i>parsing targets</i> all the XML elements having at least one attribute, and prints the first attribute:<br>
     * <br>
     * <code>parser.add( <br>
     * stack -> stack.peek().isStartElement() && stack.peek().asStartElement().getAttributes().hasNext(), <br> stack ->
     * System.out.println("[" + stack.peek().asStartElement().getAttributes().next() + "]"));
     * </code>
     *
     * @param condition the condition which defines the <i>parsing target</i>
     * @param action the action to perform on the current {@link XMLEvent} stack
     */
    public void add(Predicate<Stack<XMLEvent>> condition, Consumer<Stack<XMLEvent>> action) {

	HashMap<Predicate<Stack<XMLEvent>>, Consumer<Stack<XMLEvent>>> map = new HashMap<>();
	map.put(condition, action);

	conditionsList.add(map);
    }

    /**
     * Defines an attribute as <i>parsing target</i> according to the given <code>attributeName</code> and
     * <code>target</code> and performs <code>action</code> with the attribute value.<br>
     * The <i>parsing target</i> can be located in any position of the document.<br> The xPath corresponding to the <i>parsing target</i>
     * is: <code>&#47;&#47;*&#47;target/@attributeName</code><br>
     * <br>
     * E.g.:<br>
     * <br>
     * Defines the attribute 'id' owned by the XML element 'a' as a <i>parsing target</i> and when found, adds the attribute value to the
     * given list.<br> The xPath corresponding to the <i>parsing target</i> is: <code>&#47;&#47;*&#47;a/@id</code>
     * <br>
     * <br>
     * <code>parser.add(new QName("a"), "id", v -> list.add(v));</code>
     *
     * @param target the <i>parsing target</i>
     * @param attributeName the name of the attribute owned by the <code>target</code> XML element
     * @param action the action to perform with the value of the <i>parsing target</i> attribute
     */
    public void add(QName target, String attributeName, Consumer<String> action) {

	add(//
		stack -> {

		    boolean isStart = stack.peek().isStartElement();
		    boolean nameMatch = false;
		    boolean attrMatch = false;

		    if (isStart) {

			nameMatch = compare(target, stack.peek().asStartElement().getName());

			List<Attribute> attributes = getAttributes(stack.peek());

			attrMatch = attributes.stream().anyMatch(a -> a.getName().getLocalPart().equals(attributeName));
		    }

		    return isStart && nameMatch && attrMatch;
		},

		stack -> {

		    List<Attribute> attributes = getAttributes(stack.peek());

		    Attribute attribute = attributes.//
			    stream().//
			    filter(a -> a.getName().getLocalPart().equals(attributeName)).//
			    findFirst().//
			    get();

		    action.accept(attribute.getValue());
		});
    }

    /**
     * Finds all the XML elements according to the given <code>condition</code> and returns such elements represented as a string.<br> If
     * the XML elements defined by the given <code>condition</code> or one or more of the children (element or attribute) have a namespace
     * prefix, the declaration of all the found namespaces are provided in the returned elements
     *
     * @param condition the condition which defines the XML elements to find
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public List<String> find(Predicate<List<StartElement>> condition) throws IOException, XMLStreamException {

	List<String> out = new ArrayList<>();

	List<QName> namespaces = new ArrayList<>();

	Stack<XMLEvent> eventsStack = new Stack<>();

	StringBuilder builder = new StringBuilder();

	StreamSource source = new StreamSource(stream.clone());

	XMLEventReader reader = createReader(source);

	boolean found = false;

	boolean root = true;

	while (reader.hasNext()) {

	    XMLEvent event = reader.nextEvent();

	    if (event.isStartElement()) {

		eventsStack.push(event);

		StartElement startElement = event.asStartElement();

		if (root) {

		    Iterator<Namespace> attributes = startElement.getNamespaces();

		    while (attributes.hasNext()) {

			Namespace ns = attributes.next();

			QName qName = new QName(ns.getValue(), "*", ns.getPrefix());

			namespaces.add(qName);
		    }

		    root = false;
		}

		if ((!found && test(condition, eventsStack)) || found) {

		    found = true;

		    insertOpenElement(startElement, builder, namespaces);
		}

	    } else if (event.isCharacters()) {

		Characters characters = event.asCharacters();

		String data = characters.getData();

		if (!data.trim().isEmpty()) {

		    if (found) {

			builder.append(data);
		    }
		}

	    } else if (event.isEndElement()) {

		EndElement endElement = event.asEndElement();

		QName name = endElement.getName();

		boolean check = test(condition, eventsStack);

		if (found && !check) {

		    insertCloseElement(name, builder);

		} else if (check) {

		    found = false;

		    insertCloseElement(name, builder);

		    if (!namespaces.isEmpty()) {

			int index = builder.indexOf(">");

			for (QName qName : namespaces) {

			    insertNameSpace(index, builder, qName);
			}
		    }

		    out.add(builder.toString());

		    builder = new StringBuilder();

		    namespaces = new ArrayList<>();
		}

		eventsStack.pop();
	    }
	}

	source.getInputStream().close();

	return out;
    }

    /**
     * Finds all the XML elements according to the given <code>target</code> and returns such elements represented as a string.<br> If
     * <code>target</code> or one or more of its children (element or attribute) have a namespace prefix, the declaration of all the found
     * namespaces are provided in the <code>target</code> element
     *
     * @param target the XML element to find
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public List<String> find(QName target) throws IOException, XMLStreamException {

	return find(stack -> compare(target, stack.get(stack.size() - 1).getName()));
    }

    /**
     * Finds all the XML elements according to the given <code>parent</code> and <code>target</code> and returns such elements represented
     * as a string.<br> If <code>target</code> or one or more of its children (element or attribute) have a namespace prefix, the
     * declaration of all the found namespaces are provided in the <code>target</code> element
     *
     * @param parent the parent of the XML element to find
     * @param target the XML element to find which must be child of <code>parent</code>
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public List<String> find(QName parent, QName target) throws IOException, XMLStreamException {

	return find(stack -> {

	    int size = stack.size();

	    if (size > 2) {

		StartElement element = stack.get(size - 1);
		StartElement parentElement = stack.get(size - 2);

		return compare(target, element.getName()) && compare(parent, parentElement.getName());
	    }

	    return false;
	});
    }

    /**
     * Parses the document and executes the actions related to the defined <i>parsing targets</i> by means of the
     * <code>add</code> methods
     *
     * @throws XMLStreamException
     * @throws IOException
     */
    public void parse() throws XMLStreamException, IOException {

	Stack<QName> qnamesStack = new Stack<>();
	Stack<XMLEvent> eventsStack = new Stack<>();
	List<String> evaluatedStacks = new ArrayList<>();

	StreamSource source = new StreamSource(stream.clone());

	XMLEventReader reader = createReader(source);

	while (reader.hasNext()) {

	    XMLEvent event = reader.nextEvent();

	    if (event.isStartDocument()) {

		eventsStack.push(event);

	    } else if (event.isStartElement()) {

		StartElement startElement = event.asStartElement();

		qnamesStack.push(startElement.getName());
		eventsStack.push(startElement);

		debugStart(qnamesStack, event);

	    } else if (event.isCharacters()) {

		String value = ((Characters) event).getData();

		eventsStack.push(event);

		//
		// targets check
		//
		for (HashMap<QName, Consumer<String>> map : targetsList) {

		    QName qName = map.keySet().iterator().next();

		    if (compare(qName, qnamesStack.peek())) {

			Consumer<String> consumer = map.get(qName);
			consumer.accept(value);
		    }
		}

		List<QName> qnamesStackToList = qnamesStack.stream().collect(Collectors.toList());

		//
		// parents check
		//
		if (!parentsMap.isEmpty()) {

		    if (qnamesStackToList.size() >= 2) {

			for (List<QName> names : parentsMap.keySet()) {

			    QName parent = names.get(0);
			    QName target = names.get(1);

			    boolean parentMatch = compare(parent, qnamesStackToList.get(qnamesStackToList.size() - 2));
			    boolean targetMatch = compare(target, qnamesStackToList.get(qnamesStackToList.size() - 1));

			    if (parentMatch && targetMatch) {

				Consumer<String> consumer = parentsMap.get(names);
				consumer.accept(value);
			    }
			}
		    }
		}

		//
		// paths check
		//
		if (!pathsMap.isEmpty()) {
		    for (List<QName> path : pathsMap.keySet()) {

			if (compare(path, qnamesStackToList)) {

			    Consumer<String> consumer = pathsMap.get(path);
			    consumer.accept(value);
			}
		    }
		}

	    } else if (event.isEndElement()) {

		qnamesStack.pop();
		eventsStack.pop();

		debugEnd(qnamesStack, event);

	    } else if (event.isEndDocument()) {

		eventsStack.pop();
	    }

	    //
	    // conditions
	    //
	    if (!conditionsList.isEmpty()) {
		if (!eventsStack.isEmpty() && !evaluatedStacks.contains(eventsStack.toString())) {

		    evaluatedStacks.add(eventsStack.toString());

		    for (HashMap<Predicate<Stack<XMLEvent>>, Consumer<Stack<XMLEvent>>> map : conditionsList) {

			Predicate<Stack<XMLEvent>> condition = map.keySet().iterator().next();

			if (condition.test(eventsStack)) {

			    map.get(condition).accept(eventsStack);
			}
		    }
		}
	    }
	}

	source.getInputStream().close();
    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     */
    public boolean checkPaths() throws XMLStreamException, IOException {

	Set<List<QName>> userPaths = pathsMap.keySet();

	Stack<QName> stack = new Stack<>();

	List<List<QName>> checkedPaths = new ArrayList<>();

	StreamSource source = new StreamSource(stream.clone());

	XMLEventReader reader = createReader(source);

	while (reader.hasNext()) {

	    XMLEvent event = reader.nextEvent();

	    if (event.isStartElement()) {

		StartElement startElement = event.asStartElement();

		stack.push(startElement.getName());

	    } else if (event.isEndElement()) {

		stack.pop();
	    }

	    for (List<QName> path : userPaths) {

		List<QName> stackToList = stack.stream().collect(Collectors.toList());

		if (!checkedPaths.contains(path) && compare(path, stackToList)) {

		    checkedPaths.add(path);
		}
	    }
	}

	source.getInputStream().close();

	return checkedPaths.size() == userPaths.size();
    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     */
    public void checkPathsAndParse() throws RuntimeException, XMLStreamException, IOException {

	if (!checkPaths()) {

	    throw new RuntimeException("Check failed");
	}

	parse();
    }

    /**
     * @param condition
     * @param eventsStack
     * @return
     */
    private boolean test(Predicate<List<StartElement>> condition, Stack<XMLEvent> eventsStack) {

	return condition.test(eventsStack.//
		stream().//
		filter(e -> e.isStartElement()).//
		map(e -> e.asStartElement()).//
		collect(Collectors.toList()));

    }

    /**
     * @param stack
     * @return
     */
    private List<Attribute> getAttributes(XMLEvent event) {

	Stream<?> stream = StreamUtils.iteratorToStream(event.asStartElement().getAttributes());

	List<Attribute> list = stream.map(e -> (Attribute) e).collect(Collectors.toList());

	return list;
    }

    /**
     * @param index
     * @param builder
     * @param qName
     */
    private void insertNameSpace(int index, StringBuilder builder, QName qName) {

	builder.insert(index, "\"");

	builder.insert(index, qName.getNamespaceURI());

	builder.insert(index, "\"");

	builder.insert(index, "=");

	builder.insert(index, qName.getPrefix());

	builder.insert(index, qName.getPrefix().isBlank() ? " xmlns" : " xmlns:");
    }

    /**
     * @param startElement
     * @param builder
     * @param namespaces
     */
    private void insertOpenElement(StartElement startElement, StringBuilder builder, List<QName> namespaces) {

	QName name = startElement.getName();

	builder.append("<");

	String prefix = name.getPrefix();

	if (prefix != null && !prefix.isEmpty()) {

	    builder.append(prefix);
	    builder.append(":");

	    addNameSpace(name, namespaces);
	}

	builder.append(name.getLocalPart());

	Iterator<?> attributes = startElement.getAttributes();

	while (attributes.hasNext()) {

	    Attribute attr = (Attribute) attributes.next();

	    insertAttribute(name, attr, builder, namespaces);
	}

	builder.append(">");
    }

    /**
     * @param attr
     * @param builder
     * @param namespaces
     */
    private void insertAttribute(QName name, Attribute attr, StringBuilder builder, List<QName> namespaces) {

	QName attrName = attr.getName();

	builder.append(" ");

	String prefix = attrName.getPrefix();

	if (prefix != null && !prefix.isEmpty()) {

	    builder.append(prefix);
	    builder.append(":");

	    addNameSpace(name, namespaces);
	}

	String escapedValue = XmlEscapers.xmlAttributeEscaper().escape(attr.getValue());

	builder.append(attrName.getLocalPart());
	builder.append("=");
	builder.append("\"");
	builder.append(escapedValue);
	builder.append("\"");
    }

    /**
     * @param name
     * @param builder
     */
    private void insertCloseElement(QName name, StringBuilder builder) {

	builder.append("</");

	String prefix = name.getPrefix();

	if (prefix != null && !prefix.isEmpty()) {

	    builder.append(prefix);
	    builder.append(":");
	}

	builder.append(name.getLocalPart());

	builder.append(">");
	if (addNewLine) {
	    builder.append("\n");
	}
    }

    /**
     * @param name
     * @param namespaces
     */
    private void addNameSpace(QName name, List<QName> namespaces) {

	QName qName = new QName(name.getNamespaceURI(), "*", name.getPrefix());

	boolean contains = namespaces.//
		stream().//
		filter(qn -> compareAsNameSpace(qn, qName)).//
		findFirst().//
		isPresent();

	if (!contains) {

	    namespaces.add(qName);
	}
    }

    /**
     * @param qName1
     * @param qName2
     * @return
     */
    private boolean compareAsNameSpace(QName qName1, QName qName2) {

	return qName1.getNamespaceURI().equals(qName2.getNamespaceURI()) && qName1.getPrefix().equals(qName2.getPrefix());
    }

    /**
     * @param userStack
     * @param docStack
     * @return
     */
    private boolean compare(List<QName> userStack, List<QName> docStack) {

	if (userStack.equals(docStack)) {

	    return true;
	}

	if (userStack.size() == docStack.size()) {

	    boolean equals = true;

	    for (int i = 0; i < userStack.size(); i++) {

		QName qName1 = userStack.get(i);
		QName qName2 = docStack.get(i);

		equals &= compare(qName1, qName2);
	    }

	    return equals;
	}

	return false;
    }

    /**
     * @param qName1
     * @param qName2
     * @return
     */
    private boolean compare(QName qName1, QName qName2) {

	if (qName1.getLocalPart().equals("*")) {

	    qName2 = new QName(qName2.getNamespaceURI(), "*", qName2.getPrefix());
	}

	if (qName1.getNamespaceURI().equals("")) {

	    qName2 = new QName("", qName2.getLocalPart(), qName2.getPrefix());
	}

	return qName1.equals(qName2);
    }

    /**
     * @param source
     * @return
     * @throws XMLStreamException
     */
    private XMLEventReader createReader(StreamSource source) throws XMLStreamException {

	return FACTORY.createXMLEventReader(source);
    }

    /**
     * @param stack
     * @param event
     */
    private void debugStart(Stack<QName> stack, XMLEvent event) {

	if (debug) {

	    GSLoggerFactory.getLogger(getClass()).trace("START: " + event.asStartElement().getName().getLocalPart());
	    GSLoggerFactory.getLogger(getClass()).trace(stack.stream().map(q -> q.getLocalPart()).collect(Collectors.toList()).toString());
	}
    }

    /**
     * @param stack
     * @param event
     */
    private void debugEnd(Stack<QName> stack, XMLEvent event) {

	if (debug) {

	    GSLoggerFactory.getLogger(getClass()).trace("END: " + event.asEndElement().getName().getLocalPart());
	    GSLoggerFactory.getLogger(getClass()).trace(stack.stream().map(q -> q.getLocalPart()).collect(Collectors.toList()).toString());

	}
    }
}
