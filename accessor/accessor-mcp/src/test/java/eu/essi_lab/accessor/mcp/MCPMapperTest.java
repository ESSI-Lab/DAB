package eu.essi_lab.accessor.mcp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class MCPMapperTest {

    private MCP2Mapper mapper;
    private OriginalMetadata originalMD;
    private GSSource source;

    @Before
    public void init() throws Exception {
	this.mapper = new MCP2Mapper();
	source = Mockito.mock(GSSource.class);
	originalMD = new OriginalMetadata();
	InputStream stream = MCPMapperTest.class.getClassLoader().getResourceAsStream("MCPGetRecordsResponse.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	Node result = reader.evaluateNode("//*:SearchResults/*[1]");
	String metadata = XMLDocumentReader.asString(result);
	originalMD.setMetadata(metadata);
	originalMD.setSchemeURI(CommonNameSpaceContext.MCP_2_NS_URI);
    }

    @Test
    public void test() throws Exception {
	GSResource result = mapper.map(originalMD, source);
	Assert.assertEquals("8b469535-eb48-4f1f-9612-bdf2e83cdce8", result.getHarmonizedMetadata().getCoreMetadata().getIdentifier());
	Assert.assertEquals(
		"IMOS - ACORN - Rottnest Shelf HF ocean radar site (Western Australia, Australia) - Real-time sea water velocity",
		result.getHarmonizedMetadata().getCoreMetadata().getTitle());
	// PARAMETER IDENTIFIERS
	List<CoverageDescription> descriptions = Lists
		.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata().getCoverageDescriptions());
	List<String> attributeDescriptions = new ArrayList<>();
	List<String> attributeIdentifiers = new ArrayList<>();
	for (CoverageDescription description : descriptions) {
	    attributeDescriptions.add(description.getAttributeDescription());
	    attributeIdentifiers.add(description.getAttributeIdentifier());
	}
	Assert.assertEquals(4, attributeDescriptions.size());
	Assert.assertTrue(attributeDescriptions.contains("Current direction in the water body"));
	Assert.assertTrue(attributeDescriptions.contains("Current speed in the water body"));
	Assert.assertTrue(attributeDescriptions.contains("Northward current velocity in the water body"));
	Assert.assertTrue(attributeDescriptions.contains("Eastward current velocity in the water body"));
	Assert.assertEquals(4, attributeIdentifiers.size());
	// terms from NERC collections must end with "/" cfr. RosettaStoneTest for reference. 
	Assert.assertTrue(attributeIdentifiers.contains("http://vocab.nerc.ac.uk/collection/P01/current/LCDAZZ01/"));
	Assert.assertTrue(attributeIdentifiers.contains("http://vocab.nerc.ac.uk/collection/P01/current/LCEWZZ01/"));
	Assert.assertTrue(attributeIdentifiers.contains("http://vocab.nerc.ac.uk/collection/P01/current/LCNSZZ01/"));
	// terms from AODN must NOT end with "/" cfr. RosettaStoneTest for reference. 
	Assert.assertTrue(attributeIdentifiers.contains("http://vocab.aodn.org.au/def/discovery_parameter/entity/383"));
	// INSTRUMENT IDENTIFIERS
	List<MIInstrument> instruments = Lists
		.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIInstruments());
	List<String> instrumentIdentifiers = new ArrayList<>();
	for (MIInstrument instrument : instruments) {
	    instrumentIdentifiers.add(instrument.getMDIdentifierCode());
	}
	Assert.assertEquals(1, instrumentIdentifiers.size());
	// terms from NERC collections must end with "/" cfr. RosettaStoneTest for reference.
	Assert.assertTrue(instrumentIdentifiers.contains("http://vocab.nerc.ac.uk/collection/L05/current/303/"));
	// PLATFORM IDENTIFIERS
	List<MIPlatform> platforms = Lists.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatforms());
	List<String> platformIdentifiers = new ArrayList<>();
	for (MIPlatform platform : platforms) {
	    platformIdentifiers.add(platform.getMDIdentifierCode());
	}
	Assert.assertEquals(1, platformIdentifiers.size());
	// terms from AODN must NOT end with "/" cfr. RosettaStoneTest for reference.
	Assert.assertTrue(platformIdentifiers.contains("http://vocab.aodn.org.au/def/platform/entity/726"));

    }

}
