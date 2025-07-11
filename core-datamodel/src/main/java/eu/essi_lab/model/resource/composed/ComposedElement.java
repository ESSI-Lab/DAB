/**
 * 
 */
package eu.essi_lab.model.resource.composed;

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

import java.io.InputStream;
import java.util.ArrayList;
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
import eu.essi_lab.lib.xml.NameSpace;

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
     * @param model
     * @return
     * @throws Exception
     */
    public static ComposedElement of(JSONObject object, ComposedElement model) throws Exception {

	ComposedElement out = ComposedElement.of(model.asStream());

	JSONObject properties = object.getJSONObject(object.keys().next());

	properties.keySet().forEach(key -> {

	    ComposedElementItem item = out.//
		    getProperties().//
		    stream().//
		    filter(i -> i.getName().equals(key)).//
		    findFirst().//
		    get();

	    Object obj = properties.get(key);

	    item.setValue(obj);
	});

	return out;
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static ComposedElement of(InputStream stream) throws JAXBException {

	return new ComposedElement() {
	}.fromStream(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static ComposedElement of(Node node) throws JAXBException {

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

    @Override
    public boolean equals(Object object) {

	return object instanceof ComposedElement &&

		((ComposedElement) object).asJSON().similar(this.asJSON());
    }

    /**
     * @return
     */
    public JSONObject asJSON() {

	JSONObject out = new JSONObject();

	JSONObject inner = new JSONObject();
	out.put(name, inner);

	properties.forEach(item -> inner.put(item.getName(), item.getValue()));

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
