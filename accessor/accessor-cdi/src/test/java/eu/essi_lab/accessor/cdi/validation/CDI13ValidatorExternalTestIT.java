package eu.essi_lab.accessor.cdi.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.accessor.cdi.CDI13Validator;
import eu.essi_lab.accessor.cdi.ValidationResult;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.smtp.GmailClient;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class CDI13ValidatorExternalTestIT {
    private String giProxyEndpoint = null;
    CDI13Validator validator = new CDI13Validator();

    @Before
    public void init() {
	giProxyEndpoint = System.getProperty("giProxyEndpoint");
	if (giProxyEndpoint == null) {
	    fail("GI-proxy endpoint parameter missing");
	}

    }

    private String[] getEmailsForReport() {
	return System.getProperty("report.emails").split(",");
    }

    protected void sendEmailReport(String message) throws AddressException, MessagingException {
	String[] emails = getEmailsForReport();
	if (emails.length > 0) {
	    File localFile = new File("/home/boldrini");
	    if (localFile.exists()) {
		System.out.println("not sending report, as it is manually launched from developer machine");
		return;
	    }
	    GmailClient client = new GmailClient();
	    client.send("[SeaDataNet] CDI validation error", message, emails);

	}
    }

    @Test
    public void testCDIOfficialSample() throws IOException, AddressException, MessagingException {

	String url = "https://www.seadatanet.org/content/download/4534/file/CDI_ISO19139_full_example_13.0.0.xml";

	TestResult testResult = testURLs(new Iterator<String>() {

	    private boolean hasNext = true;

	    @Override
	    public String next() {
		hasNext = false;
		return url;
	    }

	    @Override
	    public boolean hasNext() {
		return hasNext;
	    }
	});

	if (testResult.errors > 0) {
	    sendEmailReport("The official CDI sample failed to validate!\n\n" + testResult.getReport());
	}

	assertEquals(0, testResult.errors);

    }

    @Test
    public void testCDICentral100() throws IOException {
	testCDICentral(100);
    }

    // @Test
    @Ignore
    public void testCDICentralAll() throws IOException {
	// 2020-01-07 were a total of 2876154 //
	testCDICentral(100);
    }

    @Test
    public void testCDIAggregation() throws Exception {

	Downloader downloader = new Downloader();

	String url = giProxyEndpoint + "/get?url=https://cdi.seadatanet.org/report/aggregation";

	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);

	InputStream stream = optionalStream.get();

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	stream.close();

	Node[] nodes = reader.evaluateNodes("//*:cdiUrl");

	TestResult testResult = testURLs(new Iterator<String>() {

	    private boolean hasNext = true;

	    int i = 0;

	    @Override
	    public String next() {

		if (i >= nodes.length - 1) {
		    hasNext = false;
		}

		if (i < nodes.length) {
		    Node node = nodes[i];
		    i++;
		    try {
			String child = reader.evaluateString(node, ".");
			return giProxyEndpoint + "/get?url=" + child;
		    } catch (XPathExpressionException e) {
			e.printStackTrace();
		    }
		}

		return null;
	    }

	    @Override
	    public boolean hasNext() {
		return hasNext;
	    }
	});

	assertEquals(0, testResult.errors);

    }

    private void testCDICentral(int start) throws IOException {

	TestResult testResult = testURLs(new Iterator<String>() {

	    private boolean hasNext = true;

	    int i = start;

	    @Override
	    public String next() {
		if (i == 0) {
		    hasNext = false;
		}
		String url = "https://cdi.seadatanet.org/report/" + i-- + "/v0/xml";

		return url;
	    }

	    @Override
	    public boolean hasNext() {
		return hasNext;
	    }
	});
	assertEquals(0, testResult.errors);

    }

    private TestResult testURLs(Iterator<String> urlIterator) throws IOException {

	TestResult testResult = new TestResult();

	int i = 0;

	while (urlIterator.hasNext()) {
	    String url = (String) urlIterator.next();

	    ValidationResult result;

	    try {
		result = validateURL(url);
	    } catch (Exception e) {
		result = new ValidationResult();
		result.setPassed(false);
		result.setErrorMessage(e.getMessage());
	    }

	    if (result == null) {
		testResult.skip();
	    } else if (result.isPassed()) {
		testResult.success();
	    } else {
		String error = result.getErrorMessage();
		if (error == null) {
		    error = "";
		}
		HashSet<String> identifiers = testResult.getErrorMap().get(error);
		if (identifiers == null) {
		    identifiers = new HashSet<String>();
		    testResult.getErrorMap().put(error, identifiers);
		}
		// maintain only 10 samples per error type
		if (identifiers.size() < 10) {
		    String property = "java.io.tmpdir";
		    String tempDirString = System.getProperty(property);
		    File tempDir = new File(tempDirString);
		    String t = "CDI" + url.hashCode();
		    File tempFile = new File(tempDir, t + ".xml");
		    tempFile.deleteOnExit();
		    ByteArrayInputStream bais = new ByteArrayInputStream(result.getBytes());
		    FileOutputStream baos = new FileOutputStream(tempFile);
		    IOUtils.copy(bais, baos);
		    bais.close();
		    baos.close();
		    identifiers.add(url + " " + tempFile.getAbsolutePath());
		}
		testResult.error();
	    }

	    if (i++ % 100 == 0) {
		testResult.printReport();
	    }

	}
	testResult.printReport();

	// while (true) {
	// System.out.println("ENDED");
	// try {
	// Thread.sleep(10000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

	return testResult;

    }

    private ValidationResult validateURL(String url) throws IOException {

	System.out.println("Validating CDI document: " + url);
	Downloader downloader = new Downloader();

	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);

	InputStream stream = optionalStream.get();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	IOUtils.copy(stream, baos);

	stream.close();
	baos.close();

	byte[] bytes = baos.toByteArray();

	if (bytes.length < 200000) {
	    String s = new String(bytes);
	    if (s.contains("Record not found.")) {
		System.out.println("Skipping " + url + " (not found)");
		return null;
	    }
	}
	System.out.println(bytes.length);

	ValidationResult result = validator.validate(bytes);
	result.setBytes(bytes);
	return result;
    }

}
