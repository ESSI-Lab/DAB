package eu.essi_lab.cfga.setting.editable.test;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.checker.RegisteredEditableSettingMethod;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class RegisteredEditableSettingMethodTest {

    /**
     * 
     */
    @Test
    public void test() {

	ServiceLoader<EditableSetting> loader = ServiceLoader.load(EditableSetting.class);
	long count = StreamUtils.iteratorToStream(loader.iterator()).count();

	Assert.assertEquals(14, count);

	RegisteredEditableSettingMethod method = new RegisteredEditableSettingMethod();

	CheckResponse check = method.check(null);

	Assert.assertTrue(check.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }
}
