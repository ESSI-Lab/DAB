package eu.essi_lab.accessor.localfs.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;

import eu.essi_lab.accessor.localfs.LocalFileSystemAccessor;
import eu.essi_lab.accessor.localfs.LocalFileSystemConnector;
import eu.essi_lab.accessor.localfs.LocalFileSystemConnectorSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class LocalFileSystemAccessorTestIT {

    /**
     * @throws GSException
     * @throws URISyntaxException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException, URISyntaxException {

	LocalFileSystemAccessor accessor = new LocalFileSystemAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(LocalFileSystemAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(LocalFileSystemConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceEndpoint("file://");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	LocalFileSystemConnector connector = accessor.getConnector();

	Assert.assertEquals(LocalFileSystemConnector.class, connector.getClass());

	LocalFileSystemConnectorSetting connectorSetting = (LocalFileSystemConnectorSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertTrue(listMetadataFormats.size() >= 5);
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void listRecordsTest() throws Exception {

	LocalFileSystemAccessor accessor = new LocalFileSystemAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(LocalFileSystemAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(LocalFileSystemConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "local-test");

	if (!tempDir.exists()) {

	    boolean mkdirs = tempDir.mkdirs();
	    if (!mkdirs) {

		throw new Exception("Unable to create temp test folder");
	    }
	}

	final int FILES_COUNT = 5;

	for (int i = 0; i < FILES_COUNT; i++) {

	    String fileContent = getTestRecord(UUID.randomUUID().toString());
	    ByteArrayInputStream stream = new ByteArrayInputStream(fileContent.getBytes(Charsets.UTF_8));

	    File file = new File(tempDir, "file-" + i);

	    Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	gsSourceSetting.setSourceEndpoint("file://" + tempDir.getAbsolutePath());

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	LocalFileSystemConnector connector = accessor.getConnector();

	Assert.assertEquals(LocalFileSystemConnector.class, connector.getClass());

	LocalFileSystemConnectorSetting connectorSetting = (LocalFileSystemConnectorSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	String resumptionToken = listRecordsResponse.getResumptionToken();
	Assert.assertNull(resumptionToken);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(FILES_COUNT, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	OriginalMetadata originalMetadata = gsResource.getOriginalMetadata();
	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals(CommonNameSpaceContext.CSW_NS_URI, schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	Assert.assertTrue(metadata.contains("csw:Record"));
    }

    /**
     * @param id
     * @return
     */
    private String getTestRecord(String id) {

	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<csw:Record xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" "
		+ "xmlns:ows=\"http://www.opengis.net/ows\" " + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
		+ "xmlns:dct=\"http://purl.org/dc/terms/\">" + "<dc:identifier>" + id + "</dc:identifier>"
		+ "<dc:type>http://purl.org/dc/dcmitype/Image</dc:type>" + "<dc:format>image/svg+xml</dc:format>"
		+ "<dc:title>Lorem ipsum</dc:title>" + "<dct:spatial>GR-22</dct:spatial>" + "<dc:subject>Tourism--Greece</dc:subject>"
		+ "<dct:abstract>Quisque lacus diam, placerat mollis, pharetra in, commodo sed, augue. Duis iaculis arcu vel arcu.</dct:abstract>"
		+ "</csw:Record>";

    }
}
