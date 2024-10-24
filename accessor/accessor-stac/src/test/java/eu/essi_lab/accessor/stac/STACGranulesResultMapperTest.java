package eu.essi_lab.accessor.stac;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.stac.distributed.STACGranulesResultMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roberto
 */
public class STACGranulesResultMapperTest {

    @Test
    public void test() throws GSException, IOException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = STACGranulesResultMapperTest.class.getClassLoader().getResourceAsStream("deaSTACGranuleSingleOriginal.json");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	STACGranulesResultMapper mapper = new STACGranulesResultMapper();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals("28efbc1c-8376-57ee-b9b1-4850f5ef4a57", mapped.getOriginalId().get());

	Assert.assertEquals("cgls_landcover_2015.stac-item",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Assert.assertEquals(null,
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	Iterator<String> keywords = mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getKeywordsValues();
	String firstKey = "";
	while(keywords.hasNext()) {
	    firstKey = keywords.next();
	    break;
	}
	Assert.assertEquals("tirs",
		firstKey);
	
	Assert.assertEquals(-47.970238,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getSouth(), 0.001);

	Assert.assertEquals(64.499999,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getEast(), 0.001);

	Assert.assertEquals(-26.360119,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getWest(), 0.001);

	Assert.assertEquals(38.350198,
		(double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
			.getNorth(), 0.001);

	Assert.assertEquals("2015-01-01T00:00:00Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
			.getBeginPosition());

	Assert.assertEquals("2015-12-31T23:59:59Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
			.getEndPosition());

	Assert.assertEquals("digitalearthafrica.org",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact().getContact().getOnline().getLinkage());

    }
}