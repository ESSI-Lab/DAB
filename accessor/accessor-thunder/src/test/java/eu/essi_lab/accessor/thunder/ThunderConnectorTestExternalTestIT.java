package eu.essi_lab.accessor.thunder;

import java.io.File;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class ThunderConnectorTestExternalTestIT {

    private ThunderConnector connector;
    private GSSource source;

    /**
     * @return
     */
    private static String getPassword() {

	return System.getProperty("thunder.password");
    }

    /**
     * @return
     */
    private static String getUser() {

	return System.getProperty("thunder.user");

    }

    /**
     * @return
     */
    private static String getHost() {

	return System.getProperty("thunder.host");

    }

    @Before
    public void init() {

	this.connector = new ThunderConnector();
	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testFTPService() throws Exception {

	String endpoint = "ftp://"+getUser()+":"+getPassword()+"@"+getHost();

	FTPDownloader downloader = new FTPDownloader();

	boolean check = downloader.checkConnection(endpoint);

	Assert.assertTrue(check);

    }

    @Test
    public void testFTPMetadataFileExist() throws Exception {

	String endpoint = "ftp://"+getUser()+":"+getPassword()+"@"+getHost();

	String metadataFileName = "isd-history.csv";

	FTPDownloader downloader = new FTPDownloader();

	File file = downloader.downloadStream(endpoint, metadataFileName);

	Optional<File> res = Optional.of(file);

	Assert.assertTrue(res.isPresent());

	Assert.assertTrue(file.exists());

    }

    @Test
    public void testFTPDirectoryExist() throws Exception {

	FTPClient ftpClient = new FTPClient();

	ftpClient.connect(getHost(), 21);

	ftpClient.login(getUser(), getPassword());

	// use local passive mode to pass firewall
	ftpClient.enterLocalPassiveMode();

	System.out.println("Connected");

	FTPFile[] subFiles = ftpClient.listFiles("/thunder_data_GSOD");

	Assert.assertTrue(subFiles.length > 0);

    }

    @Test
    public void testFTPDataFileExist() throws Exception {

	FTPClient ftpClient = new FTPClient();

	ftpClient.connect(getHost(), 21);

	ftpClient.login(getUser(), getPassword());

	// use local passive mode to pass firewall
	ftpClient.enterLocalPassiveMode();

	System.out.println("Connected");

	FTPFile[] subFiles = ftpClient.listFiles("/thunder_data_GSOD");
	// ftp://18.18.83.11/thunder_data_GSOD/007026.txt

	String name = subFiles[0].getName();

	String endpoint = "ftp://"+getUser()+":"+getPassword()+"@"+getHost();
	FTPDownloader downloader = new FTPDownloader();
	File res = downloader.downloadStream(endpoint, "/thunder_data_GSOD/" + name);

	Assert.assertTrue(res.exists());
    }

    @Test
    public void testFTPResumptionToken() throws Exception {

	Mockito.when(source.getEndpoint()).thenReturn("ftp://"+getHost());
	connector.setSourceURL("ftp://"+getHost());

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	// connector.setMaxRecords(5);
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	// Assert.assertEquals(secondId, response.getResumptionToken());

	listRecords.setResumptionToken("26465");
	response = connector.listRecords(listRecords);
	String lastId = "26466";
	Assert.assertEquals(lastId, response.getResumptionToken());
	listRecords.setResumptionToken(lastId);
	response = connector.listRecords(listRecords);
	Assert.assertNull(response.getResumptionToken());

    }

}
