package eu.essi_lab.accessor.wfs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wfs._1_1_0.WFS_1_1_0Connector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class WFS_1_1_0ConnectorTest {

    private WFS_1_1_0Connector connector = new WFS_1_1_0Connector();

    @Test
    public void supportTest1() throws IOException {
	InputStream cap1 = WFS_1_1_0ConnectorTest.class.getClassLoader().getResourceAsStream("geoss-132-sedac-capabilities.xml");
	Downloader downloader = new MockedDownloader(IOUtils.readStringFromStream(cap1));
	this.connector.setDownloader(downloader);
	GSSource source = new GSSource();
	source.setEndpoint("http://correct-wfs-110");
	Assert.assertTrue(connector.supports(source));
    }

    @Test
    public void supportTest2() throws IOException {
	InputStream cap1 = WFS_1_1_0ConnectorTest.class.getClassLoader().getResourceAsStream("google.html");
	Downloader downloader = new MockedDownloader(IOUtils.readStringFromStream(cap1));
	this.connector.setDownloader(downloader);
	GSSource source = new GSSource();
	source.setEndpoint("http://google");
	Assert.assertFalse(connector.supports(source));
    }

    @Test
    public void testListRecords() throws Exception {
	InputStream cap1 = WFS_1_1_0ConnectorTest.class.getClassLoader().getResourceAsStream("geoss-132-sedac-capabilities.xml");
	Downloader downloader = new MockedDownloader(IOUtils.readStringFromStream(cap1));
	this.connector.setDownloader(downloader);
	this.connector.setSourceURL("http://localhost/wfs");
	List<String> formats = this.connector.listMetadataFormats();
	Assert.assertTrue(!formats.isEmpty());
	Assert.assertTrue(formats.contains(CommonNameSpaceContext.WFS_1_1_0_NS_URI));
	ListRecordsRequest listRecords = new ListRecordsRequest();
	listRecords.setResumptionToken(null);
	ListRecordsResponse<OriginalMetadata> response = this.connector.listRecords(listRecords);
	String token = response.getResumptionToken();
	Assert.assertEquals("1", token);
	Iterator<OriginalMetadata> iterator = response.getRecords();
	List<OriginalMetadata> list = Lists.newArrayList(iterator);
	assertEquals(1, list.size());
    }

}
