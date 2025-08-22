/**
 * 
 */
package eu.essi_lab.comparator;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.util.IOUtils;

/**
 * @author Fabrizio
 */
public class DuplicatedIdentifierTest {

    @Test
    public void test1() throws Exception {

	String filename = "online_id_duplicated.txt";
	InputStream stream = DuplicatedIdentifierTest.class.getClassLoader().getResourceAsStream(filename);
	String s = IOUtils.toString(stream);
	String[] splittedLines = s.split("\r\n");

	for (String s1 : splittedLines) {
	    String toIds = s1.split("FILE_IDENTIFIERS:")[1];
	    String[] toRemove = toIds.split(";");
	    if (toRemove.length > 1) {
		for (int k = 1; k < toRemove.length; k++) {
		    System.out.println(toRemove[k]);
		}
	    }

	}
	Assert.assertTrue(splittedLines.length == 1200);
    }

}
