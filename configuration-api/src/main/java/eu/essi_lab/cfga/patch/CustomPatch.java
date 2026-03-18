package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;
import java.util.function.*;

/**
 * @author Fabrizio
 */
public class CustomPatch extends Patch {

     private Predicate<Setting> predicate;
    private Function<Setting, Setting> function;

    /**
     * @param configuration
     * @param predicate
     * @param function
     */
    private CustomPatch(Configuration configuration, Predicate<Setting> predicate, Function<Setting, Setting> function) {

	setConfiguration(configuration);

	this.predicate = predicate;
	this.function = function;
    }

    /**
     * @param configuration
     * @param property
     * @param legacyValue
     * @param newValue
     * @return
     */
    public static CustomPatch of( //
	    Configuration configuration, //
	    Predicate<Setting> predicate,//
	    Function<Setting, Setting> function) {//

	return new CustomPatch(configuration, predicate, function);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    protected boolean doPatch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		predicate,//
		list);//

	GSLoggerFactory.getLogger(getClass()).debug("Found {} legacy settings to patch", list.size());

	if (!list.isEmpty()) {

	    List<Setting> converted = list.//
		    stream().//
		    map(s -> function.apply(s.clone())). //
		    toList();

	    for (Setting setting : list) {
		getConfiguration().remove(setting.getIdentifier());
	    }

	    for (Setting s : converted) {
		getConfiguration().put(s);
	    }

	    return true;
	}

	return false;
    }
}
