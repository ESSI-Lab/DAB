package eu.essi_lab.workflow.processor.grid;

import org.junit.Test;

public class GDAL_EOX_111_GeoTIFF_4326_Test extends GDAL_ProcessorsTest {

    @Test
    public void test() throws Exception {
	super.test();
    }

    @Override
    public String getDatasetPath() {
	return "data/eox-wcs111-coverage.tif";
    }

}