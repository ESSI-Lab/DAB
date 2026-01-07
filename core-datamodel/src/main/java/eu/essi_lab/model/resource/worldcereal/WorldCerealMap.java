/**
 * 
 */
package eu.essi_lab.model.resource.worldcereal;

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
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Roberto
 */
@XmlRootElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class WorldCerealMap extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(WorldCerealMap.class);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(GSResource.class).error("Fatal initialization error!");
	    GSLoggerFactory.getLogger(GSResource.class).error(e.getMessage(), e);
	}
    }


    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String worldCerealQueryables;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<WorldCerealItem> cropTypesElements;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<WorldCerealItem> quantityTypesElements;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<WorldCerealItem> landCoverTypesElements;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<WorldCerealItem> irrigationTypesElements;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Double irrigationTypeConfidence;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Double cropTypeConfidence;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Double lcTypeConfidence;


    @XmlTransient
    public String getWorldCerealQueryables() {
        return worldCerealQueryables;
    }

    public void setWorldCerealQueryables(String worldCerealQueryables) {
        this.worldCerealQueryables = worldCerealQueryables;
    }
    @XmlTransient
    public List<WorldCerealItem> getCropTypes() {
        return cropTypesElements;
    }

    public void setCropTypes(List<WorldCerealItem> cropTypes) {
        this.cropTypesElements = cropTypes;
    }
    
    @XmlTransient
    public List<WorldCerealItem> getQuantityTypes() {
        return quantityTypesElements;
    }

    public void setQuantityTypes(List<WorldCerealItem> quantityTypes) {
        this.quantityTypesElements = quantityTypes;
    }
    @XmlTransient
    public List<WorldCerealItem> getLandCoverTypes() {
        return landCoverTypesElements;
    }

    public void setLandCoverTypes(List<WorldCerealItem> landCoverTypes) {
        this.landCoverTypesElements = landCoverTypes;
    }
    @XmlTransient
    public List<WorldCerealItem> getIrrigationTypes() {
        return irrigationTypesElements;
    }

    public void setIrrigationTypes(List<WorldCerealItem> irrigationTypes) {
        this.irrigationTypesElements = irrigationTypes;
    }
    @XmlTransient
    public Double getIrrigationTypeConfidence() {
        return irrigationTypeConfidence;
    }

    public void setIrrigationTypeConfidence(Double irrigationTypeConfidence) {
        this.irrigationTypeConfidence = irrigationTypeConfidence;
    }
    @XmlTransient
    public Double getCropTypeConfidence() {
        return cropTypeConfidence;
    }

    public void setCropTypeConfidence(Double cropTypeConfidence) {
        this.cropTypeConfidence = cropTypeConfidence;
    }
    @XmlTransient
    public Double getLcTypeConfidence() {
        return lcTypeConfidence;
    }

    public void setLcTypeConfidence(Double lcTypeConfidence) {
        this.lcTypeConfidence = lcTypeConfidence;
    }

   


      
    @Override
    public WorldCerealMap fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (WorldCerealMap) unmarshaller.unmarshal(stream);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static WorldCerealMap create(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (WorldCerealMap) unmarshaller.unmarshal(node);
    }

    @Override
    public WorldCerealMap fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (WorldCerealMap) unmarshaller.unmarshal(node);
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

    @Override
    public String toString() {

	try {
	    return asString(true);
	} catch (Exception e) {
	}

	return "error occurred";
    }


}
