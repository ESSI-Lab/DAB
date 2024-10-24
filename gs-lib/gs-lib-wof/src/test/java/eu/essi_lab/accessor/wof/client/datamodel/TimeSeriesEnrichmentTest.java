package eu.essi_lab.accessor.wof.client.datamodel;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class TimeSeriesEnrichmentTest {

    @Test
    public void test() throws Exception {
	InputStream seriesStream = TimeSeriesEnrichmentTest.class.getClassLoader().getResourceAsStream("enricher-series.xml");
	TimeSeries series = new TimeSeries(new XMLDocumentReader(seriesStream).getDocument().getDocumentElement());
	InputStream siteStream = TimeSeriesEnrichmentTest.class.getClassLoader().getResourceAsStream("enricher-site.xml");
	SiteInfo site = new SiteInfo(new XMLDocumentReader(siteStream).getDocument().getDocumentElement());
	String siteCode = site.getSiteCode();
	series = series.enrichWithSiteInfo(site);
	assertEquals(siteCode, series.getSiteInfo().getSiteCode());
    }
}
