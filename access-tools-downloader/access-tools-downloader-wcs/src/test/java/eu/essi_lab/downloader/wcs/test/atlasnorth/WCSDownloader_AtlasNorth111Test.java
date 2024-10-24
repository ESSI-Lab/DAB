package eu.essi_lab.downloader.wcs.test.atlasnorth;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_AtlasNorth111Test extends WCSDownloader_AtlasNorth111ExternalTestIT implements WCSMockedDownloader {

    public SimpleEntry<String, String> getCoverageUrlFile() {
	return new SimpleEntry<String,String>(ATLAS_NORTH_WCS_111_DOWNLOAD_URL,"atlas-north-wcs111-coverage.tif");
    }

    public String getCapabilitiesFile() {
	return "atlas-north-wcs111-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "atlas-north-wcs111-coverage-description.xml";
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

    @Test
    public void test() throws Exception {
	super.test();
    }

}
