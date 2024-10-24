package eu.essi_lab.accessor.wms;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0Connector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.model.GSSource;

public class WMSConnectorTest {

    private WMS_1_3_0Connector connector;

    @Before
    public void init() {
	this.connector = new WMS_1_3_0Connector();

    }

    @Test
    public void supportTest1() throws IOException {
	InputStream cap1 = WMSConnectorTest.class.getClassLoader().getResourceAsStream("test-capabilities-130.xml");
	Downloader downloader = new MockedDownloader(IOUtils.readStringFromStream(cap1));
	this.connector.setDownloader(downloader);
	GSSource source = new GSSource();
	source.setEndpoint("http://correct-wms-130");
	Assert.assertTrue(connector.supports(source));
    }

    @Test
    public void supportTest2() throws IOException {
	InputStream cap1 = WMSConnectorTest.class.getClassLoader().getResourceAsStream("google.html");
	Downloader downloader = new MockedDownloader(IOUtils.readStringFromStream(cap1));
	this.connector.setDownloader(downloader);
	GSSource source = new GSSource();
	source.setEndpoint("http://google");
	Assert.assertFalse(connector.supports(source));
    }
}
