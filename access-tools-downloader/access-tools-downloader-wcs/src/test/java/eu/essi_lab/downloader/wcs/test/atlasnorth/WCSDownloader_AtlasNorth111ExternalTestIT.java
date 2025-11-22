package eu.essi_lab.downloader.wcs.test.atlasnorth;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;

public class WCSDownloader_AtlasNorth111ExternalTestIT extends WCSDownloader_AtlasNorthTest {

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
	return NetProtocolWrapper.WCS_1_1_1.get();
    }

    @Override
    public String getDownloadURL() {
	return ATLAS_NORTH_WCS_111_DOWNLOAD_URL;
    }
}
