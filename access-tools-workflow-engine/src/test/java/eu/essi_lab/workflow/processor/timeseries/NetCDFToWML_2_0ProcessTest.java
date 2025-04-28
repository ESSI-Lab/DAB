
package eu.essi_lab.workflow.processor.timeseries;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.validator.wof.WML_2_0Validator;

public class NetCDFToWML_2_0ProcessTest {

    private NetCDF_To_WML20_Processor process;
    private DataObject dataObject;

    @Before
    public void init() throws Exception {
	this.process = new NetCDF_To_WML20_Processor();
	this.dataObject = new DataObject();

    }

    @Test
    public void testNetCDFToWML() throws Exception {

	InputStream stream = NetCDFToWML_2_0ProcessTest.class.getClassLoader().getResourceAsStream("netcdf-his4values.nc");
	dataObject.setFileFromStream(stream, getClass().getSimpleName() + ".nc");
	dataObject.getFile().deleteOnExit();
	stream.close();

	commonTest();

    }

    @Test
    public void testEmptyNetCDFToWML() throws Exception {

	InputStream stream = NetCDFToWML_2_0ProcessTest.class.getClassLoader().getResourceAsStream("netcdf-empty.nc");
	dataObject.setFileFromStream(stream, getClass().getSimpleName() + ".nc");
	dataObject.getFile().deleteOnExit();
	stream.close();

	commonTest();

    }

    private void commonTest() throws Exception {
	DataObject wmlObject = process.process(null, dataObject, null);

	WML_2_0Validator validator = new WML_2_0Validator();

	DataDescriptor dataDescriptor = new DataDescriptor();
	dataDescriptor.setDataFormat(DataFormat.WATERML_2_0());
	dataDescriptor.setDataType(DataType.TIME_SERIES);
	wmlObject.setDataDescriptor(dataDescriptor);

	ValidationMessage response = validator.validate(wmlObject);

	ValidationResult result = response.getResult();
	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

    }

    @After
    public void after() {
	if (dataObject != null && dataObject.getFile() != null) {
	    dataObject.getFile().delete();
	}
    }

}
