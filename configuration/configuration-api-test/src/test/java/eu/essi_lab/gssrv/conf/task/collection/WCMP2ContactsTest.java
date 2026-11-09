package eu.essi_lab.gssrv.conf.task.collection;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Tests the contacts of the WCMP2 records
 *
 * @author boldrini
 */
public class WCMP2ContactsTest {

    @Test
    public void storedEmailTest() throws Exception {

	// the e-mail of a point of contact, as set by the collection creator, is kept once the collection is stored and
	// read back
	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("INA");
	Address address = new Address();
	address.addElectronicMailAddress("info@ina.gob.ar");
	Contact contact = new Contact();
	contact.setAddress(address);
	party.setContactInfo(contact);

	DatasetCollection collection = new DatasetCollection();
	collection.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addPointOfContact(party);

	GSResource restored = GSResource.create(collection.asDocument(true));
	ResponsibleParty restoredParty = restored.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getPointOfContactParty().get(0);
	Assert.assertEquals("INA", restoredParty.getOrganisationName());
	Assert.assertEquals("info@ina.gob.ar", restoredParty.getContact().getAddress().getElectronicMailAddress());
    }

    @Test
    public void encodingTest() {

	JSONObject contact = WISUtils.getContact("INA", " info@ina.gob.ar ", "host");
	System.out.println(contact.toString(3));
	Assert.assertEquals("INA", contact.getString("organization"));
	Assert.assertEquals("info@ina.gob.ar", contact.getJSONArray("emails").getJSONObject(0).getString("value"));
	Assert.assertEquals("host", contact.getJSONArray("roles").getString(0));

	// no e-mail
	contact = WISUtils.getContact("CNR-ITIAm", null, "publisher");
	Assert.assertFalse(contact.has("emails"));
	Assert.assertEquals("publisher", contact.getJSONArray("roles").getString(0));
    }
}
