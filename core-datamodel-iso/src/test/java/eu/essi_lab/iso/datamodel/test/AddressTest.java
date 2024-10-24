package eu.essi_lab.iso.datamodel.test;

import java.util.List;

import org.junit.Assert;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Address;
import net.opengis.iso19139.gmd.v_20060504.CIAddressType;

public class AddressTest extends MetadataTest<Address, CIAddressType> {

    public AddressTest() {
	super(Address.class, CIAddressType.class);
    }

    @Override
    public void setProperties(Address metadata) {
	metadata.setCity("Sesto Fiorentino");
	metadata.setAdministrativeArea("Firenze");
	metadata.setCountry("Italy");
	metadata.setPostalCode("50019");
	metadata.addDeliveryPoint("test1");
	metadata.addElectronicMailAddress("testmail@mail.com");
	metadata.clearDeliveryPoints();
	metadata.clearElectronicMailAddresses();
	metadata.addDeliveryPoint("Via Madonna del Piano 10");
	metadata.addElectronicMailAddress("info@essi-lab.eu");
    }

    @Override
    public void checkProperties(Address metadata) {
	Assert.assertEquals("Sesto Fiorentino", metadata.getCity());
	Assert.assertEquals("Firenze", metadata.getAdministrativeArea());
	Assert.assertEquals("Italy", metadata.getCountry());
	Assert.assertEquals("50019", metadata.getPostalCode());
	Assert.assertEquals("Via Madonna del Piano 10", metadata.getDeliveryPoint());
	Assert.assertEquals("info@essi-lab.eu", metadata.getElectronicMailAddress());
	List<String> emails = Lists.newArrayList(metadata.getElectronicMailAddresses());
	Assert.assertEquals(1, emails.size());
    }

    @Override
    public void clearProperties(Address metadata) {
	metadata.setCity(null);
	metadata.setAdministrativeArea(null);
	metadata.setCountry(null);
	metadata.setPostalCode(null);
	metadata.clearDeliveryPoints();
	metadata.clearElectronicMailAddresses();

    }

    @Override
    public void checkNullProperties(Address metadata) {
	Assert.assertNull(metadata.getCity());
	Assert.assertNull(metadata.getAdministrativeArea());
	Assert.assertNull(metadata.getCountry());
	Assert.assertNull(metadata.getPostalCode());
	Assert.assertNull(metadata.getDeliveryPoint());
	Assert.assertNull(metadata.getElectronicMailAddress());
	List<String> emails = Lists.newArrayList(metadata.getElectronicMailAddresses());
	Assert.assertEquals(0, emails.size());

    }

}
