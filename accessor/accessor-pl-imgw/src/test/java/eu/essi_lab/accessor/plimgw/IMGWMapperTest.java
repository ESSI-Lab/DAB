package eu.essi_lab.accessor.plimgw;

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
public class IMGWMapperTest {

    @Test
    public void mapTest() throws GSException {

	IMGWEntity series = IMGWClientTest.createWaterLevelSeries();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(IMGWMapper.IMGW_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(IMGWClient.DEFAULT_ENDPOINT);

	GSResource resource = new IMGWMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Bągart (Dzierzgoń) - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Stan wody, cm"));
	assertTrue(coreMetadata.getAbstract().contains("-5.077 m a.s.l."));
	assertTrue(coreMetadata.getAbstract().contains("from 1980-11-01 to 2025-10-31"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1980-11-01T00:00:00Z", extent.getBeginPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("153190040|H", online.getName());
	assertEquals(NetProtocolWrapper.IMGW.getCommonURN(), online.getProtocol());

	assertTrue(coreMetadata.getDataIdentification().getLegalConstraints().next().getUseLimitations().next()
		.contains("Źródłem pochodzenia danych jest IMGW-PIB"));

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Poland", resource.getExtensionHandler().getCountry().get());
	assertEquals("P1D", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.IMGW.getCommonURN() + ":H",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }
}
