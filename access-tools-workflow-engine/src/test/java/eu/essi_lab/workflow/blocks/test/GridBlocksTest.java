package eu.essi_lab.workflow.blocks.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.worflow.test.TestUtils;
import eu.essi_lab.workflow.blocks.grid.GeoTIFF_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.NetCDF_To_GeoTIFF_FormatConverter;
import eu.essi_lab.workflow.blocks.grid.NetCDF_To_PNG_FormatConverter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * Simple hydro use case made by all workblocks
 * 
 * @author boldrini
 */
public class GridBlocksTest {

    private WorkflowBuilder workflowBuilder;

    @Before
    public void init() throws Exception {

	this.workflowBuilder = new WorkflowBuilder();

	workflowBuilder.add(new GeoTIFF_To_NetCDF_FormatConverter());
	workflowBuilder.add(new NetCDF3_GDAL_SpatialResampler());
	workflowBuilder.add(new NetCDF3_GDAL_SpatialSubsetter());
	workflowBuilder.add(new NetCDF_To_GeoTIFF_FormatConverter());
	workflowBuilder.add(new NetCDF_To_PNG_FormatConverter());
	workflowBuilder.add(new NetCDF3_4326_To_3857_CRSConverter());
    }

    /**
     * The simple identity workflow is found.
     * 
     * @throws Exception
     */
    @Test
    public void testIdentity() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.IMAGE_GEOTIFF());
	// OUTPUT
	DataDescriptor outputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.IMAGE_GEOTIFF());

	List<Workflow> result = workflowBuilder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(!result.isEmpty());
    }

    /**
     * 1 workflows is found, because 1 subset block is available
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowSubset() throws Exception {

	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF_3());
	inputDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = inputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = inputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	DataDescriptor outputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF_3());

	// THIS MEANS "DO A SPATIAL SUBSETTING"
	outputDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	ContinueDimension d3 = outputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = outputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	List<Workflow> result = workflowBuilder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void testWorkFlowSubset2() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.IMAGE_GEOTIFF());

	inputDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = inputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = inputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	DataDescriptor outputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.IMAGE_PNG());

	// THIS MEANS "DO A SPATIAL SUBSETTING"
	outputDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	ContinueDimension d3 = outputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = outputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	List<Workflow> result = workflowBuilder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(result.size() > 0);
    }

    /**
     * One workflow is found, the one that is needed. This should work as well, even with NO_SUBSETTING and
     * NO_RESAMPLING
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowFormat() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.IMAGE_GEOTIFF());

	DataDescriptor outputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF_3());

	List<Workflow> result = workflowBuilder.build(inputDescriptor, outputDescriptor);
	Assert.assertTrue(result.size() > 0);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testWorkFlowCRSSubset() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.GRID, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF_3());

	inputDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = inputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = inputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	DataDescriptor outputDescriptor = TestUtils.create(DataType.GRID, CRS.EPSG_3857(), DataFormat.NETCDF_3());

	// THIS MEANS "DO A SPATIAL SUBSETTING"
	outputDescriptor.setEPSG4326SpatialDimensions(5.0, 5.0, -5.0, -5.0);

	ContinueDimension d3 = outputDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d3.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	ContinueDimension d4 = outputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(2.); // THIS MEANS "DO A SPATIAL RESAMPLING"

	List<Workflow> result = workflowBuilder.build(inputDescriptor, outputDescriptor);
	Assert.assertTrue(result.size() > 0);
    }
}
