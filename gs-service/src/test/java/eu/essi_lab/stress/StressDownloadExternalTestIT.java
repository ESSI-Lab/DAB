package eu.essi_lab.stress;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class StressDownloadExternalTestIT extends StressExternalTestIT {

    private List<Integer> myit = Arrays.asList(1);

    private Integer currentThread = null;

    @Override
    protected int getParallel() {
	return 100;
    }

    @Override
    protected int getNumofTests() {
	return myit.size();
    }

    @Override
    protected Integer getNumberOfRequestForTest(int it) {
	currentThread = myit.get(it);
	return currentThread;
    }

    @Override
    protected Integer getIterationsPerTest() {
	return 1;
    }

    public boolean testOperation() throws SAXException, IOException, XPathExpressionException {

	Downloader downloader = new Downloader();

	String host = "https://gs-service-production.geodab.eu";

	// host = "http://localhost:9090";

	String discoveryRequest = host
		+ "/gs-service/services/essi/view/geoss/opensearch/query?searchFields=title,keywords&reqID=fwtq4za4w1k&si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,sscScore&rel=OVERLAPS&viewid=&st=sea%20OR%20ice%20OR%20concentration,%20OR%20March%20OR%20(1979-2007)&sources=FROMREGISTRY--regprefseparator--registrytestid1--regprefseparator--2059c8ea-26b9-46e4-af09-debdac21fb84";

	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(discoveryRequest);

	if (!optionalStream.isPresent()) {
	    return false;
	}

	InputStream stream = optionalStream.get();
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	stream.close();

	String url = reader.evaluateString("//*:advancedAccessLink[1]");

	url = url
		+ "?service=WPS&request=execute&identifier=gi-axe-transform&storeexecuteresponse=true&DataInputs=outputCRS%3DEPSG%253A32661%3BoutputFormat%3DNETCDF%3BoutputSize%3D30%252C30";

	Optional<String> optionalString = downloader.downloadOptionalString(url);
	if (optionalString.isPresent()) {
	    String string = optionalString.get();
	    String statusLocation = extractStatusLocation(string);

	    for (int i = 0; i < 30; i++) {

		try {
		    Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		Optional<String> optionalStatus = downloader.downloadOptionalString(statusLocation);
		if (optionalStatus.isPresent()) {
		    String status = optionalStatus.get();
		    System.out.println(status);
		    if (status.contains("COMPLETED")) {
			GSLoggerFactory.getLogger(getClass()).info("Successful download operation");
			return true;
		    } else if (status.contains("FAILED")) {
			GSLoggerFactory.getLogger(getClass()).info("Failed download operation");
			return false;
		    } else {
			GSLoggerFactory.getLogger(getClass()).info("Download operation in progress");
		    }
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Failed status request");
		    return false;
		}
	    }
	    
	    GSLoggerFactory.getLogger(getClass()).info("Failed status request after 30 attempts, about 90 seconds");
	    return false;

	} else {
	    GSLoggerFactory.getLogger(getClass()).info("Failed download request");
	    return false;
	}

    }

    private String extractStatusLocation(String string) {
	string = string.substring(string.indexOf("statusLocation=\""));
	string = string.replace("statusLocation=\"", "");
	string = string.substring(0, string.indexOf('\"'));
	return string;
    }

}
