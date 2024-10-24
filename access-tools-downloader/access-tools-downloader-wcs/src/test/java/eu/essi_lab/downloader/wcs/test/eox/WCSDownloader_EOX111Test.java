package eu.essi_lab.downloader.wcs.test.eox;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_EOX111Test extends WCSDownloader_EOX111ExternalTestIT implements WCSMockedDownloader {

    public SimpleEntry<String, String> getCoverageUrlFile() {
	return new SimpleEntry<String,String>(EOX_WCS_111_DOWNLOAD_URL,"eox-wcs111-coverage.tif");
    }

    public String getCapabilitiesFile() {
	return "eox-wcs111-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "eox-wcs111-coverage-description.xml";
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
