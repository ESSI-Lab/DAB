package eu.essi_lab.accessor.csw;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.csw.mapper.BLUECLOUDMapper;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;
import net.opengis.iso19139.gmx.v_20060504.AnchorType;

public class CSWBlueCloudMapperTest {

    private BLUECLOUDMapper mapper;

    @Before
    public void init() {
	this.mapper = new BLUECLOUDMapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = CSWBlueCloudMapperTest.class.getClassLoader().getResourceAsStream("EmodNET_Chemistry.xml");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);
	
	GSSource gsSource = Mockito.mock(GSSource.class);
	Mockito.when(gsSource.getEndpoint()).thenReturn("https://sextant.ifremer.fr/geonetwork/srv/eng/csw-EMODNET_Chemistry?request=GetCapabilities&service=CSW&version=2.0.2");

	HarmonizedMetadata result = mapper.map(originalMD, gsSource).getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	TestCase.assertEquals("35975e67-aea4-473a-9b67-806740be304b", metadata.getFileIdentifier());
	
	TestCase.assertEquals("utf8", metadata.getCharacterSetCode());
	TestCase.assertEquals("series", metadata.getHierarchyLevelScopeCodeListValue());
	TestCase.assertEquals("Product record", metadata.getHierarchyLevelName());

	// metadata contact

	TestCase.assertTrue(metadata.getContacts().hasNext());
	ResponsibleParty metadataContact = metadata.getContacts().next();
	TestCase.assertEquals("EMODnet Chemistry", metadataContact.getOrganisationName());
	Contact contactInfo = metadataContact.getContact();
//	TestCase.assertEquals("+33 (0)2 38.64.34.34", contactInfo.getPhoneVoices().next());
//	TestCase.assertEquals("+33 (0)2 38 64 35 18", contactInfo.getPhoneFaxList().next());
	Address address = contactInfo.getAddress();
//	TestCase.assertEquals("3, Avenue Claude GuilleminBP 6009", address.getDeliveryPoint());
//	TestCase.assertEquals("Orleans cedex 2", address.getCity());
//	TestCase.assertEquals("45060", address.getPostalCode());
//	TestCase.assertEquals("France", address.getCountry());
	TestCase.assertEquals("sextant@ifremer.fr", address.getElectronicMailAddress());
	Online online = contactInfo.getOnline();
	TestCase.assertEquals("https://emodnet.ec.europa.eu/en/chemistry", online.getLinkage());
	//TestCase.assertEquals("pointOfContact", metadataContact.getRoleCode());

	TestCase.assertEquals(null, metadata.getDateStamp());
	TestCase.assertEquals("ISO 19115:2003/19139 - EMODNET - SDN", metadata.getMetadataStandardName());
	TestCase.assertEquals("1.0", metadata.getMetadataStandardVersion());

	// spatial representation info
	// TODO

	// reference system info

	ReferenceSystem referenceSystem = metadata.getReferenceSystemInfos().next();
	TestCase.assertEquals("WGS 84 (EPSG:4326)", referenceSystem.getCode());
	// TODO

	// metadata extension info
	// TODO

	// data identification

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	TestCase.assertEquals(
		"Contaminants-Lead: stations below LOD/LOQ v2019",
		dataIdentification.getCitationTitle());
	TestCase.assertEquals("Lead_b_LOD_LOQ_stations_v2019", dataIdentification.getCitationAlternateTitle());
	TestCase.assertEquals(null, dataIdentification.getCitationRevisionDate());

	TestCase.assertEquals("35975e67-aea4-473a-9b67-806740be304b", dataIdentification.getResourceIdentifier());

	ResponsibleParty originator = dataIdentification.getPointOfContact("custodian");
	Object value = originator.getElementType().getOrganisationName().getCharacterString().getValue();
	AnchorType anchor = null;
	if (value instanceof AnchorType) {
	    anchor = (AnchorType) value;
	}
	Assert.assertNotNull(anchor);
	TestCase.assertEquals("https://edmo.seadatanet.org/report/120", anchor.getHref());
		
	TestCase.assertEquals("National Institute of Oceanography and Applied Geophysics - OGS, Division of Oceanography", originator.getOrganisationName());
	Contact originatorContactInfo = originator.getContact();
	//TestCase.assertEquals("+33 (0)2 38.64.34.34", originatorContactInfo.getPhoneVoices().next());
	//TestCase.assertEquals("+33 (0)2 38 64 35 18", originatorContactInfo.getPhoneFaxList().next());
	Address originatorAddress = originatorContactInfo.getAddress();
	TestCase.assertEquals("Borgo Grotta Gigante 42/c", originatorAddress.getDeliveryPoint());
	TestCase.assertEquals("Sgonico (Trieste)", originatorAddress.getCity());
	TestCase.assertEquals("34010", originatorAddress.getPostalCode());
	TestCase.assertEquals("Italy", originatorAddress.getCountry());
	TestCase.assertEquals("nodc@ogs.trieste.it", originatorAddress.getElectronicMailAddress());
	Online originatorOnline = originatorContactInfo.getOnline();
	TestCase.assertEquals("http://www.ogs.trieste.it/", originatorOnline.getLinkage());
	TestCase.assertEquals("custodian", originator.getRoleCode());

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(dataIdentification.getPointOfContacts());
	TestCase.assertEquals(118, contacts.size());

	TestCase.assertEquals(
		"This product displays the stations where lead has been measured and the values present in EMODnet Chemistry infrastructure are always below the limit of detection or quantification (LOD/LOQ), i.e quality values found in EMODnet validated dataset can be equal to 6 or Q. It is necessary to take into account that LOD/LOQ can change with time. These products aggregate data by station, producing only one final value for each station (above, below or above/below). EMODnet Chemistry has included the gathering of contaminants data since the beginning of the project in 2009. For the maps for EMODnet Chemistry Phase III, it was requested to plot data per matrix (water,sediment, biota), per biological entity and per chemical substance. The series of relevant map products have been developed according to the criteria D8C1 of the MSFD Directive, specifically focusing on the requirements under the new Commission Decision 2017/848 (17th May 2017). The Commission Decision points to relevant threshold values that are specified in the WFD, as well as relating how these contaminants should be expressed (units and matrix etc.) through the related Directives i.e. Priority substances for Water. EU EQS Directive does not fix any threshold values in sediments. On the contrary Regional Sea Conventions provide some of them, and these values have been taken into account for the development of the visualization products. To produce the maps the following process has been followed: 1. Data collection through SeaDataNet standards (CDI+ODV) 2. Harvesting, harmonization, validation and P01 code decomposition of data 3. SQL query on data sets from point 2 4. Production of map with each point representing at least one record that match the criteria The harmonization of all the data has been the most challenging task considering the heterogeneity of the data sources, sampling protocols. Preliminary processing were necessary to harmonize all the data : • For water: contaminants in the dissolved phase; • For sediment: data on total sediment (regardless of size class) or size class < 2000 μm • For biota: contaminant data will focus on molluscs, on fish (only in the muscle), and on crustaceans • Exclusion of data values equal to 0",
		dataIdentification.getAbstract());

	// resource maintenance
	// TODO

	ArrayList<String> gemetKeywords = Lists.newArrayList(dataIdentification.getKeywords("GEMET - INSPIRE themes, version 1.0"));
	
	TestCase.assertEquals(1, gemetKeywords.size());
	TestCase.assertEquals("Oceanographic geographical features", gemetKeywords.get(0));

	// // distribution info
	//
	// Distribution distributionInfo = metadata.getDistribution();
	// TestCase.assertNotNull(distributionInfo);
	//
	// Iterator<Online> onlineIterator = distributionInfo.getDistributionOnlines();
	// TestCase.assertNotNull(onlineIterator);
	// List<Online> onlines = Lists.newArrayList(onlineIterator);
	// TestCase.assertEquals(8, onlines.size());
	// Online online = onlines.get(0);
	// TestCase.assertNotNull(online);
	// TestCase.assertEquals("HTTP", online.getProtocol());
	// TestCase.assertEquals("http://myurl", online.getLinkage());
	// TestCase.assertEquals("Dataset homepage", online.getName());
	// TestCase.assertEquals("Dataset homepage", online.getDescription());
	// TestCase.assertEquals("information", online.getFunctionCode());
	//
	// ResponsibleParty poc = dataIdentification.getPointOfContact();
	// TestCase.assertNotNull(poc);
	//
	// TestCase.assertEquals("My Author", poc.getIndividualName());
	// TestCase.assertEquals("INSPIRE CZECH REPUBLIC", poc.getOrganisationName());
	// TestCase.assertEquals("author", poc.getRoleCode());
	// TestCase.assertEquals("myauthor@email.com", poc.getContact().getAddress().getElectronicMailAddress());
	//
	// ResponsibleParty poc2 = dataIdentification.getPointOfContact("custodian");
	//
	// TestCase.assertEquals("My Maintainer", poc2.getIndividualName());
	// TestCase.assertEquals("INSPIRE CZECH REPUBLIC", poc2.getOrganisationName());
	// TestCase.assertEquals("custodian", poc2.getRoleCode());
	// TestCase.assertEquals("mymaintainer@email.com", poc2.getContact().getAddress().getElectronicMailAddress());
	// List<LegalConstraints> constraints = Lists.newArrayList(dataIdentification.getLegalConstraints());
	// TestCase.assertEquals(2, constraints.size());
	// TestCase.assertEquals("Výdejní jednotka není stanovena", constraints.get(0).getUseLimitation());
	//
	// TestCase.assertEquals(
	// "Prohlížecí služba WMS pro téma Územní správní jendotky (AU) je veřejná prohlížecí služba pro poskytování dat
	// harmonizovaných dle INSPIRE tématu Územní správní jednotky (AU). Služba je dostupná na celém území České
	// Republiky. Služba splňuje technické pokyny pro INSPIRE prohlížeci služby ve verzi 3.11 a zároveň standardy
	// OGC WMS 1.1.1 a 1.3.0.",
	// dataIdentification.getAbstract());
	// List<String> keywords = Lists.newArrayList(dataIdentification.getKeywordsValues());
	// TestCase.assertEquals(17, keywords.size());
	// TestCase.assertEquals("Informační systém územní identifikace (ISÚI)", keywords.get(0));
	//
	// Iterator<TransferOptions> distributionOptionsIterator = distributionInfo.getDistributionTransferOptions();
	// TestCase.assertNotNull(distributionOptionsIterator);
	//
	// ArrayList<TransferOptions> distributionOptions = Lists.newArrayList(distributionOptionsIterator);
	//
	// TestCase.assertEquals(8, distributionOptions.size());
	//
	// TransferOptions distributionOption = distributionOptions.get(1);
	// onlineIterator = distributionOption.getOnlines();
	// online = onlineIterator.next();
	// TestCase.assertNotNull(online);
	// TestCase.assertEquals("My Format", online.getProtocol());
	// TestCase.assertEquals("http://services.cuzk.cz/wms/inspire-au-wms.asp?request=GetCapabilities",
	// online.getLinkage());
	// TestCase.assertEquals("INSPIRE prohlížecí služba pro téma Územní správní jednotky (AU)", online.getName());
	// TestCase.assertEquals("My Description", online.getDescription());
	// TestCase.assertEquals("download", online.getFunctionCode());
	// TestCase.assertEquals(999.0, distributionOption.getTransferSize());
	//
	// Distribution distribution = metadata.getDistribution();
	// TestCase.assertNotNull(distribution);
	//
	// TestCase.assertEquals("My Format", distribution.getFormat().getName());
	//
	// TestCase.assertEquals("2014-12-07", metadata.getDateStamp());
	//
	// String accessConstraints = "Opětovnému využití dat zpřístupněných službou pro obchodní účely je zamezeno
	// začleněním ochranných znaků (copyright ČÚZK).";
	//
	// TestCase.assertEquals(accessConstraints, constraints.get(1).getUseLimitation());
	//
	// GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	// TestCase.assertNotNull(bbox);
	// TestCase.assertEquals(12.09, bbox.getWest());
	// TestCase.assertEquals(18.85, bbox.getEast());
	// TestCase.assertEquals(51.05, bbox.getNorth());
	// TestCase.assertEquals(48.58, bbox.getSouth());
	// TestCase.assertEquals("cze", metadata.getLanguage());
	// ResponsibleParty metadataContact = metadata.getContacts().next();
	//
	// TestCase.assertEquals("pointOfContact", metadataContact.getRoleCode());
	// TestCase.assertEquals("cuzk.helpdesk@cuzk.cz",
	// metadataContact.getContact().getAddress().getElectronicMailAddresses().next());
	// TestCase.assertEquals("9999-09-09", dataIdentification.getCitationEditionDate());
	//
	// ReferenceSystem referenceSystem = metadata.getReferenceSystemInfos().next();
	//
	// TestCase.assertEquals("My Reference System", referenceSystem.getCode());
	// BoundingPolygon polygon = dataIdentification.getBoundingPolygons().next();
	// List<Double> coordinates = Lists.newArrayList(polygon.getCoordinates());
	// TestCase.assertEquals(48.58, coordinates.get(0), 10E-13);
	// TestCase.assertEquals(12.09, coordinates.get(1), 10E-13);
	// TestCase.assertEquals(10, coordinates.size());

    }
}
