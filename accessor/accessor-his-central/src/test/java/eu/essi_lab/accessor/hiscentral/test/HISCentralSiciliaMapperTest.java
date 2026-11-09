package eu.essi_lab.accessor.hiscentral.test;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.sicilia.HISCentralSiciliaMapper;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

public class HISCentralSiciliaMapperTest {

    @Test
    public void test() throws Exception {

	OriginalMetadata originalMD = new OriginalMetadata();
	originalMD.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
	originalMD.setMetadata(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("siciliaMetadataStation.json"),
		StandardCharsets.UTF_8));

	GSSource source = new GSSource();
	source.setEndpoint("https://www.protezionecivilesicilia.it:8443/DatascapeA/");

	GSResource resource = new HISCentralSiciliaMapper().map(originalMD, source);

	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();

	Assert.assertEquals("Canicatti' SPC73 [X] - Temperatura Aria", core.getTitle());

	GeographicBoundingBox bbox = core.getBoundingBox();
	Assert.assertEquals(37.43, bbox.getNorth(), 0.0001);
	Assert.assertEquals(13.828055, bbox.getEast(), 0.0001);

	TemporalExtent extent = core.getTemporalExtent();
	Assert.assertEquals("2020-07-14T00:00:00", extent.getBeginPosition());
	Assert.assertTrue(extent.isEndPositionIndeterminate());

	Online online = core.getOnline();
	Assert.assertEquals("https://www.protezionecivilesicilia.it:8443/DatascapeA/v3/data-combo/68615", online.getLinkage());
	Assert.assertEquals(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI, online.getProtocol());
	Assert.assertEquals("download", online.getFunctionCode());

	Assert.assertEquals("Temperatura Aria", core.getMIMetadata().getCoverageDescription().getAttributeTitle());
	Assert.assertEquals("°C", resource.getExtensionHandler().getAttributeUnits().get());
	Assert.assertEquals("776300", core.getMIMetadata().getMIPlatform().getMDIdentifierCode());
	Assert.assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
	Assert.assertEquals("PT10M", resource.getExtensionHandler().getTimeResolutionDuration8601().get());
    }
}
