package eu.essi_lab.downloader.wcs.test.drought;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_Drought111ExternalTestIT extends WCSDownloader_DroughtTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_111();
    }

    @Ignore("The WCS 1.1.1 coverage description doesn't contain any supported CRS: Use WCS version 1.0.0 for this service")
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
	return DROUGHT_WCS_111_DOWNLOAD_URL;
    }
}
