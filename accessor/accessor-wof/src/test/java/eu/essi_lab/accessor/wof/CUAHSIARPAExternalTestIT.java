package eu.essi_lab.accessor.wof;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_1;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;

public class CUAHSIARPAExternalTestIT {
@Test
public void testName() throws Exception {
    CUAHSIHISServerClient client = new CUAHSIHISServerClient1_1("http://demanio.ddns.net/italia/hsl-emr/index.php/default/services/cuahsi_1_1.asmx?WSDL");
    Iterator<SiteInfo> sites = client.getSites();
    while (sites.hasNext()) {	
	SiteInfo siteInfo = (SiteInfo) sites.next();
	System.out.println(siteInfo.getSiteName());	
	SitesResponseDocument info = client.getSiteInfo(siteInfo.getSiteCodeNetwork(), siteInfo.getSiteCode());
	List<TimeSeries> series = info.getSitesInfo().get(0).getSeries();
	for (TimeSeries serie : series) {
	    System.out.println(serie.getVariableName());
	}
	System.out.println();
    }

}
}
