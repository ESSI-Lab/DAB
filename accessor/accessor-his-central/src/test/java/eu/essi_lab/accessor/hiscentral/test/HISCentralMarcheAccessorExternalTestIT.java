package eu.essi_lab.accessor.hiscentral.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheAccessor;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheConnector;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheConnectorSetting;
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

/**
 * @author Fabrizio
 */
public class HISCentralMarcheAccessorExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	HISCentralMarcheAccessor accessor = new HISCentralMarcheAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("SIR_MARCHE", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(HISCentralMarcheConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint(HISCentralMarcheConnector.BASE_URL);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	HISCentralMarcheConnector connector = accessor.getConnector();

	Assert.assertEquals(HISCentralMarcheConnector.class, connector.getClass());

	HISCentralMarcheConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI, listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	HISCentralMarcheAccessor accessor = new HISCentralMarcheAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("SIR_MARCHE", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(HISCentralMarcheConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint(HISCentralMarcheConnector.BASE_URL);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	HISCentralMarcheConnector connector = accessor.getConnector();

	Assert.assertEquals(HISCentralMarcheConnector.class, connector.getClass());

	HISCentralMarcheConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	oaiConnectorSetting.setMaxRecords(1); // max 1 records

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(1, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	JSONObject object = new JSONObject(metadata);

	Assert.assertTrue(object.has("dataset-info"));
	Assert.assertTrue(object.has("sensor-info"));
    }
}
