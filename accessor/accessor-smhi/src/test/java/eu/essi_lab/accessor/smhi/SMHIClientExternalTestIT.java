package eu.essi_lab.accessor.smhi;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class SMHIClientExternalTestIT {

    @Test
    public void test() throws Exception {
	SMHIClient client = new SMHIClient();
	List<SMHIParameter> parameters = client.getParameters();
	for (SMHIParameter parameter : parameters) {

	    System.out.println("Parameter: " + parameter.getTitle() + " " + parameter.getUnits() + " - " + parameter.getKey());

	}
	SMHIParameter parameter = parameters.get(8);
	List<SMHIStation> stations = client.getStations(parameter);
	for (SMHIStation station : stations) {
	    System.out.println("station: " + station.getName() + " " + station.getKey() + " " + station.getStationLink());
	}
	SMHIMetadata md = new SMHIMetadata();
	md.setParameter(parameter);
	md.setStation(stations.get(21));
	String str = md.marshal();	
	System.out.println(str);

    }

    @Test
    public void testDischarge() {
	// Downloads discharge data from the first station listed
	SMHIClient client = new SMHIClient();
	SMHIParameter parameter = client.getParameter("2");
	List<SMHIStation> stations = client.getStations(parameter);
	SMHIStation station = stations.get(0);
	SMHIData data = client.getData(parameter.getKey(), station.getKey());
	List<SMHIValue> values = data.getValues();
	for (SMHIValue value : values) {
	    System.out.println(value.getDate() + " " + value.getValue());
	}

    }

}
