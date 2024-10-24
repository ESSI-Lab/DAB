package eu.essi_lab.accessor.ckan;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.ckan.datamodel.CKANDataset;
import junit.framework.TestCase;

public class CKANParserTest {

    private CKANParser parser;

    @Before
    public void init() {
	this.parser = new CKANParser();
    }

    /**
     * Tests the mapping from the example CKAN dataset from
     * http://drdsi.jrc.ec.europa.eu/api/3/action/package_show?id=inspire-prohlieci-sluba-pro-tema-uzemni-spravni-jednotky-au
     * Note: the source example has been modified to add some extra CKAN metadata fields that were missing. The added
     * fields start with "My ..."
     * 
     * @throws Exception
     */
    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = CKANParserTest.class.getClassLoader().getResourceAsStream("danube_package_show.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);

	TestCase.assertEquals("0092a24f-9fec-4f7a-958d-0a4472f8767b", dataset.getId());
	TestCase.assertEquals("inspire-prohlieci-sluba-pro-tema-uzemni-spravni-jednotky-au", dataset.getName());
	TestCase.assertEquals("INSPIRE prohlížecí služba pro téma Územní správní jednotky (AU)", dataset.getTitle());
	TestCase.assertEquals("http://myurl", dataset.getUrl());
	TestCase.assertEquals("My Author", dataset.getAuthor());
	TestCase.assertEquals("myauthor@email.com", dataset.getAuthorEmail());
	TestCase.assertEquals("My Maintainer", dataset.getMaintainer());
	TestCase.assertEquals("mymaintainer@email.com", dataset.getMaintainerEmail());
	TestCase.assertEquals("My License Id", dataset.getLicenseId());
	TestCase.assertEquals("My License Title", dataset.getLicenseTitle());
	TestCase.assertEquals("My Version", dataset.getVersion());
	TestCase.assertEquals(
		"Prohlížecí služba WMS pro téma Územní správní jendotky (AU) je veřejná prohlížecí služba pro poskytování dat harmonizovaných dle INSPIRE tématu Územní správní jednotky (AU). Služba je dostupná na celém území České Republiky. Služba splňuje technické pokyny pro INSPIRE prohlížeci služby ve verzi 3.11 a zároveň standardy OGC WMS 1.1.1 a 1.3.0.",
		dataset.getNotes());
	TestCase.assertEquals("My Primary Theme", dataset.getPrimaryTheme());
	TestCase.assertEquals("My Unpublished", dataset.getUnpublished());
	TestCase.assertNotNull(dataset.getOrganization());
	TestCase.assertEquals("INSPIRE CZECH REPUBLIC", dataset.getOrganization());
	TestCase.assertNotNull(dataset.getTags());
	TestCase.assertEquals(17, dataset.getTags().size());
	TestCase.assertNotNull(dataset.getTags().get(0));
	TestCase.assertEquals("My Vocabulary Id", dataset.getTags().get(0).getVocabularyId());
	TestCase.assertEquals("Informační systém územní identifikace (ISÚI)", dataset.getTags().get(0).getDisplayName());
	TestCase.assertEquals("Informační systém územní identifikace (ISÚI)", dataset.getTags().get(0).getName());
	TestCase.assertEquals("My Revision Timestamp", dataset.getTags().get(0).getRevisionTimestamp());
	TestCase.assertEquals("active", dataset.getTags().get(0).getState());
	TestCase.assertEquals("bd2069f3-536e-4858-93af-7487a80ab90b", dataset.getTags().get(0).getId());
	TestCase.assertEquals("active", dataset.getState());
	TestCase.assertNotNull(dataset.getRelations());
	TestCase.assertEquals(2, dataset.getRelations().size());
	TestCase.assertNotNull(dataset.getRelations().get(0));
	TestCase.assertEquals("My Comment", dataset.getRelations().get(0).getComment());
	TestCase.assertEquals("My Type", dataset.getRelations().get(0).getType());
	TestCase.assertEquals("My Id", dataset.getRelations().get(0).getObjectId());
	TestCase.assertEquals("My Subject Package Id", dataset.getRelations().get(0).getSubjectId());
	TestCase.assertNotNull(dataset.getRelations().get(1));
	TestCase.assertEquals("My Comment", dataset.getRelations().get(1).getComment());
	TestCase.assertEquals("My Type", dataset.getRelations().get(1).getType());
	TestCase.assertEquals("My Object Package Id", dataset.getRelations().get(1).getObjectId());
	TestCase.assertEquals("My Id", dataset.getRelations().get(1).getSubjectId());
	TestCase.assertNotNull(dataset.getResources());
	TestCase.assertEquals(7, dataset.getResources().size());
	TestCase.assertNotNull(dataset.getResources().get(0));
	TestCase.assertEquals("My Cache URL", dataset.getResources().get(0).getCachedURL());
	TestCase.assertEquals("http://services.cuzk.cz/wms/inspire-au-wms.asp?request=GetCapabilities",
		dataset.getResources().get(0).getUrl());
	TestCase.assertEquals("INSPIRE prohlížecí služba pro téma Územní správní jednotky (AU)", dataset.getResources().get(0).getName());
	TestCase.assertEquals("My Description", dataset.getResources().get(0).getDescription());
	TestCase.assertEquals("My Type", dataset.getResources().get(0).getType());
	TestCase.assertEquals("My Resource Type", dataset.getResources().get(0).getResourceType());
	TestCase.assertEquals("My Format", dataset.getResources().get(0).getFormat());
	TestCase.assertEquals("My Mime Type", dataset.getResources().get(0).getMimetype());
	TestCase.assertEquals("My Mime Type Inner", dataset.getResources().get(0).getMimetypeInner());
	TestCase.assertEquals("999.0", dataset.getResources().get(0).getSize());
	TestCase.assertEquals("My Last Modified", dataset.getResources().get(0).getLastModified());
	TestCase.assertEquals("My Hash", dataset.getResources().get(0).getHash());
	TestCase.assertEquals("My Resource Group Id", dataset.getResources().get(0).getResourceGroupId());
	TestCase.assertEquals("My Revision Timestamp", dataset.getResources().get(0).getRevisionTimeStamp());
	TestCase.assertEquals("f14dc685-6bbc-4617-b714-efb9a9b22075", dataset.getResources().get(0).getId());
	TestCase.assertEquals("active", dataset.getResources().get(0).getState());
	TestCase.assertEquals("My Url Type", dataset.getResources().get(0).getUrlType());
	TestCase.assertEquals("2014-12-09T20:09:34.954251", dataset.getResources().get(0).getCreated());
	TestCase.assertNotNull(dataset.getExtras());
	TestCase.assertEquals(25, dataset.getExtras().size());
	TestCase.assertNotNull(dataset.getExtras().get(1));
	TestCase.assertEquals("bbox-east-long", dataset.getExtras().get(1).getKey());
	TestCase.assertEquals("18.85", dataset.getExtras().get(1).getValue());
	TestCase.assertEquals(
		"Opětovnému využití dat zpřístupněných službou pro obchodní účely je zamezeno začleněním ochranných znaků (copyright ČÚZK).",
		dataset.getAccessConstraints());
	TestCase.assertEquals("48.58", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("12.09", dataset.getBboxWestLongitude());
	TestCase.assertEquals("51.05", dataset.getBboxNorthLatitude());
	TestCase.assertEquals("18.85", dataset.getBboxEastLongitude());
	TestCase.assertEquals("12.09 48.58 18.85 51.05 ", dataset.getBboxExtended());
	TestCase.assertEquals("cuzk.helpdesk@cuzk.cz", dataset.getContactEmail());
	TestCase.assertEquals("2013-09-29", dataset.getDatasetCreationDate());
	TestCase.assertEquals("9999-09-09", dataset.getDatasetPublicationDate());
	TestCase.assertEquals("2013-09-29", dataset.getDatasetRevisionDate());

	TestCase.assertEquals("My Frequency", dataset.getFrequencyOfUpdate());
	TestCase.assertEquals(
		"/INSPIRE-16542303-763e-11e4-8b38-52540004b857_20141208-214650/services/1/PullResults/461-480/services/2_ID_CZ-CUZK-WMS-AU",
		dataset.getGuid());
	TestCase.assertEquals("2014-12-09T20:09:34.714316", dataset.getHarvestTimestamp());
	TestCase.assertEquals("Výdejní jednotka není stanovena", dataset.getLicense());
	TestCase.assertEquals("2014-12-07", dataset.getMetadataDate());
	TestCase.assertEquals("cze", dataset.getMetadataLanguage());
	TestCase.assertEquals("My Progress", dataset.getProgress());
	TestCase.assertEquals("Český úřad zeměměřický a katastrální", dataset.getResponsibleParty());
	TestCase.assertEquals("view", dataset.getSpatialDataServiceType());
	TestCase.assertEquals("My Reference System", dataset.getSpatialReferenceSystem());
	TestCase.assertEquals("true", dataset.getSpatialHarvester());
	TestCase.assertEquals("71cd789c-047f-4005-bcfe-205f2509d266", dataset.getHarvestObjectId());
	TestCase.assertEquals("d19ded78-b6f9-4b95-ab3a-1ca54b69e7ab", dataset.getHarvestSourceId());
	TestCase.assertEquals("INSPIRE Czeck Republic", dataset.getHarvestSourceTitle());
	TestCase.assertEquals("9999-12-09T19:09:34.754708", dataset.getRevisionTimestamp());
	TestCase.assertEquals("2014-12-09T19:09:34.754708", dataset.getMetadataCreated());
	TestCase.assertEquals("2014-12-09T19:09:34.833675", dataset.getMetadataModified());
	TestCase.assertEquals("f9d4e78a-acce-4bc7-9af1-eb39f385f4a2", dataset.getCreatorUserId());

    }
}
