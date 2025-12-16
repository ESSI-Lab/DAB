package eu.essi_lab.lib.net.downloader.test;

import eu.essi_lab.lib.net.downloader.*;
import eu.essi_lab.lib.utils.*;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class DownloaderTrustStoreExternalTestIT {

    private static final String FILE_NAME = "test-trust.p12";
    private static final String PWD = "trustpass";


    @Test
    public void testWithoutTrustStore() throws Exception {

	List<String> endpoints = List.of( //
		"https://eudat-b2share-test.csc.fi/api/oai2d",//
		"https://ionbeam-dev.ecmwf.int/api/v1/",//
		"https://catalogue.nextgeoss.eu/opensearch/collection_search.atom",//
		"https://geoservice.dlr.de/catalogue/srv/eng/csw",//
		"https://sdi.iia.cnr.it/gos4mcat/srv/eng/csw",//
		"https://www.ambiente.gub.uy/dinaguaws");//

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 5);

	final ArrayList<String> errors = new ArrayList<>();

	endpoints.forEach(endpoint -> {

	    HttpResponse<InputStream> resp = null;
	    try {
		resp = downloader.downloadResponse(endpoint);

	    } catch (SSLHandshakeException e) {

 		errors.add(endpoint);

	    } catch (Exception e) {
	    }
	});

	Assert.assertEquals(endpoints.size(), errors.size());
    }

    @Test
    public void testWitTrustStore() throws Exception {

	List<String> endpoints = List.of( //
		"https://eudat-b2share-test.csc.fi/api/oai2d",//
		"https://ionbeam-dev.ecmwf.int/api/v1/",//
		"https://catalogue.nextgeoss.eu/opensearch/collection_search.atom",//
		"https://geoservice.dlr.de/catalogue/srv/eng/csw",//
		"https://sdi.iia.cnr.it/gos4mcat/srv/eng/csw",//
		"https://www.ambiente.gub.uy/dinaguaws");//

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 5);

	InputStream trustStream = DownloaderTrustStoreExternalTestIT.class.getClassLoader().
		getResourceAsStream(FILE_NAME);

	ClonableInputStream clonableInputStream = new ClonableInputStream(trustStream);

	final ArrayList<String> errors = new ArrayList<>();

	endpoints.forEach(endpoint -> {

	    HttpResponse<InputStream> resp = null;

	    try {
		resp = downloader.downloadResponse( //
			HttpRequestUtils.build(HttpRequestUtils.MethodNoBody.GET, endpoint),//
			clonableInputStream.clone(),//
			PWD);//

	    } catch (SSLHandshakeException e) {

		errors.add(endpoint);

	    } catch (Exception e) {
	    }
	});

	Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testWitGlobalTrustStore() throws Exception {

	List<String> endpoints = List.of( //
		"https://eudat-b2share-test.csc.fi/api/oai2d",//
		"https://ionbeam-dev.ecmwf.int/api/v1/",//
		"https://catalogue.nextgeoss.eu/opensearch/collection_search.atom",//
		"https://geoservice.dlr.de/catalogue/srv/eng/csw",//
		"https://sdi.iia.cnr.it/gos4mcat/srv/eng/csw",//
		"https://www.ambiente.gub.uy/dinaguaws");//

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 5);

	InputStream trustStream = DownloaderTrustStoreExternalTestIT.class.getClassLoader().
		getResourceAsStream(FILE_NAME);

	File target = new File(FileUtils.getTempDirectory(), FILE_NAME);

	FileUtils.copyInputStreamToFile(trustStream, target);

	System.setProperty("javax.net.ssl.trustStore", target.getAbsolutePath());
	System.setProperty("javax.net.ssl.trustStoreType", Downloader.DEFAULT_KEY_STORE_TYPE);
	System.setProperty("javax.net.ssl.trustStorePassword", PWD);

	final ArrayList<String> errors = new ArrayList<>();

	endpoints.forEach(endpoint -> {

	    HttpResponse<InputStream> resp = null;

	    try {
		resp = downloader.downloadResponse( //
			HttpRequestUtils.build(HttpRequestUtils.MethodNoBody.GET, endpoint)

		);//

	    } catch (SSLHandshakeException e) {

		e.printStackTrace();

		errors.add(endpoint);

	    } catch (Exception e) {
	    }
	});

	Assert.assertEquals(0, errors.size());
    }
}
