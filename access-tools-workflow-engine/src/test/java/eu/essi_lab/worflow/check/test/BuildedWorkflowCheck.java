package eu.essi_lab.worflow.check.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.test.NetCDF3_3857_SpatialSubsetter;
import eu.essi_lab.workflow.blocks.test.NetCDF3_GDAL_SpatialSubsetter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;

/**
 * @author Fabrizio
 */
public class BuildedWorkflowCheck {

    /**
     * Given a builder with the following blocks:
     * <ul>
     * <li>GeoTIFF_GDAL_To_NetCDF3_FormatConverter</li>
     * <li>NetCDF3_3857_SpatialSubsetter</li>
     * </ul>
     * The builder builds a workflow since {@link GeoTIFF_To_NetCDF_FormatConverter} accepts any GDAL CRS (for
     * example
     * 4326 and 3857), but with the {@link PresenceType#SAME_AS} (since it does not make CRS transformation but only
     * format conversion) and the process of {@link NetCDF3_3857_SpatialSubsetter} can makes subsetting but only with a
     * 3857 CRS.
     * So the workflow is a potential workflow: it works fine only if the data has a 3857 CRS, otherwise the
     * {@link Workflow#check(DataDescriptor, DataDescriptor)} method fails and the workflow is not builded.<br>
     * <br>
     * This test fails succeeds the data has a 3857 CRS
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test1() throws InstantiationException, IllegalAccessException {

	WorkflowBuilder builder = new WorkflowBuilder();

	builder.add(new GeoTIFF_To_NetCDF_FormatConverter().build());
	builder.add(new NetCDF3_3857_SpatialSubsetter().build());

	// -----------------------------------------------

	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_3857());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	initDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = initDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = initDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	DataDescriptor targetDescriptor = new DataDescriptor();

	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	// THIS MEANS "DO A SPATIAL SUBSETTING"
	targetDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	// -----------------------------------------------

	List<Workflow> build = builder.build(null, initDescriptor, targetDescriptor);

	Assert.assertEquals(1, build.size());
    }

    /**
     * Given a builder with the following blocks:
     * <ul>
     * <li>GeoTIFF_GDAL_To_NetCDF3_FormatConverter</li>
     * <li>NetCDF3_3857_SpatialSubsetter</li>
     * </ul>
     * The builder builds a workflow since {@link GeoTIFF_To_NetCDF_FormatConverter} accepts any GDAL CRS (for
     * example
     * 4326 and 3857), but with the {@link PresenceType#SAME_AS} (since it does not make CRS transformation but only
     * format conversion) and the process of {@link NetCDF3_3857_SpatialSubsetter} can makes subsetting but only with a
     * 3857 CRS.
     * So the workflow is a potential workflow: it works fine only if the data has a 3857 CRS, otherwise the
     * {@link Workflow#check(DataDescriptor, DataDescriptor)} method fails and the workflow is not builded.<br>
     * <br>
     * This test fails because the data has a 4326 CRS
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test2() throws InstantiationException, IllegalAccessException {

	WorkflowBuilder builder = new WorkflowBuilder();

	builder.add(new GeoTIFF_To_NetCDF_FormatConverter().build());
	builder.add(new NetCDF3_3857_SpatialSubsetter().build());

	// -----------------------------------------------

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

	// -----------------------------------------------

	List<Workflow> build = builder.build(null, initDescriptor, targetDescriptor);

	Assert.assertEquals(0, build.size());
    }

    /**
     * Given a builder with the following blocks:
     * <ul>
     * <li>GeoTIFF_GDAL_To_NetCDF3_FormatConverter</li>
     * <li>NetCDF3_GDAL_SpatialSubsetter</li>
     * </ul>
     * This test succeeds because NetCDF3_GDAL_SpatialSubsetter accespts any GDAL CRS
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test3() throws InstantiationException, IllegalAccessException {

	WorkflowBuilder builder = new WorkflowBuilder();

	builder.add(new GeoTIFF_To_NetCDF_FormatConverter().build());
	builder.add(new NetCDF3_GDAL_SpatialSubsetter().build());

	// -----------------------------------------------

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

	// -----------------------------------------------

	List<Workflow> build = builder.build(null, initDescriptor, targetDescriptor);

	Assert.assertEquals(1, build.size());
    }
}
