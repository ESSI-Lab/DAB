package eu.essi_lab.demo.extensions;

import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.Provider;

/**
 * Implementation of {@link Provider} used by the {@link Pluggable}s of this test extension pack
 * 
 * @author Fabrizio
 */
public class DemoProvider extends Provider {

    private static final DemoProvider INSTANCE = new DemoProvider();

    public static DemoProvider getInstance() {

	return INSTANCE;
    }

    private DemoProvider() {

	setOrganization("My organization");
	setEmail("myorg@demo.com");
    }
}
