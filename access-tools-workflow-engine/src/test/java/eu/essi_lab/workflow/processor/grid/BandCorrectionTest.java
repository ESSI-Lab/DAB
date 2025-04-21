package eu.essi_lab.workflow.processor.grid;

import java.io.File;

import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;

public class BandCorrectionTest {
    public static void main(String[] args) throws Exception {
	DataObject input = new DataObject();
	input.setFile(new File("/tmp/first.nc"));
	DataObject source = new DataObject();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	source.setDataDescriptor(descriptor );
	source.setFile(new File("/tmp/netcdf-connector-Monthly_T2m.nc"));
	
	DataObject output = GDALNetCDFPostConversionUtils.doBandCorrections(source, input);
	output = GDALNetCDFPostConversionUtils.copyAttributes(source, output);

	System.out.println(output.getFile());
	System.out.println();
    }
}
