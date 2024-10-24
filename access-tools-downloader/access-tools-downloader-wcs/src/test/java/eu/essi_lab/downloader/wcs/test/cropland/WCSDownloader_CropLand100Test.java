package eu.essi_lab.downloader.wcs.test.cropland;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;

public class WCSDownloader_CropLand100Test extends WCSDownloader_CropLand100ExternalTestIT implements WCSMockedDownloader {

    public SimpleEntry<String, String> getCoverageUrlFile() {
	return new SimpleEntry<String, String>(WCSDownloader_CropLand100Test.CROP_LAND_WCS_100_DOWNLOAD_URL, "crop-land-wcs100-coverage.tif");
    }

    public String getCapabilitiesFile() {
	return "crop-land-wcs100-capabilities.xml";
    }

    public String getCoverageDescription() {
	return "crop-land-wcs100-coverage-description.xml";
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

    @Ignore("The service gives internal server error on download requests now... check in the future")
    @Test
    public void test() throws Exception {
	super.test();
    }

}
