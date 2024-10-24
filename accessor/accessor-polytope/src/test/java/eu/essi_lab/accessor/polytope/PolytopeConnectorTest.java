package eu.essi_lab.accessor.polytope;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import junit.framework.TestCase;

public class PolytopeConnectorTest {

    private PolytopeConnector connector;
    private GSSource source;

    @Before
    public void init() {

	this.connector = new PolytopeConnector();

	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testCountFileRead() throws Exception {
	InputStream is = PolytopeConnectorTest.class.getClassLoader().getResourceAsStream("Cartel4.csv");
	TestCase.assertNotNull(is);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(is));

	// skip header line
	String temp = bfReader.readLine();
	int i = 0;

	while ((temp = bfReader.readLine()) != null) {

	    String[] split = temp.split(",", -1);
	    // System.out.println("LINE " + i);
	    for (int j = 0; j < split.length; j++) {
		// System.out.print(split[j] + ",");
	    }
	    i++;
	    // System.out.println("");
	}
	TestCase.assertEquals(2231, i);

    }


    @Test
    public void testAlternativeFileRead() throws Exception {
	InputStream is = PolytopeConnectorTest.class.getClassLoader().getResourceAsStream("Ams25_TEMP.csv");
	TestCase.assertNotNull(is);

	String file = IOStreamUtils.asUTF8String(is);
	
	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new StringReader(file));

	String temp = bfReader.readLine();
	int i = 0;

	while ((temp = bfReader.readLine()) != null) {

	    String[] split = temp.split(",", -1);
	    // System.out.println("LINE " + i);
	    for (int j = 0; j < split.length; j++) {
		System.out.println(split[j] + ",");
	    }
	    i++;
	    // System.out.println("");
	}
	TestCase.assertEquals(2231, i);
    }

}
