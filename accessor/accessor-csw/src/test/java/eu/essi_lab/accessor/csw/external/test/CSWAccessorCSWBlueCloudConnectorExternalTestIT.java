package eu.essi_lab.accessor.csw.external.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.csw.CSWAccessor;
import eu.essi_lab.accessor.csw.CSWBLUECLOUDConnector;
import eu.essi_lab.accessor.csw.CSWConnectorWrapper;
import eu.essi_lab.accessor.csw.CSWConnectorWrapperSetting;
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
public class CSWAccessorCSWBlueCloudConnectorExternalTestIT {

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	CSWAccessor accessor = new CSWAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(CSWAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(CSWConnectorWrapperSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("https://sextant.ifremer.fr/geonetwork/srv/eng/csw-SEADATANET?");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	CSWConnectorWrapper connector = accessor.getConnector();

	Assert.assertEquals(CSWConnectorWrapper.class, connector.getClass());

	CSWConnectorWrapperSetting connectorSetting = (CSWConnectorWrapperSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	// select a connector
	//
	connectorSetting.selectConnectorType(CSWBLUECLOUDConnector.TYPE);

	CSWBLUECLOUDConnector selectedConnector = (CSWBLUECLOUDConnector) connectorSetting.getSelectedConnector();

	Assert.assertEquals(CSWBLUECLOUDConnector.class, selectedConnector.getClass());

	//
	//
	//

	connectorSetting.setMaxRecords(50); // max 50 records
	connectorSetting.setPageSize(50); // page size 50

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	String resumptionToken = listRecordsResponse.getResumptionToken();
	Assert.assertNull(resumptionToken);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(50, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	OriginalMetadata originalMetadata = gsResource.getOriginalMetadata();
	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals(CommonNameSpaceContext.BLUECLOUD_NS_URI, schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	Assert.assertTrue(metadata.contains("gmd:MD_Metadata"));
    }
}
