package eu.essi_lab.iso.datamodel.classes;

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

import com.google.common.collect.*;
import eu.essi_lab.iso.datamodel.*;
import eu.essi_lab.iso.datamodel.todo.*;
import eu.essi_lab.jaxb.common.*;
import net.opengis.gml.v_3_2_0.*;
import net.opengis.iso19139.gco.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;
import net.opengis.iso19139.srv.v_20060504.*;

import javax.xml.bind.*;
import java.math.*;
import java.util.*;

/**
 * SV_ServiceIdentification
 *
 * @author Fabrizio
 */
public class ServiceIdentification extends Identification {

    public ServiceIdentification(SVServiceIdentificationType type) {

	super(type);
    }

    public ServiceIdentification() {

	this(new SVServiceIdentificationType());
    }

    /**
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent/gmd:description/gco:CharacterString")
     */
    public void addBoundingBox(String description, BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {
	GeographicBoundingBox bbox = new GeographicBoundingBox();
	bbox.setBigDecimalNorth(north);
	bbox.setBigDecimalSouth(south);
	bbox.setBigDecimalEast(east);
	bbox.setBigDecimalWest(west);

	EXGeographicExtentPropertyType exGeographicExtentPropertyType = new EXGeographicExtentPropertyType();
	exGeographicExtentPropertyType.setAbstractEXGeographicExtent(bbox.getElement());

	EXExtentType exExtentType = new EXExtentType();
	if (description != null) {
	    exExtentType.setDescription(createCharacterStringPropertyType(description));
	}
	exExtentType.setGeographicElement(Lists.newArrayList(exGeographicExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    /**
     * @XPathDirective(target = "srv:serviceType/gco:LocalName")
     */
    void setServiceType(String type) {
    }

    /**
     * @return the service type string (e.g. "view") or null
     * @XPathDirective(target = "srv:serviceType/gco:LocalName")
     */
    public String getServiceType() {
	try {
	    SVServiceIdentificationType t = getElementType();
	    if (t == null || !t.isSetServiceType())
		return null;
	    GenericNamePropertyType prop = t.getServiceType();
	    if (prop == null || !prop.isSetAbstractGenericName())
		return null;
	    javax.xml.bind.JAXBElement<?> el = prop.getAbstractGenericName();
	    if (el == null)
		return null;
	    Object val = el.getValue();
	    if (val instanceof CodeType)
		return ((CodeType) val).getValue();
	    return null;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(clear = ".//srv:serviceTypeVersion")
     */
    void clearServiceTypeVersions() {
    }

    /**
     * @XPathDirective(target = ".", create = "srv:serviceTypeVersion/gco:CharacterString", after = "srv:serviceType")
     */
    void addServiceTypeVersion(String version) {
    }

    /**
     * @XPathDirective(target = ".", clear = ".//srv:serviceTypeVersion", create = "srv:serviceTypeVersion/gco:CharacterString", after =
     * "srv:serviceType")
     */
    void setServiceTypeVersion(String version) {
    }

    /**
     * @XPathDirective(target = ".//srv:serviceTypeVersion/gco:CharacterString")
     */
    public Iterator<String> getServiceTypeVersions() {
	try {
	    SVServiceIdentificationType t = getElementType();
	    if (t == null || !t.isSetServiceTypeVersion() || t.getServiceTypeVersion() == null)
		return new ArrayList<String>().iterator();
	    List<String> out = new ArrayList<>();
	    for (CharacterStringPropertyType pt : t.getServiceTypeVersion()) {
		String s = ISOMetadata.getStringFromCharacterString(pt);
		if (s != null)
		    out.add(s);
	    }
	    return out.iterator();
	} catch (Exception e) {
	    return new ArrayList<String>().iterator();
	}
    }

    /**
     * @return the first service type version string (e.g. "1.3.0") or null
     */
    public String getServiceTypeVersion() {
	Iterator<String> it = getServiceTypeVersions();
	return (it != null && it.hasNext()) ? it.next() : null;
    }

    /**
     * @XPathDirective(target = ".", before = "srv:containsOperations", clear = "srv:couplingType")
     */
    public void setCouplingType(String couplingTypeCode) {
	SVCouplingTypePropertyType couplingTypeProperty = new SVCouplingTypePropertyType();
	couplingTypeProperty.setSVCouplingType(
		createCodeListValueType(SV_COUPLING_TYPE_CODELIST, couplingTypeCode, ISO_19119_CODESPACE, couplingTypeCode));
	getElementType().setCouplingType(couplingTypeProperty);
    }

    public String getCouplingType() {
	if (getElementType().isSetCouplingType() && getElementType().getCouplingType().isSetSVCouplingType()) {
	    return getElementType().getCouplingType().getSVCouplingType().getCodeListValue();
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".//srv:containsOperations/srv:SV_OperationMetadata")
     */
    public Iterator<OperationMetadata> getOperationMetadatas() {
	List<SVOperationMetadataPropertyType> containsOperationList = getElementType().getContainsOperations();
	ArrayList<OperationMetadata> svOperationMetadata = new ArrayList<OperationMetadata>();
	for (SVOperationMetadataPropertyType svOp : containsOperationList) {
	    SVOperationMetadataType operationMetadata = svOp.getSVOperationMetadata();
	    OperationMetadata om = new OperationMetadata(operationMetadata);
	    svOperationMetadata.add(om);
	}
	return svOperationMetadata.iterator();
    }

    public OperationMetadata getOperationMetadata() {
	Iterator<OperationMetadata> operationMetadatas = getOperationMetadatas();
	if (operationMetadatas.hasNext()) {
	    return operationMetadatas.next();
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", parent = "srv:containsOperations", position = Position.LAST)
     */
    void addContainsOperation(OperationMetadata operation) {
    }

    /**
     * @XPathDirective(target = ".", parent = "srv:containsOperations", position = Position.LAST, clear = "//srv:containsOperations")
     */
    void setContainsOperation(OperationMetadata operation) {
    }

    /**
     * @XPathDirective(clear = ".//srv:containsOperations")
     */
    void clearContainsOperation() {
    }

    /**
     * @XPathDirective(target =
     * ".//srv:operatesOn/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier//gmd:code/gco:CharacterString")
     */
    Iterator<String> getOperatesOnCodes() {
	return null;
    }

    /**
     * Returns identifiers of resources this service operates on. Reads from srv:operatesOn (href) when present, otherwise from
     * srv:coupledResource/srv:identifier.
     *
     * @XPathDirective(target = ".//srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString")
     */
    public Iterator<String> getOperatesOnIdentifiers() {
	ArrayList<String> identifiers = new ArrayList<>();
	try {
	    SVServiceIdentificationType t = getElementType();
	    if (t != null && t.isSetOperatesOn() && t.getOperatesOn() != null) {
		for (MDDataIdentificationPropertyType prop : t.getOperatesOn()) {
		    if (prop != null && prop.isSetHref()) {
			String href = prop.getHref();
			if (href != null && !href.isEmpty())
			    identifiers.add(href);
		    }
		}
	    }
	    if (identifiers.isEmpty()) {
		List<SVCoupledResourcePropertyType> coupledResources = t != null ? t.getCoupledResource() : null;
		if (coupledResources != null) {
		    for (SVCoupledResourcePropertyType coupledResource : coupledResources) {
			if (coupledResource == null)
			    continue;
			SVCoupledResourceType svCoupled = coupledResource.getSVCoupledResource();
			if (svCoupled == null)
			    continue;
			CharacterStringPropertyType identifier = svCoupled.getIdentifier();
			if (identifier != null) {
			    String s = ISOMetadata.getStringFromCharacterString(identifier);
			    if (s != null)
				identifiers.add(s);
			}
		    }
		}
	    }
	} catch (Exception e) {
	    // ignore
	}
	return identifiers.iterator();
    }

    /**
     * @XPathDirective(target = ".//srv:coupledResource/srv:SV_CoupledResource/srv:operationName/gco:CharacterString")
     */
    public Iterator<String> getOperatesOnNames() {
	List<SVCoupledResourcePropertyType> coupledResources = getElementType().getCoupledResource();
	ArrayList<String> identifiers = new ArrayList<String>();
	for (SVCoupledResourcePropertyType coupledResource : coupledResources) {
	    SVCoupledResourceType svCoupled = coupledResource.getSVCoupledResource();
	    CharacterStringPropertyType operationName = svCoupled.getOperationName();
	    identifiers.add(operationName.toString());
	}
	return identifiers.iterator();
    }

    /**
     * @XPathDirective(target = ".//srv:coupledResource/srv:SV_CoupledResource/gco:ScopedName")
     */
    public Iterator<String> getScopedNames() {
	List<SVCoupledResourcePropertyType> coupledResources = getElementType().getCoupledResource();
	ArrayList<String> identifiers = new ArrayList<String>();
	for (SVCoupledResourcePropertyType coupledResource : coupledResources) {
	    SVCoupledResourceType svCoupled = coupledResource.getSVCoupledResource();
	    CodeType scopedName = svCoupled.getScopedName();
	    identifiers.add(scopedName.getValue());
	}
	return identifiers.iterator();
    }

    @Override
    public SVServiceIdentificationType getElementType() {

	return (SVServiceIdentificationType) type;
    }

    @Override
    public JAXBElement<SVServiceIdentificationType> getElement() {

	JAXBElement<SVServiceIdentificationType> element = ObjectFactories.SRV()
		.createSVServiceIdentification((SVServiceIdentificationType) type);

	return element;
    }

    /**
     * @XPathDirective(create = "gmd:language/gco:CharacterString", target = ".", after = "gmd:spatialResolution", position =
     * Position.FIRST)
     */

    public void addBoundingBox(double north, double west, double south, double east) {
	addBoundingBox(null, new BigDecimal(north), new BigDecimal(west), new BigDecimal(south), new BigDecimal(east));

    }

    public void addBoundingBox(BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {
	addBoundingBox(null, north, west, south, east);

    }

    public void addBoundingBox(GeographicBoundingBox bbox) {
	addBoundingBox(null, new BigDecimal(bbox.getNorth()), new BigDecimal(bbox.getWest()), new BigDecimal(bbox.getSouth()),
		new BigDecimal(bbox.getEast()));

    }

    // ----------------------------
    //
    // Bounding polygon
    //

    /**
     * @param polygon
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.LAST)
     */
    public void addBoundingPolygon(BoundingPolygon polygon) {
	EXExtentPropertyType extentProperty = new EXExtentPropertyType();
	EXExtentType extent = new EXExtentType();
	List<EXGeographicExtentPropertyType> geographicExtents = new ArrayList<>();
	EXGeographicExtentPropertyType geographicExtent = new EXGeographicExtentPropertyType();
	ObjectFactory factory = new ObjectFactory();
	geographicExtent.setAbstractEXGeographicExtent(factory.createEXBoundingPolygon(polygon.getElementType()));
	geographicExtents.add(geographicExtent);
	extent.setGeographicElement(geographicExtents);
	extentProperty.setEXExtent(extent);
	getElementType().getExtent().add(extentProperty);
    }

    public void addTemporalExtent(String periodID, String beginPosition, String endPosition) {

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	temporalExtent.setEndPosition(endPosition);
	temporalExtent.setTimePeriodId(periodID);

	addTemporalExtent(temporalExtent);
    }

    /**
     * @param extent
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent", parent = "gmd:temporalElement", before =
     * "srv:extent/gmd:EX_Extent/gmd:verticalElement", position = Position.LAST)
     */
    public void addTemporalExtent(TemporalExtent extent) {

	EXTemporalExtentPropertyType exTemporalExtentPropertyType = new EXTemporalExtentPropertyType();
	exTemporalExtentPropertyType.setEXTemporalExtent(extent.getElement());

	EXExtentType exExtentType = new EXExtentType();
	exExtentType.setTemporalElement(Lists.newArrayList(exTemporalExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    public void addTemporalExtent(String beginPosition, String endPosition) {

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	temporalExtent.setEndPosition(endPosition);

	addTemporalExtent(temporalExtent);
    }

    /**
     * @param extent
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent", parent = "gmd:verticalElement", position = Position.LAST)
     */
    public void addVerticalExtent(VerticalExtent extent) {
	EXVerticalExtentPropertyType exGeographicExtentPropertyType = new EXVerticalExtentPropertyType();

	exGeographicExtentPropertyType.setEXVerticalExtent(extent.getElement().getValue());

	EXExtentType exExtentType = new EXExtentType();
	exExtentType.setVerticalElement(Lists.newArrayList(exGeographicExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    public void addVerticalExtent(double min, double max) {
	VerticalExtent extent = new VerticalExtent();
	extent.setMinimumValue(min);
	extent.setMaximumValue(max);
	addVerticalExtent(extent);
    }

    public void setSpatialRepresentationType(String string) {
	// TODO Auto-generated method stub

    }

    /**
     * @param browseGraphic
     * @XPathDirective(target = ".", after = "gmd:citation gmd:abstract gmd:purpose gmd:credit gmd_status gmd:pointOfContact
     * gmd:resourceMaintenance", position = Position.FIRST)
     */
    public void addGraphicOverview(BrowseGraphic browseGraphic) {
	MDBrowseGraphicPropertyType browseGraphicProperty = new MDBrowseGraphicPropertyType();
	browseGraphicProperty.setMDBrowseGraphic(browseGraphic.getElementType());
	getElementType().getGraphicOverview().add(browseGraphicProperty);
    }

    public void setSupplementalInformation(String string) {
	// TODO Auto-generated method stub

    }

    public void setSpatialResolution(MDResolution res) {
	// TODO Auto-generated method stub

    }

    public String getCharacterSetCode() {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * @return
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent/gmd:geographicElement//gmd:EX_GeographicBoundingBox")
     */
    @Override
    public Iterator<GeographicBoundingBox> getGeographicBoundingBoxes() {

	return super.getGeographicBoundingBoxes(getElementType().getExtent());
    }

    /**
     * @XPathDirective(target = ".//*:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/
     */
    @Override
    public Iterator<String> getGeographicDescriptionCodes() {

	return super.getGeographicDescriptionCodes(getElementType().getExtent());
    }

    /**
     * @return
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.LAST)
     */
    public Iterator<BoundingPolygon> getBoundingPolygons() {
	List<BoundingPolygon> ret = new ArrayList<>();
	if (getElementType().isSetExtent()) {
	    List<EXExtentPropertyType> extents = getElementType().getExtent();
	    for (EXExtentPropertyType exExtentPropertyType : extents) {
		if (exExtentPropertyType.isSetEXExtent()) {
		    EXExtentType extent = exExtentPropertyType.getEXExtent();
		    if (extent.isSetGeographicElement()) {
			List<EXGeographicExtentPropertyType> geographics = extent.getGeographicElement();
			for (EXGeographicExtentPropertyType geographic : geographics) {
			    if (geographic.isSetAbstractEXGeographicExtent()) {
				JAXBElement<? extends AbstractEXGeographicExtentType> abstractGeo = geographic.getAbstractEXGeographicExtent();
				AbstractEXGeographicExtentType abstractExtent = abstractGeo.getValue();
				if (abstractExtent instanceof EXBoundingPolygonType) {
				    EXBoundingPolygonType polygonType = (EXBoundingPolygonType) abstractExtent;
				    ret.add(new BoundingPolygon(polygonType));
				}
			    }
			}
		    }
		}
	    }
	}
	return ret.iterator();
    }

    /**
     * @return
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent/gmd:temporalElement//gmd:EX_TemporalExtent")
     */
    @Override
    public Iterator<TemporalExtent> getTemporalExtents() {
	ArrayList<TemporalExtent> out = new ArrayList<TemporalExtent>();
	try {
	    List<EXExtentPropertyType> extent = getElementType().getExtent();
	    for (EXExtentPropertyType exExtentPropertyType : extent) {
		List<EXTemporalExtentPropertyType> temporalElement = exExtentPropertyType.getEXExtent().getTemporalElement();
		for (EXTemporalExtentPropertyType exTemporalExtentPropertyType : temporalElement) {
		    EXTemporalExtentType value = exTemporalExtentPropertyType.getEXTemporalExtent().getValue();
		    TemporalExtent temporalExtent = new TemporalExtent(value);
		    out.add(temporalExtent);
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return out.iterator();
    }

    public TemporalExtent getTemporalExtent() {

	Iterator<TemporalExtent> temporalExtents = getTemporalExtents();
	if (temporalExtents.hasNext()) {
	    return temporalExtents.next();
	}

	return null;
    }

    public VerticalExtent getVerticalExtent() {
	Iterator<VerticalExtent> verticalExtents = getVerticalExtents();
	if (verticalExtents.hasNext()) {
	    return verticalExtents.next();
	}

	return null;
    }

    // ----------------------------
    //
    // Vertical extent
    //

    /**
     * @return
     * @XPathDirective(target = "./srv:extent/gmd:EX_Extent/gmd:verticalElement//gmd:EX_VerticalExtent")
     */
    public Iterator<VerticalExtent> getVerticalExtents() {
	ArrayList<VerticalExtent> out = new ArrayList<VerticalExtent>();

	List<EXExtentPropertyType> extent = getElementType().getExtent();
	for (EXExtentPropertyType exExtentPropertyType : extent) {
	    EXExtentType exExtent = exExtentPropertyType.getEXExtent();
	    List<EXVerticalExtentPropertyType> verticalElement = exExtent.getVerticalElement();
	    for (EXVerticalExtentPropertyType exVerticalPropertyType : verticalElement) {

		EXVerticalExtentType value = exVerticalPropertyType.getEXVerticalExtent();
		if (value != null) {
		    VerticalExtent verticalExtent = new VerticalExtent(value);
		    out.add(verticalExtent);
		}
	    }
	}

	return out.iterator();
    }

    /**
     * @return
     */
    public String getTopicCategory() {

	return null;
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
     */
    public BrowseGraphic getGraphicOverview() {
	Iterator<BrowseGraphic> iterator = getGraphicOverviews();
	if (iterator.hasNext()) {
	    return iterator.next();
	}
	return null;
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
     */
    @Override
    public Iterator<BrowseGraphic> getGraphicOverviews() {
	List<MDBrowseGraphicPropertyType> overviews = getElementType().getGraphicOverview();
	ArrayList<BrowseGraphic> ret = new ArrayList<BrowseGraphic>();
	for (MDBrowseGraphicPropertyType mdBrowseGraphicPropertyType : overviews) {
	    if (mdBrowseGraphicPropertyType.isSetMDBrowseGraphic()) {
		ret.add(new BrowseGraphic(mdBrowseGraphicPropertyType.getMDBrowseGraphic()));
	    }
	}
	return ret.iterator();

    }

    public void clearLanguages() {
	// TODO Auto-generated method stub

    }

    public void clearExtents() {
	// TODO Auto-generated method stub

    }

    public void clearTopicCategories() {
	// TODO Auto-generated method stub

    }

    public void clearGraphicOverviews() {
	// TODO Auto-generated method stub

    }

    public void clearSpatialResolution() {
	// TODO Auto-generated method stub

    }
}
