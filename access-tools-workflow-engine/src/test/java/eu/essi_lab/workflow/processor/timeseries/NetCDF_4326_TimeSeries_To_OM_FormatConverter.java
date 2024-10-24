package eu.essi_lab.workflow.processor.timeseries;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.DataFormatConverter;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.IdentityProcessor;

/**
 * @author Fabrizio
 */
public class NetCDF_4326_TimeSeries_To_OM_FormatConverter extends DataFormatConverter {

    public NetCDF_4326_TimeSeries_To_OM_FormatConverter() {
	super(DataType.TIME_SERIES, //
		CRS.EPSG_4326(), //
		DataFormat.NETCDF_3(), // INPUT FORMAT
		DataFormat.O_M() // OUTPUT FORMAT
	);
    }

    @Override
    protected DataProcessor createProcessor() {

	return new IdentityProcessor(getClass().getSimpleName());
    }
}
