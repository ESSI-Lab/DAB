package eu.essi_lab.cfga.adk.test;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;

/**
 * @author Fabrizio
 */
public class DistributionSettingValidationTest {

    private DefaultConfiguration configuration;

    @Test
    public void validationTest() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();

	DistributionSetting setting = new DistributionSetting();

	initTest(setting);
	initTest(new DistributionSetting(setting.getObject()));
	initTest(new DistributionSetting(setting.getObject().toString()));

	setting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("id");

	withIdTest(setting);
	withIdTest(new DistributionSetting(setting.getObject()));
	withIdTest(new DistributionSetting(setting.getObject().toString()));

    }

    /**
     * @param setting
     */
    private void withIdTest(DistributionSetting setting) {

	setting.getAccessorsSetting().select(s -> s.getName().equals("USGS Earthquake Events Accessor"));

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
    private void initTest(DistributionSetting setting) {

	setting.getAccessorsSetting().select(s -> s.getName().equals("USGS Earthquake Events Accessor"));

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
}
