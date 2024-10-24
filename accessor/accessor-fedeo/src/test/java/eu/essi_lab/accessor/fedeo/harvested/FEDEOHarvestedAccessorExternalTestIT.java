package eu.essi_lab.accessor.fedeo.harvested;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.lib.xml.XMLDocumentReader;
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
public class FEDEOHarvestedAccessorExternalTestIT {

    boolean passed = true;

    /**
     * The connector method getRecordsCount() do not finds the required "//*:totalResults" element
     * 
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	FEDEOMixedHarvestedAccessor accessor = new FEDEOMixedHarvestedAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.MIXED, accessorSetting.getBrokeringStrategy());

	//
	// The connector setting has already the source endpoint set
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(FEDEOCollectionConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	Assert.assertNotNull(gsSourceSetting.getSourceEndpoint());
	Assert.assertNotNull(gsSourceSetting.getSourceLabel());

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	FEDEOCollectionConnector connector = accessor.getConnector();

	Assert.assertEquals(FEDEOCollectionConnector.class, connector.getClass());

	FEDEOCollectionConnectorSetting oaiConnectorSetting = connector.getSetting();

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

	recordsAsList.forEach(r -> {

	    OriginalMetadata originalMetadata = r.getOriginalMetadata();

	    String metadata = originalMetadata.getMetadata();

	    try {

		XMLDocumentReader reader = new XMLDocumentReader(metadata);

		passed &= reader.asString().contains("entry");

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});

	Assert.assertTrue(passed);
    }

}
