package eu.essi_lab.downloader.wcs.test.sedac;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_100;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_Sedac100ExternalTestIT extends WCSDownloader_SedacTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_100();
    }

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
	return SEDAC_WCS100_DOWNLOAD_URL;
    }
}
