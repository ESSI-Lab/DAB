package eu.essi_lab.accessor.ieopw;

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
public class OPWMapperTest {

    @Test
    public void mapTest() throws GSException {

	OPWEntity series = OPWClientTest.createWaterLevelSeries();
	series.getObject().put(OPWClient.BEGIN, "1973-10-01T00:00:00Z");
	series.getObject().put(OPWClient.END, "2001-12-31T23:45:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(OPWMapper.OPW_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(OPWClient.DEFAULT_ENDPOINT);

	GSResource resource = new OPWMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Sandy Mills - Water level", coreMetadata.getTitle());
	assertTrue(coreMetadata.getAbstract().contains("Malin Head OSGM15"));

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1973-10-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("2001-12-31T23:45:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals("01041|S", online.getName());
	assertEquals(NetProtocolWrapper.OPW.getCommonURN(), online.getProtocol());

	assertEquals("m", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertEquals("Ireland", resource.getExtensionHandler().getCountry().get());
	assertNotNull(resource.getOriginalId());
	assertEquals(NetProtocolWrapper.OPW.getCommonURN() + ":S",
		coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier());
    }

    @Test
    public void unitTest() {

	assertEquals("m3/s", OPWMapper.getUnitAbbreviation("cumec"));
	assertEquals("cubic metres per second", OPWMapper.getUnitName("cumec"));
	assertEquals("°C", OPWMapper.getUnitAbbreviation("°C"));
    }
}
