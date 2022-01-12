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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.AbstractGSConfigurableJob;
import eu.essi_lab.jobs.listener.GSJobListener;
import eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.IGSConfigurationInstantiable;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.ProfilerHandler;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.request.executor.IRequestExecutor;
public class ScheduleUserJob extends AbstractGSConfigurableJob implements IGSMainConfigurable {

    /**
     * 
     */
    private static final long serialVersionUID = -6308947930226586722L;

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public static final String USER_JOB = "USER_JOB";

    public static final String RESULT_STORAGE_URI = "RESULT_STORAGE_URI";

    public static final String MESSAGE = "MESSAGE";

    public static final String HANDLER = "HANDLER";

    public static final String MAPPER = "MAPPER";

    public static final String FORMATTER = "FORMATTER";

    public static final String EXCEPTION_MISSING_CLASS = "suj_missing_class";

    public static final String EXCEPTION_MISSING_EXECUTION_ID_HINT = "suj_missing_execution_id_hint";

    public static final String EXCEPTION_MISSING_MESSAGE = "suj_missing_message";

    public static final String EXCEPTION_INSTANTIATION_HANDLER = "suj_instantiation_handler";

    public static final String EXCEPTION_INSTANTIATION_FORMATTER = "suj_instantiation_formatter";

    public static final String EXCEPTION_INSTANTIATION_MAPPER = "suj_instantiation_mapper";

    public static final String EXCEPTION_MISSING_RESULT_STORAGE_URI_COMPLEX = "suj_missing_result_storage_uri_complex";

    public static final String EXCEPTION_MISSING_RESULT_STORAGE_URI = "suj_missing_result_storage_uri";

    public static final String EXCEPTION_MISSING_RESULT_STORAGE_URI_STORAGE_NAME = "suj_missing_result_storage_uri_storage_name";

    @JsonIgnore
    private transient Boolean completed;

    @JsonIgnore
    private transient String executionId = "UNSET";

    @Override
    public String getLabel() {
	return "USER JOBS";
    }

    public ScheduleUserJob(String executionId) {
	this();
	this.executionId = executionId;
    }

    public ScheduleUserJob() {

	setKey(USER_JOB);
	getSupportedOptions().remove(START_DATE_KEY);
	getSupportedOptions().remove(INTERVAL_KEY);
	getSupportedOptions().remove(INTERVAL_PERIOD_KEY);

	GSConfOptionDBURI uriOption = new GSConfOptionDBURI();

	uriOption.setLabel("Where to store user job results");

	uriOption.setKey(RESULT_STORAGE_URI);

	uriOption.setMandatory(true);

	getSupportedOptions().put(RESULT_STORAGE_URI, uriOption);

    }

    @Override
    @JsonIgnore
    public String getId() {
	return getKey() + ":" + executionId;
    }

    @JsonIgnore
    public Long getStartDeltaMillis() {
	return 1000L;
    }

    @JsonIgnore
    @Override
    public Date getStartDate() {
	return new Date(System.currentTimeMillis() + getStartDeltaMillis());
    }

    @JsonIgnore
    @Override
    public int getInterval() {
	return 1;
    }

    @Override
    public boolean completed() {
	return completed;
    }

    @Override
    public void onFlush() throws GSException {
	// nothing to do here, the job is scheduled by ScheduleUserJobExecutor
    }

    @Override
    public void completed(boolean value) {
	completed = value;
    }

    @Override
    public GS_JOB_INTERVAL_PERIOD getIntervalPeriod() {
	return null;
    }

    public boolean validate() {
	// TODO
	return true;

    }

    /**
     * This job executes the following workflow:
     * <ol>
     * <li>Retrieves from the job map the handler, mapper and formatter classes names to be instantiated</li>
     * <li>Instantiate them</li>
     * <li>Retrieves from the job map the message to be executed</li>
     * <li>Executes the message request using the handler, mapper and formatter previously instantiated</li>
     * <li>Upon handler completion, it stores the result response on the result storage URI indicated in the
     * message</li>
     * </ol>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(Map<String, Object> jobDataMap, Boolean isRecovering, Optional<GSJobStatus> jobStatus) throws GSException {

	String handlerClass = (String) jobDataMap.get(HANDLER);
	String mapperClass = (String) jobDataMap.get(MAPPER);
	String formatterClass = (String) jobDataMap.get(FORMATTER);

	if (handlerClass == null || handlerClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing handler class from job data map");
	}

	if (mapperClass == null || mapperClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing mapper class from job data map");
	}

	if (formatterClass == null || formatterClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing formatter class from job data map");
	}

	String jobId = (String) jobDataMap.get(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID);

	if (jobId == null || jobId.length() == 0) {
	    throw createException(EXCEPTION_MISSING_EXECUTION_ID_HINT, "Missing id from job data map");
	}

	RequestMessage message = (RequestMessage) jobDataMap.get(MESSAGE);

	if (message == null) {
	    throw createException(EXCEPTION_MISSING_MESSAGE, "Missing message from job data map");
	}

	StorageUri resultStorageURI = message.getUserJobStorageURI();
	if (resultStorageURI == null) {
	    throw createException(EXCEPTION_MISSING_RESULT_STORAGE_URI_COMPLEX, "Missing result storage URI complex object from message");
	}

	if (resultStorageURI.getUri() == null || resultStorageURI.getUri().length() == 0) {
	    throw createException(EXCEPTION_MISSING_RESULT_STORAGE_URI, "Missing result storage URI from message");
	}

	if (resultStorageURI.getStorageName() == null || resultStorageURI.getStorageName().length() == 0) {
	    throw createException(EXCEPTION_MISSING_RESULT_STORAGE_URI_STORAGE_NAME,
		    "Missing result storage URI storage name from message");
	}

	ProfilerHandler handler;
	try {
	    handler = (ProfilerHandler) createInstance(handlerClass);
	} catch (Exception e) {
	    throw createException(EXCEPTION_INSTANTIATION_HANDLER, "Error instantiating handler class");
	}
	try {
	    handler.setMessageResponseMapper((MessageResponseMapper) createInstance(mapperClass));
	} catch (Exception e) {
	    throw createException(EXCEPTION_INSTANTIATION_MAPPER, "Error instantiating mapper class");
	}
	try {
	    handler.setMessageResponseFormatter((MessageResponseFormatter) createInstance(formatterClass));
	} catch (Exception e) {
	    throw createException(EXCEPTION_INSTANTIATION_FORMATTER, "Error instantiating formatter class");
	}

	ProfilerHandler wrapperHandler = new ProfilerHandler() {

	    @Override
	    protected IRequestExecutor createExecutor() {
		return handler.getExecutor();
	    }

	    @SuppressWarnings("unused")
	    @Override
	    protected void onHandlingEnded(Response response) {
		logger.info("Executor handling ended, storing the result...");
		if (response == null || response.getEntity() == null) {
		    logger.error("Empty response!");
		    return;
		}
		try {
		    StorageUri resultStorageURI = message.getUserJobStorageURI();
		    if (resultStorageURI == null) {
			logger.error("This should not happen, as previously checked!");
		    }
		    Date endDate = new Date();

		    String objectName = message.getUserJobResultId();

		    if (objectName == null || objectName.isEmpty()) {
			objectName = "unnamed-result-" + System.currentTimeMillis() + ".dat";
		    }

		    String get = resultStorageURI.getUri() + resultStorageURI.getStorageName() + "/" + objectName;

		    logger.info("Storing to: " + get);

		    ResultStorage storage = null;

		    // AMAZON specific result storage
		    if (resultStorageURI.getUri().contains("s3.amazonaws.com")) {
			storage = new AmazonResultStorage(resultStorageURI);
		    }

		    if (storage == null) {
			logger.error("Unable to connect to storage");
		    }

		    javax.ws.rs.core.StreamingOutput streamingOutput = response.readEntity(javax.ws.rs.core.StreamingOutput.class);

		    File tmpFile = File.createTempFile(ScheduleUserJob.this.getClass().getSimpleName(), ".tmp");
		    tmpFile.deleteOnExit();

		    FileOutputStream baos = new FileOutputStream(tmpFile);
		    streamingOutput.write(baos);
		    baos.close();
		    storage.store(objectName, tmpFile);
		    tmpFile.delete();

		    logger.info("Result stored");
		    
		    if (jobStatus.isPresent()) {
			jobStatus.get().setResultStorage(get);
		    }

		} catch (Exception e) {
		    e.printStackTrace();
		    logger.error("Storing failed: " + e.getMessage());
		}
	    }

	};
	wrapperHandler.setMessageResponseMapper(handler.getMessageResponseMapper());
	wrapperHandler.setMessageResponseFormatter(handler.getMessageResponseFormatter());

	wrapperHandler.handleMessageRequest(message);

	completed(true);

    }

    private Object createInstance(String handlerClass) throws Exception {
	GSLoggerFactory.getLogger(this.getClass()).info("Creating instance of class: " + handlerClass);
	if (handlerClass == null) {
	    return null;
	}
	Class<?> clazz = Class.forName(handlerClass);
	Object ret = clazz.newInstance();
	return ret;
    }

    @Override
    public GSJobValidationResult isValid(Map<String, Object> jobDataMap) {
	GSJobValidationResult ret = new GSJobValidationResult();
	ret.setValid(true);
	return ret;
    }

    @Override
    public IGSConfigurationInstantiable getInstantiableType() {

	InstantiableUserJobResultStorageURI info = new InstantiableUserJobResultStorageURI();

	try {
	    GSConfOption<?> option = getSupportedOptions().get(RESULT_STORAGE_URI);
	    if (option != null) {

		StorageUri storageUri = ((StorageUri) option.getValue());
		if (storageUri != null)
		    info = new Deserializer().deserialize(storageUri.serialize(), InstantiableUserJobResultStorageURI.class);

	    }
	    info.setComponent(this);

	} catch (GSException e) {
	    // TODO whatr should I do here?

	    logger.error("Error executing getInstantiableType", e);
	}

	return info;
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

}
