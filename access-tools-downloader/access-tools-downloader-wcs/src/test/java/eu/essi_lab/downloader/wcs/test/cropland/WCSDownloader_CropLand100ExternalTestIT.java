package eu.essi_lab.downloader.wcs.test.cropland;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_100;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_CropLand100ExternalTestIT extends WCSDownloader_CropLandTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_100();
    }

    @Ignore("The service gives internal server error on download requests now... check in the future")
    @Test
    public void test() throws Exception {
	super.test();

    }

    @Override
    public NetProtocol getProtocol() {
	return NetProtocols.WCS_1_0_0;
    }

    @Override
    public String getDownloadURL() {
	return CROP_LAND_WCS_100_DOWNLOAD_URL;
    }

}
