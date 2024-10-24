package eu.essi_lab.lib.xml.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class XMLDocumentReader3Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws SAXException, IOException {

    }

    @Test
    public void test() {

	TaskListExecutor<String> tle = new TaskListExecutor<>(5);
	int t = 1000;
	for (int i = 0; i < t; i++) {
	    tle.addTask(new Callable<String>() {

		@Override
		public String call() throws Exception {
		    XMLDocumentReader document = new XMLDocumentReader(
			    XMLDocumentReader3Test.class.getClassLoader().getResourceAsStream("test.xml"));
		    return document.evaluateString("//*:location");

		}
	    });
	}

	List<Future<String>> futures = tle.executeAndWait();

	int f = 0;
	for (Future<String> future : futures) {
	    try {
		assertEquals("LBR:USU-LBR-Mendon", future.get());
	    } catch (Exception e) {
		e.printStackTrace();
		fail();
	    }
	    f++;
	}
	assertEquals(t, f);

    }

}
