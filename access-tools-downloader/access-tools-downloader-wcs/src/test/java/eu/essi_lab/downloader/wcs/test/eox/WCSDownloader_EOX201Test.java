package eu.essi_lab.downloader.wcs.test.eox;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_EOX201Test extends WCSDownloader_EOX201ExternalTestIT implements WCSMockedDownloader {

    @Override
    public SimpleEntry<String, String> getCoverageUrlFile() {
	return null;
    }

    public String getCapabilitiesFile() {
	return "eox-wcs201-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "eox-wcs201-coverage-description.xml";
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

    @Ignore("The url should be valid, but doesn't work")
    @Test
    public void test() throws Exception {
	super.test();
    }

}
