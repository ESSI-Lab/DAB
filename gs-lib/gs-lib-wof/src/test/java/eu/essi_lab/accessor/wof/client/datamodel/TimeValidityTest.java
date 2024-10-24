package eu.essi_lab.accessor.wof.client.datamodel;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.junit.Test;

public class TimeValidityTest {

    @Test
    public void test() throws Exception {
	InputStream stream = TimeValidityTest.class.getClassLoader().getResourceAsStream("test-time-validity.xml");
	TimeSeriesResponseDocument tsrd = new TimeSeriesResponseDocument(stream);
	Value value = tsrd.getTimeSeries().get(0).getValues().get(0);
	assertEquals("-9999", value.getValue());
	String dt = value.getDateTimeUTC();
	assertEquals("2016-01-01 08:00:00", dt);
	tsrd.fixTimes();
	dt = value.getDateTimeUTC();
	assertEquals("2016-01-01T08:00:00", dt);
	stream.close();

	stream = tsrd.getReader().asStream();
	TimeSeriesResponseType trt = JAXBWML.getInstance().parseTimeSeries(stream);
	ValueSingleVariable v = trt.getTimeSeries().get(0).getValues().get(0).getValue().get(0);
	assertEquals("-9999", v.getValue().toString());
	System.out.println(v.getDateTimeUTC().toString());
	assertEquals("2016-01-01T08:00:00", v.getDateTimeUTC().toString());
	
	stream.close();

    }

}
