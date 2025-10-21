package eu.essi_lab.accessor.emodnet;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class ERDDAPRiverClientExternalTestIT {

    @Test
    public void test() {
	String baseURL = "https://data-erddap.emodnet-physics.eu/erddap/tabledap/index";
	ERDDAPRiverClient client = new ERDDAPRiverClient(baseURL);
	List<ERDDAPRow> metadata = client.getMetaData();
	System.out.println(metadata.getFirst());
	ERDDAPRow first = metadata.getFirst();
	String stationCode = (String) first.getValue("PLATFORMCODE");

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2020-01-01").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2021-01-01").get();

	List<ERDDAPRow> datas = client.getData(stationCode, begin, end);
	for (ERDDAPRow data : datas) {
	    System.out.println(data.toString());
	}
    }

}
