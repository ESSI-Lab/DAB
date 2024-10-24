package eu.essi_lab.stress.stats;

import java.util.UUID;

import eu.essi_lab.lib.net.downloader.Downloader;

public class RequestGenerator {

    public static void main(String[] args) {
	String target = "http://localhost:9090/gs-service/services/essi/opensearch/query";
	String query = target + "?reqID=" + UUID.randomUUID().toString()
		+ "&si=1&ct=10&st={ST}&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&instrumentTitle=&platformTitle=&attributeTitle=&organisationName=&searchFields=&bbox={BBOX}&rel=CONTAINS&tf=providerID,keyword,format,protocol&ts=&te=&targetId=&from=&until=&subj=&rela=&evtOrd=time&outputFormat=application/json&callback=jQuery111302624560682297763_1618234328624&_=1618234328634";

	int max = 5000;

	for (int i = 0; i < max; i++) {
	    System.out.println(i + "/" + max);
	    String term = "";
	    String[] terms = new String[] { "discharge", "temperature", "sst" };
	    double d = Math.random() * (terms.length * 2.0);
	    int g = (int) d;
	    if (g < terms.length) {
		term = terms[g];
	    }
	    String bbox = ""; // 71.533,18.8,101.533,48.124
	    d = Math.random() * (terms.length * 2.0);
	    if (d < 1) {
		bbox = (Math.random() * -80) + "," + (Math.random() * -80) + "," + (Math.random() * 80) + "," + (Math.random() * 80);
	    }

	    String url = query.replace("{ST}", term).replace("{BBOX}", bbox);
	    Downloader down = new Downloader();
	    down.downloadOptionalString(url);
	}
    }
}
