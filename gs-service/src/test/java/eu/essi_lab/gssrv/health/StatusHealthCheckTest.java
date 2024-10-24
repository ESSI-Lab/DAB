package eu.essi_lab.gssrv.health;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.indeed.status.core.CheckResultSet;
import com.indeed.status.core.CheckStatus;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;

/**
 * @author Fabrizio
 */
public class StatusHealthCheckTest {

    static {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Before
    public void before() {
	HealthCheck.startCheckPassed = true;
    }

    @Test
    public void test1() {

	GSDependencyManager.getInstance();

	HealthCheck checker = new HealthCheck();
	CheckResultSet status = checker.status();
	CheckStatus systemStatus = status.getSystemStatus();

	Assert.assertEquals(CheckStatus.OK, systemStatus);
    }

    @Test
    public void test2() {

	HealthCheck checker = Mockito.spy(new HealthCheck());

	CheckResultSet status = Mockito.mock(CheckResultSet.class);

	Mockito.doReturn(CheckStatus.OK).when(status).getSystemStatus();

	Mockito.doReturn(status).when(checker).status();

	Assert.assertTrue(checker.isHealthy());
    }

    @Test
    public void test3() {

	HealthCheck checker = Mockito.spy(new HealthCheck());

	CheckResultSet status = Mockito.mock(CheckResultSet.class);

	Mockito.doReturn(CheckStatus.MINOR).when(status).getSystemStatus();

	Mockito.doReturn(status).when(checker).status();

	Assert.assertTrue(checker.isHealthy());
    }

    @Test
    public void test4() {

	HealthCheck checker = Mockito.spy(new HealthCheck());

	CheckResultSet status = Mockito.mock(CheckResultSet.class);

	Mockito.doReturn(CheckStatus.MAJOR).when(status).getSystemStatus();

	Mockito.doReturn(status).when(checker).status();

	Assert.assertTrue(checker.isHealthy());
    }

    @Test
    public void test5() {

	HealthCheck checker = Mockito.spy(new HealthCheck());

	CheckResultSet status = Mockito.mock(CheckResultSet.class);

	Mockito.doReturn(CheckStatus.OUTAGE).when(status).getSystemStatus();

	Mockito.doReturn(status).when(checker).status();

	Assert.assertFalse(checker.isHealthy());
    }

}