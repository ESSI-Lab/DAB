package eu.essi_lab.profiler.cdi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.edmo.EDMOClient;

public class EDMO_EDMERP_ResolverExternalTestIT {

    private EDMOClient edmoClient = null;

    @Before
    public void init() {
	edmoClient = new EDMOClient();
    }

    @Test
    public void test1() {
	String originatorOrganisationIdentifier = "http://www.seadatanet.org/urnurl/SDN:EDMO::1348";
	Assert.assertEquals("Zoological Museum, Natural History Museum, University of Copenhagen",
		edmoClient.getLabelFromURI(originatorOrganisationIdentifier));
    }

    @Test
    public void test2() {
	String originatorOrganisationIdentifier = "http://www.seadatanet.org/urnurl/SDN:EDMO::631";
	Assert.assertEquals("Delft Hydraulics", edmoClient.getLabelFromURI(originatorOrganisationIdentifier));
	ResponsibleParty party = edmoClient.getResponsiblePartyFromURI(originatorOrganisationIdentifier);
	Assert.assertEquals("Delft Hydraulics", party.getOrganisationName());
	Assert.assertEquals("+31 15 2858585", party.getContact().getPhoneVoices().next());
	Assert.assertEquals("+31 15 2858582", party.getContact().getPhoneFaxList().next());
	Assert.assertEquals("Rotterdamseweg 185", party.getContact().getAddress().getDeliveryPoint());
	Assert.assertEquals("Delft", party.getContact().getAddress().getCity());
	Assert.assertEquals("2629 HD", party.getContact().getAddress().getPostalCode());
	Assert.assertEquals("Netherlands", party.getContact().getAddress().getCountry());
	Assert.assertEquals("mailto:info%40wldelft.nl", party.getContact().getAddress().getElectronicMailAddress());
	Assert.assertEquals("http://www.wldelft.nl", party.getContact().getOnline().getLinkage());
    }
}
