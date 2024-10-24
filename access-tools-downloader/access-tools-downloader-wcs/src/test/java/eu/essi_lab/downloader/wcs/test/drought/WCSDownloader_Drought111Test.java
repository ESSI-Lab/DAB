package eu.essi_lab.downloader.wcs.test.drought;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_Drought111Test extends WCSDownloader_Drought111ExternalTestIT implements WCSMockedDownloader {

    public SimpleEntry<String, String> getCoverageUrlFile() {
	return new SimpleEntry<String, String>(DROUGHT_WCS_111_DOWNLOAD_URL, "drought-wcs111-coverage.tif");
    }

    public String getCapabilitiesFile() {
	return "drought-wcs111-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "drought-wcs111-coverage-description.xml";
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

    @Ignore("The WCS 1.1.1 coverage description doesn't contain any supported CRS: Use WCS version 1.0.0 for this service")
    @Test
    public void test() throws Exception {
	super.test();
    }

}
