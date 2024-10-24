package eu.essi_lab.accessor.canada;

import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;

public class CANADAMSCExternalTestIT {

    @Test
    public void test() throws Exception {
	CANADAMSCConnector c = new CANADAMSCConnector();
	c.setSourceURL("http://dd.weather.gc.ca/hydrometric/");
	ListRecordsRequest listRecords = new ListRecordsRequest();
	c.listRecords(listRecords );
	List<ECStation> stations = c.getCanadaStations();
	TreeSet<String>codes = new TreeSet<>();
	for (ECStation station : stations) {
	    codes.add(station.getStationCode());
	}
	for (String code : codes) {
	    System.out.println(code);
	}
	assertFalse(codes.isEmpty());
	
    }
    
}
