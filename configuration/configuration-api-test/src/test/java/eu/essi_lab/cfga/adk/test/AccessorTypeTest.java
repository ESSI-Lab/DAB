package eu.essi_lab.cfga.adk.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;

/**
 * @author Fabrizio
 */
public class AccessorTypeTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void test() {

	List<IHarvestedAccessor> harvestedAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);
	harvestedAccessors.forEach(h -> testSpecificAccessorType((AccessorSetting) h.getSetting()));

	List<IHarvestedAccessor> harvestedMixedAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);
	harvestedMixedAccessors.forEach(h -> testMixedAccessorType((AccessorSetting) h.getSetting()));

	List<IDistributedAccessor> distributedAccessors = AccessorFactory.getDistributedAccessors(LookupPolicy.SPECIFIC);
	distributedAccessors.forEach(h -> testSpecificAccessorType((AccessorSetting) h.getSetting()));

	List<IDistributedAccessor> distributedMixedAccessors = AccessorFactory.getDistributedAccessors(LookupPolicy.MIXED);
	distributedMixedAccessors.forEach(h -> testMixedAccessorType((AccessorSetting) h.getSetting()));

    }

    /**
     * @param setting
     */
    private void testSpecificAccessorType(AccessorSetting setting) {

	String accessorType = setting.getAccessorType();
	System.out.println("SPECIFIC -> " + accessorType);
	Assert.assertEquals(setting.getConfigurableType(), accessorType);
    }

    /**
     * @param setting
     */
    private void testMixedAccessorType(AccessorSetting setting) {

	String accessorType = setting.getAccessorType();
	System.out.println("MIXED -> " + accessorType);
    }
}
