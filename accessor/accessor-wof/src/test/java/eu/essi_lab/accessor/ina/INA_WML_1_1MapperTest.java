package eu.essi_lab.accessor.ina;

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

import eu.essi_lab.accessor.wof.WML_1_1Mapper;
import eu.essi_lab.accessor.wof.utils.WOFIdentifierMangler;
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

public class INA_WML_1_1MapperTest {
    private WML_1_1Mapper mapper;

    @Before
    public void init() {
	this.mapper = new WML_1_1Mapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = INA_WML_1_1MapperTest.class.getClassLoader().getResourceAsStream("ina_sitesResponse.xml");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);

	GSSource source = new GSSource();
	source.setEndpoint("https://alerta.ina.gob.ar/wml");
	GSResource resource = mapper.map(originalMD, source);

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	String siteIdentifier = "alturas_bdhi:2";
	String variableCode = "INA:29";
	String variableName = "Discharge";

	WOFIdentifierMangler mangler = new WOFIdentifierMangler();

	mangler.setPlatformIdentifier(siteIdentifier);
	mangler.setParameterIdentifier(variableCode);
	mangler.setSourceIdentifier("5");
	mangler.setMethodIdentifier("1");
	mangler.setQualityIdentifier("");

	String identifier = mangler.getMangling();

	String code = AbstractResourceMapper.generateCode(resource, identifier);

	TestCase.assertEquals(code, metadata.getDataIdentification().getResourceIdentifier());
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());
	TestCase.assertEquals("English", metadata.getLanguage());
	TestCase.assertEquals("utf8", metadata.getCharacterSetCode());
	TestCase.assertEquals("dataset", metadata.getHierarchyLevelScopeCodeListValue());

	MIPlatform platform = metadata.getMIPlatform();

	TestCase.assertEquals("argentina-ina:" + siteIdentifier, platform.getMDIdentifierCode());

	String siteName = "Rosario del Tala";

	// String siteComments = "";
	//
	// String siteType = "";

	TestCase.assertEquals(siteName, platform.getDescription());

	assertKeyword("Hydrology", metadata);
	// Hydrology, Surface Water, metros cúbicos por segundo, Flow, Field Observation, Sporadic

	TestCase.assertEquals(siteName, platform.getCitation().getTitle());

	GeographicBoundingBox box = metadata.getDataIdentification().getGeographicBoundingBox();

	TestCase.assertEquals(-32.2918333333333, box.getNorth(), 10 ^ -7);
	TestCase.assertEquals(-59.0568055555556, box.getEast(), 10 ^ -7);

	Iterator<ReferenceSystem> crsIterator = metadata.getReferenceSystemInfos();

	List<ReferenceSystem> crsList = Lists.newArrayList(crsIterator);

	ReferenceSystem crs1 = crsList.get(0);
	TestCase.assertEquals("EPSG", crs1.getCodeSpace());
	TestCase.assertEquals("4326", crs1.getCode());
	ReferenceSystem crs2 = crsList.get(1);
	TestCase.assertEquals("4326", crs2.getCode());

	VerticalExtent verticalExtent = metadata.getDataIdentification().getVerticalExtent();
	TestCase.assertEquals(null, verticalExtent);

	MDResolution resolution = metadata.getDataIdentification().getSpatialResolution();
	TestCase.assertEquals(null, resolution);

	CoverageDescription coverageDescription = metadata.getCoverageDescription();
	TestCase.assertEquals("argentina-ina:" + variableCode, coverageDescription.getAttributeIdentifier());
	TestCase.assertEquals(variableName, coverageDescription.getAttributeTitle());

	String attributeDescription = variableName
		+ " Data type: Sporadic Value type: Field Observation Units: metros cúbicos por segundo Units type: Flow Unit abbreviation:  No data value: -9999 Speciation: ";

	TestCase.assertEquals(attributeDescription, coverageDescription.getAttributeDescription());

	String valueType = "Field Observation";
	assertKeywordByThesaurus(valueType, WML_1_1Mapper.CUAHSI_VALUE_TYPE_CV, metadata);

	String lineage = "medición directa";
	TestCase.assertEquals(lineage, metadata.getDataQualities().next().getLineageStatement());

	TestCase.assertEquals("1988-11-23T13:00:00Z", metadata.getDataIdentification().getTemporalExtent().getBeginPosition());
	TestCase.assertEquals("2016-07-13T15:00:00Z", metadata.getDataIdentification().getTemporalExtent().getEndPosition());

	TestCase.assertEquals(null, metadata.getDataIdentification().getTopicCategoryString());

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	String dataType = "Sporadic";
	assertKeywordByThesaurus(dataType, WML_1_1Mapper.CUAHSI_DATA_TYPE_CV, metadata);
	String title = siteName + " - " + variableName + " - " + dataType;
	TestCase.assertEquals(title, dataIdentification.getCitationTitle());

	ResponsibleParty originator = dataIdentification.getPointOfContact("custodian");

	// TestCase.assertEquals("", originator.getOrganisationName());
	// TestCase.assertEquals("Jeff Horsburgh", originator.getIndividualName());
	// TestCase.assertEquals("main", originator.getPositionName());
	// Contact originatorContactInfo = originator.getContact();
	//
	// TestCase.assertEquals("1-435-797-2946", originatorContactInfo.getPhoneVoices().next());
	// Address originatorAddress = originatorContactInfo.getAddress();
	// TestCase.assertEquals("8200 Old Main Hill\n,Logan, UT 84322-8200", originatorAddress.getDeliveryPoint());
	// TestCase.assertEquals("jeff.horsburgh@usu.edu", originatorAddress.getElectronicMailAddress());
	// Online originatorOnline = originatorContactInfo.getOnline();
	// TestCase.assertEquals("http://littlebearriver.usu.edu", originatorOnline.getLinkage());
	// TestCase.assertEquals("custodian", originator.getRoleCode());

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(dataIdentification.getPointOfContacts());
	TestCase.assertEquals(1, contacts.size());

	GridSpatialRepresentation grid = metadata.getGridSpatialRepresentation();
	Assert.assertEquals((long) 1, (long) grid.getNumberOfDimensions());
	Assert.assertEquals("point", grid.getCellGeometryCode());
	Dimension time = grid.getAxisDimension();
	Assert.assertEquals("time", time.getDimensionNameTypeCode());
	Assert.assertEquals(new BigInteger("229"), time.getDimensionSize());
	Assert.assertEquals(null, time.getResolutionUOM());
	Assert.assertEquals(null, time.getResolutionValue());

	// Surface Water, metros cúbicos por segundo, Flow, Field Observation, Sporadic

	assertKeyword("Flow", metadata);
	assertKeyword("metros cúbicos por segundo", metadata);

	assertKeyword("Sporadic", metadata);

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