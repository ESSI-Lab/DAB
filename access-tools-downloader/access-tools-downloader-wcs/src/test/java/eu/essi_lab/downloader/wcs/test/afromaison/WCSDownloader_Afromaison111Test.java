package eu.essi_lab.downloader.wcs.test.afromaison;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_Afromaison111Test extends WCSDownloader_Afromaison111ExternalTestIT implements WCSMockedDownloader {

    @Override
    public SimpleEntry<String, String> getCoverageUrlFile() {
	return null;
    }

    public String getCapabilitiesFile() {
	return "afromaison-wcs111-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "afromaison-wcs111-coverage-description.xml";
    }

    @Override
    public boolean isMockedDownload() {
	return true;
    }

    /**
     * Initializes with a mocked connector reading the responses from the file system instead of making actual WCS
     * requests
     */
    @Before
    public void init() {
	initMockedDownloader(this);
    }

    @Ignore("Unable to meaningful download something from this WCS 1.1.1 as the size is missing and the bounding box is not aligned with the specified resolution. "
	    + "Moreover trying to download something it will produce GeoTiff with inverted latitude and longitude. It is preferred to use WCS 1.0.0 for this service.")
    @Test
    public void test() throws Exception {
	super.test();
    }

}
