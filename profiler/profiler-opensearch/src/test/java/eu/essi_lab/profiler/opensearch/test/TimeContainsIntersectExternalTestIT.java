package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * The test checks that the OS time constraint is correctly supported, both in the overlaps and contains version.
 * The source used is Copernicus Open Access Hub, having data that is constantly updated (i.e. end time=now)
 * 
 * @author boldrini
 */
public class TimeContainsIntersectExternalTestIT {

    @Test
    public void test() throws Exception {

	boolean a1 = test(1900, 1950, "CONTAINS", false);
	boolean a2 = test(1900, 2020, "CONTAINS", false);
	boolean a3 = test(1900, 2100, "CONTAINS", true);
	boolean a4 = test(2020, 2021, "CONTAINS", false);
	boolean a5 = test(2020, 2100, "CONTAINS", false);
	boolean a6 = test(2050, 2100, "CONTAINS", false);

	boolean b1 = test(1900, 1950, "OVERLAPS", false);
	boolean b2 = test(1900, 2020, "OVERLAPS", true);
	boolean b3 = test(1900, 2100, "OVERLAPS", true);
	boolean b4 = test(2020, 2021, "OVERLAPS", true);
	boolean b5 = test(2020, 2100, "OVERLAPS", true);
	boolean b6 = test(2050, 2100, "OVERLAPS", false);

	// default is overlaps
	boolean c1 = test(1900, 1950, null, false);
	boolean c2 = test(1900, 2020, null, true);
	boolean c3 = test(1900, 2100, null, true);
	boolean c4 = test(2020, 2021, null, true);
	boolean c5 = test(2020, 2100, null, true);
	boolean c6 = test(2050, 2100, null, false);

	assertTrue(a1 && a2 && a3 && a4 && a5 && a6);
	assertTrue(b1 && b2 && b3 && b4 && b5 && b6);
	assertTrue(c1 && c2 && c3 && c4 && c5 && c6);

    }

    private boolean test(int start, int end, String relation, boolean expectedFull) throws Exception {

	if (relation == null) {
	    relation = "";
	} else {
	    relation = "&timeRel=" + relation;
	}
	String url = "https://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?searchFields=title,keywords,abstract&reqID=04jfvhnqbn0g&si=1&ct=12&tf=keyword,format,protocol,providerID,organisationName,sscScore"
		+ relation + "&viewid=&ts=" + start + "-08-29T02:00:00Z&sources=sentinelscihudtest&te=" + end + "-01-01T00:00:00Z";
	Downloader d = new Downloader();
	String str = d.downloadOptionalString(url).get();
	XMLDocumentReader reader = new XMLDocumentReader(str);
	Number number = reader.evaluateNumber("//*:totalResults");
	boolean actualFull = number.intValue() > 0;

	if (actualFull == expectedFull) {
	    GSLoggerFactory.getLogger(getClass()).info("Tested {} from {} to {}. Result: {}", relation, start, end, actualFull);
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Tested {} from {} to {}. Result: {}", relation, start, end, actualFull);
	}

	return actualFull == expectedFull;

    }

}
