package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.S3StorageSetting;

/**
 * @author Fabrizio
 */
public class S3StorageSettingEditableTest {

    @Test
    public void test() {

	S3StorageSetting setting = new S3StorageSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
