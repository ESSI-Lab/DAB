package eu.essi_lab.accessor.rasaqm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class RasaqmClientExternalTestIT {

    @Test
    public void test() throws Exception {
	RasaqmClient client = new RasaqmClient();
	List<SimpleEntry<String, String>> parameters = client.getParameters();
	assertTrue(parameters.size() > 5);
	boolean found = false;
	for (SimpleEntry<String, String> parameter : parameters) {
	    System.out.println(parameter.getKey() + " (" + parameter.getValue() + ")");
	    if (parameter.getKey().equals("NO2")) {
		assertEquals("Concentration of NO2", parameter.getValue());
		found = true;
	    }
	}
	assertTrue(found);
	Date date = new Date();
	RasaqmDataset dataset = client.parseData("CO", date, date, null);
	System.out.println(dataset.getParameterId());
	System.out.println(dataset.getParameterName());
	assertEquals("CO", dataset.getParameterId());
	assertEquals("Concentration of CO", dataset.getParameterName());
	Set<String> stations = dataset.getStationNames();
	String station = stations.iterator().next();
	RasaqmSeries s = dataset.getSeries(station);
	List<RasaqmData> data = s.getData();
	assertTrue(!data.isEmpty());
	Date tmpDate = null;
	for (RasaqmData simpleEntry : data) {
	    if (tmpDate == null) {
		tmpDate = simpleEntry.getDate();
	    } else {
		assertTrue(tmpDate.before(simpleEntry.getDate()));
		tmpDate = simpleEntry.getDate();
	    }
	    System.out.println(simpleEntry.getValue() + " " + simpleEntry.getDate());

	}
    }

}
