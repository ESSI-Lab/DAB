package eu.essi_lab.accessor.essaihjucar;

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
public class SAIHJucarMapperTest {

    @Test
    public void mapTest() throws GSException {

	SAIHJucarEntity series = SAIHJucarClientTest.createGaugeVariable();
	series.getObject().put(SAIHJucarClient.BEGIN, "2024-12-09T17:30:00Z");
	series.getObject().put(SAIHJucarClient.END, "2025-12-31T22:55:00Z");

	GSResource resource = map(series);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("AZUD FONT D'EN CARRÓS - River discharge (CAUDAL RÍO SERPIS SOBRE EL AZUD)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Villalonga (Valencia)"));
	assertTrue(coreMetadata.getAbstract().contains("variable 14099"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2024-12-09T17:30:00Z", extent.getBeginPosition());
	assertEquals("2025-12-31T22:55:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("14099", online.getName());
	assertEquals(NetProtocolWrapper.SAIH_JUCAR.getCommonURN(), online.getProtocol());

	assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Spain", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.SAIH_JUCAR.getCommonURN() + ":RIVER_DISCHARGE",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals(38.903287, coreMetadata.getBoundingBox().getNorth(), 0.00001);
    }

    @Test
    public void reservoirMapTest() throws GSException {

	SAIHJucarEntity series = SAIHJucarClientTest.createReservoirVariables().get(1);
	series.getObject().put(SAIHJucarClient.BEGIN, "2024-07-25T11:00:00Z");
	series.getObject().put(SAIHJucarClient.END, "2026-10-27T20:05:00Z");

	CoreMetadata coreMetadata = map(series).getHarmonizedMetadata().getCoreMetadata();

	assertEquals("EMBALSE DE ULLDECONA - Reservoir volume (VOLUMEN DE EMBALSE)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("9.11 hm³"));
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", SAIHJucarMapper.getUnitAbbreviation("m³/s"));
	assertEquals("metres above sea level", SAIHJucarMapper.getUnitName("m s.n.m."));
	assertEquals("hm3", SAIHJucarMapper.getUnitAbbreviation("hm³"));
	assertEquals("mm/h", SAIHJucarMapper.getUnitAbbreviation("mm/h"));
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private static GSResource map(SAIHJucarEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(SAIHJucarMapper.SAIH_JUCAR_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(SAIHJucarClient.DEFAULT_ENDPOINT);

	return new SAIHJucarMapper().map(originalMetadata, source);
    }
}
