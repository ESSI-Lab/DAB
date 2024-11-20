package eu.essi_lab.accessor.wof;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import eu.essi_lab.profiler.wof.info.datamodel.ArrayOfServiceInfo;

public class ArrayOfServiceInfoTest {
    @Test
    public void test() throws Exception {
	ArrayOfServiceInfo record = new ArrayOfServiceInfo();
	String serverURL = "http://his-server";
	String title = "my series";
	String serviceDescriptionURL = "http://his-server";
	String email = "email@email.com";
	String phone = "3472398";
	String organization = "University of Utah";
	String orgwebsite = "http://utah.edu";
	String citation = "my Acquisition";
	String aabstract = "bla bla bla";
	String valuecount = "9348";
	String variablecount = "34";
	String sitecount = "3";
	String serviceID = "1";
	String networkName = "LBR";
	String minx = "0";
	String miny = "1";
	String maxx = "2";
	String maxy = "3";
	record.addServiceInfo(serverURL, title, serviceDescriptionURL, email, phone, organization, orgwebsite, citation, aabstract,
		valuecount, variablecount, sitecount, serviceID, networkName, minx, miny, maxx, maxy);
	InputStream stream = ArrayOfServiceInfoTest.class.getClassLoader().getResourceAsStream("cuahsi/test2.xml");
	String str = IOUtils.readStringFromStream(stream);
//	System.out.println(record.asString());
	
	assertEquals(str.replaceAll("[^A-Za-z0-9]", ""), record.asString().replaceAll("[^A-Za-z0-9]", ""));
    }
}
