/**
 * 
 */
package eu.essi_lab.model.resource.composed;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "composedElement", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
@XmlAccessorType(XmlAccessType.FIELD)
public class ComposedElement extends DOMSerializer {

    private static JAXBContext context;
    static {

	try {
	    context = JAXBContext.newInstance(ComposedElement.class);

	} catch (JAXBException ex) {

	    GSLoggerFactory.getLogger(ComposedElement.class).error(ex);
	}
    }

    @XmlElement(name = "name", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String name;

    @XmlElementWrapper(name = "properties", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "property", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<ComposedElementItem> properties;

    /**
     * @param object
     * @return
     * @throws Exception
     */
    public static ComposedElement create(JSONObject object) throws Exception {

	ComposedElement out = new ComposedElement(object.keys().next());

	JSONObject properties = object.getJSONObject(object.keys().next());

	properties.keySet().forEach(key -> {

	    ComposedElementItem item = new ComposedElementItem(key);

	    out.addItem(item);

	    Object obj = properties.get(key);

	    switch (obj) {
	    case String s -> {

		if (ISO8601DateTimeUtils.parseISO8601ToDate(s).isPresent()) {

		    if (s.contains("T")) {

			item.setType(ContentType.ISO8601_DATE_TIME);

		    } else {

			item.setType(ContentType.ISO8601_DATE);
		    }
		} else {

		    item.setType(ContentType.TEXTUAL);
		}

		item.setValue(s);
	    }
	    case Double d -> {
		item.setType(ContentType.DOUBLE);
		item.setValue(d);
	    }
	    case BigDecimal d -> {
		item.setType(ContentType.DOUBLE);
		item.setValue(d.doubleValue());
	    }
	    case Long l -> {
		item.setType(ContentType.LONG);
		item.setValue(l);
	    }
	    case Integer i -> {
		item.setType(ContentType.INTEGER);
		item.setValue(i);
	    }

	    default -> throw new IllegalArgumentException("Unexpected value: " + obj);

	    }
	});

	return out;
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static ComposedElement create(InputStream stream) throws JAXBException {

	return new ComposedElement() {
	}.fromStream(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static ComposedElement create(Node node) throws JAXBException {

	return new ComposedElement() {
	}.fromNode(node);
    }

    /**
     * 
     */
    public ComposedElement() {

    }

    /**
     * 
     */
    public ComposedElement(String name) {

	this.name = name;
	this.properties = new ArrayList<>();
    }

    /**
     * @param item
     */
    public void addItem(ComposedElementItem item) {

	properties.add(item);
    }

    /**
     * @return
     */
    @XmlTransient
    public List<ComposedElementItem> getProperties() {

	return properties;
    }

    /**
     * @param name
     * @return
     */
    public Optional<ComposedElementItem> getProperty(String name) {

	return getProperties().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    /**
     * @return the name
     */
    @XmlTransient
    public String getName() {

	return name;
    }

    /**
     * @return
     */
    public JSONObject asJSON() {

	JSONObject out = new JSONObject();

	JSONObject inner = new JSONObject();
	out.put(name, inner);

	properties.forEach(item -> inner.put(item.getName(), item.getObjectValue()));

	return out;
    }

    @Override
    public String toString() {

	return asJSON().toString(3);
    }

    @Override
    public ComposedElement fromStream(InputStream stream) throws JAXBException {
	Unmarshaller unmarshaller = createUnmarshaller();
	Object object = unmarshaller.unmarshal(stream);
	return ((ComposedElement) object);
    }

    @Override
    public ComposedElement fromNode(Node node) throws JAXBException {
	Unmarshaller unmarshaller = createUnmarshaller();
	Object object = unmarshaller.unmarshal(node);
	return ((ComposedElement) object);
    }

    @Override
    public ComposedElement getElement() throws JAXBException {

	return this;
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	return marshaller;
    }
}
