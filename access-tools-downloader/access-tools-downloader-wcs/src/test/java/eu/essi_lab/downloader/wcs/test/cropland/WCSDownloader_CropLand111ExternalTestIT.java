package eu.essi_lab.downloader.wcs.test.cropland;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;

public class WCSDownloader_CropLand111ExternalTestIT extends WCSDownloader_CropLandTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_111();
    }

    @Ignore("The service gives internal server error on download requests now... check in the future")
    @Test
    public void test() throws Exception {
	super.test();

    }

    @Override
    public NetProtocol getProtocol() {
	return NetProtocolWrapper.WCS_1_1_1.get();
    }

    @Override
    public String getDownloadURL() {
	return CROP_LAND_WCS_111_DOWNLOAD_URL;
    }

}
