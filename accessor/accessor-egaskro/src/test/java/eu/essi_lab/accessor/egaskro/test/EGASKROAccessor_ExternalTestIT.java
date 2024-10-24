package eu.essi_lab.accessor.egaskro.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.egaskro.EGASKROAccessor;
import eu.essi_lab.accessor.egaskro.EGASKROConnector;
import eu.essi_lab.accessor.egaskro.EGASKROConnectorSetting;
import eu.essi_lab.accessor.egaskro.EGASKROResourceMapper;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
public class EGASKROAccessor_ExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	EGASKROAccessor accessor = new EGASKROAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(EGASKROAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(EGASKROConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://www.feerc.ru/geoss/egaskro");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	EGASKROConnector connector = accessor.getConnector();

	Assert.assertEquals(EGASKROConnector.class, connector.getClass());

	EGASKROConnectorSetting connectorSetting = (EGASKROConnectorSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(EGASKROResourceMapper.EGASKRO_SCHEME_URI, listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	EGASKROAccessor accessor = new EGASKROAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(EGASKROAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//
	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(EGASKROConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://www.feerc.ru/geoss/egaskro");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	EGASKROConnector connector = accessor.getConnector();

	Assert.assertEquals(EGASKROConnector.class, connector.getClass());

	EGASKROConnectorSetting connectorSetting = (EGASKROConnectorSetting) connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, connectorSetting);

	connectorSetting.setMaxRecords(10); // max 10 records

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	String resumptionToken = listRecordsResponse.getResumptionToken();
	Assert.assertNull(resumptionToken);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();
	
	GSLoggerFactory.getLogger(getClass()).debug("Found {} records", recordsAsList.size());

	Assert.assertTrue(recordsAsList.size() >= 10);

	GSResource gsResource = recordsAsList.get(0);

	OriginalMetadata originalMetadata = gsResource.getOriginalMetadata();
	String schemeURI = originalMetadata.getSchemeURI();
	Assert.assertEquals(EGASKROResourceMapper.EGASKRO_SCHEME_URI, schemeURI);

	String metadata = gsResource.getOriginalMetadata().getMetadata();
	
 	JSONObject jsonObject = new JSONObject(metadata);
 	
 	Assert.assertTrue(jsonObject.has("id"));
    }
}
