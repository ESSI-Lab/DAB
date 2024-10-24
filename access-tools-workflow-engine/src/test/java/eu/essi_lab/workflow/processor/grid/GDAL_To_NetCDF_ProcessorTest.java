package eu.essi_lab.workflow.processor.grid;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class GDAL_To_NetCDF_ProcessorTest {

    private File file1 = null;
    private File file2 = null;

    @Test
    public void test() throws Exception {
	InputStream stream = GDAL_To_NetCDF_ProcessorTest.class.getClassLoader().getResourceAsStream("eox-wcs100-coverage.nc");
	DataObject data = new DataObject();
	data.setFileFromStream(stream, "GDAL_To_NetCDF_ProcessorTest.nc");
	this.file1 = data.getFile();

	NetcdfDataset dataset1 = NetcdfDataset.openDataset(file1.getAbsolutePath());
	List<Variable> grids1 = NetCDFUtils.getGeographicVariables(dataset1);
	assertEquals(3, grids1.size());
	dataset1.close();

	GDAL_To_NetCDF_Processor processor = new GDAL_To_NetCDF_Processor();
	DataObject result = processor.postProcessCorrections(null, data);
	this.file2 = result.getFile();

	NetcdfDataset dataset2 = NetcdfDataset.openDataset(file2.getAbsolutePath());
	List<Variable> grids2 = NetCDFUtils.getGeographicVariables(dataset2);
	assertEquals(1, grids2.size());
	dataset2.close();
    }

    @After
    public void after() {
	if (file1 != null) {
	    file1.delete();
	}
	if (file2 != null) {
	    file2.delete();
	}
    }

}
