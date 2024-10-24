package eu.essi_lab.cdk.utils.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.lib.net.executor.HTTPExecutorResponse;
import eu.essi_lab.lib.net.executor.OkHttpRequestExecutor;

public class OkHttpRequestExecutorExternalTestIT {

    @Test
    public void test() throws Exception {
	OkHttpRequestExecutor executor = new OkHttpRequestExecutor();
	HTTPExecutorResponse response = executor.execute("http://www.google.com", "GET", null,null);
	assertTrue(response.getResponseCode().equals(200));
	SimpleEntry<String, String>[] headers = response.getResponseHeaders();
	assertTrue(headers.length > 0);
	boolean found = false;
	for (SimpleEntry<String, String> header : headers) {
	    System.out.println(header.getKey() + " " + header.getValue());
	    if (header.getKey().equals("Content-Type")) {
		assertTrue(header.getValue().contains("text/html"));
		found = true;
	    }
	}
	assertTrue(found);
	InputStream stream = response.getResponseStream();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	baos.close();
	assertTrue(baos.toByteArray().length > 0);
    }

}
