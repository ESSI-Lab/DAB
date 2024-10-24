package eu.essi_lab.accessor.ckan;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class CKANMapperTest {

    private CKANMapper mapper;

    @Before
    public void init() {
	this.mapper = new CKANMapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = CKANMapperTest.class.getClassLoader().getResourceAsStream("danube_package_show.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);

	HarmonizedMetadata result = mapper.map(originalMD, new GSSource()).getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	// TestCase.assertEquals("0092a24f-9fec-4f7a-958d-0a4472f8767b", result.getOriginalId());

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	TestCase.assertEquals(null, metadata.getFileIdentifier());
	// data identification

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	TestCase.assertEquals("INSPIRE prohlížecí služba pro téma Územní správní jednotky (AU)", dataIdentification.getCitationTitle());
	TestCase.assertEquals("inspire-prohlieci-sluba-pro-tema-uzemni-spravni-jednotky-au",
		dataIdentification.getCitationAlternateTitle());
	TestCase.assertEquals("My Last Modified", dataIdentification.getCitationRevisionDate());

	// distribution info

	Distribution distributionInfo = metadata.getDistribution();
	TestCase.assertNotNull(distributionInfo);

	Iterator<Online> onlineIterator = distributionInfo.getDistributionOnlines();
	TestCase.assertNotNull(onlineIterator);
	List<Online> onlines = Lists.newArrayList(onlineIterator);
	TestCase.assertEquals(8, onlines.size());
	Online online = onlines.get(0);
	TestCase.assertNotNull(online);
	TestCase.assertEquals("HTTP", online.getProtocol());
	TestCase.assertEquals("http://myurl", online.getLinkage());
	TestCase.assertEquals("Dataset homepage", online.getName());
	TestCase.assertEquals("Dataset homepage", online.getDescription());
	TestCase.assertEquals("information", online.getFunctionCode());

	ResponsibleParty poc = dataIdentification.getPointOfContact();
	TestCase.assertNotNull(poc);

	TestCase.assertEquals("My Author", poc.getIndividualName());
	TestCase.assertEquals("INSPIRE CZECH REPUBLIC", poc.getOrganisationName());
	TestCase.assertEquals("author", poc.getRoleCode());
	TestCase.assertEquals("myauthor@email.com", poc.getContact().getAddress().getElectronicMailAddress());

	ResponsibleParty poc2 = dataIdentification.getPointOfContact("custodian");

	TestCase.assertEquals("My Maintainer", poc2.getIndividualName());
	TestCase.assertEquals("INSPIRE CZECH REPUBLIC", poc2.getOrganisationName());
	TestCase.assertEquals("custodian", poc2.getRoleCode());
	TestCase.assertEquals("mymaintainer@email.com", poc2.getContact().getAddress().getElectronicMailAddress());
	List<LegalConstraints> constraints = Lists.newArrayList(dataIdentification.getLegalConstraints());
	TestCase.assertEquals(2, constraints.size());
	TestCase.assertEquals("Výdejní jednotka není stanovena", constraints.get(0).getUseLimitation());

	TestCase.assertEquals(
		"Prohlížecí služba WMS pro téma Územní správní jendotky (AU) je veřejná prohlížecí služba pro poskytování dat harmonizovaných dle INSPIRE tématu Územní správní jednotky (AU). Služba je dostupná na celém území České Republiky. Služba splňuje technické pokyny pro INSPIRE prohlížeci služby ve verzi 3.11 a zároveň standardy OGC WMS 1.1.1 a 1.3.0.",
		dataIdentification.getAbstract());
	List<String> keywords = Lists.newArrayList(dataIdentification.getKeywordsValues());
	TestCase.assertEquals(17, keywords.size());
	TestCase.assertEquals("Informační systém územní identifikace (ISÚI)", keywords.get(0));

	Iterator<TransferOptions> distributionOptionsIterator = distributionInfo.getDistributionTransferOptions();
	TestCase.assertNotNull(distributionOptionsIterator);

	ArrayList<TransferOptions> distributionOptions = Lists.newArrayList(distributionOptionsIterator);

	TestCase.assertEquals(8, distributionOptions.size());

	TransferOptions distributionOption = distributionOptions.get(1);
	onlineIterator = distributionOption.getOnlines();
	online = onlineIterator.next();
	TestCase.assertNotNull(online);
	TestCase.assertEquals("My Format", online.getProtocol());
	TestCase.assertEquals("http://services.cuzk.cz/wms/inspire-au-wms.asp?request=GetCapabilities", online.getLinkage());
	TestCase.assertEquals("INSPIRE prohlížecí služba pro téma Územní správní jednotky (AU)", online.getName());
	TestCase.assertEquals("My Description", online.getDescription());
	TestCase.assertEquals("download", online.getFunctionCode());
	TestCase.assertEquals(999.0, distributionOption.getTransferSize());

	Distribution distribution = metadata.getDistribution();
	TestCase.assertNotNull(distribution);

	TestCase.assertEquals("My Format", distribution.getFormat().getName());

	TestCase.assertEquals("2014-12-07", metadata.getDateStamp());

	String accessConstraints = "Opětovnému využití dat zpřístupněných službou pro obchodní účely je zamezeno začleněním ochranných znaků (copyright ČÚZK).";

	TestCase.assertEquals(accessConstraints, constraints.get(1).getUseLimitation());

	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertNotNull(bbox);
	TestCase.assertEquals(12.09, bbox.getWest());
	TestCase.assertEquals(18.85, bbox.getEast());
	TestCase.assertEquals(51.05, bbox.getNorth());
	TestCase.assertEquals(48.58, bbox.getSouth());
	TestCase.assertEquals("cze", metadata.getLanguage());
	ResponsibleParty metadataContact = metadata.getContacts().next();

	TestCase.assertEquals("pointOfContact", metadataContact.getRoleCode());
	TestCase.assertEquals("cuzk.helpdesk@cuzk.cz", metadataContact.getContact().getAddress().getElectronicMailAddresses().next());
	TestCase.assertEquals("9999-09-09", dataIdentification.getCitationEditionDate());

	ReferenceSystem referenceSystem = metadata.getReferenceSystemInfos().next();

	TestCase.assertEquals("My Reference System", referenceSystem.getCode());
	BoundingPolygon polygon = dataIdentification.getBoundingPolygons().next();
	List<Double> coordinates = Lists.newArrayList(polygon.getCoordinates());
	TestCase.assertEquals(48.58, coordinates.get(0), 10E-13);
	TestCase.assertEquals(12.09, coordinates.get(1), 10E-13);
	TestCase.assertEquals(10, coordinates.size());

    }
}
