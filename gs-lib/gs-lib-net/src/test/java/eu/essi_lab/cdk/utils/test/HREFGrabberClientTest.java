/**
 * 
 */
package eu.essi_lab.cdk.utils.test;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.dirlisting.HREFGrabberClient;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class HREFGrabberClientTest {

    @Test
    public void test() throws Exception {

	InputStream stream = HREFGrabberClientTest.class.getClassLoader().getResourceAsStream("grab.html");
	String html = IOStreamUtils.asUTF8String(stream);

	HREFGrabberClient client = new HREFGrabberClient(html);

	List<String> links = client.grabLinks("Download file");
	Assert.assertEquals("temp/1532424367_72075.csv", links.get(0));
    }
    
    @Test
    public void test2() throws Exception {

	InputStream stream = HREFGrabberClientTest.class.getClassLoader().getResourceAsStream("grab2.html");
	String html = IOStreamUtils.asUTF8String(stream);

	HREFGrabberClient client = new HREFGrabberClient(html);
	client.setHREF_A_ClosingTag("</td>");

	List<String> links = client.grabLinks();
	Assert.assertEquals(10, links.size());
	
	Assert.assertTrue(links.get(0).startsWith("daily"));

    }
}
