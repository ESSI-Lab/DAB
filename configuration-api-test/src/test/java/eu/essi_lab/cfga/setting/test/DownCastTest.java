package eu.essi_lab.cfga.setting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class DownCastTest {

    /**
     * 
     */
    @Test
    public void test1() {

	GSSourceSetting original = new GSSourceSetting();

	original.setSourceIdentifier("theSourceIdentifier");

	//
	// the original object is used, so the changes are applied to the original
	//

	GSSourceSetting downCast = SettingUtils.downCast(original, GSSourceSetting.class);

	Assert.assertEquals("theSourceIdentifier", downCast.getSourceIdentifier());

	//
	// a change to the cloned setting should not influence the original one
	//
	downCast.setSourceIdentifier("theNewIdentifier");

	Assert.assertEquals("theNewIdentifier", downCast.getSourceIdentifier());

	Assert.assertEquals("theNewIdentifier", original.getSourceIdentifier());

    }

    /**
     * 
     */
    @Test
    public void test2() {

	GSSourceSetting original = new GSSourceSetting();

	original.setSourceIdentifier("theSourceIdentifier");

	//
	// the clone object is used, so the changes are not applied to the original
	//

	GSSourceSetting downCast = SettingUtils.downCast(original, GSSourceSetting.class, true);

	Assert.assertEquals("theSourceIdentifier", downCast.getSourceIdentifier());

	//
	// a change to the cloned setting should not influence the original one
	//
	downCast.setSourceIdentifier("theNewIdentifier");

	Assert.assertEquals("theNewIdentifier", downCast.getSourceIdentifier());

	Assert.assertEquals("theSourceIdentifier", original.getSourceIdentifier());

    }
}
