package eu.essi_lab.accessor.adhi;

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
public class ADHIMapperTest {

    @Test
    public void mapTest() throws GSException {

	ADHIEntity series = ADHIClientTest.createMonthlySeries();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(ADHIMapper.ADHI_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(ADHIClient.DEFAULT_ENDPOINT);

	GSResource resource = new ADHIMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("KADEY a PANA - Monthly mean discharge", coreMetadata.getTitle());

	String abstrakt = coreMetadata.getAbstract();
	assertTrue(abstrakt.contains("CMQ5002"));
	assertTrue(abstrakt.contains("catchment area 20015.9 km2"));
	assertTrue(abstrakt.contains("version 6.1"));
	assertTrue(abstrakt.contains("some gap filling detected"));
	assertTrue(abstrakt.contains("https://doi.org/10.5194/essd-13-1547-2021"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1963-05-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("1963-07-01T00:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("ADHI_2:MONTHLY_MEAN", online.getName());
	assertEquals(NetProtocolWrapper.ADHI.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Cameroon", resource.getExtensionHandler().getCountry().get());
	assertEquals(InterpolationType.AVERAGE, resource.getExtensionHandler().getTimeInterpolation().get());
	assertEquals("P1M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals("P1M", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.ADHI.getCommonURN() + ":MONTHLY_MEAN",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertTrue(coreMetadata.getDataIdentification().getLegalConstraints().next().getUseLimitations().next().contains("CC BY 4.0"));
    }
}
