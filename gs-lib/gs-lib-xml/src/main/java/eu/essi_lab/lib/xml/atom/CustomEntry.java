/**
 * 
 */
package eu.essi_lab.lib.xml.atom;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

/**
 * Extends {@link Entry} in order to allow customization by adding new complex and elements
 * 
 * @author Fabrizio
 */
public class CustomEntry extends Entry {

    /**
     * 
     */
    private static final long serialVersionUID = 3227633256449466525L;
    private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
    private static final Namespace ATOM_NS = Namespace.getNamespace(ATOM_10_URI);

    private List<Element> elements;

    public CustomEntry() {

	elements = new ArrayList<Element>();
    }

    /**
     * @param name
     * @param value
     */
    public void addElement(Element element) {

	elements.add(element);
    }

    /**
     * @param name
     * @param value
     */
    public void addSimpleElement(String name, String value) {

	elements.add(createSimpleElement(name, value));
    }

    /**
     * @param name
     * @param value
     * @param attributes
     */
    public void addSimpleElement(String name, String value, Attribute... attributes) {

	elements.add(createSimpleElement(name, value, attributes));
    }

    /**
     * @param name
     * @param value
     * @param namespace
     */
    public void addSimpleElement(String name, String value, String namespace) {

	elements.add(createSimpleElement(name, value, namespace));
    }

    /**
     * @param name
     * @return
     */
    public Optional<String> getElementValue(String name) {

	Optional<Element> opt = elements.stream().filter(e -> e.getName().equals(name)).findFirst();

	return opt.isPresent() ? Optional.of(opt.get().getValue()) : Optional.empty();
    }

    /**
     * @param name
     * @return
     */
    public Optional<Element> getElement(String name) {

	return elements.stream().filter(e -> e.getName().equals(name)).findFirst();
    }

    public List<Element> getForeignMarkup() {

	return elements;
    }

    /**
     * @param name
     * @param value
     * @return
     */
    public static Element createSimpleElement(String name, String value) {

	return createSimpleElement(name, value, null, new Attribute[] {});
    }

    /**
     * @param name
     * @param value
     * @param attributes
     * @return
     */
    public static Element createSimpleElement(String name, String value, Attribute... attributes) {

	return createSimpleElement(name, value, null, attributes);
    }

    /**
     * @param name
     * @param value
     * @param attributes
     * @return
     */
    public static Element createSimpleElement(String name, String value, String namespace) {

	return createSimpleElement(name, value, namespace, new Attribute[] {});
    }

    /**
     * @param name
     * @param value
     * @param namespaces
     * @param attributes
     * @return
     */
    public static Element createSimpleElement(String name, String value, String namespace, Attribute... attributes) {

	Element element = createElement(name, namespace);
	element.addContent(value);

	if (Objects.nonNull(attributes) && attributes.length > 0) {
	    element.setAttributes(Arrays.asList(attributes));
	}

	return element;
    }

    /**
     * @param element
     * @param name
     * @param value
     */
    public static Element addContentTo(Element element, String name, String value) {

	element.addContent(createSimpleElement(name, value));

	return element;
    }
    
    /**
     * @param element
     * @param name
     * @param value
     */
    public static Element addContentTo(Element element,Element content) {

	element.addContent(content);

	return element;
    }

    /**
     * @param element
     * @param attrName
     * @param attrValue
     * @return
     */
    public static Element addAttributeTo(Element element, String attrName, String attrValue) {

	Attribute attribute = new Attribute(attrName, attrValue);
	element.setAttribute(attribute);

	return element;
    }

    /**
     * @param name
     * @return
     */
    public static Element createElement(String name) {

	return new Element(name, ATOM_NS);
    }

    /**
     * @param name
     * @return
     */
    public static Element createElement(String name, String namespace) {

	return new Element(name, Objects.nonNull(namespace) ? namespace : ATOM_NS.getURI());
    }

    /**
     * @return
     * @throws FeedException
     */
    public Element asElement() throws FeedException {

	ElementGenerator generator = new ElementGenerator(this);

	return generator.generateElement();
    }

    /**
     * @return
     * @throws FeedException
     */
    public String asString() throws FeedException {

	Element doc = asElement();

	Format format = Format.getPrettyFormat();

	format.setEncoding("UTF-8");

	final XMLOutputter outputter = new XMLOutputter(format);
	return outputter.outputString(doc);
    }

    /**
     * @param element
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public static Element createElementFromString(String element) throws JDOMException, IOException {

	Element e = new Element("parentnode");
	StringReader stringReader = new StringReader(element);

	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(stringReader);

	Element rootE = doc.getRootElement();
	e.addContent(rootE.detach());

	return rootE;
    }

    /**
     * @author Fabrizio
     */
    private class ElementGenerator extends Atom10Generator {

	private Entry entry;

	/**
	 * @param entry
	 */
	public ElementGenerator(Entry entry) {

	    this.entry = entry;
	}

	/**
	 * @return
	 * @throws FeedException
	 */
	public Element generateElement() throws FeedException {

	    Element eEntry = new Element("entry", getFeedNamespace());

	    final String xmlBase = entry.getXmlBase();
	    if (xmlBase != null) {
		eEntry.setAttribute("base", xmlBase, Namespace.XML_NAMESPACE);
	    }

	    populateEntry(entry, eEntry);
	    generateForeignMarkup(eEntry, entry.getForeignMarkup());

	    return eEntry;
	}
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, FeedException {

	Feed feed = new Feed();
	feed.setFeedType("atom_1.0");

	feed.setTitle("Sample Feed (created with ROME)");
	feed.setUpdated(new Date());

	List<Entry> entries = new ArrayList<>();

	CustomEntry entry = new CustomEntry();

	entry.setId(UUID.randomUUID().toString());

	entry.setTitle("title");

	entry.addSimpleElement("simple1", "simple1");
	entry.addSimpleElement("simple2", "simple2");

	Element complex = CustomEntry.createElement("complex");

	complex = CustomEntry.addContentTo(complex, "content1", "someValue1");
	complex = CustomEntry.addContentTo(complex, "content2", "someValue2");

	entry.addElement(complex);

	entry.addElement(CustomEntry.createElement("emptyComplex"));

	Element complex2 = CustomEntry.createElement("complex2");

	complex2 = CustomEntry.addAttributeTo(complex2, "attr1", "attrValue1");
	complex2 = CustomEntry.addAttributeTo(complex2, "attr2", "attrValue2");

	entry.addElement(complex2);

	entries.add(entry);

	feed.setEntries(entries);

	WireFeedOutput output = new WireFeedOutput();
	System.out.println(output.outputString(feed));
    }

}
