package eu.essi_lab.accessor.obis;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.obis.distributed.OBISGranulesResultMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class OBISGranulesResultMapperTest {

    @Test
    public void test() throws GSException, IOException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = OBISGranulesResultMapperTest.class.getClassLoader().getResourceAsStream("obisGranuleSingleOriginal.json");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	OBISGranulesResultMapper mapper = new OBISGranulesResultMapper();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals("418490437", mapped.getOriginalId().get());

	Assert.assertEquals("Cladophora rupestris from Seaweed data for Great Britain and Ireland",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals("2008-05-15T00:00:00Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	Assert.assertEquals(51.144455,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getSouth(), 0.001);

	Assert.assertEquals(-3.859825,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getEast(), 0.001);

	Assert.assertEquals(-3.859825,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getWest(), 0.001);

	Assert.assertEquals(51.144455,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getNorth(), 0.001);

	Assert.assertEquals("1976-05-30T11:00:00Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
			.getBeginPosition());

	Assert.assertEquals("1976-05-30T11:00:00Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
			.getEndPosition());

	Assert.assertEquals("British Phycological Society",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact()
			.getOrganisationName());

	Assert.assertEquals("urn:lsid:marinespecies.org:taxname:145064",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getKeywords().next().getElement()
			.getValue().getKeyword().get(0).getCharacterString().getValue());

	Assert.assertEquals("Chlorophyta",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getKeywords().next().getElement()
			.getValue().getKeyword().get(1).getCharacterString().getValue());


    }
}