package eu.essi_lab.accessor.wof.client;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class CUAHSIHISServerClientTest {

    protected CUAHSIHISServerClient1_1 client;

    protected SiteInfo testHISServerEndpoint(CUAHSIHISServerClient1_1 client, int minimumSiteSize) throws GSException {
	// get the sites
	Iterator<SiteInfo> sitesDocument = client.getSitesObjectStAX();
	List<SiteInfo> sitesInfo = Lists.newArrayList(sitesDocument);
	// assert that at least mimimumSiteSize are found
	TestCase.assertTrue(sitesInfo.size() > minimumSiteSize);
	SiteInfo siteInfo = sitesInfo.get(0);
	// get the first site info
	SitesResponseDocument siteInfoDocument = client.getSiteInfo(siteInfo.getSiteCodeNetwork(), siteInfo.getSiteCode());
	SiteInfo richerSiteInfo = siteInfoDocument.getSitesInfo().get(0);
	// checks that the retrieved site info is equal to the previous one
	System.out.println("Comparing " + siteInfo.toString() + " with " + richerSiteInfo.toString());
	TestCase.assertEquals(siteInfo.getSiteCodeNetwork(), richerSiteInfo.getSiteCodeNetwork());
	TestCase.assertEquals(siteInfo.getSiteCode(), richerSiteInfo.getSiteCode());
	TestCase.assertEquals(siteInfo.getSiteName(), richerSiteInfo.getSiteName());
	TestCase.assertEquals(siteInfo.getSiteId(), richerSiteInfo.getSiteId());
	TestCase.assertEquals(siteInfo.getLatitude(), richerSiteInfo.getLatitude());
	TestCase.assertEquals(siteInfo.getLongitude(), richerSiteInfo.getLongitude());
	TestCase.assertEquals(siteInfo.getCounty(), richerSiteInfo.getCounty());
	TestCase.assertEquals(siteInfo.getState(), richerSiteInfo.getState());
	return richerSiteInfo;

    }
}
