package eu.essi_lab.accessor.wfs;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.wfs._1_1_0.WFS_1_1_0Connector;
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
public class WFS_1_1_0Accessor_ExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	WFSAccessor accessor = new WFSAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(WFSAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(WFSConnectorWrapperSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://afromaison.grid.unep.ch:8080/geoserver/ows?");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	WFSConnectorWrapper connector = accessor.getConnector();

	Assert.assertEquals(WFSConnectorWrapper.class, connector.getClass());

	WFSConnectorWrapperSetting connectorSetting = (WFSConnectorWrapperSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	// default a connector
	//
	WFS_1_1_0Connector selectedConnector = (WFS_1_1_0Connector) connectorSetting.getSelectedConnector();

	Assert.assertEquals(WFS_1_1_0Connector.class, selectedConnector.getClass());

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(CommonNameSpaceContext.WFS_1_1_0_NS_URI, listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	WFSAccessor accessor = new WFSAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(WFSAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(WFSConnectorWrapperSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://afromaison.grid.unep.ch:8080/geoserver/ows?");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	WFSConnectorWrapper connector = accessor.getConnector();

	Assert.assertEquals(WFSConnectorWrapper.class, connector.getClass());

	WFSConnectorWrapperSetting connectorSetting = (WFSConnectorWrapperSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	// default a connector
	//
	WFS_1_1_0Connector selectedConnector = (WFS_1_1_0Connector) connectorSetting.getSelectedConnector();

	Assert.assertEquals(WFS_1_1_0Connector.class, selectedConnector.getClass());

	// no need to set a limit, this connector retrieves a single record per time
	// connectorSetting.setMaxRecords(100); // max 100 records

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	String resumptionToken = listRecordsResponse.getResumptionToken();
	Assert.assertEquals("1", resumptionToken);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(1, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	OriginalMetadata originalMetadata = gsResource.getOriginalMetadata();
	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals(CommonNameSpaceContext.WFS_1_1_0_NS_URI, schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	Assert.assertTrue(metadata.contains("WFS_Capabilities"));
    }
}
