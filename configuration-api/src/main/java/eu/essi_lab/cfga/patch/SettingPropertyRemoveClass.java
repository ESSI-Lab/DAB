package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class SettingPropertyRemoveClass extends Patch {

    private final Property<?> property;

    /**
     * @param configuration
     * @param property
     * @return
     */
    public static SettingPropertyRemoveClass of(Configuration configuration, Property<?> property) {

	return new SettingPropertyRemoveClass(configuration, property);
    }

    /**
     * @param configuration
     * @param property
     */
    private SettingPropertyRemoveClass(Configuration configuration, Property<?> property) {

	setConfiguration(configuration);

	this.property = property;
    }

    /**
     * @throws Exception
     */
    public void patch() throws Exception {

	ArrayList<Setting> list = new ArrayList<>();

	getConfiguration().list().forEach(s -> {

	    String object = s.getObject().toString();

	    if (object.contains(property.getKey())) {

		list.add(s);
	    }
	});

	GSLoggerFactory.getLogger(getClass())
		.debug("Found {} settings with legacy {} properties to patch", list.size(), property.getName());

	if (!list.isEmpty()) {

	    List<Setting> converted = list.//
		    stream().//
		    map(s -> {

		ArrayList<Setting> l = new ArrayList<>();

		SettingUtils.deepFind(s, inner -> s.getObject().has(property.getKey()), l);

		l.forEach(si -> si.getObject().remove(property.getKey()));

		return s.getObject();
	    }).//
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
