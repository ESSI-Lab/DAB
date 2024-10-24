package eu.essi_lab.accessor.sos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.accessor.sos.tahmo.SOSTAHMOConnector;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.OriginalMetadata;

// @Ignore
public class SOSConnectorExternalTestIT {

    private SOSConnector connector;

    private SOSTAHMOConnector tahmoConnector;

    @Before
    public void init() {
	this.connector = new SOSConnector();
	this.tahmoConnector = new SOSTAHMOConnector();
    }

    @Test
    public void testSupport() {

	GSSource source = new GSSource();
	source.setEndpoint("http://kiwis.kisters.de/KiWIS/KiWIS?");
	boolean support = connector.supports(source);
	assertTrue(support);
    }

    @Test
    public void testSupport2() {
	GSSource source = new GSSource();
	source.setEndpoint("http://www.google.com");
	boolean support = connector.supports(source);
	assertFalse(support);
    }

    // @Test
    // it seems that image is unavailable
    public void testImage() {
	String image = connector.resolveImage("http://bancodeimagenes.iecolab.es/picture.php?/305/category/14");
	System.out.println(image);
	assertEquals("http://bancodeimagenes.iecolab.es/_data/i/upload/2017/06/14/20170614103718-60a61409-me.jpg", image);
    }

    @Test
    public void testListRecords() throws Exception {
	connector.setSourceURL("http://kiwis.kisters.de/KiWIS/KiWIS?");
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	String metadata = response.getRecords().next().getMetadata();
	SOSProperties properties = new SOSProperties(metadata);
	Double.parseDouble(properties.getProperty(SOSProperty.LONGITUDE));
	Double.parseDouble(properties.getProperty(SOSProperty.LATITUDE));
	assertNotNull(properties.getProperty(SOSProperty.FOI_ID));
	assertNotNull(properties.getProperty(SOSProperty.FOI_NAME));
	assertNotNull(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID));
	assertNotNull(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_NAME));
	assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(properties.getProperty(SOSProperty.TEMP_EXTENT_BEGIN)).isPresent());
	assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(properties.getProperty(SOSProperty.TEMP_EXTENT_END)).isPresent());
	System.out.println(metadata);
    }

    @Test
    public void testTAHMOSOS() throws Exception {
	tahmoConnector.setSourceURL("http://hnapi.hydronet.com/api/service/sos?");
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = tahmoConnector.listRecords(request);
	String metadata = response.getRecords().next().getMetadata();
	SOSProperties properties = new SOSProperties(metadata);
	Double.parseDouble(properties.getProperty(SOSProperty.LONGITUDE));
	Double.parseDouble(properties.getProperty(SOSProperty.LATITUDE));
	assertNotNull(properties.getProperty(SOSProperty.FOI_ID));
	assertNotNull(properties.getProperty(SOSProperty.FOI_NAME));
	assertNotNull(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID));
	assertNotNull(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_NAME));
	assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(properties.getProperty(SOSProperty.TEMP_EXTENT_BEGIN)).isPresent());
	assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(properties.getProperty(SOSProperty.TEMP_EXTENT_END)).isPresent());
	System.out.println(metadata);
    }

}
