package eu.essi_lab.accessor.esmitecoaforos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class MITECOAforosMapperTest {

    @Test
    public void mapTest() throws Exception {

	MITECOAforosEntity series = MITECOAforosClientTest.createDischargeSeries();
	series.getObject().put(MITECOAforosClient.END, "1916-06-30T00:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(MITECOAforosMapper.MITECO_AFOROS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(MITECOAforosClient.DEFAULT_ENDPOINT);

	GSResource resource = new MITECOAforosMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("PUENTE DE SAMA - Discharge (daily)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Caudal medio diario"));
	assertTrue(coreMetadata.getAbstract().contains("river NALON"));
	assertTrue(coreMetadata.getAbstract().contains("2021-22"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1912-01-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("1916-06-30T00:00:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("CANTABRICO|1033|RIVER_DISCHARGE_DAILY", online.getName());
	assertEquals(NetProtocolWrapper.MITECO_AFOROS.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Spain", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.MITECO_AFOROS.getCommonURN() + ":RIVER_DISCHARGE_DAILY",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(43.298889, coreMetadata.getBoundingBox().getNorth(), 1e-6);
    }

    @Test
    public void resolutionTest() throws Exception {

	MITECOAforosEntity daily = MITECOAforosClientTest.createDischargeSeries();
	assertEquals("P1D", map(daily).getExtensionHandler().getTimeResolutionDuration8601().get());

	MITECOAforosEntity monthly = MITECOAforosClientTest.createDischargeSeries();
	monthly.getObject().put(MITECOAforosClient.VARIABLE, MITECOAforosVariable.PRECIPITATION_MONTHLY.name());
	GSResource resource = map(monthly);
	assertEquals("PUENTE DE SAMA - Precipitation (monthly)", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	assertEquals("P1M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals("P1M", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
    }

    /**
     * @param series
     * @return
     * @throws Exception
     */
    private static GSResource map(MITECOAforosEntity series) throws Exception {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(MITECOAforosMapper.MITECO_AFOROS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(MITECOAforosClient.DEFAULT_ENDPOINT);

	return new MITECOAforosMapper().map(originalMetadata, source);
    }

    @Test
    public void unitTest() {

	assertEquals("cubic hectometres", MITECOAforosMapper.getUnitName("hm3"));
	assertEquals("cubic metres per second", MITECOAforosMapper.getUnitName("m3/s"));
	assertEquals("x", MITECOAforosMapper.getUnitName("x"));
    }
}
