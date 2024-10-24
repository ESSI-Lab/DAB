package eu.essi_lab.iso.datamodel.test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

public class ISOMetadataTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() throws JAXBException {
	CharacterStringPropertyType characterStringPropertyType = null;
	Assert.assertNull(ISOMetadata.getStringFromCharacterString(characterStringPropertyType));
	characterStringPropertyType = new CharacterStringPropertyType();
	Assert.assertNull(ISOMetadata.getStringFromCharacterString(characterStringPropertyType));
	JAXBElement<String> characterString = ObjectFactories.GCO().createCharacterString(null);
	characterStringPropertyType.setCharacterString(characterString);
	Assert.assertNull(ISOMetadata.getStringFromCharacterString(characterStringPropertyType));
	characterString.setValue("test");
	Assert.assertEquals("test", ISOMetadata.getStringFromCharacterString(characterStringPropertyType));
    }
}
