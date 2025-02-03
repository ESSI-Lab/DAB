package eu.essi_lab.iso.datamodel.classes;

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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import net.opengis.gml.v_3_2_0.AbstractRingPropertyType;
import net.opengis.gml.v_3_2_0.AbstractRingType;
import net.opengis.gml.v_3_2_0.DirectPositionListType;
import net.opengis.gml.v_3_2_0.DirectPositionType;
import net.opengis.gml.v_3_2_0.LinearRingType;
import net.opengis.gml.v_3_2_0.MultiPointType;
import net.opengis.gml.v_3_2_0.ObjectFactory;
import net.opengis.gml.v_3_2_0.PointPropertyType;
import net.opengis.gml.v_3_2_0.PointType;
import net.opengis.gml.v_3_2_0.PolygonType;
import net.opengis.iso19139.gmd.v_20060504.EXBoundingPolygonType;
import net.opengis.iso19139.gss.v_20060504.GMObjectPropertyType;

public class BoundingPolygon extends ISOMetadata<EXBoundingPolygonType> {

    public BoundingPolygon(InputStream stream) throws JAXBException {

	super(stream);
    }

    public BoundingPolygon(EXBoundingPolygonType type) {

	super(type);
    }

    public BoundingPolygon() {

	super(new EXBoundingPolygonType());
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static BoundingPolygon create(Node node) throws JAXBException {

	EXBoundingPolygonType type = new BoundingPolygon().fromNode(node);

	return new BoundingPolygon(type);
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public static BoundingPolygon createOrNull(Node node) {

	try {
	    return create(node);
	} catch (JAXBException ex) {

	    GSLoggerFactory.getLogger(BoundingPolygon.class).error(ex);
	}

	return null;
    }

    @Override
    public JAXBElement<EXBoundingPolygonType> getElement() {

	JAXBElement<EXBoundingPolygonType> element = ObjectFactories.GMD().createEXBoundingPolygon(type);
	return element;
    }

    /**
     * @XPathDirective(target = "@id")
     * @param id
     */
    public void setId(String id) {
	type.setId(id);
    }

    /**
     * @XPathDirective(target = "@id")
     * @return
     */
    public String getId() {
	return type.getId();
    }

    /**
     * @param coordinates a list of (lat lon alt?) coordinates related to the multiple points of the dataset
     * @param coordinates
     */
    public void setMultiPoints(List<List<Double>> points) {
	List<GMObjectPropertyType> gmObjects = new ArrayList<>();
	GMObjectPropertyType objectProperty = new GMObjectPropertyType();
	ObjectFactory factory = new ObjectFactory();
	MultiPointType multiPoint = new MultiPointType();
	multiPoint.setSrsName("EPSG:4326");
	List<PointPropertyType> pointMembers = new ArrayList<>();
	for (List<Double> point : points) {
	    PointPropertyType pointType = new PointPropertyType();
	    PointType pt = new PointType();
	    DirectPositionType dpt = new DirectPositionType();
	    dpt.setValue(point);
	    pt.setPos(dpt);
	    pointType.setPoint(pt);
	    pointMembers.add(pointType);
	}
	multiPoint.setPointMember(pointMembers);
	// if lat lon alt ---> EPSG:4979
	if (multiPoint.getPointMember().get(0).getPoint().getPos().getValue().size() == 3) {
	    multiPoint.setSrsName("EPSG:4979");
	}
	objectProperty.setAbstractGeometry(factory.createMultiPoint(multiPoint));
	gmObjects.add(objectProperty);
	type.setPolygon(gmObjects);
    }

    /**
     * @param returns a list of (lat lon alt?) coordinates related to the multiple points of the dataset
     * @param coordinates
     */
    public List<List<Double>> getMultiPoints() {
	List<List<Double>> ret = new ArrayList<List<Double>>();
	if (type.isSetPolygon() && //
		!type.getPolygon().isEmpty() && //
		type.getPolygon().get(0).isSetAbstractGeometry()) {
	    Object abstractGeometry = type.getPolygon().get(0).getAbstractGeometry().getValue();
	    if (abstractGeometry instanceof MultiPointType) {
		MultiPointType multiPointType = (MultiPointType) abstractGeometry;
		List<PointPropertyType> points = multiPointType.getPointMember();
		for (PointPropertyType point : points) {
		    List<Double> values = point.getPoint().getPos().getValue();
		    ret.add(values);
		}
	    }
	}
	return ret;
    }

    /**
     * * @XPathDirective(target = "gmd:polygon/gml:Polygon/gml:LinearRing/gml:coordinates")
     * 
     * @param coordinates a list of (lat lon) coordinates forming the exterior ring of the polygon
     * @param coordinates
     */
    public void setCoordinates(List<Double> coordinates) {
	List<GMObjectPropertyType> gmObjects = new ArrayList<>();
	GMObjectPropertyType objectProperty = new GMObjectPropertyType();
	ObjectFactory factory = new ObjectFactory();
	PolygonType polygon = new PolygonType();
	polygon.setSrsName("EPSG:4326");
	AbstractRingPropertyType ringProperty = new AbstractRingPropertyType();
	LinearRingType linearRing = new LinearRingType();
	DirectPositionListType directPosition = new DirectPositionListType();
	directPosition.setValue(coordinates);
	linearRing.setPosList(directPosition);
	ringProperty.setAbstractRing(factory.createLinearRing(linearRing));
	polygon.setExterior(ringProperty);
	objectProperty.setAbstractGeometry(factory.createPolygon(polygon));
	gmObjects.add(objectProperty);
	type.setPolygon(gmObjects);
    }

    public Iterator<Double> getCoordinates() {
	List<Double> ret = new ArrayList<>();
	if (type.isSetPolygon() && //
		!type.getPolygon().isEmpty() && //
		type.getPolygon().get(0).isSetAbstractGeometry()) {
	    Object abstractGeometry = type.getPolygon().get(0).getAbstractGeometry().getValue();
	    if (abstractGeometry instanceof PolygonType) {
		PolygonType polygonType = (PolygonType) abstractGeometry;
		if (polygonType.isSetExterior() && //
			polygonType.getExterior().isSetAbstractRing()) {
		    AbstractRingType abstractRing = polygonType.getExterior().getAbstractRing().getValue();
		    if (abstractRing instanceof LinearRingType) {
			LinearRingType linearRing = (LinearRingType) abstractRing;
			if (linearRing.isSetPosList()) {
			    ret.addAll(linearRing.getPosList().getValue());
			}
		    }
		}
	    }
	}
	return ret.iterator();
    }

    public void clearCoordinates() {
	type.unsetPolygon();
    }

}
