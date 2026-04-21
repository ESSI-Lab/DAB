package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSettingLoader;

public class AugmenterWorkerSettingEditableTest {

    @Test
    public void test() {

	AugmenterWorkerSetting setting = AugmenterWorkerSettingLoader.load();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
