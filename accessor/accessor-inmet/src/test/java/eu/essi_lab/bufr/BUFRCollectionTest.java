package eu.essi_lab.bufr;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.bufr.datamodel.BUFRElement;
import eu.essi_lab.bufr.datamodel.BUFRRecord;

public class BUFRCollectionTest {
    @Test
    public void testName() throws Exception {
	BUFRCollection c = new BUFRCollection();
	BUFRRecord r = new BUFRRecord();
	BUFRElement e1 = new BUFRElement();
	e1.setName("element-1");
	e1.setUnits("m");
	e1.setValue("34");
	r.getElements().add(e1);
	BUFRElement e2 = new BUFRElement();
	e2.setName("element-2");
	e2.setUnits("m");
	e2.setValue("82");
	r.getElements().add(e2);
	c.getRecords().add(r);
	BUFRRecord r2 = new BUFRRecord();
	BUFRElement e3 = new BUFRElement();
	e3.setName("element-3");
	e3.setUnits("m");
	e3.setValue("71");
	r2.getElements().add(e3);
	c.getRecords().add(r2);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	c.marshal(baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	BUFRCollection collection = c.unmarshal(bais);
	assertEquals(2, collection.getRecords().size());
	BUFRRecord record = collection.getRecords().get(0);
	assertEquals(2, record.getElements().size());
	BUFRElement e = record.getElements().get(0);
	assertEquals("34", e.getValue());

    }
}
