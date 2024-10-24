package eu.essi_lab.accessor.apitempo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.apitempo.APITempoData.APITempoDataCode;
import eu.essi_lab.accessor.apitempo.APITempoParameter.APITempoParameterCode;
import eu.essi_lab.accessor.apitempo.APITempoStation.APITempoStationCode;

public class APITempoClientExternalTestIT {
    @Test
    public void test() throws Exception {
	APITempoClient client = new APITempoClient();
	APITempoClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	List<APITempoStation> stations = client.getStations();
	for (APITempoStation station : stations) {
	    String name = station.getValue(APITempoStationCode.NAME);
	    System.out.println(name + " "+station.getValue(APITempoStationCode.ID)+ " "+station.getValue(APITempoStationCode.WIGOS_ID));
	}
	
	assertTrue(!stations.isEmpty());
	String stationCode = "1115";
	String parameterCode = "3";
	APITempoStation station = client.getStation(stationCode);
	assertEquals("1115", station.getValue(APITempoStationCode.ID));
	assertEquals("-15.244620000000001", station.getValue(APITempoStationCode.LATITUDE));
	assertEquals("-40.22955", station.getValue(APITempoStationCode.LONGITUDE));
	assertEquals("ITAPETINGA", station.getValue(APITempoStationCode.NAME));
	assertEquals("271.5", station.getValue(APITempoStationCode.ELEVATION));
	assertNull(station.getValue(APITempoStationCode.WIGOS_ID));
	assertEquals("SBBX", station.getValue(APITempoStationCode.RESPONSIBLE));

	APITempoParameter parameter = client.getStationParameter(stationCode, parameterCode);
	assertEquals("3", parameter.getValue(APITempoParameterCode.ID));
	assertEquals("airTemperature", parameter.getValue(APITempoParameterCode.NAME));
	assertEquals("K", parameter.getValue(APITempoParameterCode.UNITS));
	assertEquals("instant_total", parameter.getValue(APITempoParameterCode.INTERPOLATION));
	assertEquals("1", parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD));
	assertEquals("hour", parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD_UNITS));
	assertEquals("1", parameter.getValue(APITempoParameterCode.TIME_SPACING));
	assertEquals("hour", parameter.getValue(APITempoParameterCode.TIME_SPACING_UNITS));

	List<APITempoData> datas = client.getData(stationCode, parameterCode, "2020-01-10", "2020-01-30");
	APITempoData data = datas.get(0);
	assertEquals("ITAPETINGA", data.getValue(APITempoDataCode.STATION_NAME));
	assertEquals("airTemperature", data.getValue(APITempoDataCode.PARAMETER_NAME));
	assertEquals("297.85", data.getValue(APITempoDataCode.VALUE));
	assertEquals("K", data.getValue(APITempoDataCode.UNITS));
	assertEquals("2020", data.getValue(APITempoDataCode.YEAR));
	assertEquals("1", data.getValue(APITempoDataCode.MONTH));
	assertEquals("29", data.getValue(APITempoDataCode.DAY));
	assertEquals("10", data.getValue(APITempoDataCode.HOUR));
	assertEquals("0", data.getValue(APITempoDataCode.MINUTE));

    }

    @Test
    public void testDates() throws Exception {
	APITempoClient client = new APITempoClient();
	SimpleEntry<Date, Date> beginEnd = client.getBeginEndDates("680", "9");
	Date begin = beginEnd.getKey();
	System.out.println(begin);
	Date end = beginEnd.getValue();
	System.out.println(end);
	assertTrue(end.after(begin));
    }

}
