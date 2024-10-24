package eu.essi_lab.downloader.wcs.test.eox;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_201;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_EOX201ExternalTestIT extends WCSDownloader_EOXTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_201();
    }

    @Ignore("The url should be valid, but doesn't work")
    @Test
    public void test() throws Exception {
	super.test();

    }

    @Override
    public NetProtocol getProtocol() {
	return NetProtocols.WCS_2_0_1;
    }

    @Override
    public String getDownloadURL() {
	return EOX_WCS_201_DOWNLOAD_URL;
    }

}
