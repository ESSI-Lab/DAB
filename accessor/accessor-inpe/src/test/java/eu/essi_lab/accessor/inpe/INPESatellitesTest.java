package eu.essi_lab.accessor.inpe;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class INPESatellitesTest {

    @Test
    public void test() throws Exception {
	InputStream stream = getStream();
	INPESatellites.getInstance().init(stream);
	assertEquals(getExpectedSatellitesCount(), INPESatellites.getInstance().getSatellites().size());
	INPESatellite satellite = INPESatellites.getInstance().getSatellites().get(0);
	assertEquals("A1", satellite.getId());
	assertEquals("AQUA", satellite.getTitle());
	List<INPESensor> sensors = satellite.getSensors();
	assertEquals(1, sensors.size());
	INPESensor sensor = sensors.get(0);
	assertEquals("MODIS", sensor.getId());
	assertEquals("MODIS", sensor.getTitle());

	satellite = INPESatellites.getInstance().getSatellites().get(1);
	assertEquals("CB2", satellite.getId());
	assertEquals("CBERS 2", satellite.getTitle());
	sensors = satellite.getSensors();
	assertEquals(3, sensors.size());
	sensor = sensors.get(0);
	assertEquals("CCD", sensor.getId());
	assertEquals("CCD", sensor.getTitle());
	sensor = sensors.get(1);
	assertEquals("IRM", sensor.getId());
	assertEquals("IRM", sensor.getTitle());
	sensor = sensors.get(2);
	assertEquals("WFI", sensor.getId());
	assertEquals("WFI", sensor.getTitle());

    }

    /**
     * @return
     */
    protected int getExpectedSatellitesCount() {

	return 11;
    }

    public InputStream getStream() {
	return INPESatellitesTest.class.getClassLoader().getResourceAsStream("inpe/panel.html");
    }
}
