package eu.essi_lab.pdk.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public class GETRequestValidatorTest extends ValidatorTest {

    private WebRequest request;
    private GETRequestValidator validator;

    @Before
    public void init() {

	this.validator = new GETRequestValidator();

	this.request = Mockito.mock(WebRequest.class);
    }

    @Test
    public void validateFalse1() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(false);

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateTrue() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	expectValidMessage(validator.validate(request));

    }

}
