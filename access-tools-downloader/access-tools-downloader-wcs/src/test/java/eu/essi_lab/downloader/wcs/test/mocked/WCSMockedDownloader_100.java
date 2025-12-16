package eu.essi_lab.downloader.wcs.test.mocked;

import dev.failsafe.FailsafeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLSession;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_0_0.WCSConnector_100;
import eu.essi_lab.downloader.wcs.WCSDownloader_100;
import eu.essi_lab.downloader.wcs.test.WCSDownloader_Test;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class WCSMockedDownloader_100 extends WCSDownloader_100 {

    private WCSMockedDownloader mock;

    public WCSMockedDownloader_100(WCSMockedDownloader mock) {
	this.mock = mock;
    }

    @Override
    public Downloader getHttpDownloader() {


	Downloader ret = new Downloader() {

	    @Override
	    public HttpResponse<InputStream> downloadResponse(//
		    HttpRequest request, //
		    String username, //
		    String password, //
		    InputStream trustStore, //
		    String trustStorePwd) throws FailsafeException, IOException, InterruptedException {

		HttpResponse<InputStream> response = new HttpResponse<InputStream>() {

		    @Override
		    public int statusCode() {
			return 200;
		    }

		    @Override
		    public HttpRequest request() {
			return request;
		    }

		    @Override
		    public Optional<HttpResponse<InputStream>> previousResponse() {
			return Optional.empty();
		    }

		    @Override
		    public HttpHeaders headers() {
			return HttpHeaders.of(Map.of(), (s1, s2) -> true);
		    }

		    @Override
		    public InputStream body() {
			Optional<InputStream> stream = downloadOptionalStream(request.uri().toString());
			if (stream.isPresent()) {
			    return stream.get();
			}
			return null;
		    }

		    @Override
		    public Optional<SSLSession> sslSession() {
			return Optional.empty();
		    }

		    @Override
		    public URI uri() {
			return request.uri();
		    }

		    @Override
		    public HttpClient.Version version() {
			return HttpClient.Version.HTTP_1_1;
		    }

		};
		return response;
	    }

	    @Override
	    public Optional<InputStream> downloadOptionalStream(String url) {
		SimpleEntry<String, String> urlFile = mock.getCoverageUrlFile();
		if (urlFile == null) {
		    return Optional.empty();
		}
		if (url.equals(urlFile.getKey())) {
		    String path = urlFile.getValue();
		    InputStream stream = WCSDownloader_Test.class.getClassLoader().getResourceAsStream(path);
		    return Optional.of(stream);
		}
		return Optional.empty();
	    }

	};

	return ret;
    }

    @Override
    public WCSConnector createConnector() {

	return new WCSConnector_100() {

	    @Override
	    public XMLDocumentReader getCapabilities(String endpoint, String version) throws SAXException, IOException {
		InputStream capabilities = WCSDownloader_Test.class.getClassLoader().getResourceAsStream(mock.getCapabilitiesFile());
		return new XMLDocumentReader(capabilities);
	    }

	    @Override
	    public XMLDocumentReader getCoverageDescription(String id) throws SAXException, IOException {
		InputStream coverage = WCSDownloader_Test.class.getClassLoader().getResourceAsStream(mock.getCoverageDescription());
		return new XMLDocumentReader(coverage);
	    }

	};
    }

}
