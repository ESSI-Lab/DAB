package eu.essi_lab.accessor.derlpwasser;

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
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class RLPWasserMapperTest {

    @Test
    public void mapTest() throws GSException {

	RLPWasserEntity series = RLPWasserClientTest.createWaterLevelSeries();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(RLPWasserMapper.RLP_WASSER_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(RLPWasserClient.DEFAULT_ENDPOINT);

	GSResource resource = new RLPWasserMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Altenahr - Water level (Wasserstand)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("river Ahr"));
	assertTrue(coreMetadata.getAbstract().contains("last 5 days"));
	assertTrue(coreMetadata.getAbstract().contains("160.522 m ü. NHN"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2026-10-22T20:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("27180403|W", online.getName());
	assertEquals(NetProtocolWrapper.RLP_WASSER.getCommonURN(), online.getProtocol());

	assertEquals(50.516, coreMetadata.getBoundingBox().getNorth(), 0.01);

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Germany", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.RLP_WASSER.getCommonURN() + ":W",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", RLPWasserMapper.getUnitAbbreviation("m³/s"));
	assertEquals("cubic metres per second", RLPWasserMapper.getUnitName("m³/s"));
	assertEquals("m", RLPWasserMapper.getUnitAbbreviation("m ü. NHN"));
	assertEquals("cm", RLPWasserMapper.getUnitAbbreviation("cm"));
    }
}
