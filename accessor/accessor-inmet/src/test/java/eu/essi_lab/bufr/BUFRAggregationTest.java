package eu.essi_lab.bufr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.bufr.datamodel.BUFRRecord;

public class BUFRAggregationTest {

    @Before
    public void before() throws IOException {

	folder = MockedBUFRDownloader.download();

    }

    File folder;
    BUFRConnector c = new BUFRConnector();

    /**
     * This test has been ignored, as very soon the BUFR accessor will be replaced with another accessor using API.
     * 
     * @throws Exception
     */
    @Ignore
    public void testName() throws Exception {
	Map<String, BUFRCollection> ret = c.aggregateRecordsByStation(folder);
	assertEquals(35, ret.size());
	for (String key : ret.keySet()) {
	    System.out.println("Key: " + key);
	    BUFRCollection value = ret.get(key);
	    System.out.println(value.getRecords().size());
	    List<BUFRRecord> records = value.getRecords();
	    for (BUFRRecord record : records) {
		System.out.println(record.getTime());
	    }
	}
	BUFRCollection c195 = ret.get("WMO:195");
	assertEquals(11, c195.getRecords().size());
	BUFRCollection c152 = ret.get("WMO:152");
	assertEquals(11, c152.getRecords().size());

	// c152.marshal(System.out);
    }

    @After
    public void after() {
	File[] files = folder.listFiles();
	for (File file : files) {
	    file.delete();
	}
	folder.delete();

    }
}
