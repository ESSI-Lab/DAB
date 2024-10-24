package eu.essi_lab.accessor.wof.client.datamodel;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.HIS4ValuesValidator;

public class SitesResponseDocumentTest {

    private HIS4ValuesValidator validator;

    @Before
    public void init() {
	this.validator = new HIS4ValuesValidator();
    }

    @Test
    public void testSites() throws Exception {

	InputStream stream = SitesResponseDocumentTest.class.getClassLoader().getResourceAsStream("cuahsi/mock/his4sites.xml");
	SitesResponseDocument sitesDocument = new SitesResponseDocument(stream);
	List<Site> sites = sitesDocument.getSites();
	assertEquals(16, sites.size());

	Site site = sites.get(0);

	String wsdl = site.getSeriesCatalogWSDL();

	assertEquals("", wsdl);

	SiteInfo siteInfo = site.getSitesInfo();

	validator.assertServer4SiteInfo(siteInfo);
	
	List<TimeSeries> series = site.getSeries();
	
	assertEquals(0, series.size());

    }

    @Test
    public void testSite() throws Exception {
	InputStream stream = SitesResponseDocumentTest.class.getClassLoader().getResourceAsStream("cuahsi/mock/his4siteInfo.xml");

	SitesResponseDocument sitesDocument = new SitesResponseDocument(stream);
	List<Site> sites = sitesDocument.getSites();
	assertEquals(1, sites.size());

	Site site = sitesDocument.getSites().get(0);

	String wsdl = site.getSeriesCatalogWSDL();

	assertEquals("http://www7.ncdc.noaa.gov/CUAHSIServices/CUAHSIServices", wsdl);

	SiteInfo siteInfo = site.getSitesInfo();

	validator.assertServer4SiteInfo(siteInfo);
	

	List<TimeSeries> series = site.getSeries();
	
	TimeSeries serie = series.get(0);
	
	validator.assertServer4Serie(serie);


    }

}
