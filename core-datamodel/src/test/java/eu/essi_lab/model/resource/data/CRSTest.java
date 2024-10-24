package eu.essi_lab.model.resource.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CRSTest {

    @Test
    public void testCRS9034() {
	assertEquals(CRS.fromIdentifier("epsg:5807"), CRS.fromIdentifier("EPSG:5807"));
	assertEquals(CRS.fromIdentifier("epsg:5807"), new EPSGCRS(5807));
	assertEquals(CRS.fromIdentifier("EPSG:5807"), new EPSGCRS(5807));
	CRS crs = CRS.fromIdentifier("EPSG:5807");
	assertEquals(AxisOrder.UNAPPLICABLE, crs.getAxisOrder());
    }

    @Test
    public void testCRS4326() {
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:EPSG::4326"), CRS.fromIdentifier("EPSG:4326"));
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:EPSG::4326"), new EPSGCRS(4326));
	assertEquals(CRS.fromIdentifier("EPSG:4326"), new EPSGCRS(4326));
	CRS crs = CRS.fromIdentifier("EPSG:4326");
	assertEquals(AxisOrder.NORTH_EAST, crs.getAxisOrder());

    }

    @Test
    public void testCRS3857() {
	assertEquals(CRS.fromIdentifier("EPSG:3857"), new EPSGCRS(3857));
	CRS crs = CRS.fromIdentifier("EPSG:3857");
	assertEquals(AxisOrder.EAST_NORTH, crs.getAxisOrder());

    }

    @Test
    public void testCRS84() {
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:OGC:1.3:CRS84"), CRS.fromIdentifier("CRS:84"));
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:OGC:1.3:CRS84"), CRS.fromIdentifier("OGC:84"));
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:OGC:1.3:CRS84"), CRS.fromIdentifier("OGC:CRS84"));
	assertEquals(CRS.fromIdentifier("urn:ogc:def:crs:OGC:1.3:CRS84"), CRS.fromIdentifier("CRS84"));
	CRS crs = CRS.fromIdentifier("CRS:84");
	assertEquals(AxisOrder.EAST_NORTH, crs.getAxisOrder());
    }

    @Test
    public void testCRS() {
	CRS crs = CRS.OGC_IMAGE();
	assertEquals(AxisOrder.EAST_NORTH, crs.getAxisOrder());
    }

    @Test
    public void testEPSGFake() {
	CRS crs = CRS.fromIdentifier("EPSG:68468468468");
	System.out.println();
    }

}
