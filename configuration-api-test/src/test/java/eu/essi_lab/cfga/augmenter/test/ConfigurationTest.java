/**
 * 
 */
package eu.essi_lab.cfga.augmenter.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.augmenter.worker.AugmenterWorker;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationTest {

    @Test
    public void test() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration configuration = new Configuration(filesSource);

	configuration.clear();

	//
	// ---
	//

	AugmenterWorker worker = new AugmenterWorker();

	configuration.put(worker.getSetting());

	configuration.flush();

	//
	// ---
	//

	// System.out.println(configuration.toString());

	//
	// ---
	//

	Optional<AugmenterWorkerSetting> optional = configuration.get(//
		worker.getSetting().getIdentifier(), //
		AugmenterWorkerSetting.class,//
		false);

	AugmenterWorkerSetting setting = optional.get();

	Assert.assertEquals(worker.getSetting(), setting);
    }

}
