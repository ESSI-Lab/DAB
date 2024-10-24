package eu.essi_lab.accessor.wof.wsdl;

import static org.junit.Assert.assertEquals;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;

public class ValidatorTest {
    protected void expectErrorMessage(ValidationMessage message) {
	ValidationResult result = message.getResult();
	assertEquals(ValidationResult.VALIDATION_FAILED, result);
    }

    protected void expectValidMessage(ValidationMessage message) {
	ValidationResult result = message.getResult();
	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
    }

}
