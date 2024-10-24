package eu.essi_lab.accessor.oaipmh.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.oaipmh.DataCiteResourceMapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class OAIPMHDataCiteMapperTest {

    DataCiteResourceMapper mapper;

    @Before
    public void init() {
	this.mapper = new DataCiteResourceMapper();
    }

    @Test
    public void test() throws IOException, GSException {

	String record = IOStreamUtils
		.asUTF8String(OAIPMHDataCiteMapperTest.class.getClassLoader().getResourceAsStream("pangaeaDatacite.xml"));

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(record);
	originalMD.setSchemeURI(CommonNameSpaceContext.OAI_DATACITE_NS_URI);

	GSSource gsSource = Mockito.mock(GSSource.class);
	Mockito.when(gsSource.getEndpoint()).thenReturn("http://ws.pangaea.de/oai/provider?");

	GSResource resource = mapper.map(originalMD, gsSource);

	String identifier = resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
	Assert.assertEquals("10.1594/PANGAEA.759690", identifier);

	String abstract_ = resource.getHarmonizedMetadata().getCoreMetadata().getAbstract();
	Assert.assertTrue(abstract_.contains("In this study"));

	String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	Assert.assertTrue(title.contains("Radiance, irradiance"));
    }
}