package eu.essi_lab.accessor.ana.sar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class ANASARClientExternalTestIT {

    @Test
    public void test() throws Exception {
	ANASARClient client = new ANASARClient();
	List<JSONObject> stations = client.getStations();
	assertFalse(stations.isEmpty());
	System.out.println(stations.size() + " reservoirs");
	assertTrue(stations.size() > 530);

	JSONObject station = client.getStation("19055");
	String reservoirId = station.get("res_id").toString();
	String networkId = station.get("tsi_id").toString();
	System.out.println("Reservoir id: " + reservoirId);
	System.out.println("Network id: " + networkId);

	Date dateBegin = ISO8601DateTimeUtils.parseISO8601ToDate("2020-04-01T00:00:00Z").get();
	Date dateEnd = ISO8601DateTimeUtils.parseISO8601ToDate("2020-04-04T00:00:00Z").get();
	List<SimpleEntry<Date, BigDecimal>> datas = client.getData(reservoirId, "cota", dateBegin, dateEnd);
	for (SimpleEntry<Date, BigDecimal> data : datas) {
	    System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(data.getKey()) + " " + data.getValue());
	}
	assertTrue(!datas.isEmpty());
    }

    @Test
    public void testActiveSeries() throws Exception {
	ANASARClient client = new ANASARClient();

	ParameterInfo info = client.getParameterInfo("19083", "volumeUtil");
	Date start = info.getBegin();
	String startString = ISO8601DateTimeUtils.getISO8601DateTime(start);
	System.out.println(startString);
	assertEquals("2008-10-31T00:00:00Z", startString);
	Date end = info.getEnd();
	String endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	System.out.println(endString);
	assertTrue(end.after(ISO8601DateTimeUtils.parseISO8601ToDate("2020-05-01T00:00:00Z").get()));
    }

    @Test
    public void testActiveSeries2() throws Exception {
	ANASARClient client = new ANASARClient();

	ParameterInfo info = client.getParameterInfo("12508", "cota");
	Date start = info.getBegin();
	String startString = ISO8601DateTimeUtils.getISO8601DateTime(start);
	System.out.println(startString);
	assertEquals("2017-10-02T00:00:00Z", startString);
	Date end = info.getEnd();
	String endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	System.out.println(endString);
	assertEquals("2017-10-02T00:00:00Z", endString);
    }

    @Test
    public void testAbandonedSeries() throws Exception {
	ANASARClient client = new ANASARClient();

	ParameterInfo info = client.getParameterInfo("12001", "cota");
	Date start = info.getBegin();
	String startString = ISO8601DateTimeUtils.getISO8601DateTime(start);
	System.out.println(startString);
	assertEquals("2003-03-27T00:00:00Z", startString);
	Date end = info.getEnd();
	String endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	System.out.println(endString);
	// the following is commented because this staiton is not really abandoned: it is manually updated about each
	// year
	// assertEquals("2020-10-18T00:00:00Z", endString);
    }

    @Test
    public void testEmptySeries() throws Exception {
	ANASARClient client = new ANASARClient();
	ParameterInfo info = client.getParameterInfo("12017", "cota");
	Date start = info.getBegin();
	assertNull(start);
	Date end = info.getEnd();
	assertNull(end);
    }

}
