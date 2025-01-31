package eu.essi_lab.accessor.test.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.test.OAIPMHWrapperAccessor;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class HarvestedAccessorFactoryTest {

    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	@SuppressWarnings("rawtypes")
	List<IHarvestedAccessor> harvestedAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	Assert.assertEquals(1, harvestedAccessors.size());

	Assert.assertEquals(OAIPMHWrapperAccessor.TYPE, harvestedAccessors.get(0).getType());
    }
}
