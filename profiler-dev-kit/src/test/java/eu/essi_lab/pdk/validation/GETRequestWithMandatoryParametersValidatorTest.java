package eu.essi_lab.pdk.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public class GETRequestWithMandatoryParametersValidatorTest extends ValidatorTest {

    private WebRequest request;
    private GETRequestValidator validator;

    @Before
    public void init() {

	this.validator = new GETRequestValidator("A", "b", "cde");

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

	Mockito.when(request.getQueryString()).thenReturn("A&B");

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateFalse3() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("A&b");

	expectErrorMessage(validator.validate(request));

    }

    @Test
    public void validateTrue1() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("A&b&cde");

	expectValidMessage(validator.validate(request));

    }

    @Test
    public void validateTrue2() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("A=3&b=2&cde");

	expectValidMessage(validator.validate(request));

    }

    @Test
    public void validateTrue3() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("A=3&t&b=2&cde&g=4");

	expectValidMessage(validator.validate(request));

    }

    @Test
    public void validateTrue4() throws GSException {

	Mockito.when(request.isGetRequest()).thenReturn(true);

	Mockito.when(request.getQueryString()).thenReturn("A=3&unll&t&b=2&cde&g=4");

	expectValidMessage(validator.validate(request));

    }

}
