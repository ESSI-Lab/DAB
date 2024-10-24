package eu.essi_lab.accessor.wcs.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.wcs.WCSAccessor;
import eu.essi_lab.accessor.wcs.WCSConnectorWrapper;
import eu.essi_lab.accessor.wcs.WCSConnectorWrapperSetting;
import eu.essi_lab.accessor.wcs_1_1_1.WCSConnector_111;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
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
public class WCSAccessor_111_ExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	WCSAccessor accessor = new WCSAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(WCSAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(WCSConnectorWrapperSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://nsidc.org/cgi-bin/atlas_north?");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	WCSConnectorWrapper connector = accessor.getConnector();

	Assert.assertEquals(WCSConnectorWrapper.class, connector.getClass());

	WCSConnectorWrapperSetting connectorSetting = (WCSConnectorWrapperSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	// selects the connector
	//
	connectorSetting.selectConnectorType("WCS Connector 1.1.1");

	WCSConnector_111 selectedConnector = (WCSConnector_111) connectorSetting.getSelectedConnector();

	Assert.assertEquals(WCSConnector_111.class, selectedConnector.getClass());

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals("WCS_SCHEME_WCSConnector_111", listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	WCSAccessor accessor = new WCSAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(WCSAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(WCSConnectorWrapperSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://nsidc.org/cgi-bin/atlas_north?");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	WCSConnectorWrapper connector = accessor.getConnector();

	Assert.assertEquals(WCSConnectorWrapper.class, connector.getClass());

	WCSConnectorWrapperSetting connectorSetting = (WCSConnectorWrapperSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	// selects the connector
	//
	connectorSetting.selectConnectorType("WCS Connector 1.1.1");

	WCSConnector_111 selectedConnector = (WCSConnector_111) connectorSetting.getSelectedConnector();

	Assert.assertEquals(WCSConnector_111.class, selectedConnector.getClass());

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
	Assert.assertEquals("WCS_SCHEME_WCSConnector_111", schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	Assert.assertTrue(metadata.contains("CoverageDescription"));
    }
}
