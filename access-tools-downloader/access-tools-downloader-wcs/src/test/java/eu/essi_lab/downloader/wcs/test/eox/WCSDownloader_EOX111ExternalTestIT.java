package eu.essi_lab.downloader.wcs.test.eox;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_EOX111ExternalTestIT extends WCSDownloader_EOXTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_111();
    }

    @Test
    public void test() throws Exception {
	super.test();

    }

    @Override
    public NetProtocol getProtocol() {
	return NetProtocols.WCS_1_1_1;
    }

    @Override
    public String getDownloadURL() {
	return EOX_WCS_111_DOWNLOAD_URL;
    }
}
