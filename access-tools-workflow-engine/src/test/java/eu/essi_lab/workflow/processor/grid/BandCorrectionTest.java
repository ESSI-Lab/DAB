package eu.essi_lab.workflow.processor.grid;

import java.io.File;

import eu.essi_lab.model.resource.data.DataObject;
import ucar.nc2.dataset.NetcdfDataset;

public class BandCorrectionTest {
    public static void main(String[] args) throws Exception {
	DataObject input = new DataObject();
	input.setFile(new File("/tmp/first.nc"));
	NetcdfDataset dataset = NetcdfDataset.openDataset("/tmp/netcdf-connector-Monthly_T2m.nc");
	DataObject sourceObject = new DataObject();
	sourceObject.setFile(new File("/tmp/netcdf-connector-Monthly_T2m.nc"));
	DataObject output = GDALNetCDFPostConversionUtils.doBandCorrections(dataset,input);
	output = GDALNetCDFPostConversionUtils.copyAttributes(sourceObject, output);

	System.out.println(output.getFile());
	System.out.println();
    }
}
