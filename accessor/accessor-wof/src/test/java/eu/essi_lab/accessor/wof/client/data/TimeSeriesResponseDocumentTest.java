package eu.essi_lab.accessor.wof.client.data;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.model.exceptions.GSException;

public class TimeSeriesResponseDocumentTest {

    @Test
    public void test() throws SAXException, IOException, TransformerException, GSException {
	InputStream stream = TimeSeriesResponseDocumentTest.class.getClassLoader().getResourceAsStream("cuahsi/mock/his4values.xml");
	TimeSeriesResponseDocument tsrd = new TimeSeriesResponseDocument(stream);
	System.out.println(tsrd.getReader().asString());
	tsrd.setValueCount("LBR", "USU3", 42l);
	tsrd.setVariableTimeInterval("LBR", "USU3", "bt","et","btUTC","etUTC");
	System.out.println(tsrd.getReader().asString());
	Assert.assertEquals((long) 42, (long) tsrd.getTimeSeries().get(0).getValueCount());
	Assert.assertEquals("bt", tsrd.getTimeSeries().get(0).getBeginTimePosition());
	Assert.assertEquals("et", tsrd.getTimeSeries().get(0).getEndTimePosition());
	Assert.assertEquals("btUTC", tsrd.getTimeSeries().get(0).getBeginTimePositionUTC());
	Assert.assertEquals("etUTC", tsrd.getTimeSeries().get(0).getEndTimePositionUTC());
    }

}
