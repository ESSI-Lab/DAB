package eu.essi_lab.accessor.wof.client;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest;
import eu.essi_lab.accessor.wof.client.datamodel.GetValuesRequest_1_1;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class CUAHSIHISServerDownloadTest {

    @Test
    public void test() throws Exception {
	SOAPExecutorDOM executor = new SOAPExecutorDOM("https://hydroportal.cuahsi.org/nwisuv/cuahsi_1_1.asmx?WSDL");

	executor.setSOAPAction("http://www.cuahsi.org/his/1.1/ws/GetValuesObject");

	// The input request
	GetValuesRequest gvr = new GetValuesRequest_1_1();
	gvr.setLocation("NWISUV:01387450");
	gvr.setVariable("NWISUV:00065");
	gvr.setStartDate("2024-10-24T20:00:00");
	gvr.setEndDate("2024-10-31T20:00:00");

	InputStream input = null;
	try {
	    input = gvr.getReader().asStream();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	executor.setBody(input);

	executor.setResultPath("//*:timeSeriesResponse");

	XMLDocumentReader reader = executor.execute();
	
	System.out.println(reader.asString());
    }

}
