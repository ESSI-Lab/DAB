package eu.essi_lab.workflow.blocks.test;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML11_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.NetCDF_4326_TimeSeries_To_WML20_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_TimeSeries_TemporalSubsetter;
import eu.essi_lab.workflow.blocks.timeseries.WML11_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_NetCDF_FormatConverter;
import eu.essi_lab.workflow.blocks.timeseries.WML20_4326_To_OM2_FormatConverter;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * Hydro use case made by 3 workblocks
 * 
 * @author boldrini
 */
public class Hydro3BlocksTest {
    private WorkflowBuilder builder;

    @Before
    public void init() throws Exception {

	this.builder = new WorkflowBuilder();

    }

    private long d1 = 599612400000l;

    public DataDescriptor getInputDescriptor() {

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(78.93, 11.84);
	descriptor.setTemporalDimension(new Date(d1), new Date(d1));
	return descriptor;
    }

    public DataDescriptor getOutputDescriptor() {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.O_M());
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(78.93, 11.84);
	descriptor.setTemporalDimension(new Date(d1), new Date(d1));
	return descriptor;
    }

    /**
     * 1) One workflow is found, giving the three correct blocks
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowCorrectBlocks() throws Exception {

	builder.add(new WML11_4326_To_NetCDF_FormatConverter());
	builder.add(new NetCDF_4326_TimeSeries_To_WML20_FormatConverter());
	builder.add(new WML20_4326_To_OM2_FormatConverter());

	List<Workflow> result = builder.build(getInputDescriptor(), getOutputDescriptor());
	Assert.assertTrue(result.size() == 1);
    }

    /**
     * 2) Some not useful blocks are added, and the workflow is found again.
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowWithAdditionalBlocks1() throws Exception {

	// correct blocks
	builder.add(new NetCDF_4326_TimeSeries_To_WML20_FormatConverter());
	builder.add(new WML11_4326_To_NetCDF_FormatConverter());
	builder.add(new WML20_4326_To_OM2_FormatConverter());
	// additional blocks
	builder.add(new NetCDF_4326_TimeSeries_TemporalSubsetter());
	builder.add(new NetCDF_4326_TimeSeries_To_WML11_FormatConverter());
	builder.add(new WML11_4326_TimeSeries_TemporalSubsetter());
	builder.add(new WML20_4326_To_NetCDF_FormatConverter());

	List<Workflow> result = builder.build(getInputDescriptor(), getOutputDescriptor());
	Assert.assertTrue(result.size() == 1);

    }

    /**
     * 3) The order of blocks is changed, and the test continues to pass
     * 
     * @throws Exception
     */
    @Test
    public void testWorkFlowWithAdditionalBlocks2() throws Exception {

	//
	// correct blocks and additional blocks scrambled
	//
	builder.add(new NetCDF_4326_TimeSeries_TemporalSubsetter());
	builder.add(new NetCDF_4326_TimeSeries_To_WML11_FormatConverter());
	builder.add(new NetCDF_4326_TimeSeries_To_WML20_FormatConverter());
	builder.add(new WML11_4326_TimeSeries_TemporalSubsetter());
	builder.add(new WML11_4326_To_NetCDF_FormatConverter());
	builder.add(new WML20_4326_To_NetCDF_FormatConverter());
	builder.add(new WML20_4326_To_OM2_FormatConverter());

	List<Workflow> result = builder.build(getInputDescriptor(), getOutputDescriptor());
	Assert.assertTrue(result.size() == 1);

    }

    /**
     * @throws Exception
     */
    @Test
    public void testWorkFlowWithAllBlocks() throws Exception {

	builder = WorkflowBuilder.createLoadedBuilder();

	List<Workflow> result = builder.build(getInputDescriptor(), getOutputDescriptor());
	Assert.assertTrue(result.size() == 1);
    }
}
