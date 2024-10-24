package eu.essi_lab.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ilsanto
 */
public class ExecutionModeTest {

    @Test
    public void test() {

	Assert.assertFalse(ExecutionMode.decode(null).isPresent());

	Assert.assertFalse(ExecutionMode.decode("").isPresent());

	Assert.assertFalse(ExecutionMode.decode("pippo").isPresent());

	Assert.assertFalse(ExecutionMode.decode("batc").isPresent());

	Assert.assertTrue(ExecutionMode.decode("batch").isPresent());

	Assert.assertTrue(ExecutionMode.decode("batcH").isPresent());

	Assert.assertTrue(ExecutionMode.decode("mixed").isPresent());

	Assert.assertTrue(ExecutionMode.decode("miXed").isPresent());

	Assert.assertTrue(ExecutionMode.decode("frontend").isPresent());

	Assert.assertTrue(ExecutionMode.decode("fRontEnd").isPresent());
	
	Assert.assertTrue(ExecutionMode.decode("access").isPresent());

	Assert.assertTrue(ExecutionMode.decode("Access").isPresent());

    }

}