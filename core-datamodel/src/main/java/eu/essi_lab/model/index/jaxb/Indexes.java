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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
@XmlType
@XmlRootElement(name = "indexes", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
class Indexes {

    private List<JAXBElement<String>> properties;

    @XmlElement(name = "bbox", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private BoundingBox bbox;

    public Indexes() {

	properties = new ArrayList<>();
    }

    /**
     * @return
     */
    @XmlTransient
    public BoundingBox getBbox() {

	return bbox;
    }

    /**
     * @param bbox
     */
    public void setBbox(BoundingBox bbox) {

	this.bbox = bbox;
    }

    /**
     * <p>
     * When this <code>MapWrapper</code>
     * instance is created by yourself (instead of through unmarshalling) the properties are only
     * JAXBElement, but after an unmarshalling the properties are transformed in to W3c nodes.
     * Then calling this method adds a new JAXBElement and the properties are mixed
     * </p>
     * 
     * @param key map key
     * @param value map value
     */
    public void add(String target, String value) {

	JAXBElement<String> prop = new JAXBElement<String>(
		new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, target, NameSpace.GS_DATA_MODEL_SCHEMA_URI), String.class, value);

	properties.add(prop);

    }

    /**
     * @param element
     * @return
     */
    public List<String> get(String target) {

	ArrayList<String> out = Lists.newArrayList();

	for (Object next : getProperties()) {

	    String name = null;
	    if (next instanceof JAXBElement<?>) {
		name = ((JAXBElement<?>) next).getName().getLocalPart();
	    } else {
		name = ((Node) next).getLocalName();
	    }

	    if (name.equals(target)) {

		String text = extractTextContent(next);
		out.add(text);
	    }
	}

	return out;
    }

    /**
     * @param element
     * @return
     */
    public List<String> get(MetadataElement element) {

	return get(element.getName());
    }

    /**
     * <p>
     * Strange fact: due to type erasure, this method may return a list
     * of W3c nodes instead of a list of JAXBElement<String> in the end;
     * this is the reason why {@link IndexesMetadata#remove(String)} method
     * makes an instance test
     * </p>
     * 
     * @return
     */
    @XmlAnyElement
    public List<JAXBElement<String>> getProperties() {

	return properties;
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
