package eu.essi_lab.iso.datamodel.test;

import java.util.List;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Online;
import junit.framework.TestCase;
import net.opengis.iso19139.gmd.v_20060504.CIContactType;

public class ContactTest extends MetadataTest<Contact, CIContactType> {

    public ContactTest() {
	super(Contact.class, CIContactType.class);
    }

    @Override
    public void setProperties(Contact metadata) {
	metadata.addPhoneVoice("phone1");
	metadata.addPhoneVoice("phone2");
	metadata.clearPhoneVoices();
	metadata.addPhoneVoice("0555226590");
	metadata.addPhoneVoice("0555226591");
	metadata.addPhoneFax("fax1");
	metadata.clearPhoneFaxList();
	metadata.addPhoneFax("0555226586");
	Address address = new Address();
	address.setCity("Firenze");
	metadata.setAddress(address);
	Online online = new Online();
	online.setLinkage("http://essi-lab.eu");
	metadata.setOnline(online);

    }

    @Override
    public void checkProperties(Contact metadata) {
	List<String> voices = Lists.newArrayList(metadata.getPhoneVoices());
	TestCase.assertEquals(2, voices.size());
	TestCase.assertEquals("0555226590", voices.get(0));
	TestCase.assertEquals("0555226591", voices.get(1));
	List<String> faxes = Lists.newArrayList(metadata.getPhoneFaxList());
	TestCase.assertEquals(1, faxes.size());
	TestCase.assertEquals("0555226586", faxes.get(0));
	TestCase.assertEquals("Firenze", metadata.getAddress().getCity());
	TestCase.assertEquals("http://essi-lab.eu", metadata.getOnline().getLinkage());

    }

    @Override
    public void clearProperties(Contact metadata) {
	metadata.clearPhoneVoices();
	metadata.clearPhoneFaxList();
	metadata.setAddress(null);
	metadata.setOnline(null);

    }

    @Override
    public void checkNullProperties(Contact metadata) {
	Assert.assertFalse(metadata.getPhoneVoices().hasNext());
	Assert.assertFalse(metadata.getPhoneFaxList().hasNext());
	Assert.assertNull(metadata.getAddress());
	Assert.assertNull(metadata.getOnline());

    }
}
