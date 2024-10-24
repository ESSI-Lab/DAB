package eu.essi_lab.worflow.execution;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.validator.netcdf.classic.NetCDF3GridValidator;
import eu.essi_lab.workflow.builder.WorkblockBuilder;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class GridExecutionTest {

    @Test
    public void test() throws Exception {

	WorkflowBuilder builder = new WorkflowBuilder();
	WorkflowBuilder.enableLogs(false);

	ServiceLoader<WorkblockBuilder> loader = ServiceLoader.load(WorkblockBuilder.class);

	loader.forEach(b -> {
	    try {

		builder.add(b);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	});

	// ---------------------------------------------
	//
	// creates a data descriptor for the data object
	// it is also used to create the input descriptor
	//
	DataDescriptor initDescriptor = new DataDescriptor();
	initDescriptor.setDataType(DataType.GRID);
	initDescriptor.setCRS(CRS.EPSG_4326());
	initDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	Double n = -3.6006610000000014;
	Double e = -36.746756042003014;
	Double s = -36.083490394158076;
	Double w = -55.3980789;
	initDescriptor.setEPSG4326SpatialDimensions(n, e, s, w);

	DataObject dataObject = new DataObject();
	dataObject.setDataDescriptor(initDescriptor);
	InputStream stream = GridExecutionTest.class.getClassLoader().getResourceAsStream("data/eox-wcs100-coverage.tif");
	dataObject.setFileFromStream(stream, "GridExecutionTest.tif");
	dataObject.getFile().deleteOnExit();

	// -------------------------------
	//
	// creates the target descriptor
	//

	// this is the descriptor after changing to netcdf 3857.. however we want to reduce extent and resolution
	// Data type: GRID, CRS: , Format: NETCDF_3, Spa.dim: [x coordinate of projection (row), l(-6166789.245395642),
	// u(-4090117.650454148), r(3301.5446660434827), s(630), y coordinate of projection (column),
	// l(-4313511.388692931), u(-401180.95943113463), r(3301.5446660434827), s(1186)], Tem.dim: null, Oth.dim:
	// [gdalband (null), l(0.0), u(2.0), r(1.0), s(3)]

	DataDescriptor targetDescriptor = new DataDescriptor();
	targetDescriptor.setDataType(DataType.GRID);
	targetDescriptor.setCRS(CRS.EPSG_3857());
	targetDescriptor.setDataFormat(DataFormat.NETCDF_3());

	ContinueDimension row = new ContinueDimension("x");
	row.setLower(new Double(-6000000.0));
	row.setUpper(new Double(-5000000.0));
	row.setSize(101l);

	row.setUom(Unit.METRE);
	row.setType(DimensionType.ROW);
	row.setResolution(10000);

	ContinueDimension column = new ContinueDimension("y");
	column.setLower(new Double(-4000000.0));
	column.setUpper(new Double(-2000000.0));
	column.setUom(Unit.METRE);
	column.setType(DimensionType.COLUMN);
	column.setResolution(10000);
	column.setSize(201l);

	targetDescriptor.setSpatialDimensions(Arrays.asList(row, column));

	// -------------------------------------
	//
	// builds the preferred workflow
	//
	Workflow workflow = builder.buildPreferred(initDescriptor, targetDescriptor).get();

	GSLoggerFactory.getLogger(getClass()).info(workflow.toString());

	// executes the workflow
	DataObject result = workflow.execute(dataObject, targetDescriptor);

	// formal validation
	DataDescriptor resultDescriptor = result.getDataDescriptor();
	Assert.assertEquals(resultDescriptor, targetDescriptor);

	// data validation
	NetCDF3GridValidator validator = new NetCDF3GridValidator();
	ValidationMessage validationResult = validator.validate(result);
	if (!validationResult.getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)) {
	    System.out.println(validationResult.getError());
	    System.out.println(validationResult.getErrorCode());
	    fail();
	}
	dataObject.getFile().delete();

    }

}
