package eu.essi_lab.model;

import java.util.List;

import javax.validation.ConstraintViolation;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class DatasetValidationTest {

    @Test
    public void failedValidationTest() {

	Dataset dataset = new Dataset();

	// --------------------------
	//
	// GSSource
	//
	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	source.setEndpoint("http://endpoint");
	source.setLabel("label");
	source.setUniqueIdentifier("SOURCE_UNIQUE_ID");
	source.setVersion("SOURCE_VERSION");
	dataset.setSource(source);

	// --------------------------
	//
	// OriginalMetadata
	//
	OriginalMetadata originalMetadata = dataset.getOriginalMetadata();

	originalMetadata.setSchemeURI("http://scheme-uri");
	dataset.setOriginalId("ORIGINAL_ID");
	originalMetadata.setMetadata("ORIGINAL_METADATA");

	Assert.assertEquals(4, validationTest(dataset));
    }

    @Test
    public void successValidationTest() {

	Dataset dataset = new Dataset();

	// --------------------------
	//
	// GSSource
	//
	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	source.setEndpoint("http://endpoint");
	source.setLabel("label");
	source.setUniqueIdentifier("SOURCE_UNIQUE_ID");
	source.setVersion("SOURCE_VERSION");
	dataset.setSource(source);

	dataset.setOriginalId("ORIGINAL_ID");
	dataset.setPublicId("PUBLIC_ID");
	dataset.setPrivateId("PRIVATE_ID");

	// --------------------------
	//
	// OriginalMetadata
	//
	OriginalMetadata originalMetadata = dataset.getOriginalMetadata();

	originalMetadata.setSchemeURI("http://scheme-uri");
	dataset.setOriginalId("ORIGINAL_ID");
	originalMetadata.setMetadata("ORIGINAL_METADATA");

	Assert.assertEquals(0, validationTest(dataset));
    }

    private int validationTest(GSResource dataset) {

	List<ConstraintViolation<GSResource>> validate = dataset.validate();

	for (ConstraintViolation<GSResource> constraintViolation : validate) {

	    String message = constraintViolation.getMessage();
	    System.out.println(message);
	}

	return validate.size();
    }
}
