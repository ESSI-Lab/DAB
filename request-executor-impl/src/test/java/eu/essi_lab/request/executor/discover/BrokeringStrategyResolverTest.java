package eu.essi_lab.request.executor.discover;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class BrokeringStrategyResolverTest {

    @Test
    public void testDistributedSource() throws GSException {

	BrokeringStrategyResolver resolver = new BrokeringStrategyResolver();

	GSSource source = Mockito.mock(GSSource.class);

	Mockito.doReturn(BrokeringStrategy.DISTRIBUTED).when(source).getBrokeringStrategy();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	BrokeringStrategy strategyResolved = resolver.resolveStrategy(source, message);

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, strategyResolved);

    }

    @Test
    public void testHarvestedSource() throws GSException {

	BrokeringStrategyResolver resolver = new BrokeringStrategyResolver();

	GSSource source = Mockito.mock(GSSource.class);

	Mockito.doReturn(BrokeringStrategy.HARVESTED).when(source).getBrokeringStrategy();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	BrokeringStrategy strategyResolved = resolver.resolveStrategy(source, message);

	Assert.assertEquals(BrokeringStrategy.HARVESTED, strategyResolved);

    }

//    @Test
//    public void testMixedSourceFirstLevel() throws GSException {
//
//	BrokeringStrategyResolver resolver = new BrokeringStrategyResolver();
//
//	GSSource source = Mockito.mock(GSSource.class);
//
//	Mockito.doReturn(BrokeringStrategy.MIXED).when(source).getBrokeringStrategy();
//
//	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
//
//	BondReducer reducer = Mockito.mock(BondReducer.class);
//
//	Mockito.doReturn(false).when(reducer).mixedSourceSecondLevelBond(Mockito.any(), Mockito.any());
//
//	BrokeringStrategy strategyResolved = resolver.resolveStrategy(source, message);
//
//	Assert.assertEquals(BrokeringStrategy.HARVESTED, strategyResolved);
//
//    }
//
//
//    @Test
//    public void testMixedSourceSecondLevel() throws GSException {
//
//	BrokeringStrategyResolver resolver = new BrokeringStrategyResolver();
//
//	GSSource source = Mockito.mock(GSSource.class);
//
//	Mockito.doReturn(BrokeringStrategy.MIXED).when(source).getBrokeringStrategy();
//
//	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
//
//	BondReducer reducer = Mockito.mock(BondReducer.class);
//
//	Mockito.doReturn(true).when(reducer).mixedSourceSecondLevelBond(Mockito.any(), Mockito.any());
//
//
//	BrokeringStrategy strategyResolved = resolver.resolveStrategy(source, message);
//
//	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, strategyResolved);
//
//    }

}