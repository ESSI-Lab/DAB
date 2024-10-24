/**
 * 
 */
package eu.essi_lab.iso.datamodel;

import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;

/**
 * @author Fabrizio
 */
public class Fix_4d0d11a10e94832f870e9534f3d616388856edad_Test {

    @Test
    public void test() throws JAXBException {

	MDMetadata metadata = new MDMetadata(getClass().getClassLoader().getResourceAsStream("mi-empty-sid.xml"));
	Iterator<DataIdentification> iterator = metadata.getDataIdentifications();

	Assert.assertTrue(iterator.hasNext());
    }
}
