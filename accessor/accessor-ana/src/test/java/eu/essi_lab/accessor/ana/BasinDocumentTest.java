package eu.essi_lab.accessor.ana;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.model.exceptions.GSException;

public class BasinDocumentTest {

	@Test
	public void testBasinDocumentMethods() throws GSException, SAXException, IOException {
		InputStream is = BasinDocumentTest.class.getClassLoader().getResourceAsStream("basin.xml");
		BasinDocument doc = new BasinDocument(is);
		String name = doc.getBasinName("1");
		Assert.assertNotEquals("RIO TOCANTINS", name);
		Assert.assertEquals("RIO AMAZONAS", name);
		String identifier = doc.getBasinIdentifier("23");
		Assert.assertNotEquals("1", identifier);
		Assert.assertEquals("2", identifier);
		String subBasinName = doc.getSubBasinName("90");
		Assert.assertEquals("OUTROS RIOS", subBasinName);

		LinkedHashSet<String> listIds = doc.getBasinIdentifiers();
		int size = listIds.size();
		Assert.assertTrue(size > 0);

		LinkedHashSet<String> listSubIds = doc.getSubBasinIdentifiers("8");
		int sizeSub = listSubIds.size();
		Assert.assertTrue(sizeSub > 0);

	}
	
	

}
