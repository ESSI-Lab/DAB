package eu.essi_lab.iso.datamodel.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.ServiceIdentification;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.iso.datamodel.todo.OperationMetadata;

public class ServiceIdentificationTest {

    @Test
    public void test() throws Exception {

	InputStream is = ServiceIdentificationTest.class.getClassLoader().getResourceAsStream("serviceIdentification.xml");

	Assert.assertNotNull(is);

	MDMetadata mdMetadata = new MDMetadata(is);

	// Iterator<DataIdentification> dataIdentifications = mdMetadata.getDataIdentifications();
	// System.out.println(dataIdentifications.hasNext());
	Iterator<ServiceIdentification> serviceIdentifications = mdMetadata.getServiceIdentifications();
	Assert.assertTrue(serviceIdentifications.hasNext());

	ServiceIdentification serviceId = serviceIdentifications.next();

	String title = serviceId.getCitationTitle();

	Assert.assertEquals("\nElevation WMS of the Earth Observation Center (EOC), DLR\n", title);

	String description = serviceId.getAbstract();

	Assert.assertEquals(
		"\nThis WMS provides access to different elevation products provided by the Earth Observation Center (EOC) of the DLR.\n",
		description);

	GeographicBoundingBox bbox = serviceId.getGeographicBoundingBox();

	Double east = bbox.getEast();
	Double west = bbox.getWest();
	Double north = bbox.getNorth();
	Double south = bbox.getSouth();

	Assert.assertEquals(Double.valueOf(-180), west);
	Assert.assertEquals(Double.valueOf(180), east);
	Assert.assertEquals(Double.valueOf(-90), south);
	Assert.assertEquals(Double.valueOf(84), north);

	String alternativeTitle = serviceId.getCitationAlternateTitle();
	Assert.assertNull(alternativeTitle);
	String editionDate = serviceId.getCitationEditionDate();
	Assert.assertNull(editionDate);
	ResponsibleParty contact = serviceId.getPointOfContact();
	String individual = contact.getIndividualName();
	Assert.assertNull(individual);
	String organisation = contact.getOrganisationName();
	Assert.assertEquals("German Aerospace Center (DLR)", organisation);
	String role = contact.getRoleCode();
	Assert.assertEquals("pointOfContact", role);
	Contact cc = contact.getContact();
	Address address = cc.getAddress();
	String mail = address.getElectronicMailAddress();
	Assert.assertEquals("geoservice@dlr.de", mail);
	Online online = cc.getOnline();

	String linkage = online.getLinkage();
	Assert.assertEquals("http://geoservice.dlr.de/eoc/elevation/wms?service=wms&request=GetCapabilities", linkage.trim());
	String name = online.getName();
	Assert.assertNull(name);
	String descrp = online.getDescription();
	Assert.assertNull(descrp);
	String functioncode = online.getFunctionCode();
	Assert.assertNull(functioncode);
	String prot = online.getProtocol();
	Assert.assertNull(prot);

	String publicationDate = serviceId.getCitationPublicationDate();
	Assert.assertNull(publicationDate);
	BrowseGraphic browseGraphic = serviceId.getGraphicOverview();
	Assert.assertNull(browseGraphic);

	TemporalExtent extent = serviceId.getTemporalExtent();
	Assert.assertNull(extent);
	Iterator<Keywords> keywords = serviceId.getKeywords();
	List<Keywords> listK = new ArrayList<>();
	while (keywords.hasNext()) {
	    listK.add(keywords.next());
	}
	Keywords firstKeywords = listK.get(0);
	Iterator<String> iteratorString = firstKeywords.getKeywords();
	List<String> tocheck = new ArrayList<String>();
	while (iteratorString.hasNext()) {
	    String el = iteratorString.next();
	    tocheck.add(el);
	}
	Assert.assertTrue(tocheck.contains("DLR"));
	Assert.assertTrue(tocheck.contains("Elevation"));
	Assert.assertTrue(tocheck.contains("SRTM"));
	Assert.assertTrue(tocheck.contains("X-SAR"));
	Assert.assertTrue(tocheck.contains("TanDEM-X"));
	Assert.assertTrue(tocheck.contains("TDM90"));
	Assert.assertTrue(tocheck.contains("EOC"));
	String creationDate = serviceId.getCitationCreationDate();
	Assert.assertNull(creationDate);
	XMLGregorianCalendar dateTime = serviceId.getCitationCreationDateTime();
	Assert.assertNull(dateTime);
	// Iterator<LegalConstraints> legalconstraints = serviceId.getLegalConstraints();
	// List<LegalConstraints> legalConst = new ArrayList<>();
	// while (legalconstraints.hasNext()) {
	// legalConst.add(legalconstraints.next());
	// }
	// LegalConstraints lc = legalConst.get(0);

	String id = serviceId.getResourceIdentifier();
	Assert.assertEquals("\nhttps://geoservice.dlr.de/catalogue/srv/metadata/caf151d7-6a7c-42f6-894c-30e6dbce96d5\n", id);
	VerticalExtent vertical = serviceId.getVerticalExtent();
	Assert.assertNull(vertical);
	Object topic = serviceId.getTopicCategory();
	Assert.assertNull(topic);

	String revision = serviceId.getCitationRevisionDate();
	Assert.assertEquals("2019-02-07T00:22:00", revision);
	XMLGregorianCalendar dateRev = serviceId.getCitationRevisionDateTime();
	Assert.assertEquals("2019-02-07T00:22:00", dateRev.toString());

	Iterator<OperationMetadata> operationsMetadata = serviceId.getOperationMetadatas();
	Assert.assertTrue(operationsMetadata.hasNext());

	// online
	Iterator<Online> onlines = mdMetadata.getDistribution().getDistributionOnlines();
	Assert.assertTrue(onlines.hasNext());
	Online o = onlines.next();
	String linkage1 = o.getLinkage();
	Assert.assertEquals("http://geoservice.dlr.de/eoc/elevation/wms?SERVICE=WMS&", linkage1.trim());
	String name1 = o.getName();
	String protocol1 = o.getProtocol();
	Assert.assertNull(name1);
	Assert.assertEquals("application/vnd.ogc.wms_xml",protocol1);
	
	Iterator<String> scopedNames = serviceId.getScopedNames();
	Assert.assertTrue(scopedNames.hasNext());
	while(scopedNames.hasNext()) {
	    String variable = scopedNames.next();
	    System.out.println(variable);
	}

    }

}