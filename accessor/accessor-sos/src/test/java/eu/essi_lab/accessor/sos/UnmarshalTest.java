package eu.essi_lab.accessor.sos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType;
import eu.essi_lab.jaxb.sos._2_0.GetObservationResponseType.ObservationData;
import eu.essi_lab.jaxb.sos._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMeasurementMetadataType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.swe._2.UnitReference;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class UnmarshalTest {

    @Test
    public void test() throws Exception {
	InputStream stream = UnmarshalTest.class.getClassLoader().getResourceAsStream("wml/test.xml");
	
	
	Object object = JAXBSOS.getInstance().unmarshal(stream);

	if (object instanceof JAXBElement<?>) {
	    JAXBElement<?> jaxb = (JAXBElement<?>) object;
	    object = jaxb.getValue();
	}
	ObservationData sosObservation = null;
	if (object instanceof GetObservationResponseType) {
	    GetObservationResponseType dataResponse = (GetObservationResponseType) object;
	    List<ObservationData> observationDatas = dataResponse.getObservationData();
	    for (ObservationData observationData : observationDatas) {
		sosObservation = observationData;
		break;
	    }
	} else {
	    throw new RuntimeException("Error parsing SOS data");
	}
	eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory of = new ObjectFactory();
	JAXBElement<OMObservationType> omObservation = of.createOMObservation(sosObservation.getOMObservation());
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBSOS.getInstance().marshal(omObservation, baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType observation = JAXBWML2.getInstance().unmarshalObservation(bais);
	Result anyResult = observation.getResult();

	ResultWrapper wrapper = new ResultWrapper(anyResult);

	MeasurementTimeseriesType measurementTimeSeries = null;
	try {
	    measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType measure = null;
	if (measurementTimeSeries == null) {
	    measure = wrapper.getMeasureType();
	}
	UnitReference uom = null;
	ReferenceType interpolation = null;
	if (measurementTimeSeries != null) {

	    List<TVPDefaultMetadataPropertyType> dpm = measurementTimeSeries.getDefaultPointMetadata();
	    if (!dpm.isEmpty()) {
		JAXBElement<? extends TVPMetadataType> jaxb = dpm.get(0).getDefaultTVPMetadata();
		if (jaxb != null) {
		    TVPMetadataType tv = jaxb.getValue();
		    if (tv instanceof TVPMeasurementMetadataType) {
			TVPMeasurementMetadataType tvMeta = (TVPMeasurementMetadataType) tv;
			uom = tvMeta.getUom();
			interpolation = tvMeta.getInterpolationType();
		    }

		}
	    }

	}
    }

}
