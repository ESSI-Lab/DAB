package eu.essi_lab.accessor.thredds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class THREDDSCrawlerExternalTestIT {

    @Test
    public void test() throws Exception {
	THREDDSCrawler crawler = new THREDDSCrawler("https://data.nodc.noaa.gov/thredds/catalog/ncei/wod/catalog.xml");
	THREDDSPage mainPage = crawler.crawl(new THREDDSReference());
	List<THREDDSReference> references = mainPage.getReferences();
	assertTrue(references.size() > 20);
	THREDDSReference ref3 = references.get(3);
	THREDDSPage page1902 = crawler.crawl(ref3);
	assertEquals("https://data.nodc.noaa.gov/thredds/catalog/ncei/wod/1902/catalog.xml", page1902.getURL().toExternalForm());
	List<THREDDSReference> childReferences = page1902.getReferences();
	assertTrue(childReferences.isEmpty());
	List<THREDDSDataset> datasets = page1902.getDatasets();
	assertEquals(1, datasets.size());
	THREDDSDataset dataset = datasets.get(0);
	assertEquals("https://data.nodc.noaa.gov/thredds-ocean/iso/ncei/wod/1902/wod_osd_1902.nc", dataset.getServices().get("ISO").toExternalForm());
    }
    

    
    
    

}
