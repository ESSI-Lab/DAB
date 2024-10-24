package eu.essi_lab.testit.connector.geoss;

import java.util.Optional;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;

public class WFSGuessTest {
    public static void main(String[] args) {
	GEOSSConnectors connectors = new GEOSSConnectors();
	// 121 - 134
	for (int i = 121; i <= 134; i++) {

	    GSSource source = new GSSource();
	    source.setUniqueIdentifier("" + i);
	    connectors.getConnector(i, source);
	    String base = source.getEndpoint();
	    if (base.endsWith("?") || base.endsWith("&")) {

	    } else {
		if (base.contains("?")) {
		    base = base + "&";
		} else {
		    base = base + "?";
		}
	    }

	    base = base + "SERVICE=WFS&request=GetCapabilities";

	    String type = getType(base);

	    if (!type.contains("error")) {
		String type2 = getType(base + "&version=2.0.2");
		if (!type2.equals(type)) {
		    type = type + " and " + type2;
		}
	    }

	    System.out.println(i + ": " + type);
	}

    }

    private static String getType(String base) {
	Downloader d = new Downloader();
	String type = "Unknown";
	try {
	    System.out.println("Downloading: " + base);
	    Optional<String> str = d.downloadString(base);
	    if (str.isPresent()) {

		try {
		    XMLDocumentReader reader = new XMLDocumentReader(str.get());
		    type = "WFS " + reader.evaluateString("/*[1]/@version");

		} catch (Exception e) {
		    e.printStackTrace();
		    type = "XML error";
		}

	    } else {
		type = "Connection error";
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return type;
    }
}
