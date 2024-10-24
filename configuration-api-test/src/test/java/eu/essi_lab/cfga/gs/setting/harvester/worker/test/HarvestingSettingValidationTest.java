package eu.essi_lab.cfga.gs.setting.harvester.worker.test;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;

/**
 * @author Fabrizio
 */
public class HarvestingSettingValidationTest {

    private DefaultConfiguration configuration;

    @Test
    public void validationTest() throws Exception {

	configuration = new DefaultConfiguration();
	configuration.clean();
	
	ConfigurationWrapper.setConfiguration(configuration);

	HarvestingSetting setting = HarvestingSettingLoader.load();

	initTest(setting);
	initTest(new HarvestingSettingImpl(setting.getObject()));
	initTest(new HarvestingSettingImpl(setting.getObject().toString()));

	setting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("id");

	withIdTest(setting);
	withIdTest(new HarvestingSettingImpl(setting.getObject()));
	withIdTest(new HarvestingSettingImpl(setting.getObject().toString()));

	setting.getSelectedAccessorSetting().getGSSourceSetting().setSourceLabel("label");

	withLabelTest(setting);
	withLabelTest(new HarvestingSettingImpl(setting.getObject()));
	withLabelTest(new HarvestingSettingImpl(setting.getObject().toString()));

	setting.getSelectedAccessorSetting().getGSSourceSetting().setSourceEndpoint("endpoint");

	withEndpointTest(setting);
	withEndpointTest(new HarvestingSettingImpl(setting.getObject()));
	withEndpointTest(new HarvestingSettingImpl(setting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void withEndpointTest(HarvestingSetting setting) {

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(0, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(1, warnings.size());
    }

    /**
     * @param setting
     */
    private void withLabelTest(HarvestingSetting setting) {

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(1, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(1, warnings.size());
    }

    /**
     * @param setting
     */
    private void withIdTest(HarvestingSetting setting) {

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(1, warnings.size());

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(2, errors.size());
    }

    /**
     * @param setting
     */
    private void initTest(HarvestingSetting setting) {

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	Optional<ValidationResponse> optional = setting.validate(configuration, ValidationContext.put());

	Assert.assertTrue(optional.isPresent());

	ValidationResponse validationResponse = optional.get();

	ValidationResult result = validationResponse.getResult();

	Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	List<String> errors = validationResponse.getErrors();

	Assert.assertEquals(3, errors.size());

	List<String> warnings = validationResponse.getWarnings();

	Assert.assertEquals(1, warnings.size());
    }
}
