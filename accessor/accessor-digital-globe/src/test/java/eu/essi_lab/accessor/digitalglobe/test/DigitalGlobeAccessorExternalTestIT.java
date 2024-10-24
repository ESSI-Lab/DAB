package eu.essi_lab.accessor.digitalglobe.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.accessor.digitalglobe.DigitalGlobeAccessor;
import eu.essi_lab.accessor.digitalglobe.DigitalGlobeConnector;
import eu.essi_lab.accessor.digitalglobe.DigitalGlobeConnectorSetting;
import eu.essi_lab.accessor.digitalglobe.DigitalGlobeResourceMapper;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class DigitalGlobeAccessorExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	DigitalGlobeAccessor accessor = new DigitalGlobeAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals(DigitalGlobeAccessor.TYPE, accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(DigitalGlobeConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("https://partner.digitalglobe.com");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	DigitalGlobeConnector connector = accessor.getConnector();

	Assert.assertEquals(DigitalGlobeConnector.class, connector.getClass());

	DigitalGlobeConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(DigitalGlobeResourceMapper.DIGITAL_GLOBE_SCHEME_URI, listMetadataFormats.get(0));
    }

    /**
     * Test ignored, source endpoint seems to be broken
     * 
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    @Ignore
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	DigitalGlobeAccessor accessor = new DigitalGlobeAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("DigitalGlobeAccessor", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(DigitalGlobeConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("https://partner.digitalglobe.com");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	DigitalGlobeConnector connector = accessor.getConnector();

	Assert.assertEquals(DigitalGlobeConnector.class, connector.getClass());

	DigitalGlobeConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	oaiConnectorSetting.setMaxRecords(50); // max 50 records

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(50, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

    }
}
