package eu.essi_lab.accessor.lvlvgmc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class LVGMCMapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(LVGMCEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(LVGMCMapper.LVGMC_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(LVGMCClient.DEFAULT_ENDPOINT);

	return new LVGMCMapper().map(originalMetadata, source);
    }

    @Test
    public void waterLevelTest() throws GSException {

	GSResource resource = map(LVGMCClientTest.createSeries(LVGMCClientTest.LEVEL));
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Jelgava - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().startsWith("Hourly time series of water level (LVGMC parameter LIMEN: Water level,"
		+ " real, hourly average / Ūdens līmenis, faktiskais, stundas vidējais, unit cm) at the hydrological station Jelgava"
		+ " (HD073801)"));
	assertTrue(coreMetadata.getAbstract().contains("last 365 days"));
	assertTrue(coreMetadata.getAbstract().contains("station elevation published by LVGMC: 0.03 m"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2025-10-27T00:00:00Z", extent.getBeginPosition());
	assertTrue(extent.isEndPositionIndeterminate());

	assertEquals(56.655, coreMetadata.getBoundingBox().getNorth(), 0);
	assertEquals(23.735, coreMetadata.getBoundingBox().getEast(), 0);

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("HD073801|LIMEN", online.getName());
	assertEquals(LVGMCClient.DEFAULT_ENDPOINT, online.getLinkage());
	assertEquals(NetProtocolWrapper.LVGMC.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("centimetres", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Latvia", resource.getExtensionHandler().getCountry().get());
	assertEquals(InterpolationType.AVERAGE, resource.getExtensionHandler().getTimeInterpolation().get());
	assertEquals("PT1H", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertEquals("PT1H", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.LVGMC.getCommonURN() + ":LIMEN",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals("Water level", coreMetadata.getMIMetadata().getCoverageDescription().getAttributeTitle());
	assertTrue(coreMetadata.getMIMetadata().getMIPlatform().getDescription().contains("in operation since 1920-01-01"));
    }

    @Test
    public void discontinuedSeaLevelTest() throws GSException {

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2025-10-27T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2025-12-31T23:00:00Z").get();

	LVGMCEntity series = LVGMCClient.createSeries(new JSONObject(LVGMCClientTest.STATION), new JSONObject(LVGMCClientTest.SEA_LEVEL),
		begin, end);

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Jelgava - Sea level", coreMetadata.getTitle());
	assertFalse(coreMetadata.getAbstract().contains("station zero"));
	assertEquals("PT15M", resource.getExtensionHandler().getTimeAggregationDuration8601().get());

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2025-12-31T23:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());
    }
}
