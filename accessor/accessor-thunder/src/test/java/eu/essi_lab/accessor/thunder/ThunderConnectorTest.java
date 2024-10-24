package eu.essi_lab.accessor.thunder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.model.GSSource;
import junit.framework.TestCase;

public class ThunderConnectorTest {

    private ThunderConnector connector;
    private GSSource source;

    @Before
    public void init() {

	this.connector = new ThunderConnector();

	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testMetadataFileRead() throws Exception {
	InputStream is = ThunderConnectorTest.class.getClassLoader().getResourceAsStream("isd-history.csv");
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
	TestCase.assertEquals(29729, i);

    }

    @Test
    public void testDataFileRead() throws Exception {
	InputStream is = ThunderConnectorTest.class.getClassLoader().getResourceAsStream("007026.txt");
	TestCase.assertNotNull(is);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(is));

	String temp = "";
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
	TestCase.assertEquals(89, i);

    }

    @Test
    public void testDataFileValues() throws Exception {
	InputStream is = ThunderConnectorTest.class.getClassLoader().getResourceAsStream("007026.txt");
	TestCase.assertNotNull(is);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(is));

	String temp = "";
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
	TestCase.assertEquals(89, i);

    }

}
