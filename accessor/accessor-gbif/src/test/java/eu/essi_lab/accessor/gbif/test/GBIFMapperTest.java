package eu.essi_lab.accessor.gbif.test;

import static eu.essi_lab.accessor.gbif.distributed.GBIFMapper.GBIFOCCURRENCE_SCHEMA;

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.gbif.distributed.GBIFMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author ilsanto
 */
public class GBIFMapperTest {

    @Test
    public void test() {

	GBIFMapper mapper = new GBIFMapper();

	Assert.assertEquals(GBIFOCCURRENCE_SCHEMA, mapper.getSupportedOriginalMetadataSchema());
    }

    @Test
    public void test2() {

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IResourceMapper.class).iterator()).//
		filter(m -> m.getClass().equals(GBIFMapper.class)).//
		findFirst().//
		isPresent());

    }

    @Test
    public void testMap() throws GSException, IOException {

	GBIFMapper mapper = new GBIFMapper();

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = GBIFMapperTest.class.getClassLoader().getResourceAsStream("gbifSingle.json");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals("4bfac3ea-8763-4f4b-a71a-76a6f5f243d3", mapped.getOriginalId().get());

	Assert.assertEquals("Ophiactis savignyi (MÃ¼ller & Troschel, 1842)", mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals("2018-07-10T12:19:06Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	// Assert.assertEquals(
	// "According to the 2008 amendment to the US E.P.A. Clean Air Act, areas (county parcels) with 8 hour average
	// ozone concentrations greater than 0.075ppm (150 ug/m^3) more than one day a year. Ozone is included in the
	// Clean Air Act (42 U.S.C. 7401 et seq.) under the National Ambient Air Quality Standards (NAAQS) that apply
	// for outdoor air for the entirety of the United States. Research has shown oil and gas development utilizing
	// hydraulic fracturing can contribute to elevated concentrations of ambient ozone ((Utah Department of
	// Environmental Quality, 2013; Olaguer, E. P. \"The Potential near-Source Ozone Impacts of Upstream Oil and Gas
	// Industry Emissions.\" Journal of the Air & Waste Management Association 62, no. 8 (2012): 966-77))",
	// mapped.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	//
	Assert.assertEquals("Animalia", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getKeywords().next().getElement().getValue().getKeyword().get(0).getCharacterString().getValue());

	Assert.assertEquals("Ophiactidae", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getKeywords().next().getElement().getValue().getKeyword().get(1).getCharacterString().getValue());

	Assert.assertEquals(-20.102312, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getSouth(), 0.001);

	Assert.assertEquals(57.488424, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getEast(), 0.001);

	Assert.assertEquals(57.488424, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getWest(), 0.001);

	Assert.assertEquals(-20.102312, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getNorth(), 0.001);

	Assert.assertEquals("1870-01-01T00:00:00Z", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getTemporalExtent().getBeginPosition());

	Assert.assertEquals("1870-01-01T00:00:00Z", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getTemporalExtent().getEndPosition());

	Assert.assertEquals("Museum of Comparative Zoology, Harvard University", mapped.getHarmonizedMetadata().getCoreMetadata()
		.getMIMetadata().getDataIdentification().getPointOfContact().getOrganisationName());

    }

}