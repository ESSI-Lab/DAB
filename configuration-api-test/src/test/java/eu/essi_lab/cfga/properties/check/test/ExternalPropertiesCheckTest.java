/**
 * 
 */
package eu.essi_lab.cfga.properties.check.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.check.PropertiesMethod;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.S3Source;

/**
 * @author Fabrizio
 */
public class ExternalPropertiesCheckTest {

//    @Test
    public void prodConfigTest() throws Exception {

	S3Source source = S3Source.of(System.getProperty("prod.config.url"));

	Configuration configuration = new Configuration(source);

	ConfigurationWrapper.setConfiguration(configuration);

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(Setting.FOLDED_MODE);

	CheckResponse check = method.check(configuration);

	if (check.getCheckResult() == CheckResult.CHECK_FAILED) {

	    ConfigurationUtils.fix(configuration, check.getSettings(), true, true);

	    //
	    //
	    //

	    check = method.check(configuration);

	    Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, check.getCheckResult());
	}
    }

//    @Test
    public void preProdConfigTest() throws Exception {

	S3Source source = S3Source.of(System.getProperty("preprod.config.url"));

	Configuration configuration = new Configuration(source);

	ConfigurationWrapper.setConfiguration(configuration);

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(Setting.FOLDED_MODE);

	CheckResponse check = method.check(configuration);

	if (check.getCheckResult() == CheckResult.CHECK_FAILED) {

	    ConfigurationUtils.fix(configuration, check.getSettings(), true, true);

	    //
	    //
	    //

	    check = method.check(configuration);

	    Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, check.getCheckResult());
	}
    }
}
