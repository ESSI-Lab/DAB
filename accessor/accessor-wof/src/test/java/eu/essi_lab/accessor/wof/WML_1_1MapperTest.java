package eu.essi_lab.accessor.wof;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wof.utils.WOFIdentifierMangler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import junit.framework.TestCase;

public class WML_1_1MapperTest {
    private WML_1_1Mapper mapper;

    @Before
    public void init() {
	this.mapper = new WML_1_1Mapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = WML_1_1MapperTest.class.getClassLoader().getResourceAsStream("cuahsi/mock/his4wmlTS.xml");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);

	GSSource source = new GSSource();
	source.setEndpoint("http://my-his-server");

	GSResource resource = mapper.map(originalMD, source);

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	String siteIdentifier = "LBR:USU-LBR-Mendon";
	String variableCode = "USU3";
	String variableName = "Battery voltage";
	String seriesIdentifier = "LBR:" + variableCode;

	WOFIdentifierMangler mangler = new WOFIdentifierMangler();

	mangler.setPlatformIdentifier(siteIdentifier);
	mangler.setParameterIdentifier(seriesIdentifier);
	mangler.setSourceIdentifier("1");
	mangler.setMethodIdentifier("4");
	mangler.setQualityIdentifier("0");

	String identifier = mangler.getMangling();

	String code = AbstractResourceMapper.generateCode(resource, identifier);

	// method;4;parameter;LBR:USU3;platform;LBR:USU-LBR-Mendon;quality;0;source;1
	// 044BF55FFE8AB2A68DF043217DEFC78F29D67561

	TestCase.assertEquals(code, metadata.getDataIdentification().getResourceIdentifier());
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());
	TestCase.assertEquals("English", metadata.getLanguage());
	TestCase.assertEquals("utf8", metadata.getCharacterSetCode());
	TestCase.assertEquals("dataset", metadata.getHierarchyLevelScopeCodeListValue());

	MIPlatform platform = metadata.getMIPlatform();

	TestCase.assertEquals("cuahsi:" + siteIdentifier, platform.getMDIdentifierCode());

	String siteName = "Little Bear River at Mendon Road near Mendon, Utah";

	String siteComments = "Located below county road bridge at Mendon Road crossing";

	String siteType = "Stream";

	TestCase.assertEquals(siteName + " ; " + siteType + " ; " + siteComments, platform.getDescription());

	assertKeyword("Stream", metadata);

	String county = "Cache";
	String state = "Utah";
	String geographicLocation = county + " ; " + state;

	assertKeywordByType(county, "place", metadata);

	assertKeywordByType(state, "place", metadata);

	TestCase.assertEquals(siteName, platform.getCitation().getTitle());

	GeographicBoundingBox box = metadata.getDataIdentification().getGeographicBoundingBox();
	String description = metadata.getDataIdentification().getGeographicDescription();

	TestCase.assertEquals(41.718473, box.getNorth(), 10 ^ -7);
	TestCase.assertEquals(-111.946402, box.getEast(), 10 ^ -7);
	TestCase.assertEquals(geographicLocation, description);
	Iterator<ReferenceSystem> crsIterator = metadata.getReferenceSystemInfos();

	List<ReferenceSystem> crsList = Lists.newArrayList(crsIterator);

	ReferenceSystem crs1 = crsList.get(0);
	TestCase.assertEquals("EPSG", crs1.getCodeSpace());
	TestCase.assertEquals("4269", crs1.getCode());
	ReferenceSystem crs2 = crsList.get(1);
	TestCase.assertEquals("NAD83 / UTM zone 12N", crs2.getCode());

	VerticalExtent verticalExtent = metadata.getDataIdentification().getVerticalExtent();
	TestCase.assertEquals(1345., verticalExtent.getMinimumValue(), 10 ^ -7);
	TestCase.assertEquals(1345., verticalExtent.getMaximumValue(), 10 ^ -7);

	String vcrsId = verticalExtent.getVerticalCRS().getId();
	TestCase.assertEquals("NGVD29", vcrsId);

	MDResolution resolution = metadata.getDataIdentification().getSpatialResolution();
	TestCase.assertEquals("m", resolution.getDistanceUOM());
	TestCase.assertEquals(10., resolution.getDistanceValue(), 10 ^ -7);

	CoverageDescription coverageDescription = metadata.getCoverageDescription();
	TestCase.assertEquals("cuahsi:" + seriesIdentifier, coverageDescription.getAttributeIdentifier());
	TestCase.assertEquals(variableName, coverageDescription.getAttributeTitle());

	String attributeDescription = variableName
		+ " Data type: Minimum Value type: Field Observation Units: volts Units type: Potential Difference Unit abbreviation: V No data value: -9999 Speciation: Not Applicable";

	TestCase.assertEquals(attributeDescription, coverageDescription.getAttributeDescription());

	String valueType = "Field Observation";
	assertKeywordByThesaurus(valueType, WML_1_1Mapper.CUAHSI_VALUE_TYPE_CV, metadata);

	String lineage = "Battery voltage measured by Campbell Scientific CR206 datalogger.";
	TestCase.assertEquals(lineage, metadata.getDataQualities().next().getLineageStatement());

	TestCase.assertEquals("2005-08-04T18:30:00Z", metadata.getDataIdentification().getTemporalExtent().getBeginPosition());
	TestCase.assertEquals("2016-08-02T19:30:00Z", metadata.getDataIdentification().getTemporalExtent().getEndPosition());

	TestCase.assertEquals("inlandWaters", metadata.getDataIdentification().getTopicCategoryString());

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	String dataType = "Minimum";
	assertKeywordByThesaurus(dataType, WML_1_1Mapper.CUAHSI_DATA_TYPE_CV, metadata);
	String title = siteName + " - " + variableName + " - " + dataType;
	TestCase.assertEquals(title, dataIdentification.getCitationTitle());

	TestCase.assertEquals("Little Bear River Conservation Effects Assessment Project Water Quality Data",
		dataIdentification.getCitationAlternateTitle());

	TestCase.assertEquals(
		"Under funding from the United States Department of Agricultures Conservations Effects Assessment program, USU is conducting continuous water quality monitoring of the Little Bear River to demonstrate and compare the use of alternative monitoring approaches in estimating pollutant concentrations and loads.",
		dataIdentification.getAbstract());

	ResponsibleParty originator = dataIdentification.getPointOfContact("pointOfContact");

	TestCase.assertEquals("Utah State University Utah Water Research Laboratory", originator.getOrganisationName());
	TestCase.assertEquals("Jeff Horsburgh", originator.getIndividualName());
	TestCase.assertEquals("main", originator.getPositionName());
	Contact originatorContactInfo = originator.getContact();

	TestCase.assertEquals("1-435-797-2946", originatorContactInfo.getPhoneVoices().next());
	Address originatorAddress = originatorContactInfo.getAddress();
	TestCase.assertEquals("8200 Old Main Hill\n,Logan, UT 84322-8200", originatorAddress.getDeliveryPoint());
	TestCase.assertEquals("jeff.horsburgh@usu.edu", originatorAddress.getElectronicMailAddress());
	Online originatorOnline = originatorContactInfo.getOnline();
	TestCase.assertEquals("http://littlebearriver.usu.edu", originatorOnline.getLinkage());
	TestCase.assertEquals("pointOfContact", originator.getRoleCode());

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(dataIdentification.getPointOfContacts());
	TestCase.assertEquals(1, contacts.size());

	GridSpatialRepresentation grid = metadata.getGridSpatialRepresentation();
	Assert.assertEquals((long) 1, (long) grid.getNumberOfDimensions());
	Assert.assertEquals("point", grid.getCellGeometryCode());
	Dimension time = grid.getAxisDimension();
	Assert.assertEquals("time", time.getDimensionNameTypeCode());
	Assert.assertEquals(new BigInteger("186664"), time.getDimensionSize());

	assertKeywordByType("Instrumentation", "discipline", metadata);

	assertKeywordByType("Not Relevant", "stratum", metadata);

	assertKeyword("Potential Difference", metadata);
	assertKeyword("volts", metadata);

	assertKeyword("Raw data", metadata);

	Assert.assertEquals("WaterML 1.1", metadata.getDistribution().getFormat().getName());
	Online online = metadata.getDistribution().getDistributionOnline();
	Assert.assertEquals(identifier, online.getName());
	Assert.assertEquals("", online.getLinkage());
	Assert.assertEquals(NetProtocolWrapper.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN(), online.getProtocol());
	Assert.assertEquals("download", online.getFunctionCode());

    }

    private void assertKeyword(String expectedValue, MIMetadata metadata) {
	assertKeywordByType(expectedValue, null, metadata);
    }

    private void assertKeywordByThesaurus(String expectedValue, String thesaurus, MIMetadata metadata) {
	assertKeyword(expectedValue, null, thesaurus, metadata);
    }

    private void assertKeywordByType(String expectedValue, String expectedType, MIMetadata metadata) {
	assertKeyword(expectedValue, expectedType, null, metadata);
    }

    /**
     * Assert there is the keyword with the specified value and/or type and/or thesaurus
     * 
     * @param expectedValue
     * @param expectedType can be null
     * @param metadata
     */
    private void assertKeyword(String expectedValue, String expectedType, String expectedThesaurus, MIMetadata metadata) {
	List<Keywords> keys = Lists.newArrayList(metadata.getDataIdentification().getKeywords());
	for (Keywords key : keys) {
	    List<String> values = Lists.newArrayList(key.getKeywords());
	    for (String v : values) {
		if (v.equals(expectedValue)) {
		    if (expectedType != null && !key.getTypeCode().equals(expectedType)) {
			TestCase.fail("Keyword not found: " + expectedValue);
		    }
		    if (expectedThesaurus != null && !key.getThesaurusNameCitationTitle().equals(expectedThesaurus)) {
			TestCase.fail("Keyword not found: " + expectedValue);
		    }
		    return;
		}
	    }

	}
	// keyword/type not found!
	TestCase.fail("Keyword not found: " + expectedValue);

    }
}