package eu.essi_lab.accessor.wof.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.profiler.wof.access.GetValuesObjectResultSetMapper;

public class GetValuesObjectResultSetMapperTest {

    /**
     * Asserts the template is as expected
     * @throws Exception
     */
    @Test
    public void testTemplateAsExpected() throws Exception {
	GetValuesObjectResultSetMapper mapper = new GetValuesObjectResultSetMapper();
	XMLDocumentReader template = mapper.getResponseTemplate();
	String root = template.evaluateString("local-name(/*[1])");
	assertEquals("Envelope", root);
	assertNotNull(template.evaluateNode("/*:Envelope/*:Body/*:TimeSeriesResponse"));
	assertNull(template.evaluateNode("/*:Envelope/*:Body/*:TimeSeriesResponse/*[1]"));
	
	
    }

}

