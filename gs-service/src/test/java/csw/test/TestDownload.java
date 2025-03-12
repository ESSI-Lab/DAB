package csw.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class TestDownload {
    public static void main(String[] args) throws Exception {
	Downloader d = new Downloader();
	// String url =
	// "https://gs-service-test.geodab.eu/gs-service/services/essi/token/whos-d40a452b-b865-4fbe-8165-43a96ebf1b3d/view/whos/wms-cluster?what=&from=&to=&ontology=whos&semantics=sameas-narrow&REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image%2Fpng&STYLES=&TRANSPARENT=true&LAYERS=whos&WIDTH=256&HEIGHT=256&CRS=EPSG%3A3857&BBOX=-17532819.79994059%2C7514065.628545966%2C-15028131.257091934%2C10018754.171394622";
	Long r = (long) (Math.random() * 1000.0);
	String url = "http://localhost:9090/gs-service/services/essi/token/whos-d40a452b-b865-4fbe-8165-43a96ebf1b3d/view/whos/wms-cluster?what=&from=&to=&ontology=whos&semantics=sameas-narrow&REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image%2Fpng&STYLES=&TRANSPARENT=true&LAYERS=whos&WIDTH=256&HEIGHT=256&CRS=EPSG%3A3857&BBOX=-17532819."
		+ r + "%2C7514065." + r + "%2C-15028131." + r + "%2C10018754." + r;
	// String url =
	// "http://52.90.144.63:32772/gs-service/services/essi/view/test/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&semantics=sameas-narrow&ontology=whos&instrumentTitle=&platformTitle=&attributeTitle=&observedPropertyURI=&organisationName=&searchFields=&bbox=&rel=CONTAINS&tf=providerID,keyword,organisationName,attributeTitle,platformTitle&ts=&te=&targetId=&from=&until=&subj=&rela=&outputFormat=application/json&callback=jQuery1121024264357855208551_1741599728005&_=1741599728008&requestId=test";
	// String url =
	// "http://127.0.0.1:9090/gs-service/services/essi/view/test/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&semantics=sameas-narrow&ontology=whos&instrumentTitle=&platformTitle=&attributeTitle=&observedPropertyURI=&organisationName=&searchFields=&bbox=&rel=CONTAINS&tf=providerID,keyword,organisationName,attributeTitle,platformTitle&ts=&te=&targetId=&from=&until=&subj=&rela=&outputFormat=application/json&callback=jQuery1121024264357855208551_1741599728005&_=1741599728008&requestId=test";
	// String url =
	// "https://gs-service-test.geodab.eu/gs-service/services/essi/view/test/opensearch/query?si=1&ct=10&st=&kwd=&frmt=&prot=&kwdOrBbox=&sscScore=&semantics=sameas-narrow&ontology=whos&instrumentTitle=&platformTitle=&attributeTitle=&observedPropertyURI=&organisationName=&searchFields=&bbox=&rel=CONTAINS&tf=providerID,keyword,organisationName,attributeTitle,platformTitle&ts=&te=&targetId=&from=&until=&subj=&rela=&outputFormat=application/json&callback=jQuery1121024264357855208551_1741599728005&_=1741599728008&requestId=test";

	Chronometer c = new Chronometer();
	c.setTimeFormat(TimeFormat.MIN_SEC_MLS);
	c.start();
	GSLoggerFactory.getLogger(TestDownload.class).info("send");
	download(d, url);

	int maxParallelTasks = 5;
	int totalTasks = 50;
	// 5;50 -> 1220
	ExecutorService executor = Executors.newFixedThreadPool(maxParallelTasks);
	List<Future<Long>> futures = new ArrayList();
	for (int i = 1; i <= totalTasks; i++) {
	    final int taskId = i;
	    Future<Long> future = executor.submit(() -> {
		System.out.println("Task " + taskId + " is running on " + Thread.currentThread().getName());
		Chronometer c2 = new Chronometer();
		c2.setTimeFormat(TimeFormat.MIN_SEC_MLS);
		c2.start();
		GSLoggerFactory.getLogger(TestDownload.class).info("send");
		try {
		    download(d, url);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		c2.formatAndPrintElapsedTime();
		long time = c2.getElapsedTimeMillis();
		return time;
	    });
	    futures.add(future);

	}
	executor.shutdown();
	executor.awaitTermination(5, TimeUnit.HOURS);

	long total = 0;
	for (Future<Long> future : futures) {
	    try {
		Long time = future.get();
		System.out.println(time);
		total += time;
	    } catch (Exception e) {
		System.err.println("Error retrieving task result: " + e.getMessage());
	    }
	}
	System.out.println("Mean: " + ((double) total / (double) totalTasks));

    }

    private static void download(Downloader d, String url) throws Exception {
	
	Long r = (long) (Math.random() * 1000.0);
	url = "http://localhost:9090/gs-service/services/essi/token/whos-d40a452b-b865-4fbe-8165-43a96ebf1b3d/view/whos/wms-cluster?what=&from=&to=&ontology=whos&semantics=sameas-narrow&REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image%2Fpng&STYLES=&TRANSPARENT=true&LAYERS=whos&WIDTH=256&HEIGHT=256&CRS=EPSG%3A3857&BBOX=-17532819."
		+ r + "%2C7514065." + r + "%2C-15028131." + r + "%2C10018754." + r;
	Optional<HttpResponse<InputStream>> response = d.downloadOptionalResponse(url);
	HttpResponse<InputStream> res = response.get();
	System.out.println(res.statusCode());
	GSLoggerFactory.getLogger(TestDownload.class).info("get {}", res.statusCode());
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	InputStream body = res.body();
	IOUtils.copy(body, bos);
	bos.close();
	body.close();

    }
}
