package eu.essi_lab.accessor.wof;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import commonj.sdo.helper.XMLDocument;
import eu.essi_lab.accessor.wof.client.CUAHSIHISCentralClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class HisCentralComparator {

    public static void main(String[] args) throws Exception, IOException {
	Downloader d = new Downloader();
	String url1 = "https://hiscentral.cuahsi.org/webservices/hiscentral.asmx/GetWaterOneFlowServiceInfo";
	String url2 = "https://whos.geodab.eu/gs-service/services/essi/token/token/view/whos-cuahsi/hiscentral.asmx/GetWaterOneFlowServiceInfo";
	// url2 =
	// "https://gs-service-test.geodab.eu/gs-service/services/essi/token/token/view/whos-cuahsi/hiscentral.asmx/GetWaterOneFlowServiceInfo";
	Optional<InputStream> s1 = d.downloadOptionalStream(url1);
	XMLDocumentReader reader = new XMLDocumentReader(s1.get());
	Optional<InputStream> s2 = d.downloadOptionalStream(url2);
	XMLDocumentReader reader2 = new XMLDocumentReader(s2.get());

	Node[] services = reader.evaluateNodes("//*:ServiceInfo");
	Node[] services2 = reader2.evaluateNodes("//*:ServiceInfo");

	int good = 0;
	int bad = 0;
	int notfound = 0;

	String notFoundReport = "";

	String differentSitesReport = "";
	String differentVariablesReport = "";
	String differentValuesReport = "";

	for (Node service : services) {
	    String title = reader.evaluateString(service, "*:Title");
	    Long variables = reader.evaluateNumber(service, "*:variablecount").longValue();
	    Long sites = reader.evaluateNumber(service, "*:sitecount").longValue();
	    Long values = reader.evaluateNumber(service, "*:valuecount").longValue();

	    String servURL = reader.evaluateString(service, "*:servURL");
	    String abs = "Original data publication service endpoint: " + servURL;
	    Node node = reader2.evaluateNode("//*:ServiceInfo[*:aabstract='" + abs + "']");
	    if (node == null) {
		notFoundReport += "not found: " + title + "\n";
		notFoundReport += "URL: " + servURL + "\n\n";
		bad++;
		notfound++;
	    } else {
		long variables2 = reader2.evaluateNumber(node, "*:variablecount").longValue();
		long sites2 = reader2.evaluateNumber(node, "*:sitecount").longValue();
		long values2 = reader2.evaluateNumber(node, "*:valuecount").longValue();

		boolean differentVariables = variables2 < 0.8 * variables;
		boolean differentSites = sites2 < 0.8 * sites;
		boolean differentValues = values2 < 0.8 * values;

		if (differentVariables || differentSites || differentValues) {
		    String msg = "Differences in " + title + "\n";
		    msg += "URL: " + servURL + "\n";
		    if (differentSites) {

//			CUAHSIHISServerClient1_1 client = new CUAHSIHISServerClient1_1(servURL);
//			Iterator<SiteInfo> staxi = client.getSitesObjectStAX();
//			long total = 0;
//			while (staxi.hasNext()) {
//			    staxi.next();
//			    total++;
//			}
//			if (total != sites2) {
//			    System.err.println("different sites for "+title+": actually they are " + total + ", however his-central says they are "
//				    + sites + " and DAB " + sites2);
//			}

			msg += "Different sites: " + sites2 + " instead of " + sites + "\n";
		    }
		    if (differentVariables) {
			msg += "Different variables: " + variables2 + " instead of " + variables + "\n";
		    }
		    if (differentValues) {
			 msg += "Different values: " + values2 + " instead of " + values + "\n";
		    }
		    msg += "\n";
		    if (differentSites) {
			differentSitesReport += msg;
		    } else if (differentVariables) {
			differentVariablesReport += msg;
		    } else if (differentValues) {
			differentValuesReport += msg;
		    }
		    bad++;
		} else {
		    // System.out.println("Perfect: "+title);
		    // System.out.println();
		    good++;
		}

	    }

	}
	System.out.println(notFoundReport);
	System.out.println(differentSitesReport);
	System.out.println(differentVariablesReport);
	System.out.println(differentValuesReport);
	System.out.println("Services from #1 HIS-Central: " + services.length);
	System.out.println("Services from #2 HIS-Central: " + services2.length);
	System.out.println("Good services: " + good + "/" + (good + bad) + " (" + bad + " bad, " + notfound + " not found)");
    }
}
