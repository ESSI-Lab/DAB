package eu.essi_lab.accessor.nodc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class NODCMapperTest {

    private NODCMapper mapper;
    private OriginalMetadata originalMD;
    private GSSource source;

    @Before
    public void init() throws Exception {
	this.mapper = new NODCMapper();
	source = Mockito.mock(GSSource.class);
	originalMD = new OriginalMetadata();
	InputStream stream = NODCMapperTest.class.getClassLoader().getResourceAsStream("NODCGetRecordsResponse.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	Node result = reader.evaluateNode("//*:SearchResults/*[1]");
	String metadata = XMLDocumentReader.asString(result);
	originalMD.setMetadata(metadata);
	originalMD.setSchemeURI(CommonNameSpaceContext.MCP_2_NS_URI);
    }

    @Test
    public void test() throws Exception {
	GSResource result = mapper.map(originalMD, source);
	Assert.assertEquals("gov.noaa.nodc:0123645", result.getHarmonizedMetadata().getCoreMetadata().getIdentifier());
	Assert.assertEquals(
		"Oceanographic and surface meteorological data collected from University of Michigan Marine Hydrodynamics Laboratories Bio Buoy by University of Michigan and assembled by Great Lakes Observing System (GLOS) in the Great Lakes region from 2014-07-01 to 2017-05-31 (NODC Accession 0123645)",
		result.getHarmonizedMetadata().getCoreMetadata().getTitle());
	// PARAMETER IDENTIFIERS
	List<CoverageDescription> descriptions = Lists
		.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata().getCoverageDescriptions());
	List<String> attributesDescriptions = new ArrayList<>();
	List<String> attributesIdentifiers = new ArrayList<>();
	for (CoverageDescription description : descriptions) {
	    attributesDescriptions.add(description.getAttributeDescription());
	    attributesIdentifiers.add(description.getAttributeIdentifier());
	}
	Assert.assertEquals(9, attributesDescriptions.size());
	List<String> expectedParametersIdentifiers = Arrays
		.asList(new String[] { "https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/112", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/3", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/373", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/38", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/431", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/436", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/437", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/590", //
			"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details/591" });
	Assert.assertTrue(attributesIdentifiers.containsAll(expectedParametersIdentifiers));
	List<String> expectedParametersDescriptions = Arrays.asList(new String[] { //
		"DEWPOINT", "AIR TEMPERATURE", "WATER TEMPERATURE", "BAROMETRIC PRESSURE", "WIND DIRECTION", "WIND GUST", "WIND SPEED",
		"LATITUDE", "LONGITUDE" });
	Assert.assertTrue(attributesDescriptions.containsAll(expectedParametersDescriptions));
	// INSTRUMENT IDENTIFIERS
	List<MIInstrument> instruments = Lists
		.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIInstruments());
	List<String> instrumentIdentifiers = new ArrayList<>();
	for (MIInstrument instrument : instruments) {
	    instrumentIdentifiers.add(instrument.getMDIdentifierCode());
	}
	Assert.assertEquals(4, instrumentIdentifiers.size());
	List<String> expectedInstruments = Arrays.asList(new String[] { "Aerovanes", //
		"barometers", //
		"CTD", //
		"Temperature Sensors"}); //
//		"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/172", //
//		"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/174", //
//		"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/175", //
//		"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details/8" });
	Assert.assertTrue(instrumentIdentifiers.containsAll(expectedInstruments));
	// PLATFORM IDENTIFIERS
	List<MIPlatform> platforms = Lists.newArrayList(result.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatforms());
	List<String> platformIdentifiers = new ArrayList<>();
	for (MIPlatform platform : platforms) {
	    platformIdentifiers.add(platform.getMDIdentifierCode());
	}
	Assert.assertEquals(1, platformIdentifiers.size());
	List<String> expectedPlatforms = Arrays.asList(new String[] { "FIXED PLATFORM"}); //
		//"https://www.nodc.noaa.gov/cgi-bin/OAS/prd/platform/details/2576" });
	Assert.assertTrue(platformIdentifiers.containsAll(expectedPlatforms));
	// ORIGINATOR ORGANIZATION IDENTIFIER
	ExtensionHandler handler = result.getExtensionHandler();
	List<String> originatorOrganisationIdentifiers = handler.getOriginatorOrganisationIdentifiers();
	Assert.assertEquals(1, originatorOrganisationIdentifiers.size());
	Assert.assertTrue(originatorOrganisationIdentifiers.contains("https://www.nodc.noaa.gov/cgi-bin/OAS/prd/institution/details/1717"));

    }

}
