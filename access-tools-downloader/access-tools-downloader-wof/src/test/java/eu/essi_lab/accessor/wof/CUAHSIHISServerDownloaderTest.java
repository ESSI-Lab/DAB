package eu.essi_lab.accessor.wof;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.FakeHISServerClient;
import eu.essi_lab.model.exceptions.GSException;

public class CUAHSIHISServerDownloaderTest extends CUAHSIHISServerDownloaderExternalTestIT {
    @Override
    @Before
    public void init() throws GSException, UnsupportedEncodingException, JAXBException {
	super.init();
	this.downloader = Mockito.spy(this.downloader);
	CUAHSIHISServerClient1_1 client = new FakeHISServerClient(getEndpoint());
	Mockito.doReturn(client).when(downloader).getConnector();
    }
    
    @Test
    public void testDownloader() throws Exception {
	super.testDownloader();
    }
}
