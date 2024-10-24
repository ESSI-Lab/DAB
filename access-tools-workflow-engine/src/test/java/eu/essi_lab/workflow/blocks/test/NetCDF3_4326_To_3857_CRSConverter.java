package eu.essi_lab.workflow.blocks.test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.CRSConverter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.grid.GDAL_NetCDF_CRS_Converter_Processor;

/**
 * @author Fabrizio
 */
public class NetCDF3_4326_To_3857_CRSConverter extends CRSConverter {

    public NetCDF3_4326_To_3857_CRSConverter() {

	super(DataType.GRID, //
		DataFormat.NETCDF_3(), //
		CRS.EPSG_4326(), // INPUT CRS
		CRS.EPSG_3857() // OUTPUT CRS
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new GDAL_NetCDF_CRS_Converter_Processor();
    }
}
