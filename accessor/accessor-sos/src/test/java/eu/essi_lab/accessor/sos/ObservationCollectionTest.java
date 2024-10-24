/**
 * 
 */
package eu.essi_lab.accessor.sos;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.sos.downloader._1_0_0.DataRecord;
import eu.essi_lab.accessor.sos.downloader._1_0_0.ObservationCollection;

/**
 * @author Fabrizio
 */
public class ObservationCollectionTest {

    @Test
    public void test() throws SAXException, IOException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("ObservationCollection100Example.xml");

	ObservationCollection collection = new ObservationCollection(stream);

	List<DataRecord> dataRecords = collection.//
		getDataRecords().//
		stream().//
		sorted((d1, d2) -> d1.getTime().compareTo(d2.getTime())).//
		collect(Collectors.toList());

	Assert.assertEquals(2, dataRecords.size());

	Assert.assertEquals("2023-10-09T14:12:00+00:00", dataRecords.get(0).getTime());
	Assert.assertEquals("2024-03-18T14:12:00+00:00", dataRecords.get(1).getTime());

	Assert.assertEquals(new BigDecimal("4"), dataRecords.get(0).getValue());
	Assert.assertEquals(new BigDecimal("4.1"), dataRecords.get(1).getValue());
    }
}
