package eu.essi_lab.jaxb.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author ilsanto
 */
public class CommonContextTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testClassCastException() throws JAXBException {

	expectedException.expect(JAXBException.class);

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	CommonContext.unmarshal(content, GetRecords.class);

    }

    @Test
    public void testClassCastCorrect() throws JAXBException {

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	CommonContext.unmarshal(content, Capabilities.class);

    }

    @Test
    public void testClassCastExceptionNode() throws JAXBException, ParserConfigurationException {

	expectedException.expect(JAXBException.class);

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	Node node = CommonContext.asDocument(CommonContext.unmarshal(content, Capabilities.class), true);

	CommonContext.unmarshal(node, GetRecords.class);

    }

    @Test
    public void testClassCastCorrectNode() throws JAXBException, ParserConfigurationException {

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	Node node = CommonContext.asDocument(CommonContext.unmarshal(content, Capabilities.class), true);

	CommonContext.unmarshal(node, Capabilities.class);

    }

    @Test
    public void testClassCastExceptionReader() throws JAXBException, ParserConfigurationException {

	expectedException.expect(JAXBException.class);

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	InputStreamReader reader = new InputStreamReader(content);

	CommonContext.unmarshal(reader, GetRecords.class);

    }

    @Test
    public void testClassCastCorrectReader() throws JAXBException, ParserConfigurationException {

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	InputStreamReader reader = new InputStreamReader(content);

	CommonContext.unmarshal(reader, Capabilities.class);

    }

    @Test
    public void testClassCastExceptionString() throws JAXBException, IOException {

	expectedException.expect(JAXBException.class);

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	StringWriter writer = new StringWriter();

	IOUtils.copy(new InputStreamReader(content), writer);

	CommonContext.unmarshal(writer.toString(), GetRecords.class);

    }

    @Test
    public void testClassCastCorrectString() throws JAXBException, IOException {

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	StringWriter writer = new StringWriter();

	IOUtils.copy(new InputStreamReader(content), writer);

	CommonContext.unmarshal(writer.toString(), Capabilities.class);

    }

    @Test
    public void testParsing() throws Exception {
	
	int max = 10000;
	CountDownLatch cdl = new CountDownLatch(max);
	long[] results = new long[max];
	List<Thread> threads = new ArrayList<>();
	for (int i = 0; i < max; i++) {
	    final int index = i;
	    Thread t = new Thread() {
		@Override
		public void run() {
		    String str = "<gs:out xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\"><gs:estimate>1</gs:estimate><gs:termFrequency/></gs:out>";
		    try {
			XMLDocumentReader reader = CommonNameSpaceContext.createCommonReader(str);
			Number n = reader.evaluateNumber("//*:estimate");
			results[index] = n.longValue();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    cdl.countDown();
		}

	    };
	    threads.add(t);
	}
	for (int i = 0; i < max; i++) {
	    threads.get(i).start();
	}
	System.out.println("wait");
	cdl.await(max,TimeUnit.SECONDS);
	System.out.println("wake");
	for (int i = 0; i < max; i++) {
	    assertEquals(1l, results[i]);
	}

    }
}