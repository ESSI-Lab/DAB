package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.cmr.distributed.CMRGranulesMapper;
import eu.essi_lab.accessor.cmr.distributed.CMRGranulesMetadataSchemas;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CMRGranulesMapperTest {

    @Test
    public void test() throws IOException, GSException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = CMRGranulesMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/single-granule-response.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	Mockito.doReturn(md).when(om).getMetadata();

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	CMRGranulesMapper mapper = new CMRGranulesMapper();

	GSResource mapped = mapper.map(om, source);

	Assert.assertEquals("http://cwic.wgiss.ceos.org/opensearch/granules.atom?uid=C179003487-ORNL_DAAC:G1422884702-ORNL_DAAC",
		mapped.getOriginalId().get());

	Assert.assertEquals("ISLSCPII_GPCC_PRECIP.gpcc_numgauges_1deg.zip", mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());

	// Assert.assertEquals("2014-01-28T00:04:17Z",
	// mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationCreationDate());

	Assert.assertEquals("2011-02-25T00:00:00Z",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getCitationRevisionDate());

	Assert.assertEquals("Granule metadata for ISLSCPII_GPCC_PRECIP.gpcc_numgauges_1deg.zip",
		mapped.getHarmonizedMetadata().getCoreMetadata().getAbstract());

	Assert.assertEquals("https://webmap.ornl.gov/sdat/pimg/995_21.png", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.getDataIdentification().getGraphicOverview().getFileName());

	Assert.assertEquals(-90, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getSouth(), 0.001);

	Assert.assertEquals(180, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getEast(), 0.001);

	Assert.assertEquals(-180, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getWest(), 0.001);

	Assert.assertEquals(90, (double) mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getGeographicBoundingBox().getNorth(), 0.001);

	Assert.assertEquals("1986-01-01T00:00:00Z", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getTemporalExtent().getBeginPosition());

	Assert.assertEquals("1995-12-31T00:00:00Z", mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getTemporalExtent().getEndPosition());

	// Assert.assertEquals(
	// "https://daac.ornl.gov/daacdata/islscp_ii/hydrology_soils/gpcc_precip_monthly_xdeg/data/gpcc_numgauges_1deg.zip",
	// mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnlines().next()
	// .getLinkage());
    }

    @Test
    public void test2() {

	CMRGranulesMapper mapper = new CMRGranulesMapper();

	Assert.assertEquals(CMRGranulesMetadataSchemas.ATOM_ENTRY.toString(), mapper.getSupportedOriginalMetadataSchema());

    }

}