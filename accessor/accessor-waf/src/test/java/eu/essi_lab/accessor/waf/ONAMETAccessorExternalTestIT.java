package eu.essi_lab.accessor.waf;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.waf.onamet.ONAMETAccessor;
import eu.essi_lab.accessor.waf.onamet.ONAMETConnector;
import eu.essi_lab.accessor.waf.onamet.ONAMETConnectorSetting;
import eu.essi_lab.accessor.waf.onamet.ONAMETMapper;
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
public class ONAMETAccessorExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	ONAMETAccessor accessor = new ONAMETAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("ONAMET", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(ONAMETConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://186.149.199.244/ftp/");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	ONAMETConnector connector = accessor.getConnector();

	Assert.assertEquals(ONAMETConnector.class, connector.getClass());

	ONAMETConnectorSetting onametConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, onametConnectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(ONAMETMapper.ONAMET_METADATA_SCHEMA, listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	ONAMETAccessor accessor = new ONAMETAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("ONAMET", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(ONAMETConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint("http://186.149.199.244/ftp/");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	ONAMETConnector connector = accessor.getConnector();

	Assert.assertEquals(ONAMETConnector.class, connector.getClass());

	ONAMETConnectorSetting onametConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, onametConnectorSetting);

	//
	//
	//
	
	ONAMETConnector.closeTarGz = false; // to speed up the test

	onametConnectorSetting.setMaxRecords(1); // max 1 records
	onametConnectorSetting.setMaxProcessedEntries(5); // max 5 netcdf per gzip or per folder

	String accessKey = System.getProperty("accessKey");
	String secretKey = System.getProperty("secretKey");

	onametConnectorSetting.setS3BucketName("thredds-data/onamet-ext-test");
	onametConnectorSetting.setS3AccessKey(accessKey);
	onametConnectorSetting.setS3SecretKey(secretKey);

	//
	//
	//

	ListRecordsRequest listRecordsRequest = new ListRecordsRequest();

	ListRecordsResponse<GSResource> listRecordsResponse = accessor.listRecords(listRecordsRequest);

	List<GSResource> recordsAsList = listRecordsResponse.getRecordsAsList();

	Assert.assertEquals(1, recordsAsList.size());

	GSResource gsResource = recordsAsList.get(0);

	String title = gsResource.getHarmonizedMetadata().getCoreMetadata().getTitle();

	Assert.assertTrue(title.contains("OUTPUT FROM WRF V3.8.1 MODEL"));
    }
}
