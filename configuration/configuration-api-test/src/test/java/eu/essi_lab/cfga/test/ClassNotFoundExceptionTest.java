package eu.essi_lab.cfga.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ClassNotFoundExceptionTest {

    /**
     * The loaded configuration contains a setting having no related Java class. This test demonstrates that even in
     * this case, the configuration can correctly list its setting, ignoring the non existing ones and printing a
     * ClassNotFoundException
     * 
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

	FileSource fileSource = new FileSource(getClass().getClassLoader().getResource("class-not-found-config.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	configuration.put(new SystemSetting());

	List<SystemSetting> list = configuration
		.list(//
			SystemSetting.class, //
			false)
		.//
		stream().//
		collect(Collectors.toList());

	Assert.assertEquals(1, list.size());
    }

}
