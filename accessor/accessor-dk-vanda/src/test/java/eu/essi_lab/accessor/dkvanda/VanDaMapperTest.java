package eu.essi_lab.accessor.dkvanda;

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
public class VanDaMapperTest {

    @Test
    public void mapTest() throws GSException {

	VanDaEntity series = VanDaClientTest.createWaterLevelSeries();
	series.getObject().put(VanDaClient.END, "2001-12-31T23:45:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(VanDaMapper.VANDA_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(VanDaClient.DEFAULT_ENDPOINT);

	GSResource resource = new VanDaMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Ellerup Bæk, Bæk, Ved Vejbro (measurement point 1) - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Vandstand"));
	assertTrue(coreMetadata.getAbstract().contains("local gauge zero"));
	assertTrue(coreMetadata.getAbstract().contains("Miljøstyrelsen"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2023-11-03T06:30:00Z", extent.getBeginPosition());
	assertEquals("2001-12-31T23:45:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("21006844|1|25|1233", online.getName());
	assertEquals(NetProtocolWrapper.VANDA.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("centimetres", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Denmark", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.VANDA.getCommonURN() + ":25:1233",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertTrue(coreMetadata.getDataIdentification().getLegalConstraints().next().getUseLimitation()
		.contains("vilkaar-for-brug"));
    }

    @Test
    public void propertyNameTest() {

	assertEquals("Discharge", VanDaMapper.getPropertyName(VanDaClient.parseSeriesId("21006844|1|27|1155")));
	assertEquals("Groundwater level", VanDaMapper.getPropertyName(VanDaClient.parseSeriesId("70000426|1|31|1233")));
	assertEquals("Water level (DVR90)", VanDaMapper.getPropertyName(VanDaClient.parseSeriesId("21006844|1|25|439")));
    }

    @Test
    public void unitTest() {

	assertEquals("°C", VanDaMapper.getUnitAbbreviation("grader C"));
	assertEquals("litres per second", VanDaMapper.getUnitName("l/s"));
	assertEquals("l/s", VanDaMapper.getUnitAbbreviation("l/s"));
    }
}
