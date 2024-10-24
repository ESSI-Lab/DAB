package eu.essi_lab.accessor.oaipmh.test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.RecordType;
import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OAIPMHAccessorExternalTestIT {

    private static final int MAX_RECORDS = 10;

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	OAIPMHAccessor accessor = new OAIPMHAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("OAIPMH", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(OAIPMHConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://gs-service-production.geodab.eu/gs-service/services/oaipmh");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	OAIPMHConnector connector = accessor.getConnector();

	Assert.assertEquals(OAIPMHConnector.class, connector.getClass());

	OAIPMHConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(5, listMetadataFormats.size());

	Assert.assertEquals("ISO19139-2006", listMetadataFormats.get(0));
	Assert.assertEquals("ISO19139-2006-GMI", listMetadataFormats.get(1));
	Assert.assertEquals("ISO19139-2007", listMetadataFormats.get(2));
	Assert.assertEquals("WIGOS-1.0", listMetadataFormats.get(3));
	Assert.assertEquals("oai_dc", listMetadataFormats.get(4));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	OAIPMHAccessor accessor = new OAIPMHAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("OAIPMH", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(OAIPMHConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://gs-service-production.geodab.eu/gs-service/services/oaipmh");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	OAIPMHConnector connector = accessor.getConnector();

	Assert.assertEquals(OAIPMHConnector.class, connector.getClass());

	OAIPMHConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	oaiConnectorSetting.setMaxRecords(MAX_RECORDS); // max 10 records
	oaiConnectorSetting.setPreferredPrefix("oai_dc"); // oaidc as prefix

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(MAX_RECORDS, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	String metadata = gsResource.getOriginalMetadata().getMetadata();

	@SuppressWarnings("rawtypes")
	JAXBElement record = CommonContext.unmarshal(metadata, JAXBElement.class);

	eu.essi_lab.jaxb.csw._2_0_2.RecordType type = (RecordType) record.getValue();

	List<JAXBElement<SimpleLiteral>> dcElements = type.getDCElements();

	Assert.assertFalse(dcElements.isEmpty());
    }
}
