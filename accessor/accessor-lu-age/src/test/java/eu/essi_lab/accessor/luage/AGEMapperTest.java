package eu.essi_lab.accessor.luage;

import static org.junit.Assert.assertEquals;
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
public class AGEMapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(AGEEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(AGEMapper.AGE_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(AGEClient.DEFAULT_ENDPOINT);

	return new AGEMapper().map(originalMetadata, source);
    }

    @Test
    public void mapTest() throws GSException {

	GSResource resource = map(AGEClientTest.createSeries());
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Ettelbrück / Alzette (Alzette) - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().startsWith("Time series of water level at station Ettelbrück / Alzette on the Alzette"));
	assertTrue(coreMetadata.getAbstract().contains("gauge zero 194,06 m NN."));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2026-10-25T00:45:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("Ettelbrück-Alzette|W", online.getName());
	assertEquals(AGEClient.DEFAULT_ENDPOINT, online.getLinkage());
	assertEquals(NetProtocolWrapper.AGE.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("centimetres", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Luxembourg", resource.getExtensionHandler().getCountry().get());
	assertEquals("PT15M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.AGE.getCommonURN() + ":W",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(49.84479999, coreMetadata.getBoundingBox().getNorth(), 0);
    }
}
