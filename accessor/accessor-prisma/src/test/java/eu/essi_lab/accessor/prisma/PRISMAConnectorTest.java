package eu.essi_lab.accessor.prisma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.WebConnector;

public class PRISMAConnectorTest {

    public static void main(String[] asrgs)
	    throws TransformerFactoryConfigurationError, TransformerException, XPathExpressionException, IOException {

	String endpoint = "http://90.147.167.250/data/PRISMA%20DATA/";

	Downloader dataDownloader = new Downloader();

	WebConnector connector = new WebConnector();

	List<String> hrefs = connector.getHrefs(endpoint, null);
	System.out.println(hrefs.size());

	for (String href : hrefs) {

	    while (!href.contains(".csv")) {

		List<String> res = connector.getHrefs(href, null);
		System.out.println(res.size());
		for (String s : res) {
		    href = s;
		    if (href.contains("csv."))
			break;
		}
	    }

	    Optional<InputStream> inputStream = dataDownloader.downloadOptionalStream(href);
	    if (inputStream.isPresent()) {

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream.get()))) {
		    String temp = br.readLine(); // skip header line
		    String[] headersSplit = new String[] {};
		    String separator = "\\|";

		    while ((temp = br.readLine()) != null) {

			if (!temp.equals("")) {
			    String[] dataSplit = temp.split(separator);
			    if (headersSplit.length == dataSplit.length) {
				HashMap<String, String> values = new HashMap<>();
				for (int i = 0; i < dataSplit.length; i++) {
				    String header = headersSplit[i];
				    String data = dataSplit[i];
				    values.put(header, data);
				}
				String mainCode = "";

			    }
			}
		    }
		}

	    }

	}

    }

}
