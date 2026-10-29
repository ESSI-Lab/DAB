package eu.essi_lab.accessor.hrdhmz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class DHMZMapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(DHMZEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(DHMZMapper.DHMZ_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(DHMZClient.DEFAULT_ENDPOINT);

	return new DHMZMapper().map(originalMetadata, source);
    }

    @Test
    public void mapDailyTest() throws GSException {

	DHMZEntity series = DHMZClientTest.createSeries(DHMZClientTest.STATION_4).get(0);

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("BADLJEVINA UZV. - Water level (daily)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("137.140 m a.s.l."));
	assertTrue(coreMetadata.getAbstract().contains("river BIJELA"));
	assertTrue(coreMetadata.getAbstract().contains("1949-2025"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1949-01-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("2025-12-31T00:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("4|WATER_LEVEL", online.getName());
	assertEquals(NetProtocolWrapper.DHMZ.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Croatia", resource.getExtensionHandler().getCountry().get());
	assertEquals("P1D", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.DHMZ.getCommonURN() + ":WATER_LEVEL",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(NetProtocolWrapper.DHMZ.getCommonURN() + "/3171",
		coreMetadata.getMIMetadata().getMIPlatform().getMDIdentifierCode());
	assertEquals(45.5042777778, coreMetadata.getBoundingBox().getNorth(), 0.000001);
    }

    @Test
    public void mapPropertiesTest() throws GSException {

	DHMZEntity load = DHMZClientTest.createSeries(DHMZClientTest.STATION_4).get(4);

	GSResource resource = map(load);

	assertEquals("BADLJEVINA UZV. - Suspended sediment load (daily)", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	assertEquals("t", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertTrue(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeDescription()
		.contains("pronos suspendiranog nanosa"));
    }

    @Test
    public void mapRealTimeTest() throws GSException {

	DHMZEntity realTime = DHMZClientTest.createSeries(DHMZClientTest.STATION_4).get(5);
	// as if harvested now
	realTime.getObject().put(DHMZClient.END, eu.essi_lab.lib.utils.ISO8601DateTimeUtils.getISO8601DateTime());

	GSResource resource = map(realTime);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("BADLJEVINA UZV. - Water level (real time)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("not verified"));
	assertEquals("4|WATER_LEVEL|RT", coreMetadata.getOnline().getName());
	assertTrue(coreMetadata.getTemporalExtent().isEndPositionIndeterminate());
    }
}
