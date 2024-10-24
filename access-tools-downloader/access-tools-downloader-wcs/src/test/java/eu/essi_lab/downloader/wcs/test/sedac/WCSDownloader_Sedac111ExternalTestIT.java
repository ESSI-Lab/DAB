package eu.essi_lab.downloader.wcs.test.sedac;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_Sedac111ExternalTestIT extends WCSDownloader_SedacTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_111();
    }

    @Ignore("the url should be correct, but download doesn't work. error: CRS urn:ogc:def:crs:EPSG::4326 is not among the\n"
	    + "	    // supported ones for coverage wildareas-v3-1993-human-footprint")
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
	return SEDAC_WCS111_DOWNLOAD_URL;
    }
}
