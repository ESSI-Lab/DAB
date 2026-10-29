package eu.essi_lab.accessor.atehyd;

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
public class EHYDMapperTest {

    @Test
    public void mapTest() throws GSException {

	EHYDEntity series = EHYDClientTest.createWaterLevelSeries();
	series.getObject().put(EHYDClient.BEGIN, "1970-12-31T23:00:00Z");
	series.getObject().put(EHYDClient.END, "2021-12-30T23:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(EHYDMapper.EHYD_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(EHYDClient.DEFAULT_ENDPOINT);

	GSResource resource = new EHYDMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Mühlen (Navisbach) - Water level, daily mean", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("W-Tagesmittel"));
	assertTrue(coreMetadata.getAbstract().contains("gauge zero 1009.23 m"));
	assertTrue(coreMetadata.getAbstract().contains("Hydrographischer Dienst (HD-Tirol)"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1970-12-31T23:00:00Z", extent.getBeginPosition());
	assertEquals("2021-12-30T23:00:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("201582|owf_wasserst_tagesmittel", online.getName());
	assertEquals(NetProtocolWrapper.EHYD.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Austria", resource.getExtensionHandler().getCountry().get());
	assertEquals(InterpolationType.AVERAGE_SUCC, resource.getExtensionHandler().getTimeInterpolation().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.EHYD.getCommonURN() + ":owf_wasserst_tagesmittel",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(47.116, coreMetadata.getBoundingBox().getNorth(), 0.01);
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", EHYDMapper.getUnitAbbreviation("m³/s"));
	assertEquals("cubic metres per second", EHYDMapper.getUnitName("m³/s"));
	assertEquals("m", EHYDMapper.getUnitAbbreviation("m ü.A."));
	assertEquals("°C", EHYDMapper.getUnitAbbreviation("°C"));
    }
}
