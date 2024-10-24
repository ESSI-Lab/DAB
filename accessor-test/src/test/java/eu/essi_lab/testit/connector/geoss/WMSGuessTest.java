package eu.essi_lab.testit.connector.geoss;

import java.util.Optional;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;

public class WMSGuessTest {
    public static void main(String[] args) {
	GEOSSConnectors connectors = new GEOSSConnectors();
	// 135 - 180
	for (int i = 154; i <= 180; i++) {
	    String type = "Unknown";
	    Downloader d = new Downloader();
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
	    try {
		base = base + "SERVICE=WMS&request=GetCapabilities&version=1.3.0";
		System.out.println("Downloading: " + base);
		Optional<String> str = d.downloadString(base);
		if (str.isPresent()) {
		    if (str.get().contains("DOCTYPE WMT_MS_Capabilities")) {
			type = "1.1.1";
		    } else {
			try {
			    XMLDocumentReader reader = new XMLDocumentReader(str.get());
			    String root = reader.evaluateString("/*[1]/local-name(.)");
			    if (root.equals("WMS_Capabilities")) {
				type = "1.3.0";
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}

		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    System.out.println(i + " WMS " + type);
	}

    }
}
