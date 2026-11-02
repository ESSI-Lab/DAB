package eu.essi_lab.accessor.nlrijkswaterstaat;

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
public class RijkswaterstaatMapperTest {

    @Test
    public void mapTest() throws GSException {

	RijkswaterstaatEntity series = RijkswaterstaatClientTest.createWaterLevelSeries();
	series.getObject().put(RijkswaterstaatClient.BEGIN, "1900-01-01T00:00:00Z");
	series.getObject().put(RijkswaterstaatClient.END, "2001-12-31T23:50:00Z");

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(RijkswaterstaatMapper.RIJKSWATERSTAAT_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(RijkswaterstaatClient.DEFAULT_ENDPOINT);

	GSResource resource = new RijkswaterstaatMapper().map(originalMetadata, source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	assertEquals("Hoek van Holland - Waterhoogte in Oppervlaktewater t.o.v. Normaal Amsterdams Peil in cm", coreMetadata.getTitle());

	TemporalExtent extent = coreMetadata.getTemporalExtent();
	assertEquals("1900-01-01T00:00:00Z", extent.getBeginPosition());
	assertEquals("2001-12-31T23:50:00Z", extent.getEndPosition());

	Online online = coreMetadata.getOnline();
	assertNotNull(online);
	assertEquals(series.getSeriesId(), online.getName());
	assertEquals(NetProtocolWrapper.RIJKSWATERSTAAT.getCommonURN(), online.getProtocol());

	assertEquals("cm", resource.getExtensionHandler().getAttributeUnitsAbbreviation().get());
	assertNotNull(resource.getOriginalId());
	assertTrue(coreMetadata.getMIMetadata().getCoverageDescription().getAttributeIdentifier().endsWith(":meting|OW|WATHTE|cm|NAP|NVT|NVT|NVT|NVT|"));
    }

    @Test
    public void propertyNameFallbackTest() {

	RijkswaterstaatEntity series = RijkswaterstaatClientTest.createWaterLevelSeries();
	series.getObject().remove(RijkswaterstaatClient.PROPERTY_DESCRIPTION);

	assertEquals("Waterhoogte in Oppervlaktewater", RijkswaterstaatMapper.getPropertyName(series));
    }
}
