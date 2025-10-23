/**
 * 
 */
package eu.essi_lab.lib.skoss.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;

/**
 * @author Fabrizio
 */
public class ExpansionLimitTest {

    @Test
    public void test1() {

	ExpansionLimit exp = ExpansionLimit.of("[CONCEPTS:50]").get();

	Assert.assertEquals(LimitTarget.CONCEPTS, exp.getTarget());

	Assert.assertEquals(50, exp.getLimit());
    }
    
    @Test
    public void test2() {

	ExpansionLimit exp = ExpansionLimit.of("[LABELS:30]").get();

	Assert.assertEquals(LimitTarget.LABELS, exp.getTarget());

	Assert.assertEquals(30, exp.getLimit());
    }

    @Test
    public void test3() {

	ExpansionLimit exp = ExpansionLimit.of("[ALT_LABELS:10]").get();

	Assert.assertEquals(LimitTarget.ALT_LABELS, exp.getTarget());

	Assert.assertEquals(10, exp.getLimit());
    }
    
    @Test
    public void test4() {

	Assert.assertFalse(ExpansionLimit.of("[ALT_LABELS:10").isEmpty());

	Assert.assertFalse(ExpansionLimit.of("ALT_LABELS:10]").isEmpty());

	Assert.assertFalse(ExpansionLimit.of("ALT_LABELS:10").isEmpty());

	Assert.assertTrue(ExpansionLimit.of("[ALT_ABELS:10]").isEmpty());

	Assert.assertTrue(ExpansionLimit.of("[LABELS:xx]").isEmpty());
    }
}
