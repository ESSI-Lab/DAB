package eu.essi_lab.accessor.odatahidro;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Before;
import org.junit.Test;

public class ODataHidrologyMapperTest {

    double TOL = .00001;
    private ODataHidrologyMapper mapper;

    @Before
    public void init() {
	this.mapper = new ODataHidrologyMapper();
    }

    @Test
    public void test() {

	testCRSConvert(7017085, 573141, 63.27521847951158, 28.45780850324803);
	testCRSConvert(6926501, 677699, 62.427157950097246, 30.44212826457296);
	testCRSConvert(6813622, 624940, 61.436230799184166, 29.342574956476263);
	testCRSConvert(6716077, 539394, 60.57869295020598, 27.718869337780802);
	testCRSConvert(6702322, 476575, 60.45644565324672, 26.57414735995583);
	testCRSConvert(6864076, 495616, 61.9092734434815,26.916547632968314);
    }

    private void testCRSConvert(double x, double y, double lat, double lon) {
	SimpleEntry<Double, Double> latLon = mapper.getLatLon(x, y);
	System.out.println("Obtained: " + latLon.getKey() + "," + latLon.getValue());
	assertEquals(lat, latLon.getKey(), TOL);
	assertEquals(lon, latLon.getValue(), TOL);

    }

}
