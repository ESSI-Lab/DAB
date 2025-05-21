package eu.essi_lab.accessor.hiscentral.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;

public class HISCentralBolzanoClientExternalTestIT {

    @Test
    public void test() {
	System.out.println("Start");
	Downloader d = new Downloader();
	String stationsString = d.downloadOptionalString("https://daten.buergernetz.bz.it/services/meteo/v1/stations").get();
	JSONObject stations = new JSONObject(stationsString);
	JSONArray features = stations.getJSONArray("features");
	HashMap<String, Integer> parCont = new HashMap<String, Integer>();
	int s = 0;
	int ts = 0;
	for (int i = 0; i < features.length(); i++) {
	    JSONObject feature = features.getJSONObject(i);
	    JSONObject props = feature.getJSONObject("properties");
	    String scode = props.getString("SCODE");
	    System.out.println(scode);
	    String url2 = "https://daten.buergernetz.bz.it/services/meteo/v1/sensors?station_code=" + scode;
	    String stationString = d.downloadOptionalString(url2).get();
	    JSONArray parameters = new JSONArray(stationString);
	    for (int j = 0; j < parameters.length(); j++) {
		ts++;
		JSONObject parameter = parameters.getJSONObject(j);
		String par = parameter.getString("DESC_I");
		if (par.contains("Precipitazioni")) {
		    System.out.println("Precipitazioni: " + scode);
		}
		Integer c = parCont.get(par);
		if (c == null) {
		    c = 0;
		}
		c++;
		parCont.put(par, c);
	    }
	    s++;
	}
	System.out.println("Stations: " + s);
	System.out.println("Time series: " + ts);
	Set<Entry<String, Integer>> sets = parCont.entrySet();
	ArrayList<Entry<String, Integer>> list = new ArrayList<>();
	list.addAll(sets);
	list.sort(new Comparator<Entry<String, Integer>>() {

	    @Override
	    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
		return o1.getValue().compareTo(o2.getValue());
	    }
	});

	for (Entry<String, Integer> e : list) {
	    Integer cont = e.getValue();
	    System.out.println(e.getKey() + ": " + cont);
	}

    }

}
