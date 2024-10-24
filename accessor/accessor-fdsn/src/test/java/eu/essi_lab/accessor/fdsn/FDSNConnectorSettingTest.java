package eu.essi_lab.accessor.fdsn;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class FDSNConnectorSettingTest {

    /**
     * 
     */
    @Test
    public void test() {

	FDSNConnectorSetting setting = new FDSNConnectorSetting();

	boolean ignoreComplexQueries = setting.isIgnoreComplexQueries();
	Assert.assertTrue(ignoreComplexQueries);

	setting.setIgnoreComplexQueries(false);
	ignoreComplexQueries = setting.isIgnoreComplexQueries();
	Assert.assertFalse(ignoreComplexQueries);

	setting.reset();

	ignoreComplexQueries = setting.isIgnoreComplexQueries();
	Assert.assertTrue(ignoreComplexQueries);
    }
}
