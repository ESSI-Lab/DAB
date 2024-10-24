package eu.essi_lab.accessor.fedeo.distributed;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class FEDEOGranulesMapperTest {

    @Test
    public void test() throws IOException, GSException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = FEDEOGranulesMapperTest.class.getClassLoader().getResourceAsStream(
		"eu/essi_lab/accessor/fedeo/test/fedeo_single_granule.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	FEDEOGranulesMapper mapper = new FEDEOGranulesMapper();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals("https://fedeo.ceos.org/opensearch/request?parentIdentifier=EOP:SSARA&startDate=2000-01-01T00:00:00Z&endDate=2018-01-31T00:00:00Z&bbox=&httpAccept=application/atom%2Bxml&uid=1",
		mapped.getOriginalId().get().trim());

	Assert.assertEquals("UAVSAR - UA_SanAnd_08523_15003-008_18083-002_1408d_s01_L090_01", mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	//	Assert.assertEquals("2014-01-28T00:04:17Z",
	//		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationCreationDate());

	Assert.assertEquals("2015-01-08T22:57:31Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());
	
	Assert.assertEquals("https://datapool.asf.alaska.edu/DEM_TIFF/UA/SanAnd_08523_15003-008_18083-002_1408d_s01_L090_01_hgt_grd_tiff.zip",
		mapped.getHarmonizedMetadata().getCoreMetadata().getOnline().getLinkage());

    }

    @Test
    public void test2() {

	FEDEOGranulesMapper mapper = new FEDEOGranulesMapper();

	Assert.assertEquals(FEDEOGranulesMetadataSchemas.ATOM_ENTRY_FEDEO.toString(), mapper.getSupportedOriginalMetadataSchema());

    }
    
    
    
}