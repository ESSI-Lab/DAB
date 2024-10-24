package eu.essi_lab.accessor.localfs.test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.localfs.cswtestdata.CSWTestDataAccessor;
import eu.essi_lab.accessor.localfs.cswtestdata.CSWTestDataConnector;
import eu.essi_lab.accessor.localfs.cswtestdata.CSWTestDataConnectorSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.NameSpace;
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
public class CSWTestDataAccessorTestIT {

    /**
     * @throws GSException
     * @throws URISyntaxException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException, URISyntaxException {

	CSWTestDataAccessor accessor = new CSWTestDataAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(CSWTestDataAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(CSWTestDataConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	// String path = new
	// File(getClass().getClassLoader().getResource("csw-test-data/README.txt").toURI()).getParentFile().getPath();
	// gsSourceSetting.setSourceEndpoint("file://" + path);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	CSWTestDataConnector connector = accessor.getConnector();

	Assert.assertEquals(CSWTestDataConnector.class, connector.getClass());

	CSWTestDataConnectorSetting connectorSetting = (CSWTestDataConnectorSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(2, listMetadataFormats.size());

	Assert.assertEquals("OAI-DC", listMetadataFormats.get(0));
	Assert.assertEquals(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX, listMetadataFormats.get(1));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException, URISyntaxException {

	CSWTestDataAccessor accessor = new CSWTestDataAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(CSWTestDataAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(CSWTestDataConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceEndpoint("file://");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	CSWTestDataConnector connector = accessor.getConnector();

	Assert.assertEquals(CSWTestDataConnector.class, connector.getClass());

	CSWTestDataConnectorSetting connectorSetting = (CSWTestDataConnectorSetting) connector.getSetting();

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

	Assert.assertEquals(12, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	OriginalMetadata originalMetadata = gsResource.getOriginalMetadata();
	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals(CommonNameSpaceContext.CSW_NS_URI, schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	Assert.assertTrue(metadata.contains("csw:Record"));
    }
}
