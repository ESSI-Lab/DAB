/**
 *
 */
package eu.essi_lab.model;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.configuration.ClusterType;

/**
 * @author Fabrizio
 */
public class ClusterTypeTest {

    @Test
    public void test() {

	Assert.assertEquals(ClusterType.LOCAL, ClusterType.get());

	System.setProperty("cluster", "Test");
	Assert.assertEquals(ClusterType.TEST, ClusterType.get());

	System.setProperty("cluster", "Preproduction");
	Assert.assertEquals(ClusterType.PRE_PRODUCTION, ClusterType.get());

	System.setProperty("cluster", "Production");
	Assert.assertEquals(ClusterType.PRODUCTION, ClusterType.get());

	System.setProperty("cluster", "Local");
	Assert.assertEquals(ClusterType.LOCAL, ClusterType.get());

	System.setProperty("cluster", "Unknown");
	Assert.assertEquals(ClusterType.LOCAL, ClusterType.get());

	//
	//
	//

	System.setProperty("cluster", ClusterType.TEST.getLabel());
	Assert.assertEquals(ClusterType.TEST, ClusterType.get());

	System.setProperty("cluster", ClusterType.PRE_PRODUCTION.getLabel());
	Assert.assertEquals(ClusterType.PRE_PRODUCTION, ClusterType.get());

	System.setProperty("cluster", ClusterType.PRODUCTION.getLabel());
	Assert.assertEquals(ClusterType.PRODUCTION, ClusterType.get());

	System.setProperty("cluster", ClusterType.LOCAL.getLabel());
	Assert.assertEquals(ClusterType.LOCAL, ClusterType.get());
    }
}
