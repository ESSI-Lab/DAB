package eu.essi_lab.downloader.wcs.test.mocked;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_1_1.WCSConnector_111;
import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.downloader.wcs.test.WCSDownloader_Test;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class WCSMockedDownloader_111 extends WCSDownloader_111 {

    private WCSMockedDownloader mock;

    public WCSMockedDownloader_111(WCSMockedDownloader mock) {
	this.mock = mock;
    }

    @Override
    public Downloader getHttpDownloader() {
	Downloader ret = new Downloader() {

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

	return new WCSConnector_111() {

	    @Override
	    public XMLDocumentReader getCapabilities(String endpoint, String version) throws SAXException, IOException {
		InputStream capabilities = WCSDownloader_Test.class.getClassLoader().getResourceAsStream(mock.getCapabilitiesFile());
		return new XMLDocumentReader(capabilities);
	    }

	    @Override
	    public XMLDocumentReader getCoverageDescription(String id) throws SAXException, IOException {
		InputStream coverageDescription = WCSDownloader_Test.class.getClassLoader()
			.getResourceAsStream(mock.getCoverageDescription());
		return new XMLDocumentReader(coverageDescription);
	    }

	};
    }

}
