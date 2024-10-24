package eu.essi_lab.accessor.uknrfa;

import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;

public class UKNRFAClientExternalTestIT {

    @Test
    public void test() {
	String endpoint = UKNRFAClient.DEFAULT_ENDPOINT;
	UKNRFAClient client = new UKNRFAClient(endpoint);
	Set<String> ids = client.getStationIdentifiers();
	assertFalse(ids.isEmpty());
	String first = ids.iterator().next();

	JSONObject info = client.getStationInfo(first);
	JSONObject data = info.getJSONArray("data").getJSONObject(0);
	Set<String> keys = data.keySet();
	Set<String> parameters = new HashSet<>();

	int i = 0;
	for (String id : ids) {
	    info = client.getStationInfo(id);
	    data = info.getJSONArray("data").getJSONObject(0);
	    // keys = data.keySet();
	    // for (String key : keys) {
	    // if (key.endsWith("-start-date")) {
	    // parameters.add(key.replace("-start-date", ""));
	    // }
	    // }
	    if (data.has("station-type")) {
		parameters.add(data.getString("station-type"));

	    }
	    System.out.println(i++ + "/" + ids.size());
	}

	for (String par : parameters) {
	    System.out.println(par);
	}

	// for (String key : keys) {
	// Object object = data.get(key);
	// System.out.println( key + ": " + object.toString()+" \t"+object.getClass().getSimpleName());
	// }

    }

}
