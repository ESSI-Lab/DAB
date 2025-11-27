package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * This patch finds all the configuration settings having the given <code>property</code> value equals to the given
 * <code>legacyValue</code>, and replaces it with the new given <code>newValue</code>
 *
 * @author Fabrizio
 */
public class SettingPropertyReplacePatch extends Patch {

    private final Property<?> property;
    private final String legacyValue;
    private final String newClass;

    /**
     *
     * @param configuration
     * @param property
     * @param legacyValue
     * @param newValue
     * @return
     */
    public static SettingPropertyReplacePatch of(Configuration configuration, Property<?> property, String legacyValue, String newValue) {

	return new SettingPropertyReplacePatch(configuration, property, legacyValue, newValue);
    }

    /**
     *
     * @param configuration
     * @param property
     * @param legacyValue
     * @param newClass
     */
    private SettingPropertyReplacePatch(Configuration configuration, Property<?> property, String legacyValue, String newClass) {

	setConfiguration(configuration);

	this.property = property;
	this.legacyValue = legacyValue;
	this.newClass = newClass;
    }

    /**
     * @throws Exception
     */
    public void patch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		s -> s.getObject().getString(property.getKey()).equals(legacyValue),//
		list);//

	GSLoggerFactory.getLogger(getClass()).debug("Found {} legacy {} properties to patch", list.size(), property.getName());

	if (!list.isEmpty()) {

	    List<Setting> converted = list.//
		    stream().//
		    map(s -> s.getObject().toString().replace( //
		    legacyValue, //
		    newClass)).//
		    map(Setting::new).//
		    toList();

	    for (Setting setting : list) {
		getConfiguration().remove(setting.getIdentifier());
	    }

	    for (Setting s : converted) {
		getConfiguration().put(s);
	    }

	    getConfiguration().getSource().backup();
	    getConfiguration().flush();
	}
    }
}
