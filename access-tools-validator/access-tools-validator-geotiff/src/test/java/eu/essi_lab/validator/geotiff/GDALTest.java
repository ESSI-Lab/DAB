package eu.essi_lab.validator.geotiff;

import org.gdal.gdal.gdal;

public class GDALTest {
    public static void main(String[] args) {

	System.out.println(gdal.VersionInfo());
	
//	String path = "/home/boldrini/git/GI-project/accessor/accessor-wcs/src/test/resources/drought-wcs100-coverage.tif";
	String path = "/home/boldrini/git/GI-project/accessor/accessor-wcs/src/test/resources/eox-wcs100-coverage.tif";
//	Dataset hDataset = gdal.Open(path);
//	String ret = gdal.GDALInfo(hDataset , null);
//	System.out.println(ret);
	gdalinfo.main(new String[] {path});
	
	
    }
}
