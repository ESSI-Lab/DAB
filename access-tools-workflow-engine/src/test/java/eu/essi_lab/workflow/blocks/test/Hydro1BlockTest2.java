package eu.essi_lab.workflow.blocks.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.worflow.test.TestUtils;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * Simple hydro use case made by a single workblock
 * 
 * @author boldrini
 */
public class Hydro1BlockTest2 {
    private WorkflowBuilder builder;

    @Before
    public void init() throws Exception {
	this.builder = new WorkflowBuilder();
	builder.add(new NetCDF_4326_TimeSeries_TemporalSubsetter());
    }

    /**
     * The simple identity workflow is found.
     * 
     * @throws Exception
     */
    @Test
    public void testIdentity() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());
	// OUTPUT

	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());
	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(!result.isEmpty());
    }

    /**
     * 1 workflow is found, because 1 subset block is available
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowSubset() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF_3());

	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);

	inputDescriptor.setTemporalDimension(timeDimension);

	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF());

	// THIS MEANS "DO A TEMPORAL SUBSETTING"
	ContinueDimension targetTimeDimension = new ContinueDimension("time");
	targetTimeDimension.setUom(Unit.SECOND);
	targetTimeDimension.setLower(3);
	targetTimeDimension.setUpper(7);
	targetTimeDimension.setResolution(1.0);

	outputDescriptor.setTemporalDimension(targetTimeDimension);
	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);

	Assert.assertTrue(!result.isEmpty());
    }

    /**
     * No workflows are found because formatter not available
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowBasic() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());
	// OUTPUT

	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF());

	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);
	Assert.assertTrue(result.isEmpty());
    }

}
