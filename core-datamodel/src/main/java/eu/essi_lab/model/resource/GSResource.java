package eu.essi_lab.model.resource;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.constraints.NotEmpty;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.ObjectFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;

/**
 * Abstract class for generic GI-Suite resources
 *
 * @author Fabrizio
 */
@XmlSeeAlso({ Dataset.class, DatasetCollection.class, DatasetService.class })
@XmlRootElement(name = "GSResource", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public abstract class GSResource extends DOMSerializer {

    private static JAXBContext context;
    @NotNull(message = "source field  of GSResource cannot be null")
    @Valid
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private GSSource source;
    @Valid
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private HarmonizedMetadata harmonizedMetadata;
    @Valid
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private OriginalMetadata originalMetadata;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private IndexesMetadata indexesMetadata;
    @XmlTransient
    private ResourcePropertyHandler propHandler;

    static {
	try {
	    context = JAXBContext.newInstance(GSResource.class, ObjectFactory.class);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSResource.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(GSResource.class).error(e.getMessage(), e);
	}
    }

    public GSResource() {
    }

    /**
     * @param resourceType
     */
    public GSResource(ResourceType resourceType) {

	this.harmonizedMetadata = new HarmonizedMetadata(resourceType);
	this.originalMetadata = new OriginalMetadata();
	this.indexesMetadata = new IndexesMetadata();
	this.propHandler = new ResourcePropertyHandler(this);

	setProperty(ResourceProperty.TYPE, resourceType.getType());
    }

    /**
     * @return
     */
    @XmlTransient
    @NotNull(message = "privateId field of GSResource cannot be null")
    @NotEmpty(message = "privateId field of GSResource cannot be empty")
    public String getPrivateId() {

	return getPropertyValue(ResourceProperty.PRIVATE_ID).orElse(null);
    }

    /**
     * @param privateId
     */
    public void setPrivateId(String privateId) {

	setProperty(ResourceProperty.PRIVATE_ID, privateId);
    }

    @XmlTransient
    public Optional<String> getOriginalId() {

	Optional<String> value = getPropertyValue(ResourceProperty.ORIGINAL_ID);

	if (value.isPresent() && value.get().isEmpty()) {

	    return Optional.empty();
	}

	return value;
    }

    /**
     * @param originalId
     */
    public void setOriginalId(String originalId) {

	setProperty(ResourceProperty.ORIGINAL_ID, originalId);
    }

    /**
     * Shortcut for <code>resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier()</code>
     *
     * @return
     */
    @XmlTransient
    @NotNull(message = "publicId field of GSResource cannot be null")
    @NotEmpty(message = "publicId field of GSResource cannot be empty")
    public String getPublicId() {

	return getHarmonizedMetadata().getCoreMetadata().getIdentifier();
    }

    /**
     * Shortcut for <code>resource.getHarmonizedMetadata().getCoreMetadata().setIdentifier(publicId)</code>
     *
     * @param publicId
     */
    public void setPublicId(String publicId) {

	getHarmonizedMetadata().getCoreMetadata().setIdentifier(publicId);
    }

    @XmlTransient
    @NotNull(message = "source field of GSResource cannot be null")
    public GSSource getSource() {

	return source;
    }

    public void setSource(GSSource source) {

	if (source != null) {
	    this.source = source;
	    setProperty(ResourceProperty.SOURCE_ID, source.getUniqueIdentifier());
	}
    }

    @XmlTransient
    @NotNull(message = "resourceType property  of GSResource cannot be null")
    public ResourceType getResourceType() {

	Optional<String> p = getPropertyValue(ResourceProperty.TYPE);

	if (p.isPresent())

	    return ResourceType.fromType(p.get());

	throw new IllegalArgumentException("resourceType is null");

    }

    @XmlTransient
    public OriginalMetadata getOriginalMetadata() {

	return originalMetadata;
    }

    public void setOriginalMetadata(OriginalMetadata originalMetadata) {

	this.originalMetadata = originalMetadata;
    }

    @XmlTransient
    public HarmonizedMetadata getHarmonizedMetadata() {

	return harmonizedMetadata;
    }

    public IndexesMetadata getIndexesMetadata() {

	return indexesMetadata;
    }

    /**
     * Return an instance of {@link ResourcePropertyHandler} for this resource
     */
    public ResourcePropertyHandler getPropertyHandler() {

	return propHandler;
    }

    /**
     * Return an instance of {@link ExtensionHandler} for this resource
     */
    public ExtensionHandler getExtensionHandler() {

	return new ExtensionHandler(this);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static GSResource create(InputStream stream) throws JAXBException {

	return new GSResource() {
	}.fromStream(stream);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    public static GSResource create(String string) throws JAXBException, UnsupportedEncodingException {

	return new GSResource() {
	}.fromStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static GSResource create(Node node) throws JAXBException {

	return new GSResource() {
	}.fromNode(node);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static GSResource createOrNull(Node node) {

	try {
	    return new GSResource() {
	    }.fromNode(node);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSResource.class).error(e);
	}
	return null;
    }

    @Override
    public GSResource fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = new GSResource() {
	}.createUnmarshaller();
	GSResource ret = (GSResource) unmarshaller.unmarshal(stream);
	try {
	    stream.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return ret;
    }

    @Override
    public GSResource fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = new GSResource() {
	}.createUnmarshaller();
	return (GSResource) unmarshaller.unmarshal(node);
    }

    /**
     * Validates this resource and returns the list, possible empty, of constraint violations
     *
     * @return the list, possible empty, of constraint violations
     */
    public List<ConstraintViolation<GSResource>> validate() {

	ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	Validator validator = factory.getValidator();

	Set<ConstraintViolation<GSResource>> validate = validator.validate(this);

	return Lists.newArrayList(validate.iterator());
    }

    /**
     * Writes an {@link IndexedResourceProperty} related to the supplied <code>property</code> and with the given
     * <code>value</code>. If an
     * {@link IndexedResourceProperty} related to the same {@link ResourceProperty} already exists, it is replaced
     *
     * @param property
     * @param value
     * @see #getPropertyValue(ResourceProperty)
     */
    void setProperty(ResourceProperty property, String value) {

	getIndexesMetadata().remove(property.getName());
	getIndexesMetadata().write(new IndexedResourceProperty(property, value));
    }

    /**
     * Writes an {@link IndexedResourceProperty} related to the supplied <code>property</code> and with the given
     * <code>value</code>
     *
     * @param property
     * @param value
     * @see #getPropertyValues(ResourceProperty)
     */
    void addProperty(ResourceProperty property, String value) {

	getIndexesMetadata().write(new IndexedResourceProperty(property, value));
    }

    /**
     * Get the value of the supplied <code>property</code>
     *
     * @param property
     * @return
     * @see #setProperty(ResourceProperty, String)
     */
    Optional<String> getPropertyValue(ResourceProperty property) {

	return getIndexesMetadata().read(property);
    }

    /**
     * Get the values of the supplied <code>property</code>
     *
     * @param property
     * @return
     * @see #addProperty(ResourceProperty, String)
     */
    List<String> getPropertyValues(ResourceProperty property) {

	return getIndexesMetadata().read(property.getName());
    }

    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Object getElement() throws JAXBException {

	return this;
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	// NioEscapeHandler nioEscapeHandler = new NioEscapeHandler("UTF-8");
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	// marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
	// @Override
	// public void escape(char[] ac, int i, int j, boolean flag, Writer writer) throws IOException {
	//
	// String value = new String(ac);
	// if (value.contains("CDATA")) {
	// writer.write(ac, i, j);
	// } else {
	// nioEscapeHandler.escape(ac, i, j, flag, writer);
	// }
	// }
	// });
	return marshaller;
    }

    @Override
    public String toString() {

	return getResourceType() + //
		"[" + getPrivateId() + "][" + getOriginalId().orElse("none") + "][" + getPublicId() + "]";
    }
}
