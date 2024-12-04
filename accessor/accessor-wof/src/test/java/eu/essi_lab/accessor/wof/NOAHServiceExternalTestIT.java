package eu.essi_lab.accessor.wof;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient;
import eu.essi_lab.accessor.wof.client.CUAHSIHISServerClient1_0;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.model.exceptions.GSException;

public class NOAHServiceExternalTestIT {

	@Test
	public void test() throws Exception {
		String endpoint = "https://hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_NOAH_002.cgi?WSDL";
		CUAHSIHISServerClient client = new CUAHSIHISServerClient1_0(endpoint);
		Iterator<SiteInfo> siteIterator = client.getSitesObject();
		int i = 0;
		while (siteIterator.hasNext()) {
			SiteInfo siteInfo = (SiteInfo) siteIterator.next();
			System.out.println(siteInfo.getSiteName());
			i++;
		}
		System.out.println("Total sites: " + i);
	}

}
