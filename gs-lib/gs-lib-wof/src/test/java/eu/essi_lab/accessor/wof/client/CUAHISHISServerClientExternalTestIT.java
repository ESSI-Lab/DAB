package eu.essi_lab.accessor.wof.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CUAHISHISServerClientExternalTestIT {

    @Test
    public void testDifferentGetSitesImplementations() throws GSException {
	String endpoint = "http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL";	
	CUAHSIHISServerClient client = new CUAHSIHISServerClient1_1(endpoint);
	
	// GET SITES - DOM implementation
	Iterator<SiteInfo> sitesResponse1 = client.getSites();
	List<SiteInfo> sites1 = Lists.newArrayList(sitesResponse1);
	assertTrue(sites1.size() > 0);
	String name1 = sites1.get(0).getSiteName();
	
	System.out.println(sites1.size());
	System.out.println(sites1.size() + " sites");
	System.out.println("First site: " + name1);
	
	// GET SITES - StAX implementation
	Iterator<SiteInfo> sitesResponse4 = client.getSites();
	List<SiteInfo> sites4 = Lists.newArrayList(sitesResponse4);
	assertEquals(sites1.size(), sites4.size());
	String name4 = sites4.get(0).getSiteName();
	assertEquals(name1, name4);
	System.out.println(sites4.size());
	System.out.println(sites4.size() + " sites");
	System.out.println("First site: " + name4);
	
	// GET SITES OBJECT - DOM implementation
	Iterator<SiteInfo> sitesResponse2 = client.getSitesObject();
	List<SiteInfo> sites2 = Lists.newArrayList(sitesResponse2);
	assertEquals(sites1.size(), sites2.size());
	String name2 = sites2.get(0).getSiteName();
	assertEquals(name1, name2);
	System.out.println(sites2.size() + " sites");
	System.out.println("First site: " + name2);

	
	// GET SITES OBJECT - StAX implementation
	Iterator<SiteInfo> sitesResponse3 = client.getSitesObjectStAX();
	List<SiteInfo> sites3 = Lists.newArrayList(sitesResponse3);
	assertEquals(sites1.size(), sites3.size());
	String name3 = sites3.get(0).getSiteName();
	assertEquals(name1, name3);	
	System.out.println(sites3.size() + " sites");
	System.out.println("First site: " + name3);

	
	
	
	
	
	

    }

}
