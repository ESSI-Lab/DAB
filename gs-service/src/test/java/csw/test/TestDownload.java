package csw.test;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Optional;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;

public class TestDownload {
public static void main(String[] args) {
    GSLoggerFactory.getLogger(TestDownload.class).info("start");
    Downloader d = new Downloader();
    GSLoggerFactory.getLogger(TestDownload.class).info("send");
    Optional<HttpResponse<InputStream>> r = d.downloadOptionalResponse("http://localhost:9090/gs-service/services/essi/view/whos/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&semantics=sameas-narrow&ontology=whos&instrumentTitle=&platformTitle=&attributeTitle=&observedPropertyURI=&organisationName=&searchFields=&bbox=&rel=CONTAINS&tf=providerID,keyword,organisationName,attributeTitle,platformTitle&ts=&te=&targetId=&from=&until=&subj=&rela=&outputFormat=application/json&callback=jQuery1121024264357855208551_1741599728005&_=1741599728008");
    HttpResponse<InputStream> res = r.get();
    System.out.println(res.statusCode());
    GSLoggerFactory.getLogger(TestDownload.class).info("get {}",res.statusCode());
    
}
}
