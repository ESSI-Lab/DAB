package eu.essi_lab.request.executor.schedule;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.JobDataMap;
import org.slf4j.Logger;

import eu.essi_lab.jobs.listener.GSJobListener;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
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
public class ScheduleUserJobExecutor<M extends RequestMessage, I, CR extends AbstractCountResponse, MR extends MessageResponse<I, CR>>
	implements IScheduleExecutor<M, I, CR, MR> {

    public static final String EXCEPTION_MISSING_CLASSES = "suje_missing_classes";
    public static final String EXCEPTION_MISSING_STORAGE_URI_COMPLEX = "suje_missing_result_storage_uri_complex";
    public static final String EXCEPTION_MISSING_STORAGE_URI = "suje_missing_result_storage_uri";
    public static final String EXCEPTION_MISSING_STORAGE_NAME = "suje_missing_result_storage_name";
    public static final String EXCEPTION_MISSING_JOB_SCHEDULER_INITIALIZATION_FAILED = "suje_missing_job_scheduler_initialization_failed";
    public static final String EXCEPTION_MISSING_JOB_SCHEDULER = "suje_missing_job_scheduler";

    private transient Logger logger = GSLoggerFactory.getLogger(ScheduleUserJobExecutor.class);
    private String workerHandlerClass = null;
    private String workerMapperClass = null;
    private String workerFormatterClass = null;
    private IGSJobScheduler jobScheduler;
    private Throwable defaultJobSchedulerInstantiationException = null;

    /**
     * Returns the job scheduler to be used by this {@link ScheduleUserJobExecutor}
     * 
     * @return
     */
    private IGSJobScheduler getJobScheduler() {
	return jobScheduler;
    }

    /**
     * Sets the job scheduler to be used by this {@link ScheduleUserJobExecutor}
     * 
     * @param jobScheduler
     */
    public void setJobScheduler(IGSJobScheduler jobScheduler) {
	this.jobScheduler = jobScheduler;
    }

    public ScheduleUserJobExecutor() {
	initJobScheduler();

    }

    /**
     * Initializes the job scheduler at initialization time.
     */
    public void initJobScheduler() {
	GSJobSchedulerFactory factory = new GSJobSchedulerFactory();
	try {
	    logger.info("Initializing job scheduler from default factory: "+factory.getClass().getName());
	    this.jobScheduler = factory.getGSJobScheduler();
	    logger.info("Initialized job scheduler with object: " + this.jobScheduler.getClass().getName());
	} catch (Throwable e) {
	    e.printStackTrace();
	    logger.error("Major problem instantiating default job scheduler");
	    this.defaultJobSchedulerInstantiationException = e;
	}
    }

    @Override
    public CountSet count(RequestMessage message) throws GSException {

	return null;
    }

    @Override
    public ResultSet<ScheduleReport> retrieve(RequestMessage message) throws GSException {

	logger.info("Scheduled retrieval executor");

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

	StorageUri resultStorageURI = message.getUserJobStorageURI();

	if (resultStorageURI == null) {
	    throw createException(EXCEPTION_MISSING_STORAGE_URI_COMPLEX, "Missing result storage URI object in the message");
	}
	if (resultStorageURI.getUri() == null || resultStorageURI.getUri().length() == 0) {
	    throw createException(EXCEPTION_MISSING_STORAGE_URI, "Missing result storage URI in the message");
	}
	if (resultStorageURI.getStorageName() == null || resultStorageURI.getStorageName().length() == 0) {
	    throw createException(EXCEPTION_MISSING_STORAGE_NAME, "Missing result storage name in the message");
	}

	String id = UUID.randomUUID().toString();
	ScheduleUserJob job = new ScheduleUserJob(id);

	GSConfOptionDBURI uriOption = (GSConfOptionDBURI) job.getSupportedOptions().get(ScheduleUserJob.RESULT_STORAGE_URI);

	StorageUri uri = new StorageUri(resultStorageURI.getUri());
	uri.setStorageName(resultStorageURI.getStorageName());
	uri.setUser(resultStorageURI.getUser());
	uri.setPassword(resultStorageURI.getPassword());
	uriOption.setValue(uri);

	GSUser user = message.getCurrentUser().orElse(null);

	IGSJobScheduler jobScheduler = getJobScheduler();

	if (jobScheduler == null) {
	    if (this.defaultJobSchedulerInstantiationException != null) {
		GSException ret = createException(EXCEPTION_MISSING_JOB_SCHEDULER_INITIALIZATION_FAILED,
			"Missing job scheduler. Error during initialization.");
		ret.getErrorInfoList().get(0).setCause(this.defaultJobSchedulerInstantiationException);
		throw ret;
	    }
	    throw createException(EXCEPTION_MISSING_JOB_SCHEDULER, "Missing job scheduler. ");
	}

	ResultSet<ScheduleReport> ret = new ResultSet<>();
	List<ScheduleReport> results = new ArrayList<>();
	ScheduleReport report = new ScheduleReport();
	report.setJobId(id);
	results.add(report);
	ret.setResultsList(results);

	Map<String, Object> map = new HashMap<String, Object>();

	// no need for the web request, as the web request transformation should be already done at this point
//	message.setWebRequest(null);

	map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, id);
	map.put(ScheduleUserJob.MESSAGE, message);

	map.put(ScheduleUserJob.HANDLER, workerHandlerClass);
	map.put(ScheduleUserJob.MAPPER, workerMapperClass);
	map.put(ScheduleUserJob.FORMATTER, workerFormatterClass);

	jobScheduler.scheduleJob(user, job, null, new JobDataMap(map));

	logger.info("Job submitted for execution. Job ID: " + id);

	return ret;
    }

    @Override
    public boolean isAuthorized(RequestMessage message) throws GSException {
        // TODO Auto-generated method stub
        return true;
    }

    private GSException createException(String errorId, String errorDescription) {
	GSException gse = new GSException();
	ErrorInfo info = new ErrorInfo();
	info.setContextId(this.getClass().getName());
	info.setErrorDescription(errorDescription);
	info.setErrorId(errorId);
	info.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	info.setSeverity(ErrorInfo.SEVERITY_ERROR);
	gse.addInfo(info);
	return gse;
    }

    @Override
    public void setWorkerHandler(String workerHandlerClass, String workerMapperClass, String workerFormatterClass) {
	this.workerHandlerClass = workerHandlerClass;
	this.workerMapperClass = workerMapperClass;
	this.workerFormatterClass = workerFormatterClass;

    }

}
