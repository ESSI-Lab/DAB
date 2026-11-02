package eu.essi_lab.accessor.enhydris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
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
public class EnhydrisMapperTest {

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private GSResource map(EnhydrisEntity series) throws GSException {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(EnhydrisMapper.ENHYDRIS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(EnhydrisClient.DEFAULT_ENDPOINT);

	return new EnhydrisMapper().map(originalMetadata, source);
    }

    @Test
    public void mapTest() throws GSException {

	GSResource resource = map(EnhydrisClientTest.createSeries());
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Ευρώτας - Eurotas - Mean stage (Initial, 3h)", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract(),
		coreMetadata.getAbstract().startsWith("Time series of mean stage (stage) at station Ευρώτας - Eurotas, owned by"));
	assertTrue(coreMetadata.getAbstract().contains("OpenHi.net"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("2019-11-12T18:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("2022/1244/10846", online.getName());
	assertEquals(EnhydrisClient.DEFAULT_ENDPOINT, online.getLinkage());
	assertEquals(NetProtocolWrapper.ENHYDRIS.getCommonURN(), online.getProtocol());

	assertEquals("m", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Meters", resource.getExtensionHandler().getAttributeUnits().get());
	assertEquals("Greece", resource.getExtensionHandler().getCountry().get());
	assertEquals("PT3H", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.ENHYDRIS.getCommonURN() + ":14",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	assertEquals("Stage", coreMetadata.getMIMetadata().getCoverageDescription().getAttributeTitle());
	assertEquals(37.130856, coreMetadata.getBoundingBox().getNorth(), 0);
	assertEquals(22.399872, coreMetadata.getBoundingBox().getEast(), 0);
    }

    @Test
    public void aggregatedTest() {

	JSONObject object = EnhydrisClientTest.createSeries().getObject();
	object.put(EnhydrisClient.TIMESERIES_TYPE, "Aggregated");
	object.put(EnhydrisClient.TIMESERIES_NAME, "Sum");
	object.put(EnhydrisClient.TIME_STEP, "1D");

	EnhydrisEntity entity = new EnhydrisEntity(object);

	assertEquals("Aggregated sum, 1D", EnhydrisMapper.getTimeseriesDescription(entity));
	assertEquals(InterpolationType.TOTAL_PREC, EnhydrisMapper.getInterpolation(entity));
    }

    @Test
    public void resolutionTest() {

	assertEquals("PT10M", EnhydrisMapper.getResolution("10min").get());
	assertEquals("PT3H", EnhydrisMapper.getResolution("3H").get());
	assertEquals("PT1H", EnhydrisMapper.getResolution("h").get());
	assertEquals("P7D", EnhydrisMapper.getResolution("7D").get());
	assertEquals("P1M", EnhydrisMapper.getResolution("1M").get());
	assertFalse(EnhydrisMapper.getResolution("").isPresent());
	assertFalse(EnhydrisMapper.getResolution(null).isPresent());
    }
}
