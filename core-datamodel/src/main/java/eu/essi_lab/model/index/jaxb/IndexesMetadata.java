package eu.essi_lab.model.index.jaxb;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "indexesMetadata", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class IndexesMetadata extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(IndexesMetadata.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    @XmlAnyElement
    private List<Object> properties;

    @XmlElement(name = "bbox", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private BoundingBox bbox;

    /**
     * Creates a new <code>IndexesMetadata</code> with the supplied list of indexes
     * 
     * @param indexes
     */
    public IndexesMetadata() {

	properties = new ArrayList<>();
    }

    @Override
    public IndexesMetadata fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (IndexesMetadata) unmarshaller.unmarshal(stream);
    }

    @Override
    public Indexes fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (Indexes) unmarshaller.unmarshal(node);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static IndexesMetadata create(InputStream stream) throws JAXBException {

	return new IndexesMetadata().fromStream(stream);
    }

    /**
     * Writes the supplied indexed <code>element</code>. In case of multiple values, this method
     * writes an indexed element for each value.<br>
     * 
     * @param element
     * @throws IllegalArgumentException if the supplied element has no value/s
     */
    public void write(IndexedElement element) {

	if (element instanceof IndexedMetadataElement) {

	    IndexedMetadataElement el = (IndexedMetadataElement) element;

	    if (el.getBoundingBox() != null) {

		this.bbox = el.getBoundingBox();

		return;
	    }
	}

	List<String> values = element.getValues();

	if (values.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Empty values element found: " + element.getElementName());
	    return;
	}

	for (String value : values) {
	    write(element.getElementName(), value);
	}
    }

    /**
     * Read the values of all the {@link IndexedElement}s with the supplied <code>elementName</code>
     *
     * @param element
     * @return
     */
    public List<String> read(String elementName) {

	List<String> out = Lists.newArrayList();

	for (Object next : properties) {

	    String name = getPropertyName(next);

	    if (name.equals(elementName)) {

		String text = extractTextContent(next).trim().strip();
		out.add(text);
	    }
	}

	return out;
    }

    @Override
    public String toString() {

	StringBuilder builder = new StringBuilder();

	getProperties().forEach(prop -> {

	    builder.append(prop);
	    builder.append(":");
	    builder.append(read(prop));
	    builder.append("\n");
	});

	return builder.toString();
    }

    /**
     * @return
     */
    public List<String> getProperties() {

	return properties.//
		stream().//
		map(prop -> getPropertyName(prop)).//
		sorted().//
		collect(Collectors.toList());
    }

    /**
     * @param property
     * @return
     */
    private String getPropertyName(Object property) {

	String name = null;
	if (property instanceof JAXBElement<?>) {
	    name = ((JAXBElement<?>) property).getName().getLocalPart();
	} else {
	    name = ((Node) property).getLocalName();
	}

	return name;
    }

    /**
     * Read the values of all the {@link IndexedMetadataElement} related to the supplied <code>element</code>
     * 
     * @param element
     * @return
     */
    public List<String> read(MetadataElement element) {

	return read(element.getName());
    }

    /**
     * Read the value of the {@link ResourceProperty} related to the supplied <code>property</code>
     * 
     * @param element
     * @return
     */
    public Optional<String> read(ResourceProperty property) {

	List<String> values = read(property.getName());
	if (values.isEmpty()) {
	    return Optional.empty();
	}
	return Optional.of(values.get(0));
    }

    /**
     * @return
     */
    public Optional<BoundingBox> readBoundingBox() {

	return Optional.ofNullable(bbox);
    }

    /**
     * @return
     */
    public boolean hasBoundingBox() {

	return readBoundingBox().isPresent();
    }

    /**
     * Removes all the {@link IndexedMetadataElement} with the supplied <code>elementName</code>
     * 
     * @param elementName
     */
    public void remove(String elementName) {

	@SuppressWarnings("rawtypes")
	Iterator iterator = properties.iterator();

	while (iterator.hasNext()) {

	    Object next = iterator.next();
	    String name = getPropertyName(next);

	    if (name.equals(elementName)) {
		iterator.remove();
	    }
	}
    }

    /**
     * @param removeBbox
     */
    public void clear(boolean removeBbox) {

	properties.clear();

	if (removeBbox) {

	    this.bbox = null;
	}
    }

    /**
     * @return
     */
    public boolean isEmpty() {

	return properties.isEmpty();
    }

    /**
     * @return
     */
    public int size() {

	int size = properties.size();
	if (bbox != null) {
	    size += 1;
	}

	return size;
    }

    /**
     * Adds the {@link IndexedElement} with the supplied <code>elementName</code> and the supplied <code>value</code>
     * 
     * @param elementName
     * @param value
     */
    private void write(String elementName, String value) {

	// -------------------------------------------------------------------------------------------
	//
	// When this <code>MapWrapper</code>
	// instance is created by yourself (instead of through unmarshalling) the properties are only
	// JAXBElement, but after an unmarshalling the properties are transformed in to W3c nodes.
	// Then calling this method adds a new JAXBElement and the properties are mixed
	//
	// -------------------------------------------------------------------------------------------

	JAXBElement<String> prop = new JAXBElement<String>(
		new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, elementName, NameSpace.GS_DATA_MODEL_SCHEMA_URI), String.class, value);

	properties.add(prop);
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty("jaxb.formatted.output", true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());
	return marshaller;
    }

    @Override
    protected Object getElement() throws JAXBException {

	return this;
    }

    /**
     * <p>
     * Extract local name from <code>obj</code>, whether it's
     * javax.xml.bind.JAXBElement or org.w3c.dom.Element;
     * </p>
     * 
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    private String extractLocalName(Object obj) {
	Map<Class<?>, Function<? super Object, String>> strFuncs = new HashMap<>();
	strFuncs.put(JAXBElement.class, (jaxb) -> ((JAXBElement<String>) jaxb).getName().getLocalPart());
	strFuncs.put(Element.class, ele -> ((Element) ele).getLocalName());
	return extractPart(obj, strFuncs).orElse("");
    }

    /**
     * <p>
     * Extract text content from <code>obj</code>, whether it's
     * javax.xml.bind.JAXBElement or org.w3c.dom.Element;
     * </p>
     * 
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    private String extractTextContent(Object obj) {
	Map<Class<?>, Function<? super Object, String>> strFuncs = new HashMap<>();
	strFuncs.put(JAXBElement.class, (jaxb) -> ((JAXBElement<String>) jaxb).getValue());
	strFuncs.put(Element.class, ele -> ((Element) ele).getTextContent());
	return extractPart(obj, strFuncs).orElse("");
    }

    /**
     * Check class type of <code>obj</code> according to types listed in
     * <code>strFuncs</code> keys, then extract some string part from it
     * according to the extract function specified in <code>strFuncs</code>
     * values.
     * 
     * @param obj
     * @param strFuncs
     * @return
     */
    private <ObjType, T> Optional<T> extractPart(ObjType obj, Map<Class<?>, Function<? super ObjType, T>> strFuncs) {

	for (Class<?> clazz : strFuncs.keySet()) {
	    if (clazz.isInstance(obj)) {
		T apply = strFuncs.get(clazz).apply(obj);
		if (apply != null) {
		    return Optional.of(apply);
		} else {
		    return Optional.empty();
		}
	    }
	}

	return Optional.empty();
    }
}
