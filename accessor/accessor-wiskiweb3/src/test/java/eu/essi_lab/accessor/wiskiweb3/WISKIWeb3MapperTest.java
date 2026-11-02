package eu.essi_lab.accessor.wiskiweb3;

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
public class WISKIWeb3MapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(WISKIWeb3Entity series) throws GSException {

	series.getObject().put(WISKIWeb3Client.ORGANISATION, "Hessisches Landesamt für Naturschutz, Umwelt und Geologie");
	series.getObject().put(WISKIWeb3Client.LICENSE, "Creative Commons Attribution 4.0 International (CC BY 4.0)");
	series.getObject().put(WISKIWeb3Client.COUNTRY, "Germany");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(WISKIWeb3Mapper.WISKIWEB3_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(WISKIWeb3Client.DEFAULT_ENDPOINT);

	return new WISKIWeb3Mapper().map(originalMetadata, source);
    }

    @Test
    public void weekSeriesTest() throws GSException {

	WISKIWeb3Entity series = WISKIWeb3ClientTest.createWaterLevelSeries(WISKIWeb3Client.WEEK_FILE);
	series.getObject().put(WISKIWeb3Client.BEGIN, "2026-10-19T22:00:00Z");
	series.getObject().put(WISKIWeb3Client.END, "2026-10-27T19:30:00Z");

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Fahrenbach - Water level (15 minutes values)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Water level - Wasserstand [cm] (gauge datum 177.523 ü. NHN)"));
	assertTrue(coreMetadata.getAbstract().contains("rolling window of about one week"));
	assertTrue(coreMetadata.getAbstract().contains("Station operated by RPU Darmstadt"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2026-10-19T22:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("0/23940359/W|week|15m.Cmd.RelAbs.P", online.getName());
	assertEquals(NetProtocolWrapper.WISKIWEB3.getCommonURN(), online.getProtocol());
	assertEquals(WISKIWeb3Client.DEFAULT_ENDPOINT, online.getLinkage());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Germany", resource.getExtensionHandler().getCountry().get());
	assertEquals("PT15M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.WISKIWEB3.getCommonURN() + ":W",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals("Hessisches Landesamt für Naturschutz, Umwelt und Geologie",
		coreMetadata.getDataIdentification().getPointOfContact().getOrganisationName());
    }

    @Test
    public void yearSeriesTest() throws GSException {

	WISKIWeb3Entity series = WISKIWeb3ClientTest.createWaterLevelSeries(WISKIWeb3Client.YEAR_FILE);
	series.getObject().put(WISKIWeb3Client.BEGIN, "1985-01-31T23:00:00Z");
	series.getObject().put(WISKIWeb3Client.END, "2026-10-25T23:00:00Z");

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Fahrenbach - Water level (daily mean values)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("whole period published by the site"));
	assertEquals("P1D", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertEquals(InterpolationType.AVERAGE_SUCC, resource.getExtensionHandler().getTimeInterpolation().get());
    }

    @Test
    public void namesTest() {

	assertEquals("Spring discharge", WISKIWeb3Mapper.getEnglishName("Quellschüttung").get());
	assertEquals("Discharge", WISKIWeb3Mapper.getEnglishName("Durchfluss").get());
	assertEquals("Water temperature", WISKIWeb3Mapper.getEnglishName("Wassertemperatur").get());
	assertEquals("Precipitation", WISKIWeb3Mapper.getEnglishName("Niederschlag").get());
	assertEquals("Water level", WISKIWeb3Mapper.getEnglishName("S").get());
	assertEquals("m3/s", WISKIWeb3Mapper.getUnitAbbreviation("m³/s"));
	assertEquals("cubic metres per second", WISKIWeb3Mapper.getUnitName("m³/s"));

	WISKIWeb3Entity precipitation = WISKIWeb3Client.parseSeriesId("0/L121/N|week|h.CmdTotal");
	assertEquals("PT1H", WISKIWeb3Mapper.getResolution(precipitation).get());
	assertEquals("hourly totals", WISKIWeb3Mapper.getSeriesDescription(precipitation));
    }
}
