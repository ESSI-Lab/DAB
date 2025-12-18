package eu.essi_lab.lib.net.downloader.test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class DownloaderTrustStoreExternalTestIT {

    private static final String FILE_NAME = "test-trust.p12";
    private static final String PWD = "trustpass";

    private List<String> endpoints = List.of( //
	    // The following are invalid https endpoints
	    "https://eudat-b2share-test.csc.fi/api/oai2d",//
	    "https://ionbeam-dev.ecmwf.int/api/v1/",//
	    "https://catalogue.nextgeoss.eu/opensearch/collection_search.atom",//
	    "https://geoservice.dlr.de/catalogue/srv/eng/csw",//
	    "https://sdi.iia.cnr.it/gos4mcat/srv/eng/csw",//
	    "https://www.ambiente.gub.uy/dinaguaws",

	    // The following are valid https endpoints
	    "https://registry.opendata.aws/sentinel-2/",//
	    "https://planetarycomputer.microsoft.com/api/stac/v1/collections/landsat-c2-l2");//

    @Before
    public void setup() {

	try {
	    TimeUnit.SECONDS.sleep(5);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    @Test
    public void testWithoutTrustStore() throws Exception {

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

	Assert.assertEquals(endpoints.size() - 2, errors.size());
    }

    @Test
    public void testWitTrustStore() throws Exception {

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

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 5);

	InputStream trustStream = DownloaderTrustStoreExternalTestIT.class.getClassLoader().
		getResourceAsStream(FILE_NAME);

	File target = new File(FileUtils.getTempDirectory(), FILE_NAME);

	FileUtils.copyInputStreamToFile(trustStream, target);

	System.setProperty("dab.net.ssl.trustStore", target.getAbsolutePath());
	System.setProperty("dab.net.ssl.trustStorePassword", PWD);

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
