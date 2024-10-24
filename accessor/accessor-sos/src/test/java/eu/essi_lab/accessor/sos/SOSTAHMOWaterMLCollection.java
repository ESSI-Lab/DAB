package eu.essi_lab.accessor.sos;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.om__2.TimeObjectPropertyType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.ShapeType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class SOSTAHMOWaterMLCollection {

    @Test
    public void test() throws JAXBException {

	DataDescriptor ret = new DataDescriptor();

	CollectionType collection = null;
	try {
	    InputStream stream = SOSTAHMOWaterMLCollection.class.getClassLoader().getResourceAsStream("wmlCollectionError.xml");
	    XMLDocumentReader xdoc = new XMLDocumentReader(stream);
	    xdoc.getDocument().getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi",
		    "http://www.w3.org/2001/XMLSchema-instance");
	    xdoc.setNamespaceContext(new SOSNamespaceContext());
	    //xdoc = new XMLDocumentReader(xdoc.asStream());
	    XMLDocumentWriter writer = new XMLDocumentWriter(xdoc);
	    Node[] nodesResult = xdoc.evaluateNodes("//*:result");
	    for (Node node : nodesResult) {
		String text = xdoc.evaluateString(node, ".");
		// writer.setText(node,".", "");
		XMLNodeReader nr = new XMLNodeReader(node);
		XMLNodeWriter nw = new XMLNodeWriter(nr);
		nw.addAttributesNS(".", CommonNameSpaceContext.XSI_SCHEMA_INSTANCE_NS_URI, "xsi:type", "gml:MeasureType");
		nw.addAttributes(".", "uom", "unkown");
	    }
	    System.out.println(xdoc.asString());
	    collection = JAXBWML2.getInstance().unmarshalCollection(xdoc.asStream());
	    stream.close();
	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

	ret.setDataFormat(DataFormat.WATERML_2_0());
	ret.setDataType(DataType.TIME_SERIES);

	Long timeBegin = null;
	Long timeEnd = null;
	Long timeResolution = null;
	boolean invalidResolution = false;
	Long previousTime = null;

	HashSet<Long> times = new HashSet<>();

	List<OMObservationPropertyType> observationMembers = collection.getObservationMember();
	for (OMObservationPropertyType observationMember : observationMembers) {
	    OMObservationType observation = observationMember.getOMObservation();
	    TimeObjectPropertyType phenomenonTime = observation.getPhenomenonTime();
	    if (phenomenonTime != null) {
		JAXBElement<? extends AbstractTimeObjectType> abstractTimeObject = phenomenonTime.getAbstractTimeObject();
		if (abstractTimeObject != null) {
		    String timeName = abstractTimeObject.getName().getLocalPart();
		    if (timeName.equals("AbstractTimeObject")) {
			throw new IllegalArgumentException("Abstract time element present instead of realization");
		    }
		}
	    }
	    FeaturePropertyType foi = observation.getFeatureOfInterest();
	    JAXBElement<? extends eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractFeatureType> abstractFeature = foi.getAbstractFeature();
	    if (abstractFeature != null) {
		eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractFeatureType feature = abstractFeature.getValue();
		if (feature instanceof SFSpatialSamplingFeatureType) {
		    SFSpatialSamplingFeatureType sfssf = (SFSpatialSamplingFeatureType) feature;
		    ShapeType shape = sfssf.getShape();
		    JAXBElement<? extends AbstractGeometryType> jaxbAbstractGeometry = shape.getAbstractGeometry();
		    String geometryName = jaxbAbstractGeometry.getName().getLocalPart();
		    if (geometryName.equals("AbstractGeometry")) {
			throw new IllegalArgumentException("Abstract geometry element present instead of realization");
		    }
		    AbstractGeometryType abstractGeometry = jaxbAbstractGeometry.getValue();
		    if (abstractGeometry instanceof PointType) {
			PointType point = (PointType) abstractGeometry;
			String srs = point.getSrsName();
			DirectPositionType pos = point.getPos();
			if (srs == null) {
			    srs = pos.getSrsName();
			}
			List<Double> values = pos.getValue();
			if (values.size() == 2) {
			    CRS crs = CRS.fromIdentifier(srs);
			    ret.setCRS(crs);

			    DataDimension spatial1 = new ContinueDimension(crs.getFirstAxisName());
			    spatial1.getContinueDimension().setLower(values.get(0));
			    spatial1.getContinueDimension().setUpper(values.get(0));
			    spatial1.getContinueDimension().setSize(1l);

			    DataDimension spatial2 = new ContinueDimension(crs.getSecondAxisName());
			    spatial2.getContinueDimension().setLower(values.get(1));
			    spatial2.getContinueDimension().setUpper(values.get(1));
			    spatial2.getContinueDimension().setSize(1l);

			    ret.setSpatialDimensions(Arrays.asList(spatial1, spatial2));

			}
		    }
		} else {
		    throw new IllegalArgumentException("Unexpected abstract feature");
		}
	    } else {
		// by default if no feature is found, is set a CRS of 4326 without spatial axis
		ret.setCRS(CRS.EPSG_4326());
	    }

	    Result anyResult = observation.getResult();

	    ResultWrapper wrapper = new ResultWrapper(anyResult);

	    MeasurementTimeseriesType measurements = null;
	    try {
		measurements = wrapper.getMeasurementTimeseriesType();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    MeasureType measure = null;
	    if (measurements == null) {
		measure = wrapper.getMeasureType();
	    }

	    if (measure != null) {
		// String uom = measure.getUom();
		// double value = measure.getValue();
		// in this case a single time instant must be present

		TimeObjectPropertyType time = observation.getPhenomenonTime();
		if (time != null) {
		    JAXBElement<? extends AbstractTimeObjectType> abstractTime = time.getAbstractTimeObject();
		    if (abstractTime != null) {
			AbstractTimeObjectType timeValue = abstractTime.getValue();
			if (timeValue instanceof TimeInstantType) {
			    TimeInstantType instant = (TimeInstantType) timeValue;
			    String timeString = instant.getTimePosition().getValue().get(0);
			    Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
			    if (optionalMilliseconds.isPresent()) {
				long milliseconds = optionalMilliseconds.get().getTime();
				times.add(milliseconds);
				if (timeBegin == null || timeBegin > milliseconds) {
				    timeBegin = milliseconds;
				}
				if (timeEnd == null || timeEnd < milliseconds) {
				    timeEnd = milliseconds;
				}
			    }
			} else if (timeValue instanceof TimePeriodType) {
			    TimePeriodType tpt = (TimePeriodType) timeValue;
			    TimePositionType end = tpt.getEndPosition();
			    String timeString = end.getValue().get(0);
			    Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
			    if (optionalMilliseconds.isPresent()) {
				long milliseconds = optionalMilliseconds.get().getTime();
				times.add(milliseconds);
				if (timeBegin == null || timeBegin > milliseconds) {
				    timeBegin = milliseconds;
				}
				if (timeEnd == null || timeEnd < milliseconds) {
				    timeEnd = milliseconds;
				}
			    }

			}
		    }
		}

	    } else {
		throw new IllegalArgumentException("Expected MeasurementTimeseriesType or MeasureType");
	    }

	    if (timeBegin != null && timeEnd != null) {
		ret.setTemporalDimension(new Date(timeBegin), new Date(timeEnd));
		if (!invalidResolution) {
		    ret.getTemporalDimension().getContinueDimension().setResolution(timeResolution);
		}
		ret.getTemporalDimension().getContinueDimension().setSize((long) times.size());

	    }

	}
	if (ret.getTemporalDimension() != null)
	    return;

	fail();
    }

}
