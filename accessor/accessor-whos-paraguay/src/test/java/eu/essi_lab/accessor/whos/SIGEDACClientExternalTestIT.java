package eu.essi_lab.accessor.whos;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import eu.essi_lab.accessor.whos.sigedac.SIGEDACClient;
import eu.essi_lab.accessor.whos.sigedac.SIGEDACData;
import eu.essi_lab.accessor.whos.sigedac.SIGEDACProperty;
import eu.essi_lab.accessor.whos.sigedac.SIGEDACStation;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class SIGEDACClientExternalTestIT {

    
    @Test
    public void testRiver() throws Exception{
	SIGEDACClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	SIGEDACClient client = new SIGEDACClient();
	List<SIGEDACStation> stations = client.getStations();
	System.out.println(stations.size() + " stations found");
	int number = 0;
	for (SIGEDACStation station : stations) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		SIGEDACData data = client.getRiverLevel(station.getCode(), sdf.parse("1900-10-01"), sdf.parse("2023-11-01"), 1);
		System.out.println("Got river data from station: " + data.getStationName());
		System.out.println("Total data pages: " + data.getTotalPages());
		List<SimpleEntry<Date, BigDecimal>> values = data.getData();
		System.out.println("Total values: " + values.size());
		
		if(values.size() > 0)
		    number++;
	}
	assertTrue(number == 3);
    }
    
    //@Test
    public void test() throws Exception {
	SIGEDACClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	SIGEDACClient client = new SIGEDACClient();
	List<SIGEDACStation> stations = client.getStations();
	System.out.println(stations.size() + " stations found");
	assertTrue(stations.size() > 10);
	HashSet<String> types = new HashSet<>();
	for (SIGEDACStation station : stations) {
	    System.out.print("id: " + station.getId());
	    System.out.print(" code: " + station.getCode());
	    System.out.print(" wmo code: " + station.getWMOCode());
	    System.out.print(" name: " + station.getName());
	    System.out.print(" lat: " + station.getLatitude());
	    System.out.print(" lon: " + station.getLongitude());
	    System.out.print(" elev: " + station.getElevation());
	    System.out.print(" active: " + station.isActive());
	    System.out.print(" city id: " + station.getCityId());
	    System.out.print(" city name: " + station.getCityName());
	    System.out.print(" station type: " + station.getStationType());
	    types.add(station.getStationType());
	    System.out.println();
	}
	System.out.println("Station types: ");
	for (String type : types) {
	    System.out.print(type+" ");
	}	
	System.out.println();
	List<SIGEDACProperty> properties = client.getProperties();
	System.out.println(properties.size() + " observed properties found");
	assertTrue(properties.size() > 3);
	HashMap<String, SIGEDACProperty> map = new HashMap<String, SIGEDACProperty>();
	for (SIGEDACProperty property : properties) {
	    System.out.print("id: " + property.getId());
	    System.out.print(" name: " + property.getName());
	    System.out.print(" short name: " + property.getShortName());
	    System.out.print(" unit id: " + property.getUnitId());
	    System.out.print(" unit code: " + property.getUnitCode());
	    System.out.print(" unit name: " + property.getUnitName());
	    System.out.print(" unit symbol: " + property.getUnitSymbol());
	    System.out.print(" magnitude: " + property.getMagnitude());
	    System.out.println();
	    map.put(property.getId(), property);
	}
	System.out.println();

	SIGEDACStation station1 = stations.get(0);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	SIGEDACData data = client.getRiverLevel(station1.getCode(), sdf.parse("2023-10-01"), sdf.parse("2023-10-10"), 1);

	System.out.println("Got river data from station: " + data.getStationName());
	System.out.println("Total data pages: " + data.getTotalPages());
	List<SimpleEntry<Date, BigDecimal>> values = data.getData();
	for (int i = 0; i < values.size(); i++) {
	    SimpleEntry<Date, BigDecimal> value = values.get(i);
	    System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(value.getKey()) + " - " + value.getValue());
	}

	int n;

	n = 0;
	for (SIGEDACStation station : stations) {
	    data = client.getRiverLevel(station.getCode(), sdf.parse("2023-10-01"), sdf.parse("2023-10-10"), 1);
	    Integer pages = data.getTotalPages();
	    if (!data.getData().isEmpty()) {
		System.out
			.println(pages + " river level data page(s) available for station: " + station.getName() + " " + station.getCode());
		n++;
	    } else {
		System.out.println("No river level data available for station: " + station.getName());
	    }
	}
	System.out.println(n + " out of " + stations.size() + " stations have river data available");

	checkData(client, stations, map.get("1"));
	checkData(client, stations, map.get("87"));
	checkData(client, stations, map.get("88"));
	checkData(client, stations, map.get("89"));
	checkData(client, stations, map.get("90"));
	checkData(client, stations, map.get("123"));

    }

    private void checkData(SIGEDACClient client, List<SIGEDACStation> stations, SIGEDACProperty property) throws Exception {
	SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

	int n = 0;
	SIGEDACData data;
	for (SIGEDACStation station : stations) {
	    data = client.getHourlyData(station.getId(), property.getId(), sdf.parse("1900-10-01"), sdf.parse("2030-01-01"), 1);
	    Integer pages = data.getTotalPages();
	    if (!data.getData().isEmpty()) {
		n++;
		System.out.println(pages + " hourly (" + property.getName() + ") data page(s) available for station: " + station.getName()
			+ " " + station.getCode());
	    } else {
		System.out.println("no hourly (" + property.getName() + ") data available");
	    }
	}
	System.out.println(n + " out of " + stations.size() + " stations have " + property.getName() + " hourly data available");

	n = 0;
	for (SIGEDACStation station : stations) {
	    data = client.getDailyData(station.getId(), property.getId(), sdf.parse("1900-10-01"), sdf.parse("2030-01-01"), 1);
	    Integer pages = data.getTotalPages();
	    if (!data.getData().isEmpty()) {
		n++;
		System.out.println(pages + " daily (" + property.getName() + ") data page(s) available for station: " + station.getName()
			+ " " + station.getCode());
	    } else {
		System.out.println("no daily (" + property.getName() + ") data available");
	    }
	}
	System.out.println(n + " out of " + stations.size() + " stations have " + property.getName() + " daily data available");

    }
}
