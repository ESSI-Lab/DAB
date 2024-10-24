package eu.essi_lab.workflow.processor.grid;

import org.junit.Test;

public class GDAL_DROUGHT_100_GeoTIFF_SINUSOIDAL_Test extends GDAL_ProcessorsTest {

    @Test
    public void test() throws Exception {
	super.test();
    }

    @Override
    public String getDatasetPath() {
	return "data/drought-wcs100-coverage.tif";
    }

}
