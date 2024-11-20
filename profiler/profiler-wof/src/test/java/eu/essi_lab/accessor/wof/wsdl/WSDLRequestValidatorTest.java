package eu.essi_lab.accessor.wof.wsdl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.wof.wsdl.WSDLRequestValidator;

/**
 * @author boldrini
 */
public class WSDLRequestValidatorTest extends ValidatorTest {

    private WebRequest request;
    private WSDLRequestValidator validator;

    @Before
    public void init() {

	this.validator = new WSDLRequestValidator();

	this.request = Mockito.mock(WebRequest.class);
    }

    @Test
    public void validateFalse1() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(false);

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateFalse2() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("getWSDL");

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateFalse3() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("WSDLget");

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateTrue1() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("WSDL");

	expectValidMessage(validator.validate(request));

    }
    
    @Test
    public void validateTrue2() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("a&wsdl");

	expectValidMessage(validator.validate(request));

    }

}
