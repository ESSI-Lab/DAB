package eu.essi_lab.accessor.opensearch.shape;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ShapeBoundingBoxNormalizerTest {

    @Test
    public void convertsProjectedBoundsToWgs84() throws Exception {

	// UTM zone 32N bounds (Piedmont, Italy) — not Web Mercator
	double west = 411604.4689999996;
	double east = 424839.81346573366;
	double south = 5066134.5;
	double north = 5086596.492645611;

	double[] wgs84 = ShapeBoundingBoxNormalizer.toWgs84SouthWestNorthEast(west, east, south, north, "EPSG:32632");

	assertTrue(wgs84[1] > 7.7 && wgs84[1] < 8.1);
	assertTrue(wgs84[3] > 7.9 && wgs84[3] < 8.2);
	assertTrue(wgs84[0] > 45.6 && wgs84[0] < 46.0);
	assertTrue(wgs84[2] > 45.8 && wgs84[2] < 46.1);
	assertTrue(wgs84[0] < wgs84[2]);
	assertTrue(wgs84[1] < wgs84[3]);
    }

    @Test
    public void leavesGeographicBoundsUnchanged() throws Exception {

	double[] wgs84 = ShapeBoundingBoxNormalizer.toWgs84SouthWestNorthEast(10.38, 13.92, 45.09, 47.09, "EPSG:4326");

	assertTrue(Math.abs(wgs84[0] - 45.09) < 0.01);
	assertTrue(Math.abs(wgs84[1] - 10.38) < 0.01);
    }
}
