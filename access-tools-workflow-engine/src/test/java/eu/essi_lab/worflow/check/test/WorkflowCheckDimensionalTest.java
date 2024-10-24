package eu.essi_lab.worflow.check.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.test.NetCDF3_GDAL_SpatialResampler;
import eu.essi_lab.workflow.blocks.test.NetCDF3_GDAL_SpatialSubsetter;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;

/**
 * @author Fabrizio
 */
public class WorkflowCheckDimensionalTest {

    /**
     * This check succeeds.
     * From:
     * GEOTIFF
     * 4326
     * To:
     * NETCDF3
     * 4326
     * SPATIAL RESAMPLED
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test1() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a resampled NETCDF
	Workblock blocks2 = new NetCDF3_GDAL_SpatialResampler().build();
	workflow.getWorkblocks().add(blocks2);

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

	targetDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d3 = targetDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = targetDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertTrue(check);
    }

    /**
     * This check succeeds.
     * From:
     * GEOTIFF
     * 4326
     * To:
     * NETCDF3
     * 4326
     * SPATIAL SUBSETTED
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test2() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a subsetted NETCDF
	Workblock blocks2 = new NetCDF3_GDAL_SpatialSubsetter().build();
	workflow.getWorkblocks().add(blocks2);

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
	d3.setResolution(1.);

	ContinueDimension d4 = targetDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(1.);

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertTrue(check);
    }

    /**
     * This check fails because the last process cannot make resampling, only subsetting
     * From:
     * GEOTIFF
     * 4326
     * To:
     * NETCDF3
     * 4326
     * SPATIAL SUBSETTED
     * SPATIAL RESAMPLED
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void test3() throws InstantiationException, IllegalAccessException {

	Workflow workflow = new Workflow();

	// transforms GEOTIFF in 4326 to NETCDF3 in 4326
	Workblock blocks1 = new GeoTIFF_To_NetCDF_FormatConverter().build();
	workflow.getWorkblocks().add(blocks1);

	// transforms a NETCDF3 in 4326 to a subsetted NETCDF
	Workblock blocks2 = new NetCDF3_GDAL_SpatialSubsetter().build();
	workflow.getWorkblocks().add(blocks2);

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

	targetDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	ContinueDimension d3 = targetDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = targetDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(1.);

	boolean check = workflow.check(initDescriptor, targetDescriptor);
	Assert.assertFalse(check);
    }

}
