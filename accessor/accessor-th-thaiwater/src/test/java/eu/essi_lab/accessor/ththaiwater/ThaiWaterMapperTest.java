package eu.essi_lab.accessor.ththaiwater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
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
public class ThaiWaterMapperTest {

    @Test
    public void mapTest() throws GSException {

	ThaiWaterEntity series = ThaiWaterClientTest.createWaterLevelSeries();
	series.getObject().put(ThaiWaterClient.BEGIN, "2012-11-29T23:00:00Z");
	series.getObject().put(ThaiWaterClient.END, "2020-12-31T23:00:00Z");

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertTrue(coreMetadata.getTitle().endsWith(" (C.36) - Water level"));
	assertTrue(coreMetadata.getAbstract().contains("mean sea level"));
	assertTrue(coreMetadata.getAbstract().contains("Royal Irrigation Department"));
	assertTrue(coreMetadata.getAbstract().contains("waterlevel_msl"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2012-11-29T23:00:00Z", extent.getBeginPosition());
	assertEquals("2020-12-31T23:00:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("tele_waterlevel|2611|WATER_LEVEL", online.getName());
	assertEquals(NetProtocolWrapper.THAIWATER.getCommonURN(), online.getProtocol());

	assertEquals("m", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Thailand", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.THAIWATER.getCommonURN() + ":WATER_LEVEL",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }

    @Test
    public void mapDamTest() throws GSException {

	ThaiWaterEntity series = ThaiWaterClient
		.createSeries(new JSONObject(ThaiWaterClientTest.DAM_ENTRY), ThaiWaterStationType.DAM).stream()
		.filter(s -> s.getParameter().get() == ThaiWaterParameter.DAM_INFLOW).findFirst().get();
	series.getObject().put(ThaiWaterClient.BEGIN, "1998-12-31T17:00:00Z");

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Ratchaprapa (31) - Reservoir inflow", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().startsWith("Daily time series of reservoir inflow"));
	assertEquals("Mm3", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("P1D", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals("dam|25|DAM_INFLOW", coreMetadata.getOnline().getName());
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(ThaiWaterEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(ThaiWaterMapper.THAIWATER_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(ThaiWaterClient.DEFAULT_ENDPOINT);

	return new ThaiWaterMapper().map(originalMetadata, source);
    }
}
