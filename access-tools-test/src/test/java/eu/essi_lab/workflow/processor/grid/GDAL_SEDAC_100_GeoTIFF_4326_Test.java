package eu.essi_lab.workflow.processor.grid;

import org.junit.Test;

public class GDAL_SEDAC_100_GeoTIFF_4326_Test extends GDAL_ProcessorsTest {

    @Test
    public void test() throws Exception {
	super.test();
    }

    @Override
    public String getDatasetPath() {
	return "data/sedac-wcs100-coverage.tif";
    }

}
