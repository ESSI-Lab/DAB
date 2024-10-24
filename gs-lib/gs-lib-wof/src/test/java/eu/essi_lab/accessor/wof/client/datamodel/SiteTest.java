package eu.essi_lab.accessor.wof.client.datamodel;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class SiteTest {

    @Test
    public void testFull() throws Exception {
	SitesResponseDocument srd1 = new SitesResponseDocument(SiteTest.class.getClassLoader().getResourceAsStream("sitesResponse1.xml"));
	Site site = srd1.getSites().get(0);
	String endpoint = site.getSeriesCatalogWSDL();
	assertEquals("A", endpoint);
	site.setSeriesCatalogWSDL("B");
	endpoint = site.getSeriesCatalogWSDL();
	assertEquals("B", endpoint);
	System.out.println(srd1.getReader().asString());
    }

    @Test
    public void testEmpty() throws Exception {
	SitesResponseDocument srd1 = new SitesResponseDocument(SiteTest.class.getClassLoader().getResourceAsStream("sitesResponse2.xml"));
	Site site = srd1.getSites().get(0);
	String endpoint = site.getSeriesCatalogWSDL();
	assertEquals("", endpoint);
	site.setSeriesCatalogWSDL("B");
	endpoint = site.getSeriesCatalogWSDL();
	assertEquals("B", endpoint);
	System.out.println(srd1.getReader().asString());
    }

    @Test
    public void testAddSeries() throws Exception {
	SitesResponseDocument srd1 = new SitesResponseDocument(SiteTest.class.getClassLoader().getResourceAsStream("sitesResponse3.xml"));

	Site site = srd1.getSites().get(0);
	List<TimeSeries> seriess = site.getSeries();
	assertEquals(41, seriess.size());
	TimeSeries series = seriess.get(0);
	assertEquals("Battery voltage", series.getVariableName());
	site.clearSeries();
	seriess = site.getSeries();
	assertEquals(0, seriess.size());
	site.addSeries(series);
	seriess = site.getSeries();
	assertEquals(1, seriess.size());
	series = seriess.get(0);
	assertEquals("Battery voltage", series.getVariableName());
    }
}
