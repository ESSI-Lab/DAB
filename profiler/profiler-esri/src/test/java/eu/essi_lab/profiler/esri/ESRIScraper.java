package eu.essi_lab.profiler.esri;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.net.downloader.Downloader;

public class ESRIScraper {
    public static void main(String[] args) throws Exception {
	InputStream stream = ESRIScraper.class.getClassLoader().getResourceAsStream("requests");
	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	HashSet<String> reqs = new HashSet<String>();
	while (reader.ready()) {
	    String str = reader.readLine();
	    if (str.contains("Execution of https://") && str.contains("STARTED")) {
		str = str.substring(str.indexOf("https://"), str.indexOf(" START"));
		if (str.contains("&callback")) {
		    int index = str.indexOf("&callback");
		    String first = str.substring(0, index);
		    String last = str.substring(index + 1);
		    if (!last.contains("&")) {
			str = first;
		    }
		}
		if (!reqs.contains(str)) {
		    long s = System.currentTimeMillis();
		    Downloader d = new Downloader();
		    d.setConnectionTimeout(TimeUnit.SECONDS, 20);
		    Optional<String> content = d.downloadOptionalString(str);
		    long e = System.currentTimeMillis() - s;
		    if (content.isPresent()) {
			String skip = "({\"objectIdFieldName\":\"objectid\",\"globalIdFieldName\":\"\",\"features\":[]});";
			if (!content.get().contains(skip)) {
			    System.err.println();
			    System.err.println(str);
			    System.err.println("-----------");
			    System.err.println(e + " ms");
			    // System.err.println(content.get());
			    System.err.println();
			}
		    }else {
			System.err.println("ERROR!");
		    }
		    reqs.add(str);
		}
	    }
	}
    }
}
