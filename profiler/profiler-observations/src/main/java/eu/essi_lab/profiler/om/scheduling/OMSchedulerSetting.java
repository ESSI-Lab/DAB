/**
 * 
 */
package eu.essi_lab.profiler.om.scheduling;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class OMSchedulerSetting extends SchedulerWorkerSetting {

    private static final String REQUEST_URL_OPTION_KEY = "requestURL";
    private static final String OPERATION_ID_OPTION_KEY = "operationId";

    /**
     * 
     */
    public OMSchedulerSetting() {

	setConfigurableType(OMSchedulerWorker.CONFIGURABLE_TYPE);

	setGroup(SchedulingGroup.ASYNCH_ACCESS);

	Option<String> requestUrlOption = StringOptionBuilder.get().//
		withKey(REQUEST_URL_OPTION_KEY).//
		build();
	addOption(requestUrlOption);

	Option<String> operationIdOption = StringOptionBuilder.get().//
		withKey(OPERATION_ID_OPTION_KEY).//
		build();
	addOption(operationIdOption);
    }

    /**
     * @param url
     */
    public void setRequestURL(String url) {

	getOption(REQUEST_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @param id
     */
    public void setOperationId(String id) {

	getOption(OPERATION_ID_OPTION_KEY, String.class).get().setValue(id);
    }

    /**
     * @return
     */
    public String getRequestURL() {

	return getOption(REQUEST_URL_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @return
     */
    public String getOperationId() {

	return getOption(OPERATION_ID_OPTION_KEY, String.class).get().getValue();
    }
}
