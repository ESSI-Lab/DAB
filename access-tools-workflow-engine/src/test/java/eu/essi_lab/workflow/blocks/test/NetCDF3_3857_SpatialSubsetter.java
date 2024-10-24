package eu.essi_lab.workflow.blocks.test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.SpatialSubsetter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.IdentityProcessor;

/**
 * @author Fabrizio
 */
public class NetCDF3_3857_SpatialSubsetter extends SpatialSubsetter {

    public NetCDF3_3857_SpatialSubsetter() {
	super(DataType.GRID, //
		DataFormat.NETCDF_3(), //
		CRS.EPSG_3857() //
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new IdentityProcessor();
    }

}
