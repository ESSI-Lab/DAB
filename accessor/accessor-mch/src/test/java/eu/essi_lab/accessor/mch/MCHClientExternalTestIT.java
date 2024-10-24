package eu.essi_lab.accessor.mch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.mch.datamodel.MCHAvailability;
import eu.essi_lab.accessor.mch.datamodel.MCHCountry;
import eu.essi_lab.accessor.mch.datamodel.MCHStation;
import eu.essi_lab.accessor.mch.datamodel.MCHValue;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class MCHClientExternalTestIT {

    @Test
    public void testDRendpoint() {
	MCHClient client = new MCHClient("http://mch2.westus2.cloudapp.azure.com:5000/API");
	MCHCountry country = client.getCountry();
	System.out.println(country.getName());
	assertEquals("Dominican Republic", country.getName());
	List<MCHStation> stations = client.getStations();
	System.out.println(stations.size() + " stations downloaded");
	assertTrue(stations.size() > 250);
	MCHStation station = client.getStationById("1811");
	System.out.println(station.getStationName());
	System.out.println(station.getStationId());
	List<MCHAvailability> availabilities = client.getAvailability(station.getStationName());
	String var = null;
	for (MCHAvailability avail : availabilities) {
	    var = avail.getVariable();
	    System.out.println(avail.getVariable() + " " + avail.getStartDate() + " " + avail.getEndDate());
	}
	Date start = ISO8601DateTimeUtils.parseISO8601ToDate("1990-01-01").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1991-01-01").get();
	List<MCHValue> data = client.getDailyData(station.getStationId(), var, start, end);
	for (MCHValue d : data) {
	    System.out.println(d.getDate()+ " "+d.getValue());
	}

    }

}
