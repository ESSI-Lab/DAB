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
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML11_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML20_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;
import eu.essi_lab.workflow.processor.timeseries.NetCDF_4326_TimeSeries_To_OM_FormatConverter;

/**
 * Simple hydro use case made by all workblocks
 * 
 * @author boldrini
 */
public class HydroBlocksTest {
    private WorkflowBuilder builder;

    @Before
    public void init() throws Exception {

	this.builder = new WorkflowBuilder();

	this.builder.add(new WML11_4326_To_NetCDF_FormatConverter());
	this.builder.add(new NetCDF_4326_TimeSeries_TemporalSubsetter());
	this.builder.add(new WML20_4326_To_NetCDF_FormatConverter());
	this.builder.add(new NetCDF_4326_TimeSeries_To_OM_FormatConverter());
	this.builder.add(new NetCDF_4326_TimeSeries_To_WML11_FormatConverter());
	this.builder.add(new NetCDF_4326_TimeSeries_To_WML20_FormatConverter());
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

	Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void testWorkFlowSubset2() throws Exception {
	// INPUT
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());

	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);

	inputDescriptor.setTemporalDimension(timeDimension);

	// OUTPUT
	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_2_0());

	// THIS MEANS "DO A TEMPORAL SUBSETTING"
	ContinueDimension targetTimeDimension = new ContinueDimension("time");
	targetTimeDimension.setUom(Unit.SECOND);
	targetTimeDimension.setLower(3);
	targetTimeDimension.setUpper(7);
	targetTimeDimension.setResolution(1.0);

	outputDescriptor.setTemporalDimension(targetTimeDimension);

	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);

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
	DataDescriptor inputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.WATERML_1_1());
	// OUTPUT

	DataDescriptor outputDescriptor = TestUtils.create(DataType.TIME_SERIES, CRS.fromIdentifier("EPSG:4326"), DataFormat.NETCDF());

	List<Workflow> result = builder.build(inputDescriptor, outputDescriptor);
	Assert.assertTrue(result.size() > 0);
    }
}
