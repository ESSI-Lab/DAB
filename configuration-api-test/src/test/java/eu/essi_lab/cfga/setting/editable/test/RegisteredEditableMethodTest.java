package eu.essi_lab.cfga.setting.editable.test;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.RegisteredEditableMethod;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class RegisteredEditableMethodTest {

    /**
     * 
     */
    @Test
    public void test() {

	ServiceLoader<EditableSetting> loader = ServiceLoader.load(EditableSetting.class);
	long count = StreamUtils.iteratorToStream(loader.iterator()).count();

	Assert.assertEquals(14, count);

	RegisteredEditableMethod method = new RegisteredEditableMethod();

	CheckResponse check = method.check(null);

	Assert.assertTrue(check.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }
}
