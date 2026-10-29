package eu.essi_lab.accessor.czchmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class CHMIMapperTest {

    @Test
    public void mapOperationalTest() throws GSException {

	CHMIEntity series = CHMIClient.createSeries(CHMIClientTest.getStation(), CHMIParameter.H, CHMIClient.getYearBegin(1925),
		ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-27T14:30:00Z").get());

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Vestřev (Labe) - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Vodní stav"));
	assertTrue(coreMetadata.getAbstract().contains("Měřené okamžité vodní stavy"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1925-01-01T00:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("0-203-1-004200|H", online.getName());
	assertEquals(NetProtocolWrapper.CHMI.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Czechia", resource.getExtensionHandler().getCountry().get());
	assertEquals("CZE", resource.getExtensionHandler().getCountryISO3().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.CHMI.getCommonURN() + ":H",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertTrue(coreMetadata.getMIMetadata().getMIPlatform().getDescription().contains("database number 004200"));
    }

    @Test
    public void mapHistoricalTest() throws GSException {

	CHMIEntity series = CHMIClient.createSeries(CHMIClientTest.getStation(), CHMIParameter.QD, CHMIClient.getYearBegin(1911),
		ISO8601DateTimeUtils.parseISO8601ToDate("2025-12-31T00:00:00Z").get());

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Vestřev (Labe) - Daily mean discharge", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Validated"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2025-12-31T00:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("P1D", resource.getExtensionHandler().getTimeAggregationDuration8601().get());
	assertEquals(InterpolationType.AVERAGE_SUCC, resource.getExtensionHandler().getTimeInterpolation().get());
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", CHMIMapper.getUnitAbbreviation("M3_S"));
	assertEquals("cubic metres per second", CHMIMapper.getUnitName("M3_S"));
	assertEquals("°C", CHMIMapper.getUnitAbbreviation("0C"));
	assertEquals("g/m3", CHMIMapper.getUnitAbbreviation("G_M3"));
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private static GSResource map(CHMIEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(CHMIMapper.CHMI_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(CHMIClient.DEFAULT_ENDPOINT);

	return new CHMIMapper().map(originalMetadata, source);
    }
}
