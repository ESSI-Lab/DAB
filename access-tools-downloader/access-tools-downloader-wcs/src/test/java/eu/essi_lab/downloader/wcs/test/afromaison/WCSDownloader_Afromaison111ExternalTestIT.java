package eu.essi_lab.downloader.wcs.test.afromaison;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.downloader.wcs.WCSDownloader_111;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSDownloader_Afromaison111ExternalTestIT extends WCSDownloader_AfromaisonTest {

    @Before
    public void init() {
	this.downloader = new WCSDownloader_111();
    }

    @Ignore("Unable to meaningful download something from this WCS 1.1.1 as the size is missing and the bounding box is not aligned with the specified resolution. "
	    + "Moreover trying to download something it will produce GeoTiff with inverted latitude and longitude. It is preferred to use WCS 1.0.0 for this service.")
    @Test
    public void test() throws Exception {
	super.test();

    }

    @Override
    public NetProtocol getProtocol() {
	return NetProtocols.WCS_1_1_1;
    }

    // this should be a correct download request, however Geoserver WCS 1.1.1 gives back a GeoTIFF with
    // inverted lat & lon!
    @Override
    public String getDownloadURL() {
	return "http://afromaison.grid.unep.ch:8080/geoserver/ows?SERVICE=WCS&VERSION=1.1.1&REQUEST=GetCoverage&identifier=ethiopia%3AETH_Aster&GridBaseCRS=urn:ogc:def:crs:EPSG::4326&GridType=urn:ogc:def:method:WCS:1.1:2dGridIn2dCrs&GridCS=urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS&BoundingBox=11.556,37.441,12.171,38.055,urn:ogc:def:crs:EPSG::4326&GridOffsets=2.777777777777777E-4,0.0,0.0,-2.7782805429864125E-4&FORMAT=image%2Ftiff%3Bsubtype%3D%22geotiff%22";
    }

}
