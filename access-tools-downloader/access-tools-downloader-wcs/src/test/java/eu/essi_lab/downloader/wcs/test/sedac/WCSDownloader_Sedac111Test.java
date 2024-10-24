package eu.essi_lab.downloader.wcs.test.sedac;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_Sedac111Test extends WCSDownloader_Sedac111ExternalTestIT implements WCSMockedDownloader {

    @Override
    public SimpleEntry<String, String> getCoverageUrlFile() {
	return null;
    }

    public String getCapabilitiesFile() {
	return "sedac-wcs111-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "sedac-wcs111-coverage-description.xml";
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

    @Ignore("the url should be correct, but download doesn't work. error: CRS urn:ogc:def:crs:EPSG::4326 is not among the\n"
	    + "	    // supported ones for coverage wildareas-v3-1993-human-footprint")
    @Test
    public void test() throws Exception {
	super.test();
    }

}
