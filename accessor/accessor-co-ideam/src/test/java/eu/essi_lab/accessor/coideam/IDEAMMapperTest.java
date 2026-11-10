package eu.essi_lab.accessor.coideam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

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
public class IDEAMMapperTest {

    @Test
    public void mapTest() throws GSException {

	IDEAMEntity series = IDEAMClientTest.createWaterLevelSeries();
	series.getObject().put(IDEAMClient.END, "2020-07-16T07:00:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(IDEAMMapper.IDEAM_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(IDEAMClient.DEFAULT_ENDPOINT);

	GSResource resource = new IDEAMMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("SANTA ROSITA [21209920] - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Nivel Instantáneo del Rio"));
	assertTrue(coreMetadata.getAbstract().contains("stream BOGOTÁ"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2019-03-31T12:00:00Z", extent.getBeginPosition());
	assertEquals("2020-07-16T07:00:00Z", extent.getEndPosition());
	assertFalse(extent.isEndPositionIndeterminate());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("NIVEL|0021209920|0230", online.getName());
	assertEquals(NetProtocolWrapper.IDEAM.getCommonURN(), online.getProtocol());

	assertEquals("m", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Colombia", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.IDEAM.getCommonURN() + ":NIVEL",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(new BigDecimal("5.105878"),
		new BigDecimal(coreMetadata.getBoundingBox().getNorth().toString()).setScale(6, java.math.RoundingMode.HALF_UP));
    }

    @Test
    public void locationTest() {

	IDEAMEntity series = IDEAMClientTest.createWaterLevelSeries();
	series.getObject().put(IDEAMClient.LON, "0");
	series.getObject().put(IDEAMClient.CATALOG_LON, "-73.7");

	// the zero longitude is replaced by the location of the station catalog
	assertEquals(new BigDecimal("-73.7"), IDEAMMapper.getLocation(series).get()[1]);

	series.getObject().remove(IDEAMClient.CATALOG_LON);
	assertFalse(IDEAMMapper.getLocation(series).isPresent());
    }

    @Test
    public void unitTest() {

	assertEquals("m", IDEAMMapper.getUnitAbbreviation("mt"));
	assertEquals("m", IDEAMMapper.getUnitAbbreviation("M"));
	assertEquals("metres", IDEAMMapper.getUnitName("mt"));
	assertEquals("mm", IDEAMMapper.getUnitAbbreviation("mm"));
    }
}
