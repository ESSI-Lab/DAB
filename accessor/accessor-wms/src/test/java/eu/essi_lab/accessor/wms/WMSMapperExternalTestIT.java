package eu.essi_lab.accessor.wms;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wms._1_3_0.WMS_1_3_0ResourceMapper;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class WMSMapperExternalTestIT {

    private WMS_1_3_0ResourceMapper mapper;

    @Before
    public void init() {
	this.mapper = new WMS_1_3_0ResourceMapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = WMSMapperExternalTestIT.class.getClassLoader().getResourceAsStream("geoss-135-afromaison-capabilities.xml");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);

	GSResource resource = mapper.map(originalMD, new GSSource());
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	TestCase.assertEquals("clima_anual_iberia", resource.getOriginalId().get());
	TestCase.assertEquals(null, metadata.getLanguage());
	TestCase.assertEquals(null, metadata.getCharacterSetCode());
	TestCase.assertEquals(null, metadata.getHierarchyLevelScopeCodeListValue());
	TestCase.assertEquals(null, metadata.getHierarchyLevelName());

	// metadata contact

	TestCase.assertTrue(metadata.getContacts().hasNext());
	ResponsibleParty metadataContact = metadata.getContacts().next();
	TestCase.assertEquals("Unidad de Botánica", metadataContact.getOrganisationName());
	Contact contactInfo = metadataContact.getContact();
	TestCase.assertEquals("+34 93 581 29 85", contactInfo.getPhoneVoices().next());
	TestCase.assertEquals("+34 93 581 13 21", contactInfo.getPhoneFaxList().next());
	Address address = contactInfo.getAddress();
	TestCase.assertEquals("Fac. Ciencias. Universidad Autónoma de Barcelona", address.getDeliveryPoint());
	TestCase.assertEquals("Bellaterra", address.getCity());
	TestCase.assertEquals("08193", address.getPostalCode());
	TestCase.assertEquals("España", address.getCountry());
	TestCase.assertEquals("miquel.niyerola@uab.es", address.getElectronicMailAddress());
	Online contactOnline = contactInfo.getOnline();
	TestCase.assertEquals("http://www.creaf.uab.es", contactOnline.getLinkage());
	TestCase.assertEquals(null, metadataContact.getRoleCode());

	TestCase.assertEquals(null, metadata.getDateStamp());
	TestCase.assertEquals(null, metadata.getMetadataStandardName());
	TestCase.assertEquals(null, metadata.getMetadataStandardVersion());

	// spatial representation info
	// TODO

	// reference system info

	ReferenceSystem referenceSystem = metadata.getReferenceSystemInfos().next();
	TestCase.assertEquals("EPSG:25829", referenceSystem.getCode());
	// TODO

	// metadata extension info
	// TODO

	// data identification

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	TestCase.assertEquals("Clima Anual de la Península Ibèrica.", dataIdentification.getCitationTitle());
	TestCase.assertEquals(null, dataIdentification.getCitationAlternateTitle());
	TestCase.assertEquals(null, dataIdentification.getCitationRevisionDate());

	TestCase.assertEquals("clima_anual_iberia", dataIdentification.getResourceIdentifier());

	ResponsibleParty originator = dataIdentification.getPointOfContact("custodian");

	BrowseGraphic overview = dataIdentification.getGraphicOverview();
	TestCase.assertEquals("image/png", overview.getFileType());
	TestCase.assertEquals(
		"http://www.opengis.uab.es/cgi-bin/iberia/MiraMon.cgi?SERVICE=WMS&REQUEST=GetMap&LAYERS=clima_anual_iberia&VERSION=1.3.0&STYLES=Pluvio&CRS=EPSG:4326&FORMAT=image%2Fpng&TRANSPARENT=TRUE&WIDTH=300&HEIGHT=183&BBOX=35.795746,-9.348001,43.744508,3.631159",
		overview.getFileName());

	TestCase.assertNull(originator);
	// TestCase.assertEquals("BRGM / Office of Geological and Mining Resources", originator.getOrganisationName());
	// Contact originatorContactInfo = originator.getContact();
	// TestCase.assertEquals("+33 (0)2 38.64.34.34", originatorContactInfo.getPhoneVoices().next());
	// TestCase.assertEquals("+33 (0)2 38 64 35 18", originatorContactInfo.getPhoneFaxList().next());
	// Address originatorAddress = originatorContactInfo.getAddress();
	// TestCase.assertEquals("3, Avenue Claude GuilleminBP 6009", originatorAddress.getDeliveryPoint());
	// TestCase.assertEquals("Orleans cedex 2", originatorAddress.getCity());
	// TestCase.assertEquals("45060", originatorAddress.getPostalCode());
	// TestCase.assertEquals("France", originatorAddress.getCountry());
	// TestCase.assertEquals("unknown", originatorAddress.getElectronicMailAddress());
	// Online originatorOnline = originatorContactInfo.getOnline();
	// TestCase.assertEquals("http://www.brgm.fr/", originatorOnline.getLinkage());
	// TestCase.assertEquals("custodian", originator.getRoleCode());

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(dataIdentification.getPointOfContacts());
	TestCase.assertEquals(0, contacts.size());

	// TestCase.assertEquals("Servidor del Atlas Climático Digital de la Península Ibérica",
	// dataIdentification.getAbstract());

	// resource maintenance
	// TODO

	// ArrayList<String> gemetKeywords = Lists.newArrayList(dataIdentification.getKeywords("GEMET - INSPIRE themes,
	// version 1.0"));

	// TestCase.assertEquals(1, gemetKeywords.size());
	// TestCase.assertEquals("Oceanographic geographical features", gemetKeywords.get(0));

	// // distribution info
	//
	Distribution distributionInfo = metadata.getDistribution();
	TestCase.assertNotNull(distributionInfo);

	Iterator<Online> onlineIterator = distributionInfo.getDistributionOnlines();
	TestCase.assertNotNull(onlineIterator);
	List<Online> onlines = Lists.newArrayList(onlineIterator);
	TestCase.assertEquals(1, onlines.size());
	Online online = onlines.get(0);
	TestCase.assertNotNull(online);
	TestCase.assertEquals(NetProtocolWrapper.WMS_1_3_0.getCommonURN(), online.getProtocol());
	TestCase.assertEquals("http://www.opengis.uab.es/cgi-bin/iberia/MiraMon.cgi?", online.getLinkage());
	TestCase.assertEquals("clima_anual_iberia", online.getName());
	TestCase.assertEquals(null, online.getDescription());
	TestCase.assertEquals("download", online.getFunctionCode());
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
	List<String> keywords = Lists.newArrayList(dataIdentification.getKeywordsValues());
	TestCase.assertEquals(9, keywords.size());
	TestCase.assertTrue(keywords.contains("Clima"));
	TestCase.assertTrue(keywords.contains("precipitación"));
	TestCase.assertTrue(keywords.contains("temperatura"));
	TestCase.assertTrue(keywords.contains("radiación solar"));
	TestCase.assertTrue(keywords.contains("Radiació solar"));
	TestCase.assertTrue(keywords.contains("Temperatura màxima"));
	TestCase.assertTrue(keywords.contains("Temperatura mínima"));
	TestCase.assertTrue(keywords.contains("Temperatura mitjana"));
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
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertNotNull(bbox);
	TestCase.assertEquals(-9.348001, bbox.getWest(), 0.01);
	TestCase.assertEquals(3.631159, bbox.getEast(), 0.01);
	TestCase.assertEquals(43.744508, bbox.getNorth(), 0.01);
	TestCase.assertEquals(35.795746, bbox.getSouth(), 0.01);
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
