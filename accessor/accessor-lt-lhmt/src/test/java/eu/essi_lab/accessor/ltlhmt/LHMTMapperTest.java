package eu.essi_lab.accessor.ltlhmt;

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
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class LHMTMapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(LHMTEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(LHMTMapper.LHMT_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(LHMTClient.DEFAULT_ENDPOINT);

	return new LHMTMapper().map(originalMetadata, source);
    }

    @Test
    public void dailyTest() throws GSException {

	GSResource resource = map(LHMTClientTest.createSeries(LHMTParameter.DISCHARGE_DAILY));
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Nemajūnų VMS (Nemunas) - Discharge (daily mean)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().startsWith("Daily mean time series of discharge"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2000-01-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("2024-12-31T00:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("nemajunu-vms|DISCHARGE_DAILY", online.getName());
	assertEquals(NetProtocolWrapper.LHMT.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Lithuania", resource.getExtensionHandler().getCountry().get());
	assertEquals(InterpolationType.AVERAGE, resource.getExtensionHandler().getTimeInterpolation().get());
	assertEquals("P1D", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.LHMT.getCommonURN() + ":DISCHARGE_DAILY",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }

    @Test
    public void hourlyTest() throws GSException {

	GSResource resource = map(LHMTClientTest.createSeries(LHMTParameter.WATER_LEVEL));
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Nemajūnų VMS (Nemunas) - Water level", coreMetadata.getTitle());
	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("PT1H", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
    }
}
