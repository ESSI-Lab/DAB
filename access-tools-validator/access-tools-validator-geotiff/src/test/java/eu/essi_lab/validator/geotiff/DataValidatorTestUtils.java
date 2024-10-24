package eu.essi_lab.validator.geotiff;

import java.io.InputStream;

import org.junit.Assert;

import eu.essi_lab.access.DataValidator;
import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;

public class DataValidatorTestUtils {

    protected DataValidator validator;

    public void test(String resource, DataDescriptor descriptor, Boolean expectedToPass, DataValidatorErrorCode expectedErrorCode)
	    throws Exception {

	DataObject dataObject = new DataObject();
	try {
	    InputStream stream = DataValidatorTestUtils.class.getClassLoader().getResourceAsStream(resource);
	    dataObject.setFileFromStream(stream, "DataValidatorTestUtils");
	    dataObject.setDataDescriptor(descriptor);
	    ValidationMessage result = validator.validate(dataObject);
	    if (dataObject.getFile() != null) {
		dataObject.getFile().delete();
	    }
	    System.out.println(result.getResult());
	    if (result.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
		System.out.println(result.getError());
		System.out.println(result.getErrorCode());
	    }
	    if (expectedToPass) {
		Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result.getResult());
	    } else {
		Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result.getResult());
		Assert.assertEquals(expectedErrorCode.toString(), result.getErrorCode());
	    }

	} catch (Exception e) {
	    if (dataObject.getFile() != null) {
		dataObject.getFile().delete();
	    }
	    throw e;
	}
    }

}
