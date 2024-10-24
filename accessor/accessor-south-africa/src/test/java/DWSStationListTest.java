import java.io.InputStream;
import java.util.HashMap;

import org.junit.Test;

import eu.essi_lab.accessor.dws.client.DWSStation;
import eu.essi_lab.accessor.dws.client.DWSStationList;

public class DWSStationListTest {

    @Test
    public void testName() throws Exception {
	InputStream is = DWSStationListTest.class.getResourceAsStream("stations.xhtml");
	DWSStationList list = new DWSStationList(is);
	HashMap<String, DWSStation> stations = list.getStations();
	for (String stationCode : stations.keySet()) {
	    DWSStation station = stations.get(stationCode);
	    System.out.println(station.getStationCode() + " " + station.getStationName() + " " + station.getCatchmentAreaKm2() + " "
		    + station.getLatitude() + " " + station.getLongitude() + station.getBeginDate() + " " + station.getEndDate());
	}

    }
    
}
