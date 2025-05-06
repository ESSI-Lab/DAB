/**
 * 
 */
package eu.essi_lab.tool;

import java.util.ServiceLoader;

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class IncrementalConnectorsPrinter {

    /**
     * @param args
     */
    public static void main(String[] args) {

	@SuppressWarnings("rawtypes")
	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	loader.stream().filter(c -> {
	    try {
		return c.get().supportsIncrementalHarvesting();
	    } catch (GSException e) {

		e.printStackTrace();
	    }

	    return false;

	}).map(c -> c.get().getClass().getSimpleName()).sorted().forEach(n -> System.out.println(n));
    }
}
