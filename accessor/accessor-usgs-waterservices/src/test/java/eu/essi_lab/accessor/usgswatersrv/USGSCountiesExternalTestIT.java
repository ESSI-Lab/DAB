package eu.essi_lab.accessor.usgswatersrv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.TreeMap;

import org.junit.Test;

public class USGSCountiesExternalTestIT {

    @Test
    public void test() throws Exception {
	USGSClient client = new USGSClient();
	USGSCounties counties = client.getCounties();
	TreeMap<String, USGSCounty> map = counties.getCounties();
	for (String key : map.keySet()) {
	    USGSCounty county = map.get(key);
	    System.out.println(county.getStateCode() + " " + county.getCountyCode() + " " + county.getCountyName());
	}
	assertTrue(map.size() > 3200);
	String[] keys = map.keySet().toArray(new String[] {});
	String key5 = keys[4];
	String key6 = counties.getNextKey(key5);
	assertEquals(keys[5], key6);
    }

}
