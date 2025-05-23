package eu.essi_lab.validator.netcdf;

import java.util.Iterator;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.ReferenceIdentifier;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.cs.CoordinateSystem;
import org.geotools.referencing.CRS;
import org.junit.Test;

public class WKTTest {

    @Test
    public void test() throws NoSuchAuthorityCodeException, FactoryException {
	test("EPSG:4326");
	test("urn:ogc:def:crs:OGC::CRS84");
	test("EPSG:3857");
	test("EPSG:3309");
	test("EPSG:3035");
	test("EPSG:3005");
    }

    public void test(String crs) throws NoSuchAuthorityCodeException, FactoryException {
	CoordinateReferenceSystem geoCRS = org.geotools.referencing.CRS.decode(crs);
	String wkt = geoCRS.toWKT();
	// wktpar
	System.out.println(wkt);
	String id = "NONE";
	Iterator<ReferenceIdentifier> it = geoCRS.getCoordinateSystem().getIdentifiers().iterator();
	if (it.hasNext()) {
	    ReferenceIdentifier n = it.next();
	    id = n.getCodeSpace() + ":" + n.getCode();
	}
	CoordinateSystem cs = geoCRS.getCoordinateSystem();

	System.out.println(crs + ": " + geoCRS.getCoordinateSystem().getName() + " - " + geoCRS.getCoordinateSystem().getClass().getName()
		+ " AXIS ORDER: " + CRS.getAxisOrder(geoCRS));
	System.out.println();
    }

}
