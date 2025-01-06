package eu.essi_lab.access.wml;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.cuahsi.waterml._1.essi.JAXBWML.WML_SiteProperty;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.MonitoringPointType;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.CodeType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.CodeWithAuthorityType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.iso2005.gco.CharacterStringPropertyType;
import eu.essi_lab.jaxb.wml._2_0.iso2005.gco.ObjectFactory;
import eu.essi_lab.jaxb.wml._2_0.iso2005.gmd.CIResponsiblePartyPropertyType;
import eu.essi_lab.jaxb.wml._2_0.iso2005.gmd.CIResponsiblePartyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.NamedValuePropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.NamedValueType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.ShapeType;
import eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

/**
 * Helper for downloaders in the hydrology domain. A subclass can useful call getTimeSeriesTemplate method to have a WML
 * 2.0 template prefilled with metadata from the {@link GSResource}.
 * Then it should only call addValue method to add the user requested data values.
 * 
 * @author Boldrini
 */
public abstract class WML2DataDownloader extends DataDownloader {

    public CollectionType getCollectionTemplate() {
	CollectionType collection = new CollectionType();
	augmentCollection(collection, resource);
	return collection;
    }

    public static void augmentCollection(CollectionType collection, GSResource resource) {
	if (resource == null) {
	    return;
	}

	ExtensionHandler extensions = resource.getExtensionHandler();

	if (collection.getId() == null) {
	    collection.setId(UUID.randomUUID().toString());
	}

	OMObservationPropertyType member;
	if (collection.getObservationMember().isEmpty()) {
	    member = new OMObservationPropertyType();
	    collection.getObservationMember().add(member);
	} else {
	    member = collection.getObservationMember().get(0);
	}

	OMObservationType observation = member.getOMObservation();
	if (observation == null) {
	    observation = new OMObservationType();
	    member.setOMObservation(observation);
	}

	if (observation.getId() == null) {
	    observation.setId(UUID.randomUUID().toString());
	}
	try {
	    ReferenceType property = observation.getObservedProperty();
	    if (property == null) {
		property = new ReferenceType();
		observation.setObservedProperty(property);
	    }
	    Optional<String> attributeURI = extensions.getObservedPropertyURI();
	    String label = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeTitle();
	    if (attributeURI.isPresent()) {
		property.setHref(attributeURI.get());
		String uri = attributeURI.get();
		if (uri != null) {
		    HydroOntology ontology = new WHOSOntology();
		    SKOSConcept concept = ontology.getConcept(uri);
		    if (concept != null) {
			label = concept.getPreferredLabel().getKey();
			HashSet<String> closeMatches = concept.getCloseMatches();
			if (closeMatches != null && !closeMatches.isEmpty()) {
			    try {
				WMOOntology wmoOntology = new WMOOntology();
				for (String closeMatch : closeMatches) {
				    SKOSConcept variable = wmoOntology.getVariable(closeMatch);
				    if (variable != null) {
					SimpleEntry<String, String> preferredLabel = variable.getPreferredLabel();
					if (preferredLabel != null) {
					    label = preferredLabel.getKey();
					}
				    }
				}
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}
		    }
		}
		property.setTitle(label);
	    } else {
		property.setHref(extensions.getUniqueAttributeIdentifier().get());
		property.setTitle(label);
	    }

	} catch (Exception e) {
	}

	FeaturePropertyType foi = observation.getFeatureOfInterest();
	if (foi == null) {
	    foi = new FeaturePropertyType();
	    observation.setFeatureOfInterest(foi);
	}

	JAXBElement<? extends AbstractFeatureType> aFoi = foi.getAbstractFeature();
	if (aFoi == null) {
	    MonitoringPointType monitoringPoint = new MonitoringPointType();
	    monitoringPoint.setId(UUID.randomUUID().toString());
	    aFoi = JAXBWML2.getInstance().getFactory().createMonitoringPoint(monitoringPoint);
	    foi.setAbstractFeature(aFoi);
	}

	AbstractFeatureType feature = aFoi.getValue();
	if (feature instanceof MonitoringPointType) {
	    MonitoringPointType monitoringPoint = (MonitoringPointType) feature;

	    List<JAXBElement<?>> rest = monitoringPoint.getRest();
	    CIResponsiblePartyPropertyType relatedParty = null;
	    for (JAXBElement<?> r : rest) {
		Object innerValue = r.getValue();
		if (innerValue instanceof CIResponsiblePartyPropertyType) {
		    relatedParty = (CIResponsiblePartyPropertyType) innerValue;
		}
	    }
	    if (relatedParty == null) {
		relatedParty = new CIResponsiblePartyPropertyType();
		rest.add(JAXBWML2.getInstance().getFactory().createMonitoringPointTypeRelatedParty(relatedParty));
	    }
	    ResponsibleParty poc = null;
	    try {
		poc = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact();
	    } catch (Exception e) {
	    }
	    try {
		if (poc == null) {
		    poc = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			    .getCitationResponsibleParties().get(0);
		}
	    } catch (Exception e) {
	    }
	    if (poc != null) {

		try {
		    Object obj = JAXBWML2.getInstance().getUnmarshaller().unmarshal(poc.asStream());
		    if (obj instanceof JAXBElement) {
			JAXBElement<?> jaxb = (JAXBElement<?>) obj;
			obj = jaxb.getValue();
		    }
		    if (obj instanceof CIResponsiblePartyType) {
			CIResponsiblePartyType cir = (CIResponsiblePartyType) obj;
			relatedParty.setCIResponsibleParty(cir);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    CodeWithAuthorityType code = new CodeWithAuthorityType();
	    monitoringPoint.setIdentifier(code);
	    try {
		if (resource.getExtensionHandler().getUniquePlatformIdentifier().isPresent()) {
		    code.setValue(resource.getExtensionHandler().getUniquePlatformIdentifier().get());
		} else {
		    code.setValue(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getMDIdentifierCode());
		}
	    } catch (Exception e) {
	    }
	    try {
		code.setCodeSpace(resource.getSource().getUniqueIdentifier());
	    } catch (Exception e) {
	    }

	    try {
		CodeType codeType = new CodeType();
		codeType.setValue(
			resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle());
		codeType.setCodeSpace(resource.getSource().getUniqueIdentifier());
		if (monitoringPoint.getName().isEmpty()) {
		    monitoringPoint.getName().add(codeType);
		}
	    } catch (Exception e) {
	    }
	    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ObjectFactory ofg = new eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ObjectFactory();
	    List<FeaturePropertyType> sf = monitoringPoint.getSampledFeature();
	    if (sf.isEmpty()) {
		FeaturePropertyType sfp = new FeaturePropertyType();
		sfp.setTitle(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle());
		sf.add(sfp);
	    }

	    ShapeType shape = monitoringPoint.getShape();
	    if (shape == null) {
		shape = new ShapeType();
		monitoringPoint.setShape(shape);
	    }
	    JAXBElement<? extends AbstractGeometryType> geometry = shape.getAbstractGeometry();
	    if (geometry == null) {
		PointType point = new PointType();
		geometry = ofg.createPoint(point);
		shape.setAbstractGeometry(geometry);
	    }
	    AbstractGeometryType gv = geometry.getValue();
	    if (gv instanceof PointType) {
		PointType point = (PointType) gv;
		DirectPositionType dpt = new DirectPositionType();
		dpt.getValue().add(resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox().getNorth());
		dpt.getValue().add(resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox().getEast());
		dpt.setSrsName("EPSG:4326");
		point.setPos(dpt);
	    }

	    // Sets BNHS properties needed for the WHOS-Arctic
	    List<SimpleEntry<BNHSProperty, String>> properties = BNHSPropertyReader.readProperties(resource);
	    for (SimpleEntry<BNHSProperty, String> property : properties) {
		BNHSProperty key = property.getKey();
		if (key.isInWML()) {
		    addPropertyIfNotPresent(monitoringPoint, key.getLabel(), property.getValue());
		}
	    }
	    // Sets the country property
	    Optional<String> country = resource.getExtensionHandler().getCountryISO3();
	    if (country.isPresent()) {
		Country decoded = Country.decode(country.get());
		if (decoded != null) {
		    String shortName = decoded.getShortName();
		    addPropertyIfNotPresent(monitoringPoint, WML_SiteProperty.COUNTRY.getCode(), shortName);
		}
	    }
	}

	Result anyResult = observation.getResult();
	MeasurementTimeseriesType mtt = new MeasurementTimeseriesType();
	if (anyResult != null) {
	    ResultWrapper wrapper = new ResultWrapper(anyResult);
	    try {
		mtt = wrapper.getMeasurementTimeseriesType();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	List<TVPDefaultMetadataPropertyType> pointMetadata = mtt.getDefaultPointMetadata();

	if (pointMetadata.isEmpty()) {
	    TVPDefaultMetadataPropertyType pointMetadata1 = new TVPDefaultMetadataPropertyType();
	    pointMetadata.add(pointMetadata1);
	}
	TVPDefaultMetadataPropertyType pointMetadata1 = pointMetadata.get(0);

	JAXBElement<? extends TVPMetadataType> defaultPMetadata = pointMetadata1.getDefaultTVPMetadata();
	if (defaultPMetadata == null) {
	    TVPMeasurementMetadataType tvpm = new TVPMeasurementMetadataType();
	    defaultPMetadata = JAXBWML2.getInstance().getFactory().createDefaultTVPMeasurementMetadata(tvpm);
	    pointMetadata1.setDefaultTVPMetadata(defaultPMetadata);
	}
	TVPMetadataType defaultPointMetadata = defaultPMetadata.getValue();
	if (defaultPointMetadata instanceof TVPMeasurementMetadataType) {
	    TVPMeasurementMetadataType tvpmm = (TVPMeasurementMetadataType) defaultPointMetadata;

	    ReferenceType interpolation = tvpmm.getInterpolationType();
	    if (interpolation == null) {
		interpolation = new ReferenceType();
		tvpmm.setInterpolationType(interpolation);
	    }
	    if (extensions.getTimeInterpolation().isPresent()) {
		switch (extensions.getTimeInterpolation().get()) {
		case AVERAGE:
		case AVERAGE_PREC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/AveragePrec");
		    interpolation.setTitle("Average in preceding interval");
		    break;
		case AVERAGE_SUCC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc");
		    interpolation.setTitle("Average in scceeding interval");
		    break;
		case CONST:
		case CONST_PREC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstPrec");
		    interpolation.setTitle("Constant in preceding interval");
		    break;
		case CONST_SUCC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstSucc");
		    interpolation.setTitle("Constant in succeeding interval");
		    break;
		case CONTINUOUS:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/Continuous");
		    interpolation.setTitle("Continuous");
		    break;
		case DISCONTINUOUS:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/Discontinuous");
		    interpolation.setTitle("Discontinuous");
		    break;
		case INSTANT_TOTAL:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/InstantTotal");
		    interpolation.setTitle("Instantaneous total");
		    break;
		case MAX:
		case MAX_PREC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxPrec");
		    interpolation.setTitle("Maximum in preceding interval");
		    break;
		case MAX_SUCC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxSucc");
		    interpolation.setTitle("Maximum in succeeding interval");
		    break;
		case MIN:
		case MIN_PREC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/MinPrec");
		    interpolation.setTitle("Minimum in preceding interval");
		    break;
		case MIN_SUCC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/MinSucc");
		    interpolation.setTitle("Minimum in succeeding interval");
		    break;
		case STATISTICAL:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/Statistical");
		    interpolation.setTitle("Statistical");
		    break;
		case TOTAL:
		case TOTAL_PREC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalPrec");
		    interpolation.setTitle("Preceding total");
		    break;
		case TOTAL_SUCC:
		    interpolation.setHref("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalSucc");
		    interpolation.setTitle("Scceeding total");
		    break;
		case CATEGORICAL:
		case INCREMENTAL:
		default:
		    // what to do?
		}
	    }
	    UnitReference uom = tvpmm.getUom();
	    if (uom == null) {
		uom = new UnitReference();
		tvpmm.setUom(uom);
	    }
	    if (extensions.getAttributeUnits().isPresent()) {
		uom.setTitle(extensions.getAttributeUnits().get());
	    }
	    if (extensions.getAttributeUnitsURI().isPresent()) {
		uom.setHref(extensions.getAttributeUnitsURI().get());
	    }

	}

	ResultWrapper resultWrapper = new ResultWrapper();
	try {
	    resultWrapper.setMeasurementTimeseriesType(mtt);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	observation.setResult(resultWrapper);
    }

    protected static void addPropertyIfNotPresent(MonitoringPointType monitoringPoint, String label, String value) {
	List<NamedValuePropertyType> parameters = monitoringPoint.getParameter();
	HashSet<String> names = new HashSet<>();
	for (NamedValuePropertyType parameter : parameters) {
	    String name = parameter.getNamedValue().getName().getTitle();
	    names.add(name);
	}
	if (!names.contains(label)) {
	    NamedValuePropertyType nvpt = new NamedValuePropertyType();
	    NamedValueType nvt = new NamedValueType();
	    ReferenceType rt = new ReferenceType();
	    rt.setTitle(label);
	    nvt.setName(rt);
	    nvt.setValue(getCharacterString(value));
	    nvpt.setNamedValue(nvt);
	    parameters.add(nvpt);
	}
    }

    private static CharacterStringPropertyType getCharacterString(String organisationName) {
	eu.essi_lab.jaxb.wml._2_0.iso2005.gco.ObjectFactory f = new ObjectFactory();
	JAXBElement<String> ja = f.createCharacterString(organisationName);
	CharacterStringPropertyType ret = new CharacterStringPropertyType();
	ret.setCharacterString(ja);
	return ret;
    }

    public Point createPoint(Date date, Double value) {
	Point point = new Point();
	String timeString = ISO8601DateTimeUtils.getISO8601DateTime(date);
	TimePositionType time = new TimePositionType();
	time.getValue().add(timeString);
	MeasureTVPType measurement = new MeasureTVPType();
	measurement.setTime(time);
	eu.essi_lab.jaxb.wml._2_0.MeasureType measureType = new eu.essi_lab.jaxb.wml._2_0.MeasureType();
	JAXBElement<eu.essi_lab.jaxb.wml._2_0.MeasureType> measure = JAXBWML2.getInstance().getFactory().createValue(measureType);
	if (value == null || !Double.isFinite(value)) {
	    measure.setNil(true);
	} else {
	    measureType.setValue(value);
	}
	measurement.setValue(measure);
	point.setMeasurementTVP(measurement);
	return point;
    }

    public void addPoints(CollectionType collection, List<Point> points) {

	OMObservationType observation = collection.getObservationMember().get(0).getOMObservation();

	Result result = observation.getResult();

	ResultWrapper wrapper = new ResultWrapper(result);

	MeasurementTimeseriesType measurementTimeSeries = null;
	try {
	    measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	measurementTimeSeries.getPoint().addAll(points);

	ResultWrapper resultWrapper = new ResultWrapper();
	try {
	    resultWrapper.setMeasurementTimeseriesType(measurementTimeSeries);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	observation.setResult(resultWrapper);
    }

}
