package eu.essi_lab.cfga.test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.checker.ReferencedClassesMethod;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ReferencedClassesMethodTest {

    @Test
    public void checkClassesTest() throws URISyntaxException, Exception {

	Configuration configuration = new Configuration(
		new FileSource(new File(getClass().getClassLoader().getResource("config-with-errors.json").toURI())));

	CheckResponse checkResponse = new ReferencedClassesMethod().check(configuration);

	Assert.assertTrue(checkResponse.getCheckResult() == CheckResult.CHECK_FAILED);

	Set<String> messages = checkResponse.getMessages();

	Assert.assertEquals(14, messages.size());
    }
}
