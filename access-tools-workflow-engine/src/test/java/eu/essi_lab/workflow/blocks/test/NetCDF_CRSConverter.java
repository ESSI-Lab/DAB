package eu.essi_lab.workflow.blocks.test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.CRSConverter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.grid.GDAL_NetCDF_CRS_Converter_Processor;

public abstract class NetCDF_CRSConverter extends CRSConverter {

    public NetCDF_CRSConverter() {
	super(DataType.GRID, //
		DataFormat.NETCDF_3(), //
		CRS.GDAL_ALL(), // INPUT CRS
		CRS.GDAL_ALL() // OUTPUT CRS
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new GDAL_NetCDF_CRS_Converter_Processor();
    }

}
