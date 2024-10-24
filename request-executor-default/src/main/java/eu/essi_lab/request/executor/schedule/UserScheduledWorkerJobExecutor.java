/**
 * 
 */
package eu.essi_lab.request.executor.schedule;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

import org.quartz.SchedulerException;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ScheduleReport;
import eu.essi_lab.request.executor.IScheduleExecutor;

/**
 * It implements asynchronous execution of user requests through scheduling of a correspondent Quartz job
 * 
 * @author Fabrizio
 * @author boldrini
 */
public class UserScheduledWorkerJobExecutor<M extends RequestMessage, I, CR extends AbstractCountResponse, MR extends MessageResponse<I, CR>>
	implements IScheduleExecutor<M, I, CR, MR> {

    public static final String EXCEPTION_MISSING_CLASSES = "suje_missing_classes";
    public static final String EXCEPTION_MISSING_STORAGE_URI_COMPLEX = "suje_missing_result_storage_uri_complex";
    public static final String EXCEPTION_MISSING_STORAGE_URI = "suje_missing_result_storage_uri";
    public static final String EXCEPTION_MISSING_STORAGE_NAME = "suje_missing_result_storage_name";
    public static final String SCHEDULING_ERROR = "USER_SCHEDULED_JOB_HNADLER_SCHEDULING_ERROR";

    protected String workerHandlerClass;
    protected String workerMapperClass;
    protected String workerFormatterClass;

    public UserScheduledWorkerJobExecutor() {
    }

    @Override
    public ResultSet<ScheduleReport> retrieve(RequestMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Scheduled retrieval executor");

	checkInputs(message);

	UserScheduledSetting setting = createSetting(message);

	try {
	    getScheduler().schedule(setting);
	} catch (SchedulerException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SCHEDULING_ERROR, //
		    e);
	}

	String jobId = SchedulerUtils.createJobKey(setting).getName();

	GSLoggerFactory.getLogger(getClass()).info("Job submitted for execution. Job ID: " + jobId);

	//
	// returns a result set with an empty report
	//

	return createDefaultResultSet(jobId);
    }

    @Override
    public void setWorkerHandler(String workerHandlerClass, String workerMapperClass, String workerFormatterClass) {

	this.workerHandlerClass = workerHandlerClass;
	this.workerMapperClass = workerMapperClass;
	this.workerFormatterClass = workerFormatterClass;
    }

    /**
     * We are using default scheduling to run now once and as soon as possible.
     * Maybe a different scheduling is expected?
     * 
     * @param message
     * @param jobId
     * @return
     */
    protected UserScheduledSetting createSetting(RequestMessage message) {

	//
	// we are using default scheduling to run now once.
	// maybe a different scheduling is expected?
	//
	UserScheduledSetting setting = new UserScheduledSetting();

	//
	// the group is used by the SchedulerJob in order to decide if run the job or reschedule it, according
	// to the ExecutionMode of the host
	//
	setting.setGroup(message instanceof AccessMessage ? SchedulingGroup.ASYNCH_ACCESS : SchedulingGroup.BULK_DOWNLOAD);

	setting.setRequestMessage(message);

	setting.setHandler(workerHandlerClass);
	setting.setMapper(workerMapperClass);
	setting.setFormatter(workerFormatterClass);

	return setting;
    }

    /**
     * @param setting
     * @return
     * @throws GSException
     */
    protected Scheduler getScheduler() throws GSException {

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();
	return SchedulerFactory.getScheduler(schedulerSetting);
    }

    /**
     * @param jobId
     * @return
     */
    private ResultSet<ScheduleReport> createDefaultResultSet(String jobId) {

	ResultSet<ScheduleReport> ret = new ResultSet<>();
	List<ScheduleReport> results = new ArrayList<>();

	ScheduleReport report = new ScheduleReport();

	report.setJobId(jobId);
	results.add(report);

	ret.setResultsList(results);

	return ret;
    }

    /**
     * @param message
     * @throws GSException
     */
    private void checkInputs(RequestMessage message) throws GSException {

	if (workerHandlerClass == null || workerHandlerClass.length() == 0 || //
		workerMapperClass == null || workerMapperClass.length() == 0 || //
		workerFormatterClass == null || workerFormatterClass.length() == 0 //
	) {
	    String missing = "";
	    if (workerHandlerClass == null || workerHandlerClass.length() == 0) {
		missing += "handler, ";
	    }
	    if (workerMapperClass == null || workerMapperClass.length() == 0) {
		missing += "mapper, ";
	    }
	    if (workerFormatterClass == null || workerFormatterClass.length() == 0) {
		missing += "formatter, ";
	    }
	    throw createException(EXCEPTION_MISSING_CLASSES, "Needed classes (names) to be instantiated are missing: " + missing);
	}

	StorageInfo resultStorageURI = message.getUserJobStorageURI();

	if (resultStorageURI == null) {
	    throw createException(EXCEPTION_MISSING_STORAGE_URI_COMPLEX, "Missing result storage URI object in the message");
	}
	if (resultStorageURI.getUri() == null || resultStorageURI.getUri().length() == 0) {
	    throw createException(EXCEPTION_MISSING_STORAGE_URI, "Missing result storage URI in the message");
	}
	if (resultStorageURI.getName() == null || resultStorageURI.getName().length() == 0) {
	    throw createException(EXCEPTION_MISSING_STORAGE_NAME, "Missing result storage name in the message");
	}
    }

    /**
     * @param errorId
     * @param errorDescription
     * @return
     */
    private GSException createException(String errorId, String errorDescription) {

	GSLoggerFactory.getLogger(getClass()).error(errorDescription);

	return GSException.createException(//
		getClass(), //
		errorDescription, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		errorId);
    }

    @Override
    public CountSet count(RequestMessage message) throws GSException {

	return null;
    }

    @Override
    public boolean isAuthorized(RequestMessage message) throws GSException {

	return true;
    }
}
