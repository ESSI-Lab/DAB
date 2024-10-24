package eu.essi_lab.iso.datamodel.test;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import junit.framework.TestCase;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyType;

public class ResponsiblePartyTest extends MetadataTest<ResponsibleParty, CIResponsiblePartyType> {

    public ResponsiblePartyTest() {
	super(ResponsibleParty.class, CIResponsiblePartyType.class);
    }

    @Override
    public void setProperties(ResponsibleParty party) {
	party.setIndividualName("Individual Name");
	party.setOrganisationName("Organisation Name");
	party.setPositionName("Position Name");
	Contact contact = new Contact();
	Online online = new Online();
	online.setLinkage("http://essi-lab.eu");
	contact.setOnline(online);
	party.setContactInfo(contact);
	party.setRoleCode("custodian");

    }

    @Override
    public void checkProperties(ResponsibleParty party) {
	TestCase.assertEquals("http://essi-lab.eu", party.getContact().getOnline().getLinkage());
	TestCase.assertEquals("Individual Name", party.getIndividualName());
	TestCase.assertEquals("Organisation Name", party.getOrganisationName());
	TestCase.assertEquals("Position Name", party.getPositionName());
	TestCase.assertEquals("custodian", party.getRoleCode());

    }

    @Override
    public void clearProperties(ResponsibleParty party) {
	party.setIndividualName(null);
	party.setOrganisationName(null);
	party.setPositionName(null);
	party.setContactInfo(null);
	party.setRoleCode(null);

    }

    @Override
    public void checkNullProperties(ResponsibleParty party) {
	TestCase.assertEquals(null, party.getContact());
	TestCase.assertEquals(null, party.getIndividualName());
	TestCase.assertEquals(null, party.getOrganisationName());
	TestCase.assertEquals(null, party.getPositionName());
	TestCase.assertEquals(null, party.getRoleCode());

    }
}
