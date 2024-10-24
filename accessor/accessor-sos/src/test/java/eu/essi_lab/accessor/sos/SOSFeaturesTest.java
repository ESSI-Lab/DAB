package eu.essi_lab.accessor.sos;

import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Test;

import eu.essi_lab.jaxb.sos._2_0.GetFeatureOfInterestResponseType;
import eu.essi_lab.jaxb.sos._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.sos._2_0.wml.MonitoringPointType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;

public class SOSFeaturesTest {

    @Test
    public void test() throws JAXBException {
	InputStream stream = SOSFeaturesTest.class.getClassLoader().getResourceAsStream("features.xml");
	Object object = JAXBSOS.getInstance().unmarshal(stream);
	if (object instanceof JAXBElement<?>) {
	    JAXBElement<?> jaxb = (JAXBElement<?>) object;
	    object = jaxb.getValue();	    
	}
	if (object instanceof GetFeatureOfInterestResponseType) {
	    GetFeatureOfInterestResponseType response = (GetFeatureOfInterestResponseType) object;
	    AbstractFeatureType af = response.getFeatureMember().get(0).getAbstractFeature().getValue();
	    if (af instanceof MonitoringPointType) {
		MonitoringPointType mpt = (MonitoringPointType) af;
		return;
	    }
	    fail();
	}
	fail();
    }

}
