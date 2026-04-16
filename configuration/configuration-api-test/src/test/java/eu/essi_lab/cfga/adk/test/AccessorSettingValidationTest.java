package eu.essi_lab.cfga.adk.test;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;

/**
 * @author Fabrizio
 */
public class AccessorSettingValidationTest {

    private DefaultConfiguration configuration;

    @Test
    public void validationTest() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();
	
	AccessorSetting setting = new AccessorSetting();

	initTest(setting);
	initTest(new AccessorSetting(setting.getObject()));
	initTest(new AccessorSetting(setting.getObject().toString()));

	setting.getGSSourceSetting().setSourceIdentifier("id");

	withIdTest(setting);
	withIdTest(new AccessorSetting(setting.getObject()));
	withIdTest(new AccessorSetting(setting.getObject().toString()));

	setting.getGSSourceSetting().setSourceLabel("label");

	withLabelTest(setting);
	withLabelTest(new AccessorSetting(setting.getObject()));
	withLabelTest(new AccessorSetting(setting.getObject().toString()));

	setting.getGSSourceSetting().setSourceEndpoint("endpoint");

	withEndpointTest(setting);
	withEndpointTest(new AccessorSetting(setting.getObject()));
	withEndpointTest(new AccessorSetting(setting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void withEndpointTest(AccessorSetting setting) {

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
    private void withLabelTest(AccessorSetting setting) {

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
    private void withIdTest(AccessorSetting setting) {

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
    private void initTest(AccessorSetting setting) {

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
