package eu.essi_lab.accessor.stac;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.stac.harvested.STACCollectionMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class STACResourceMapperTest {

    @Test
    public void test() throws IOException, GSException {

	InputStream stream = STACResourceMapperTest.class.getClassLoader().getResourceAsStream("deaSTACOriginal.json");

	String md = IOStreamUtils.asUTF8String(stream);

	OriginalMetadata om = new OriginalMetadata();
	om.setMetadata(md);

	GSSource source = new GSSource();
	source.setUniqueIdentifier(UUID.randomUUID().toString());

	STACCollectionMapper mapper = new STACCollectionMapper();
	GSResource mapped = mapper.map(om, source);

	String origid = mapped.getOriginalId().get();

	Assert.assertEquals("cci_landcover", origid);

	Assert.assertEquals("1992-01-01T00:00:00+00:00",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent().getBeginPosition());
	
	Assert.assertEquals("2019-01-01T00:00:00+00:00",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent().getEndPosition());

	Assert.assertEquals("ESA CCI Landcover",
		mapped.getHarmonizedMetadata().getCoreMetadata().getTitle());
	
	Assert.assertTrue(mapped.getHarmonizedMetadata().getCoreMetadata().getAbstract().contains("As part of the ESA Climate Change Initiative (CCI)"));
	
	Assert.assertEquals("https://ows.digitalearth.africa/wms?",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline().getLinkage());
	
	Assert.assertEquals("cci_landcover",
		mapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline().getName());

//	Assert.assertEquals("Marine and Coastal Management; Department of Environmental Affairs and Tourism", mapped.getHarmonizedMetadata()
//		.getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact().getOrganisationName());

    }
}