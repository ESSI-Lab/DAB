package eu.essi_lab.model.index.jaxb;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.model.Queryable;

/**
 * @author Fabrizio
 */
@XmlRootElement(name = "bbox", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class BoundingBox extends DOMSerializer {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(BoundingBox.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public static BoundingBox create(InputStream stream) throws JAXBException {

	return new BoundingBox().fromStream(stream);
    }

    public static final String AREA_ELEMENT_NAME = "area";
    public static final String IS_CROSSED_ELEMENT_NAME = "isCrossed";
    public static final String SOUTH_ELEMENT_NAME = "south";
    public static final String WEST_ELEMENT_NAME = "west";
    public static final String NORTH_ELEMENT_NAME = "north";
    public static final String EAST_ELEMENT_NAME = "east";
    public static final String SW_ELEMENT_NAME = "sw";
    public static final String SE_ELEMENT_NAME = "se";
    public static final String NW_ELEMENT_NAME = "nw";
    public static final String NE_ELEMENT_NAME = "ne";
    public static final String DISJ_SOUTH_ELEMENT_NAME = "disjSouth";
    public static final String DISJ_NORTH_ELEMENT_NAME = "disjNorth";
    public static final String DISJ_WEST_ELEMENT_NAME = "disjWest";
    public static final String DISJ_EAST_ELEMENT_NAME = "disjEast";

    public static final QualifiedName AREA_QUALIFIED_NAME = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL.getURI(), AREA_ELEMENT_NAME,
	    NameSpace.GI_SUITE_DATA_MODEL.getPrefix());

    public static final QualifiedName IS_CROSSED_QUALIFIED_NAME = new QualifiedName(NameSpace.GI_SUITE_DATA_MODEL.getURI(),
	    IS_CROSSED_ELEMENT_NAME, NameSpace.GI_SUITE_DATA_MODEL.getPrefix());

    /**
     * 
     */
    public static final Queryable AREA_QUERYABLE = new Queryable() {

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public boolean isVolatile() {

	    return false;
	}

	@Override
	public boolean isEnabled() {

	    return false;
	}

	@Override
	public String getName() {

	    return AREA_QUALIFIED_NAME.getLocalPart();
	}

	@Override
	public ContentType getContentType() {

	    return ContentType.DOUBLE;
	}
    };

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String sw;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String se;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String nw;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String ne;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String isCrossed;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String area;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String center;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private DisjointValues disjValues;

    @XmlElementWrapper(name = "cardinalValues", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<CardinalValues> values;

    public BoundingBox() {

	values = new ArrayList<CardinalValues>();
    }

    @XmlTransient
    public String getSw() {
	return sw;
    }

    public void setSw(String sw) {
	this.sw = sw;
    }

    @XmlTransient
    public String getSe() {
	return se;
    }

    public void setSe(String se) {
	this.se = se;
    }

    @XmlTransient
    public String getNw() {
	return nw;
    }

    public void setNw(String nw) {
	this.nw = nw;
    }

    @XmlTransient
    public String getNe() {
	return ne;
    }

    public void setNe(String ne) {
	this.ne = ne;
    }

    @XmlTransient
    public String getIsCrossed() {
	return isCrossed;
    }

    public void setIsCrossed(String isCrossed) {
	this.isCrossed = isCrossed;
    }

    @XmlTransient
    public String getArea() {
	return area;
    }

    public void setArea(String area) {
	this.area = area;
    }

    @XmlTransient
    public String getCenter() {
	return center;
    }

    public void setCenter(String center) {
	this.center = center;
    }

    @XmlTransient
    public DisjointValues getDisjointValues() {
	return disjValues;
    }

    public void setDisjointValues(DisjointValues disjoint) {
	this.disjValues = disjoint;
    }

    @XmlTransient
    public List<CardinalValues> getCardinalValues() {
	return values;
    }

    public void addCardinalValues(CardinalValues values) {
	getCardinalValues().add(values);
    }

    @Override
    public BoundingBox fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (BoundingBox) unmarshaller.unmarshal(stream);
    }

    @Override
    public BoundingBox fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (BoundingBox) unmarshaller.unmarshal(node);
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

}
