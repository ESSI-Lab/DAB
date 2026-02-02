/**
 * 
 */
package eu.essi_lab.model.resource;

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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;

/**
 * Model-specific fields extension for DataHub models
 * 
 * @author Generated
 */
@XmlRootElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class ModelSpecificFields extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(ModelSpecificFields.class);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSResource.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(GSResource.class).error(e.getMessage(), e);
	}
    }

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String modelMaturityLevel;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String modelCategory;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String modelMethodologyDescription;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String modelTypes; // Comma-separated list
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String supportedPlatforms; // Comma-separated list
    
    // Computational requirements
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String cpu;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String gpu;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String ram;
    
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String storage;

    /**
     * 
     */
    public ModelSpecificFields() {
    }

    /**
     * @return
     */
    @XmlTransient
    public String getModelMaturityLevel() {
	return modelMaturityLevel;
    }

    /**
     * @param modelMaturityLevel
     */
    public void setModelMaturityLevel(String modelMaturityLevel) {
	this.modelMaturityLevel = modelMaturityLevel;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getModelCategory() {
	return modelCategory;
    }

    /**
     * @param modelCategory
     */
    public void setModelCategory(String modelCategory) {
	this.modelCategory = modelCategory;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getModelMethodologyDescription() {
	return modelMethodologyDescription;
    }

    /**
     * @param modelMethodologyDescription
     */
    public void setModelMethodologyDescription(String modelMethodologyDescription) {
	this.modelMethodologyDescription = modelMethodologyDescription;
    }

    /**
     * @return
     */
    @XmlTransient
    public List<String> getModelTypes() {
	if (this.modelTypes == null) {
	    return Lists.newArrayList();
	}
	return Arrays.asList(modelTypes.split(","));
    }

    /**
     * @param modelType
     */
    public void addModelType(String modelType) {
	if (this.modelTypes == null) {
	    this.modelTypes = modelType;
	} else {
	    this.modelTypes = this.modelTypes + "," + modelType;
	}
    }

    /**
     * @param modelTypes
     */
    public void setModelTypes(String modelTypes) {
	this.modelTypes = modelTypes;
    }

    /**
     * @return
     */
    @XmlTransient
    public List<String> getSupportedPlatforms() {
	if (this.supportedPlatforms == null) {
	    return Lists.newArrayList();
	}
	return Arrays.asList(supportedPlatforms.split(","));
    }

    /**
     * @param platform
     */
    public void addSupportedPlatform(String platform) {
	if (this.supportedPlatforms == null) {
	    this.supportedPlatforms = platform;
	} else {
	    this.supportedPlatforms = this.supportedPlatforms + "," + platform;
	}
    }

    /**
     * @param supportedPlatforms
     */
    public void setSupportedPlatforms(String supportedPlatforms) {
	this.supportedPlatforms = supportedPlatforms;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getCpu() {
	return cpu;
    }

    /**
     * @param cpu
     */
    public void setCpu(String cpu) {
	this.cpu = cpu;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getGpu() {
	return gpu;
    }

    /**
     * @param gpu
     */
    public void setGpu(String gpu) {
	this.gpu = gpu;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getRam() {
	return ram;
    }

    /**
     * @param ram
     */
    public void setRam(String ram) {
	this.ram = ram;
    }

    /**
     * @return
     */
    @XmlTransient
    public String getStorage() {
	return storage;
    }

    /**
     * @param storage
     */
    public void setStorage(String storage) {
	this.storage = storage;
    }

    @Override
    public ModelSpecificFields fromStream(InputStream stream) throws JAXBException {
	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (ModelSpecificFields) unmarshaller.unmarshal(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static ModelSpecificFields create(Node node) throws JAXBException {
	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (ModelSpecificFields) unmarshaller.unmarshal(node);
    }

    @Override
    public ModelSpecificFields fromNode(Node node) throws JAXBException {
	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (ModelSpecificFields) unmarshaller.unmarshal(node);
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

    @Override
    protected Object getElement() throws JAXBException {
	return this;
    }

    @Override
    public String toString() {
	try {
	    return asString(true);
	} catch (Exception e) {
	}
	return "error occurred";
    }
}

