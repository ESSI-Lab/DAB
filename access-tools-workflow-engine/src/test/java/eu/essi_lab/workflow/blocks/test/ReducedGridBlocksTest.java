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
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * Simple hydro use case made by all workblocks
 * 
 * @author boldrini
 */
public class ReducedGridBlocksTest {

    private WorkflowBuilder builder;

    @Before
    public void init() throws Exception {

	this.builder = new WorkflowBuilder();

	this.builder.add(new NetCDF3_4326_To_3857_CRSConverter());
	this.builder.add(new NetCDF3_3857_SpatialSubsetter());
	this.builder.add(new NetCDF3_4326_SpatialSubsetter());
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

	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);

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
	d3.setResolution(1.);

	ContinueDimension d4 = outputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(1.);

	Workflow result = builder.buildPreferred(inputDescriptor, outputDescriptor).get();

	Assert.assertEquals(1, result.getWorkblocks().size());

	Assert.assertEquals(NetCDF3_4326_SpatialSubsetter.class, result.getWorkblocks().get(0).getBuilder().getClass());
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
	d3.setResolution(1.);

	ContinueDimension d4 = outputDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d4.setResolution(1.);

	Workflow result = builder.buildPreferred(inputDescriptor, outputDescriptor).get();

	Assert.assertEquals(2, result.getWorkblocks().size());

	Assert.assertEquals(NetCDF3_4326_To_3857_CRSConverter.class, result.getWorkblocks().get(0).getBuilder().getClass());
	Assert.assertEquals(NetCDF3_3857_SpatialSubsetter.class, result.getWorkblocks().get(1).getBuilder().getClass());
    }

    /**
     * This test fails because there is no resampler available
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowCRSSubsetResampling() throws Exception {
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

	List<Workflow> workflows = builder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(workflows.isEmpty());
    }
}
