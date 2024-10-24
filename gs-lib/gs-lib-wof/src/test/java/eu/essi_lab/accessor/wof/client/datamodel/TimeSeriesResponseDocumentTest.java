package eu.essi_lab.accessor.wof.client.datamodel;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

public class TimeSeriesResponseDocumentTest {

    
    @Test
    public void testName() throws Exception {
	InputStream stream = TimeSeriesResponseDocumentTest.class.getClassLoader().getResourceAsStream("test-reduce.xml");
	TimeSeriesResponseDocument doc = new TimeSeriesResponseDocument(stream);
	assertEquals(31, doc.getTimeSeries().get(0).getValues().size());
	doc.reduceValues("34", "0", "1");
	assertEquals(16, doc.getTimeSeries().get(0).getValues().size());
    }
}
