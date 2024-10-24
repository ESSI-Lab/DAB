package eu.essi_lab.wml._2;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;

public class JAXBWML2Test {

    private static final String ID = "myId";

    @Test
    public void test() throws Exception {
	OMObservationType obs = new OMObservationType();
	// eu.essi_lab.jaxb.wml._2_0.om__2.Result result = new eu.essi_lab.jaxb.wml._2_0.om__2.Result();
	// result.set
	// Result result = new Result();
	 ResultWrapper result = new ResultWrapper();
	obs.setResult(result);
	MeasurementTimeseriesType measurement1 = new MeasurementTimeseriesType();
	measurement1.setId(ID);
	result.setMeasurementTimeseriesType(measurement1);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBWML2.getInstance().marshal(obs, baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	OMObservationType obs2 = JAXBWML2.getInstance().unmarshalObservation(bais);
	eu.essi_lab.jaxb.wml._2_0.om__2.Result res = obs2.getResult();
	ResultWrapper extendedRes = new ResultWrapper(res);

	MeasurementTimeseriesType measurement2 = extendedRes.getMeasurementTimeseriesType();
	assertEquals(measurement1.getId(), measurement2.getId());

    }

}
