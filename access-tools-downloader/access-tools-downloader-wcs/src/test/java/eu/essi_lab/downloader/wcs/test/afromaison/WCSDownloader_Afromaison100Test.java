package eu.essi_lab.downloader.wcs.test.afromaison;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_Afromaison100Test extends WCSDownloader_Afromaison100ExternalTestIT implements WCSMockedDownloader {

    public SimpleEntry<String, String> getCoverageUrlFile() {
	return new SimpleEntry<String, String>(WCSDownloader_AfromaisonTest.AFROMAISON_WCS_100_DOWNLOAD_URL,
		"afromaison-wcs100-coverage.tif");
    }

    public String getCapabilitiesFile() {
	return "afromaison-wcs100-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "afromaison-wcs100-coverage-description.xml";
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