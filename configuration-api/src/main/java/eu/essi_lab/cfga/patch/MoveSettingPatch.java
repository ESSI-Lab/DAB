package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class MoveSettingPatch extends Patch {

    private final Class<? extends Setting> targetClass;
    private final Setting parent;

    /**
     * @param configuration
     * @param target
     * @param parent
     * @return
     */
    public static MoveSettingPatch of(Configuration configuration, //
	    Class<? extends Setting> target, //
	    Setting parent) {//

	return new MoveSettingPatch(configuration, target, parent);
    }

    /**
     * @param configuration
     * @param target
     * @param targetSettingId
     * @param parent
     * @return
     */
    public static MoveSettingPatch of( //
	    Configuration configuration, //
	    Class<? extends Setting> target, //
	    String targetSettingId, //
	    Setting parent) {//

	return new MoveSettingPatch(configuration, target, parent);
    }

    /**
     * @param targetClass
     * @param parent
     */
    private MoveSettingPatch(Configuration configuration,//
	    Class<? extends Setting> targetClass,//
	    Setting parent) {

	setConfiguration(configuration);

	this.targetClass = targetClass;
	this.parent = parent;
    }

    @Override
    public boolean doPatch() throws Exception {

	List<Setting> targetSettings = parent. //
		getSettings().//
		stream().//
		filter(s -> s.getSettingClass().equals(targetClass)).//
		toList();

	GSLoggerFactory.getLogger(getClass()).debug("Found {} settings to move", targetSettings.size());

	if (!targetSettings.isEmpty()) {

	    for (Setting setting : targetSettings) {

		parent.removeSetting(setting);
	    }

	    getConfiguration().replace(parent);

	    for (Setting setting : targetSettings) {

		getConfiguration().put(setting);
	    }

	    return true;
	}

	return false;
    }
}
