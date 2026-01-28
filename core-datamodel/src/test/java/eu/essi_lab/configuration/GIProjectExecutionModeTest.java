package eu.essi_lab.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ilsanto
 */
public class GIProjectExecutionModeTest {

    @Test
    public void test1() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.BATCH.name());

	Assert.assertEquals(ExecutionMode.BATCH, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.BATCH.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.BATCH, ExecutionMode.get());
    }

    @Test
    public void test2() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.MIXED.name());

	Assert.assertEquals(ExecutionMode.MIXED, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.MIXED.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.MIXED, ExecutionMode.get());

    }

    @Test
    public void test3() {

	Assert.assertEquals(ExecutionMode.MIXED, ExecutionMode.get());
    }

    @Test
    public void test4() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, "test");

	Assert.assertEquals(ExecutionMode.MIXED, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, "test");

	Assert.assertEquals(ExecutionMode.MIXED, ExecutionMode.get());
    }

    @Test
    public void test5() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.FRONTEND.name());

	Assert.assertEquals(ExecutionMode.FRONTEND, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.FRONTEND.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.FRONTEND, ExecutionMode.get());

    }

    @Test
    public void test6() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.ACCESS.name());

	Assert.assertEquals(ExecutionMode.ACCESS, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.ACCESS.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.ACCESS, ExecutionMode.get());

    }

    @Test
    public void test7() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.CONFIGURATION.name());

	Assert.assertEquals(ExecutionMode.CONFIGURATION, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.CONFIGURATION.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.CONFIGURATION, ExecutionMode.get());

    }

    @Test
    public void test8() {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.LOCAL_PRODUCTION.name());

	Assert.assertEquals(ExecutionMode.LOCAL_PRODUCTION, ExecutionMode.get());

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.LOCAL_PRODUCTION.name().toLowerCase());

	Assert.assertEquals(ExecutionMode.LOCAL_PRODUCTION, ExecutionMode.get());

    }

}