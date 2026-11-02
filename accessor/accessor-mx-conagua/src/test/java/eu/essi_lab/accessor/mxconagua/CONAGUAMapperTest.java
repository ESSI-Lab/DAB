package eu.essi_lab.accessor.mxconagua;

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
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class CONAGUAMapperTest {

    @Test
    public void mapTest() throws GSException {

	CONAGUAEntity series = CONAGUAClient.createSeries(//
		CONAGUAClientTest.createStation(), //
		CONAGUAProperty.DISCHARGE, //
		CONAGUAClient.parseStationFile(CONAGUAClientTest.LEVEL_FILE)).get();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(CONAGUAMapper.CONAGUA_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(CONAGUAClient.DEFAULT_ENDPOINT);

	GSResource resource = new CONAGUAMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Candelaria, Camp. - Discharge (Gasto)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("discharge (Gasto, m³/s)"));
	assertTrue(coreMetadata.getAbstract().contains("Grijalva - Usumacinta"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1990-01-03T00:00:00Z", extent.getBeginPosition());
	assertEquals("1990-01-04T00:00:00Z", extent.getEndPosition());
	// historical series
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("CNDCA|DISCHARGE", online.getName());
	assertEquals(NetProtocolWrapper.CONAGUA.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("cubic metres per second", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Mexico", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.CONAGUA.getCommonURN() + ":DISCHARGE",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(18.183056, coreMetadata.getBoundingBox().getNorth(), 0.000001);
    }

    @Test
    public void unitTest() {

	CONAGUAEntity level = CONAGUAClient.parseSeriesId("CNDCA|LEVEL");

	assertEquals("m3/s", CONAGUAMapper.getUnitAbbreviation("m³/s", level));
	assertEquals("m", CONAGUAMapper.getUnitAbbreviation("", level));
	assertEquals("metres", CONAGUAMapper.getUnitName("m", level));
    }
}
