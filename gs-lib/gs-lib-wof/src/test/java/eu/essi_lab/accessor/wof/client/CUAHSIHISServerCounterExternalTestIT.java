package eu.essi_lab.accessor.wof.client;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;

public class CUAHSIHISServerCounterExternalTestIT {

    @Test
    public void testCount() throws Exception {
	String endpoint = "http://hydrolite.ddns.net/italia/hsl-tos/index.php/default/services/cuahsi_1_1.asmx?WSDL";
	CUAHSIHISServerClient1_1 client = new CUAHSIHISServerClient1_1(endpoint);

	Iterator<SiteInfo> sites = client.getSites();
	int sitesCount = 0;
	int seriesCount = 0;
	List<String> outs = new ArrayList<>();
	outs.add("Checking service: " + endpoint);
	while (sites.hasNext()) {
	    SiteInfo site = (SiteInfo) sites.next();
	    String out = "SITE #" + ++sitesCount + " " + site.getSiteName() + " " + site.getSiteCode();
	    outs.add(out);
	    SitesResponseDocument srd = client.getSiteInfo(site.getSiteCodeNetwork(), site.getSiteCode());
	    List<TimeSeries> series = srd.getSites().get(0).getSeries();
	    for (TimeSeries s : series) {
		String out2 = "SERIES #" + ++seriesCount + " " + s.getVariableCode();
		outs.add(out2);
	    }
	    outs.add("");
	    if (sitesCount > 10) {
		break;
	    }
	}
	assertTrue(sitesCount > 0);

	for (String out : outs) {
	    System.out.println(out);
	}
    }
}
