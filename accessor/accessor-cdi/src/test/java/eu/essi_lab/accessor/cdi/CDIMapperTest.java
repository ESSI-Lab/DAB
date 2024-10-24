package eu.essi_lab.accessor.cdi;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

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

public class CDIMapperTest {

    private CDIMapper mapper;

    @Before
    public void init() {
	this.mapper = new CDIMapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = CDIMapperTest.class.getClassLoader().getResourceAsStream("cdiExample.xml");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);
	
	GSSource gsSource = Mockito.mock(GSSource.class);
	Mockito.when(gsSource.getEndpoint()).thenReturn("http://seadatanet.maris2.nl/cdi_aggregation/sdn-cdi-aggr-seadatanet_v3.xml");

	HarmonizedMetadata result = mapper.map(originalMD, gsSource).getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	TestCase.assertEquals("urn:SDN:CDI:LOCAL:1022-DS02-4", metadata.getFileIdentifier());
	TestCase.assertEquals("English", metadata.getLanguage());
	TestCase.assertEquals("utf8", metadata.getCharacterSetCode());
	TestCase.assertEquals("dataset", metadata.getHierarchyLevelScopeCodeListValue());
	TestCase.assertEquals("Common Data Index record", metadata.getHierarchyLevelName());

	// metadata contact

	TestCase.assertTrue(metadata.getContacts().hasNext());
	ResponsibleParty metadataContact = metadata.getContacts().next();
	TestCase.assertEquals("BRGM / Office of Geological and Mining Resources", metadataContact.getOrganisationName());
	Contact contactInfo = metadataContact.getContact();
	TestCase.assertEquals("+33 (0)2 38.64.34.34", contactInfo.getPhoneVoices().next());
	TestCase.assertEquals("+33 (0)2 38 64 35 18", contactInfo.getPhoneFaxList().next());
	Address address = contactInfo.getAddress();
	TestCase.assertEquals("3, Avenue Claude GuilleminBP 6009", address.getDeliveryPoint());
	TestCase.assertEquals("Orleans cedex 2", address.getCity());
	TestCase.assertEquals("45060", address.getPostalCode());
	TestCase.assertEquals("France", address.getCountry());
	TestCase.assertEquals("unknown", address.getElectronicMailAddress());
	Online online = contactInfo.getOnline();
	TestCase.assertEquals("http://www.brgm.fr/", online.getLinkage());
	TestCase.assertEquals("pointOfContact", metadataContact.getRoleCode());

	TestCase.assertEquals("2017-02-13", metadata.getDateStamp());
	TestCase.assertEquals("ISO 19115/SeaDataNet profile", metadata.getMetadataStandardName());
	TestCase.assertEquals("1.0", metadata.getMetadataStandardVersion());
	TestCase.assertEquals("2017-02-13", metadata.getDateStamp());

	// spatial representation info
	// TODO

	// reference system info

	ReferenceSystem referenceSystem = metadata.getReferenceSystemInfos().next();
	TestCase.assertEquals("World Geodetic System 84", referenceSystem.getCode());
	// TODO

	// metadata extension info
	// TODO

	// data identification

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	TestCase.assertEquals(
		"SeaDataNet - Chemical oceanography from BRGM / Office of Geological and Mining Resources, point  observations",
		dataIdentification.getCitationTitle());
	TestCase.assertEquals("1022-DS02-4", dataIdentification.getCitationAlternateTitle());
	TestCase.assertEquals("2017-02-13", dataIdentification.getCitationRevisionDate());

	TestCase.assertEquals("urn:SDN:CDI:LOCAL:1022-DS02-4", dataIdentification.getResourceIdentifier());

	ResponsibleParty originator = dataIdentification.getPointOfContact("custodian");

	TestCase.assertEquals("BRGM / Office of Geological and Mining Resources", originator.getOrganisationName());
	Contact originatorContactInfo = originator.getContact();
	TestCase.assertEquals("+33 (0)2 38.64.34.34", originatorContactInfo.getPhoneVoices().next());
	TestCase.assertEquals("+33 (0)2 38 64 35 18", originatorContactInfo.getPhoneFaxList().next());
	Address originatorAddress = originatorContactInfo.getAddress();
	TestCase.assertEquals("3, Avenue Claude GuilleminBP 6009", originatorAddress.getDeliveryPoint());
	TestCase.assertEquals("Orleans cedex 2", originatorAddress.getCity());
	TestCase.assertEquals("45060", originatorAddress.getPostalCode());
	TestCase.assertEquals("France", originatorAddress.getCountry());
	TestCase.assertEquals("unknown", originatorAddress.getElectronicMailAddress());
	Online originatorOnline = originatorContactInfo.getOnline();
	TestCase.assertEquals("http://www.brgm.fr/", originatorOnline.getLinkage());
	TestCase.assertEquals("custodian", originator.getRoleCode());

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(dataIdentification.getPointOfContacts());
	TestCase.assertEquals(1, contacts.size());

	TestCase.assertEquals(
		"SeaDataNet is the Pan-European infrastructure for marine and ocean data management and delivery services. It is supported by the EU under its Research Infrastructures programme. It  connects 40 National Oceanographic Data Centres (NODC's) and 50 other data centres from 35 countries, bordering the European seas and Atlantic Ocean. The centres are mostly part of major marine management and research organisations that are acquiring and managing a large collection of marine and ocean data from various disciplines. This includes major international organisations, ICES and IOC-IODE. The overall objective is provide overview and access to marine and oceanographic data and data-products from government and research institutes in Europe. SeaDataNet contributes to the implementation of the EU INSPIRE and Marine Strategy Framework Directives. It also plays a key role in the development and operation of the EU EMODNet initiative. The SeaDataNet infrastructure is fully operational and INSPIRE compliant. It includes a versatile SeaDataNet portal (http://www.seadatanet.org) that provides users with a range of metadata, data and data product access services as well as standards, tools and guides for good marine data management. The Common Data Index (CDI) data discovery & access service provides harmonised access to the large volumes of datasets that are managed by the connected data centres. The CDI service contains already references and gives access to more than 1,5 milllion marine and oceanographic datasets as managed by 90 data centres. These numbers are increasing regularly because of further data population and more connected data centres as part of SeaDataNet II, EMODnet and other EU projects. For inclusion in the SeaDataNet INSPIRE compliant CSW service, the CDI records (at granule level) have been aggregated into CDI collections by a combination of Discipline, Data Centre, and geometric type. Each CSW XML record therefore represents a large collection of individual metadata records and associated datasets. By following the specified URL to the SeaDataNet portal users can evaluate these metadata in detail and request access by downloading of interesting datasets via the shopping cart transaction system that is integrated in the SeaDataNet portal.",
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
