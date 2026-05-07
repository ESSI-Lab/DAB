package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class PutSettingPatch extends Patch {

    private final String settingId;
    private Class<? extends Setting> settingClass;

    /**
     * @param configuration
     * @param settingClass
     * @param settingId
     */
    private PutSettingPatch(Configuration configuration, Class<? extends Setting> settingClass, String settingId) {

	setConfiguration(configuration);

	this.settingId = settingId;
	this.settingClass = settingClass;
    }

    /**
     * @param configuration
     * @param settingClass
     * @param settingId
     * @return
     */
    public static PutSettingPatch of( //
	    Configuration configuration, //
	    Class<? extends Setting> settingClass, //
	    String settingId) {//

	return new PutSettingPatch(configuration, settingClass, settingId);
    }

    /**
     * @return
     * @throws Exception
     */
    @Override
    protected boolean doPatch() {

	ArrayList<Setting> list = new ArrayList<>();

	ConfigurationUtils.deepFind(//
		getConfiguration(),//
		s -> s.getSettingClass().equals(settingClass) && s.getIdentifier().equals(settingId),//
		list);//

	if (list.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Target setting missing, new setting required");

	    Setting target = SettingUtils.create(settingClass);

	    target.setIdentifier(settingId);

	    boolean put = getConfiguration().put(target);

	    if (!put) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to put new setting");

		return false;
	    }

	    return true;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Target setting already exists, no actions required");

	return false;
    }
}
