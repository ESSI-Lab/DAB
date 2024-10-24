/**
 * 
 */
package eu.essi_lab.cfga.test;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class CreateConfigurableTest {

    @Test
    public void nullTypeTest() {

	List<String> nullTypeConfigurables = StreamUtils.//
		iteratorToStream(ServiceLoader.load(Configurable.class).iterator()).//
		filter(c -> c.getType() == null).//
		map(c -> c.getClass().getName()).//
		collect(Collectors.toList());

	if (!nullTypeConfigurables.isEmpty()) {

	    Assert.fail("Null type configurables found: " + nullTypeConfigurables);
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createConfigurableTest() throws Exception {

	ArrayList<Configurable> fromSettingConfigurables = new ArrayList<>();
	ArrayList<Configurable> fromFactoriesConfigurables = new ArrayList<>();

	StreamUtils.//
		iteratorToStream(ServiceLoader.load(Configurable.class).iterator()).//
		forEach(c -> {

		    Configurable configurable = null;
		    try {

			configurable = c.getClass().newInstance();

			test(configurable);

			fromSettingConfigurables.add(configurable);

		    } catch (RuntimeException ex) {

			fromFactoriesConfigurables.add(configurable);

		    } catch (Exception ex) {

			ex.printStackTrace();
		    }

		});

	System.out.println("Following configurables can be created from the related setting:\n");
	fromSettingConfigurables.forEach(c -> System.out.println(c.getClass() + " -> " + c.getType()));

	System.out.println("\n---\n");

	System.out.println("Following configurables can be created from the related factory:\n");
	fromFactoriesConfigurables.forEach(c -> System.out.println(c.getClass() + " -> " + c.getType()));
    }

    /**
     * @param config
     * @throws Exception
     */
    private void test(Configurable<?> config) throws Exception {

	config.getSetting().createConfigurable();
    }
}
