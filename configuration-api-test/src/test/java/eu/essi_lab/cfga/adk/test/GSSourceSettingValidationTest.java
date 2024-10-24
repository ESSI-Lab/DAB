package eu.essi_lab.cfga.adk.test;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;

/**
 * @author Fabrizio
 */
public class GSSourceSettingValidationTest {

    private DefaultConfiguration configuration;

    @Test
    public void putEndEditValidationTest() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();

	GSSourceSetting setting = new GSSourceSetting();
	//
	// label and id already exists, put fails and edit works
	//
	setting.setSourceLabel("USGS Earthquake Events");  
	setting.setSourceIdentifier("defaultFDSNSource");  
	setting.setSourceEndpoint("endpoint");

	{

	    Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	    Assert.assertTrue(optional.isPresent());

	    ValidationResponse validationResponse = optional.get();

	    ValidationResult result = validationResponse.getResult();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    List<String> errors = validationResponse.getErrors();

	    Assert.assertEquals(2, errors.size());

	    Assert.assertEquals("A source with the given identifier already exists", errors.get(0));
	    Assert.assertEquals("A source with the given label already exists", errors.get(1));

	    List<String> warnings = validationResponse.getWarnings();

	    Assert.assertEquals(0, warnings.size());

	}

	{

	    Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.edit());

	    Assert.assertTrue(optional.isPresent());

	    ValidationResponse validationResponse = optional.get();

	    ValidationResult result = validationResponse.getResult();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	    List<String> errors = validationResponse.getErrors();

	    Assert.assertEquals(0, errors.size());

	    List<String> warnings = validationResponse.getWarnings();

	    Assert.assertEquals(0, warnings.size());
	}
	
	//
	// label and id do not exist, edit fails for an internal error
	//
//	setting.setSourceLabel("label");  
//	setting.setSourceIdentifier("identifier");  
//	setting.setEndpoint("endpoint");
//	
//	{
//	    Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.edit());
//
//	    Assert.assertTrue(optional.isPresent());
//
//	    ValidationResponse validationResponse = optional.get();
//
//	    ValidationResult result = validationResponse.getResult();
//
//	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
//
//	    List<String> errors = validationResponse.getErrors();
//
//	    Assert.assertEquals(2, errors.size());
//
//	    Assert.assertEquals("An error is occurred. A source with the given identifier should be already in the configuration", errors.get(0));
//	    Assert.assertEquals("An error is occurred. A source with the label should be already in the configuration", errors.get(1));
//
//	    List<String> warnings = validationResponse.getWarnings();
//
//	    Assert.assertEquals(0, warnings.size());
//	}
    }

    @Test
    public void validationTest() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();
	
	GSSourceSetting setting = new GSSourceSetting();

	initTest(setting);
	initTest(new GSSourceSetting(setting.getObject()));
	initTest(new GSSourceSetting(setting.getObject().toString()));

	setting.setSourceIdentifier("id");

	withIdTest(setting);
	withIdTest(new GSSourceSetting(setting.getObject()));
	withIdTest(new GSSourceSetting(setting.getObject().toString()));

	setting.setSourceLabel("label");

	withLabelTest(setting);
	withLabelTest(new GSSourceSetting(setting.getObject()));
	withLabelTest(new GSSourceSetting(setting.getObject().toString()));

	setting.setSourceEndpoint("endpoint");

	withEndpointTest(setting);
	withEndpointTest(new GSSourceSetting(setting.getObject()));
	withEndpointTest(new GSSourceSetting(setting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void withEndpointTest(GSSourceSetting setting) {

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(0, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(0, warnings.size());
    }

    /**
     * @param setting
     */
    private void withLabelTest(GSSourceSetting setting) {

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(1, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(0, warnings.size());
    }

    /**
     * @param setting
     */
    private void withIdTest(GSSourceSetting setting) {

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(2, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(0, warnings.size());
    }

    /**
     * @param setting
     */
    private void initTest(GSSourceSetting setting) {

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(3, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(0, warnings.size());

    }

}
