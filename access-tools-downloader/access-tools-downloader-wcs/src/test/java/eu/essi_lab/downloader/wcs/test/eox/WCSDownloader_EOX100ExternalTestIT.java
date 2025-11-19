package eu.essi_lab.downloader.wcs.test.eox;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_100;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;

public class WCSDownloader_EOX100ExternalTestIT extends WCSDownloader_EOXTest {

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
	return NetProtocolWrapper.WCS_1_0_0.get();
    }

    @Override
    public String getDownloadURL() {
	return EOX_WCS_100_DOWNLOAD_URL;
    }

}
