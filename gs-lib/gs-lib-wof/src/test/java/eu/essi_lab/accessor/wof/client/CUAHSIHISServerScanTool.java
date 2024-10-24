package eu.essi_lab.accessor.wof.client;

import java.util.Iterator;
import java.util.List;

import eu.essi_lab.accessor.wof.client.datamodel.Site;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.model.exceptions.GSException;

public class CUAHSIHISServerScanTool {

    public static void main(String[] args) throws GSException {
	CUAHSIHISServerScanTool tool = new CUAHSIHISServerScanTool();
	String endpoint = "https://hydroportal.cuahsi.org/woftest/cuahsi_1_1.asmx";
	tool.scan(endpoint);
    }

    private void scan(String endpoint) throws GSException {
	CUAHSIHISServerClient client = new CUAHSIHISServerClient1_1(endpoint);
	Iterator<SiteInfo> sitesIterator = client.getSites();
	int i = 0;
	int s = 0;
	while (sitesIterator.hasNext()) {
	    SiteInfo siteInfo = (SiteInfo) sitesIterator.next();
	    System.out.println("Site #" + ++i + ": " + siteInfo.getSiteName());
	    SitesResponseDocument info = client.getSiteInfo(siteInfo.getSiteCodeNetwork(), siteInfo.getSiteCode());
	    List<Site> sites = info.getSites();
	    if (sites.size() != 1) {
		System.err.println("SHOULDN'T BE " + sites.size());
	    }
	    for (Site site : sites) {
		List<TimeSeries> series = site.getSeries();
		for (TimeSeries serie : series) {
		    String name = serie.getVariableName() + " " + serie.getDataType();
		    System.out.println("Series #: " + ++s + name);
		}
	    }
	}
	System.out.println("Total sites: " + i);
	System.out.println("Total time series: " + s);
    }
}
