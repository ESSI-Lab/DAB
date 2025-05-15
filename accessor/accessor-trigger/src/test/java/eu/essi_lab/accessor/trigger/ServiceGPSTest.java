package eu.essi_lab.accessor.trigger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ServiceGPSTest {



    @Before
    public void init() {

    }

    @Test
    public void testMetadataFileRead() throws Exception {
	InputStream is = ServiceGPSTest.class.getClassLoader().getResourceAsStream("gps.json");
	TestCase.assertNotNull(is);

	JSONArray json = new JSONArray(IOUtils.toString(is, "UTF-8"));
	Map<String,Integer> map = new HashMap<String, Integer>();
	Set<String> set = new HashSet<String>();
	for(int i = 0; i <json.length();i++) {
	    JSONObject obj = json.getJSONObject(i);
	    String id = obj.optString("deviceId");
	    if(map.containsKey(id)) {
		Integer count = map.get(id);
		count++;
		map.put(id, count);
	    } else {
		map.put(id, 1);
	    }
	    set.add(id);
	}

	for(String s: set) {
	    System.out.println(s);
	}
	for(Map.Entry<String, Integer> entry: map.entrySet()) {
	    System.out.println(entry.getKey() + ":" + entry.getValue());
	}
	Assert.assertTrue(json.length() == 3231);
	Assert.assertTrue(set.size() == 16);
	
	
	
    }

}
