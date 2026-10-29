package eu.essi_lab.accessor.siarso;

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
public class ARSOMapperTest {

    @Test
    public void mapTest() throws Exception {

	ARSOEntity series = ARSOClientTest.createSeries().get(1);
	series.getObject().put(ARSOClient.BEGIN, "2001-12-01T00:00:00Z");
	series.getObject().put(ARSOClient.END, "2001-12-31T23:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(ARSOMapper.ARSO_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(ARSOClient.DEFAULT_ENDPOINT);

	GSResource resource = new ARSOMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Gornja Radgona I (Mura) - Discharge", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Pretok [m³/s]"));
	assertTrue(coreMetadata.getAbstract().contains("second (local floods) 905.0 m³/s"));
	assertTrue(coreMetadata.getAbstract().contains("last 30 days"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2001-12-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("2001-12-31T23:00:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("1060|Pretok", online.getName());
	assertEquals(NetProtocolWrapper.ARSO.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Slovenia", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.ARSO.getCommonURN() + ":DISCHARGE",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }

    @Test
    public void waterLevelTest() throws Exception {

	ARSOEntity series = ARSOClientTest.createSeries().get(0);

	assertTrue(ARSOMapper.getPropertyDescription(series).contains("gauge zero at 202.35 m a.s.l."));
	// discharge thresholds are not reported for the water level
	assertEquals("", ARSOMapper.getThresholdsDescription(series));
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", ARSOMapper.getUnitAbbreviation("m³/s"));
	assertEquals("cubic metres per second", ARSOMapper.getUnitName("m³/s"));
	assertEquals("centimetres", ARSOMapper.getUnitName("cm"));
	assertEquals("°C", ARSOMapper.getUnitAbbreviation("°C"));
    }
}
