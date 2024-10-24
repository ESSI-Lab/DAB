package eu.essi_lab.workflow.blocks.test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.SpatialResampler;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.IdentityProcessor;

/**
 * @author Fabrizio
 */
public class NetCDF3_GDAL_SpatialResampler extends SpatialResampler {

    public NetCDF3_GDAL_SpatialResampler() {
	super(DataType.GRID, //
		DataFormat.NETCDF_3(), //
		CRS.GDAL_ALL() //
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new IdentityProcessor(getClass().getSimpleName());
    }

}
