package eu.essi_lab.accessor.ukeafloodmonitoring;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Optional;

/**
 * Conversion of Ordnance Survey National Grid references (e.g. "SY 5876090850", "TQ3910") to WGS84 latitude and longitude.<br>
 * Some stations of the flood-monitoring API (mostly groundwater boreholes) are published only with a grid reference. The
 * reference is converted to OSGB36 easting and northing (centre of the referenced square), then to OSGB36 latitude and longitude
 * (inverse Transverse Mercator projection on the Airy 1830 ellipsoid) and finally to WGS84 with the Helmert transformation
 * published by the Ordnance Survey (accuracy of a few metres, better than the precision of the grid references)
 *
 * @author boldrini
 */
public class EAFloodMonitoringGridReference {

    // Airy 1830 ellipsoid
    private static final double AIRY_A = 6377563.396;
    private static final double AIRY_B = 6356256.909;

    // WGS84 ellipsoid
    private static final double WGS84_A = 6378137.0;
    private static final double WGS84_B = 6356752.314245;

    // National Grid projection
    private static final double F0 = 0.9996012717;
    private static final double LAT0 = Math.toRadians(49);
    private static final double LON0 = Math.toRadians(-2);
    private static final double E0 = 400000;
    private static final double N0 = -100000;

    // OSGB36 to WGS84 Helmert transformation
    private static final double TX = 446.448;
    private static final double TY = -125.157;
    private static final double TZ = 542.060;
    private static final double S = -20.4894;
    private static final double RX = Math.toRadians(0.1502 / 3600);
    private static final double RY = Math.toRadians(0.2470 / 3600);
    private static final double RZ = Math.toRadians(0.8421 / 3600);

    private EAFloodMonitoringGridReference() {
    }

    /**
     * @param gridReference e.g. "SY 5876090850"
     * @return the OSGB36 easting and northing of the centre of the referenced square, or empty if the reference is not valid
     */
    public static Optional<double[]> toEastingNorthing(String gridReference) {

	if (gridReference == null) {
	    return Optional.empty();
	}

	String reference = gridReference.replace(" ", "").toUpperCase();
	if (!reference.matches("[HJNOST][A-HJ-Z](\\d\\d){0,5}")) {
	    return Optional.empty();
	}

	int l1 = reference.charAt(0) - 'A';
	int l2 = reference.charAt(1) - 'A';
	// the letter I is not used
	if (l1 > 7) {
	    l1--;
	}
	if (l2 > 7) {
	    l2--;
	}

	int e100km = (((l1 - 2) % 5 + 5) % 5) * 5 + (l2 % 5);
	int n100km = (19 - (l1 / 5) * 5) - (l2 / 5);

	String digits = reference.substring(2);
	int half = digits.length() / 2;

	// size of the referenced square, in metres
	double resolution = Math.pow(10, 5 - half);

	double easting = e100km * 100000.0;
	double northing = n100km * 100000.0;

	if (half > 0) {
	    easting += Integer.parseInt(digits.substring(0, half)) * resolution;
	    northing += Integer.parseInt(digits.substring(half)) * resolution;
	}

	return Optional.of(new double[] { easting + resolution / 2, northing + resolution / 2 });
    }

    /**
     * @param gridReference e.g. "SY 5876090850"
     * @return the WGS84 latitude and longitude (degrees), or empty if the reference is not valid
     */
    public static Optional<double[]> toLatLon(String gridReference) {

	return toEastingNorthing(gridReference).map(en -> toLatLon(en[0], en[1]));
    }

    /**
     * @param easting OSGB36 easting
     * @param northing OSGB36 northing
     * @return the WGS84 latitude and longitude (degrees)
     */
    public static double[] toLatLon(double easting, double northing) {

	double[] osgb36 = inverseProjection(easting, northing);
	return helmert(osgb36[0], osgb36[1]);
    }

    /**
     * Inverse Transverse Mercator projection of the National Grid (see "A guide to coordinate systems in Great Britain", Ordnance
     * Survey)
     *
     * @param easting
     * @param northing
     * @return the OSGB36 latitude and longitude (radians)
     */
    private static double[] inverseProjection(double easting, double northing) {

	double a = AIRY_A;
	double b = AIRY_B;
	double e2 = 1 - (b * b) / (a * a);
	double n = (a - b) / (a + b);
	double n2 = n * n;
	double n3 = n * n * n;

	double lat = LAT0;
	double m = 0;

	do {
	    lat = (northing - N0 - m) / (a * F0) + lat;

	    double ma = (1 + n + (5.0 / 4) * n2 + (5.0 / 4) * n3) * (lat - LAT0);
	    double mb = (3 * n + 3 * n2 + (21.0 / 8) * n3) * Math.sin(lat - LAT0) * Math.cos(lat + LAT0);
	    double mc = ((15.0 / 8) * n2 + (15.0 / 8) * n3) * Math.sin(2 * (lat - LAT0)) * Math.cos(2 * (lat + LAT0));
	    double md = (35.0 / 24) * n3 * Math.sin(3 * (lat - LAT0)) * Math.cos(3 * (lat + LAT0));
	    m = b * F0 * (ma - mb + mc - md);

	} while (Math.abs(northing - N0 - m) >= 0.00001);

	double sinLat = Math.sin(lat);
	double cosLat = Math.cos(lat);

	double nu = a * F0 / Math.sqrt(1 - e2 * sinLat * sinLat);
	double rho = a * F0 * (1 - e2) / Math.pow(1 - e2 * sinLat * sinLat, 1.5);
	double eta2 = nu / rho - 1;

	double tanLat = Math.tan(lat);
	double tan2 = tanLat * tanLat;
	double tan4 = tan2 * tan2;
	double tan6 = tan4 * tan2;
	double secLat = 1 / cosLat;
	double nu3 = nu * nu * nu;
	double nu5 = nu3 * nu * nu;
	double nu7 = nu5 * nu * nu;

	double vii = tanLat / (2 * rho * nu);
	double viii = tanLat / (24 * rho * nu3) * (5 + 3 * tan2 + eta2 - 9 * tan2 * eta2);
	double ix = tanLat / (720 * rho * nu5) * (61 + 90 * tan2 + 45 * tan4);
	double x = secLat / nu;
	double xi = secLat / (6 * nu3) * (nu / rho + 2 * tan2);
	double xii = secLat / (120 * nu5) * (5 + 28 * tan2 + 24 * tan4);
	double xiia = secLat / (5040 * nu7) * (61 + 662 * tan2 + 1320 * tan4 + 720 * tan6);

	double de = easting - E0;
	double de2 = de * de;
	double de3 = de2 * de;
	double de4 = de3 * de;
	double de5 = de4 * de;
	double de6 = de5 * de;
	double de7 = de6 * de;

	double latitude = lat - vii * de2 + viii * de4 - ix * de6;
	double longitude = LON0 + x * de - xi * de3 + xii * de5 - xiia * de7;

	return new double[] { latitude, longitude };
    }

    /**
     * @param lat OSGB36 latitude (radians)
     * @param lon OSGB36 longitude (radians)
     * @return the WGS84 latitude and longitude (degrees)
     */
    private static double[] helmert(double lat, double lon) {

	// OSGB36 cartesian coordinates (height 0)
	double e2 = 1 - (AIRY_B * AIRY_B) / (AIRY_A * AIRY_A);
	double sinLat = Math.sin(lat);
	double nu = AIRY_A / Math.sqrt(1 - e2 * sinLat * sinLat);

	double x1 = nu * Math.cos(lat) * Math.cos(lon);
	double y1 = nu * Math.cos(lat) * Math.sin(lon);
	double z1 = (1 - e2) * nu * sinLat;

	double s1 = S / 1e6 + 1;

	double x2 = TX + x1 * s1 - y1 * RZ + z1 * RY;
	double y2 = TY + x1 * RZ + y1 * s1 - z1 * RX;
	double z2 = TZ - x1 * RY + y1 * RX + z1 * s1;

	// WGS84 geodetic coordinates
	double wgsE2 = 1 - (WGS84_B * WGS84_B) / (WGS84_A * WGS84_A);
	double p = Math.sqrt(x2 * x2 + y2 * y2);

	double latitude = Math.atan2(z2, p * (1 - wgsE2));
	double previous = 2 * Math.PI;

	while (Math.abs(latitude - previous) > 1e-12) {
	    double sin = Math.sin(latitude);
	    double wgsNu = WGS84_A / Math.sqrt(1 - wgsE2 * sin * sin);
	    previous = latitude;
	    latitude = Math.atan2(z2 + wgsE2 * wgsNu * sin, p);
	}

	double longitude = Math.atan2(y2, x2);

	return new double[] { Math.toDegrees(latitude), Math.toDegrees(longitude) };
    }
}
