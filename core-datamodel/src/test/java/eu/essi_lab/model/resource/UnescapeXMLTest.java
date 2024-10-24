/**
 * 
 */
package eu.essi_lab.model.resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public class UnescapeXMLTest {

    /**
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void UnescapeXMLTest() throws IOException, NoSuchAlgorithmException {

	InputStream stream = UnescapeXMLTest.class.getClassLoader().getResourceAsStream("his4wmlTS.xml");

	String string = IOUtils.toString(stream, "UTF-8");
	string = string.replace(System.getProperty("line.separator"), "");

	for (int i = 0; i < string.length(); i++) {
	    if (i % 50 == 0) {
		System.out.println();
	    }
	    char c = string.charAt(i);
	    System.out.print((int) c + " ");
	}
	System.out.println();

	String digest = StringUtils.hashSHA1messageDigest(string);

	Assert.assertEquals("C48051D0BD9C1BD70D35615F8A59AEBFBEEF46C6", digest);

    }

}
