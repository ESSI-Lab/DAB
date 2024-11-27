package eu.essi_lab.accessor.wof;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import commonj.sdo.helper.XMLDocument;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class HisCentralComparator {

	public static void main(String[] args) throws Exception, IOException {
		Downloader d = new Downloader();
		String url1 = "https://hiscentral.cuahsi.org/webservices/hiscentral.asmx/GetWaterOneFlowServiceInfo";
		String url2 = "https://whos.geodab.eu/gs-service/services/essi/token/token/view/whos-cuahsi/hiscentral.asmx/GetWaterOneFlowServiceInfo";
		Optional<InputStream> s1 = d.downloadOptionalStream(url1);
		XMLDocumentReader reader = new XMLDocumentReader(s1.get());
		Optional<InputStream> s2 = d.downloadOptionalStream(url2);
		XMLDocumentReader reader2 = new XMLDocumentReader(s2.get());

		Node[] services = reader.evaluateNodes("//*:ServiceInfo");
		Node[] services2 = reader2.evaluateNodes("//*:ServiceInfo");

		int good = 0;
		int bad = 0;
		for (Node service : services) {
			String title = reader.evaluateString(service, "*:Title");
			Long variables = reader.evaluateNumber(service, "*:variablecount").longValue();
			Long sites = reader.evaluateNumber(service, "*:sitecount").longValue();
			Long values = reader.evaluateNumber(service, "*:valuecount").longValue();

			String servURL = reader.evaluateString(service, "*:servURL");
			String abs = "Original data publication service endpoint: " + servURL;
			Node node = reader2.evaluateNode("//*:ServiceInfo[*:aabstract='" + abs + "']");
			if (node == null) {
				System.out.println("Checking " + title);
				System.err.println("not found: " + servURL);
				System.out.println();
				bad++;
			} else {
				Long variables2 = reader2.evaluateNumber(node, "*:variablecount").longValue();
				Long sites2 = reader2.evaluateNumber(node, "*:sitecount").longValue();
				Long values2 = reader2.evaluateNumber(node, "*:valuecount").longValue();
				if (!variables2.equals(variables) || !sites2.equals(sites) || !values2.equals(values)) {
					System.out.println("Checking " + title);
					if (!variables2.equals(variables)) {
						System.out.println("Different variables: " + variables2 + " instead of " + variables);
					}
					if (!sites2.equals(sites)) {
						System.out.println("Different sites: " + sites2 + " instead of " + sites);
					}
					if (!values2.equals(values)) {
						System.out.println("Different values: " + values2 + " instead of " + values);
					}
					bad++;
					System.out.println();
				} else {
					good++;
				}

			}

		}
		System.out.println("Services from #1 HIS-Central: " + services.length);
		System.out.println("Services from #2 HIS-Central: " + services2.length);
		System.out.println("Good services: " + good + "/" + (good + bad) + " (" + bad + " bad)");
	}
}
