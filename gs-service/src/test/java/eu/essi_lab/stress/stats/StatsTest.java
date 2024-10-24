package eu.essi_lab.stress.stats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class StatsTest {
    public static void main(String[] args) throws IOException {
	InputStream stream = StatsTest.class.getClassLoader().getResourceAsStream("es-stats/stats.json");
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	JSONObject json = new JSONObject(new String(baos.toByteArray()));
	JSONArray a = json.getJSONArray("results");
	long total = 0;
	long max = 0;
	int days = 0;
	for (int i = 0; i < a.length(); i++) {
	    days++;
	    JSONObject b = a.getJSONObject(i);
	    int v = b.getInt("value");
	    total+=v;
	    if (v>max) {
		max = v;
	    }
	}
	System.out.println(total);
	System.out.println(max);
	System.out.println(total/days);
	
    }
}
