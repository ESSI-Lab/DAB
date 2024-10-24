package eu.essi_lab.worflow.check.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.test.NetCDF3_GDAL_SpatialResampler;
import eu.essi_lab.workflow.blocks.test.NetCDF3_GDAL_SpatialSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;

/**
 * @author Fabrizio
 */
public class WorkflowCheckComplianceLevelTest {

    /**
     * Data compliance {@link OutputDescriptor} properties:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: spatial, required. Temporal and other not strictly required</li>
     * <li>Resampling: spatial, required. Temporal and other not strictly required</li>
     * </ul>
     */
    @Test
    public void GRID_BASIC_DATA_COMPLIANCE_Test() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a resampled NETCDF
	Workblock blocks2 = new NetCDF3_GDAL_SpatialResampler().build();
	workflow.getWorkblocks().add(blocks2);

	// transforms a NETCDF3 in 4326 to a subsetted NETCDF
	Workblock blocks3 = new NetCDF3_GDAL_SpatialSubsetter().build();
	workflow.getWorkblocks().add(blocks3);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	initDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = initDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = initDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_4326());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	// THIS MEANS "DO A SPATIAL SUBSETTING"
	targetDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	ContinueDimension d3 = targetDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = targetDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertTrue(check);
    }

    /**
     * Data compliance {@link OutputDescriptor} properties:
     * <ul>
     * <li>CRS: EPSG:4326</li>
     * <li>Format: NetCDF 3</li>
     * <li>Subsetting: temporal required. Spatial and other not strictly required</li>
     * <li>Resampling: not strictly required</li>
     * </ul>
     */
    @Test
    public void TIMESERIES_BASIC_DATA_COMPLIANCE_Test() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms WATERLML20 in 4326 to NETCDF in 4326
	Workblock blocks1 = new WML20_4326_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF in 4326 to a temporal subsetted NETCDF
	Workblock blocks3 = new NetCDF_4326_TimeSeries_TemporalSubsetter().build();
	workflow.getWorkblocks().add(blocks3);

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.TIME_SERIES);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.WATERML_2_0());

	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);

	initDescriptor.setTemporalDimension(timeDimension);

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.TIME_SERIES);
	targetDescriptor.setCRS(CRS.EPSG_4326());
	targetDescriptor.setDataFormat(DataFormat.NETCDF());

	// THIS MEANS "DO A TEMPORAL SUBSETTING"
	ContinueDimension targetTimeDimension = new ContinueDimension("time");
	targetTimeDimension.setUom(Unit.SECOND);
	targetTimeDimension.setLower(3);
	targetTimeDimension.setUpper(7);
	targetTimeDimension.setResolution(1.0);

	targetDescriptor.setTemporalDimension(targetTimeDimension);

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertTrue(check);
    }
}
