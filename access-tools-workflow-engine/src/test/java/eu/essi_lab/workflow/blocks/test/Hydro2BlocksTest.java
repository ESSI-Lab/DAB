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
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * Simple hydro use case made by 2 workblocks
 * 
 * @author boldrini
 */
public class Hydro2BlocksTest {
    private WorkflowBuilder builder;

    @Before
    public void init() throws Exception {

	this.builder = new WorkflowBuilder();

	builder.add(new WML11_4326_To_NetCDF_FormatConverter());
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
     * 1 workflows is found, because 1 subset block is available
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowSubset() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());

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

	Assert.assertTrue(result.size() == 1);
    }

    /**
     * One workflow is found, the one that is needed. This should work as well, even with NO_SUBSETTING and
     * NO_RESAMPLING
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
	Assert.assertTrue(result.size() == 1);
    }

    /**
     * One workflow is found, the one that is needed.
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowAny() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());
	// OUTPUT
	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF());

	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);
	Assert.assertTrue(result.size() == 1);
    }

}
