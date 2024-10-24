
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
import eu.essi_lab.validator.wof.WML_1_1Validator;
import eu.essi_lab.validator.wof.WML_2_0Validator;

public class WML_1_1ToWML_2_0ProcessTest {

    private WML11_To_WML20_Processor process;
    private DataObject dataObject;

    @Before
    public void init() throws Exception {
	this.process = new WML11_To_WML20_Processor();
	InputStream stream = WML_1_1ToWML_2_0ProcessTest.class.getClassLoader().getResourceAsStream("wml_1_1-his4values.xml");
	this.dataObject = new DataObject();
	dataObject.setFileFromStream(stream, getClass().getSimpleName() + ".xml");
	dataObject.getFile().deleteOnExit();
	stream.close();
    }

    @Test
    public void testConversion() throws Exception {

	WML_1_1Validator wml1Validator = new WML_1_1Validator();

	DataDescriptor wml1Descriptor = wml1Validator.readDataAttributes(dataObject);

	DataObject wml2Object = process.process(dataObject, null);

	WML_2_0Validator wml2Validator = new WML_2_0Validator();

	wml1Descriptor.getOtherDimensions().clear(); // vertical dimension is not supported yet in WML 2.0
	wml1Descriptor.setDataFormat(DataFormat.WATERML_2_0());

	wml2Object.setDataDescriptor(wml1Descriptor);

	ValidationMessage response = wml2Validator.validate(wml2Object);

	ValidationResult result = response.getResult();
	if (result.equals(ValidationResult.VALIDATION_FAILED)) {
	    System.out.println(response.getError());
	    System.out.println(response.getErrorCode());
	}
	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

    }

    @After
    public void after() {
	if (dataObject != null && dataObject.getFile() != null) {
	    dataObject.getFile().delete();
	}
    }

}
