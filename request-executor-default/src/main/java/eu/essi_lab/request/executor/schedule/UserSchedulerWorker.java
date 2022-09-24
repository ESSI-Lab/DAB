package eu.essi_lab.request.executor.schedule;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.Response;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.ProfilerHandler;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.request.executor.IRequestExecutor;
import eu.essi_lab.shared.driver.ConfiguredDriverUtils;
import eu.essi_lab.shared.resultstorage.ResultStorage;
import eu.essi_lab.shared.resultstorage.ResultStorageFactory;

/**
 * {@link UserSchedulerWorker} represents a scheduled job correspondent to a user asynchronous request (e.g. typically a
 * delayed access request)
 * 
 * @author boldrini
 */
public class UserSchedulerWorker extends SchedulerWorker<UserScheduledSetting> {

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

    static final String CONFIGURABLE_TYPE = "UserSchedulerWorker";

    public UserSchedulerWorker() {
    }

    protected UserScheduledSetting initSetting() {

	return new UserScheduledSetting();
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	String handlerClass = getSetting().getHandler();
	String mapperClass = getSetting().getMapper();
	String formatterClass = getSetting().getFormatter();

	RequestMessage message = getSetting().getRequestMessage();

	checkInputs(handlerClass, mapperClass, formatterClass, message);

	ProfilerHandler handler = createHandler(handlerClass, mapperClass, formatterClass);

	ProfilerHandler wrapper = createHandlerWrapper(handler, message, status);

	wrapper.handleMessageRequest(message);
    }

    /**
     * @param handler
     * @param message
     * @param status
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ProfilerHandler createHandlerWrapper(ProfilerHandler handler, RequestMessage message, SchedulerJobStatus status) {

	ProfilerHandler wrapperHandler = new ProfilerHandler() {

	    @Override
	    protected IRequestExecutor createExecutor() {
		return handler.getExecutor();
	    }

	    @SuppressWarnings("unused")
	    @Override
	    protected void onHandlingEnded(Response response) {

		GSLoggerFactory.getLogger(this.getClass()).info("Executor handling ended, storing the result...");

		if (response == null || response.getEntity() == null) {
		    GSLoggerFactory.getLogger(this.getClass()).error("Empty response!");
		    return;
		}

		try {

		    Date endDate = new Date();

		    String objectName = message.getUserJobResultId();

		    if (objectName == null || objectName.isEmpty()) {
			objectName = "unnamed-result-" + System.currentTimeMillis() + ".dat";
		    }

		    DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();

		    DownloadStorage downloadStorage = downloadSetting.getDownloadStorage();

		    GSLoggerFactory.getLogger(this.getClass()).info("Uploading file to {} STARTED", downloadStorage.getLabel());

		    ResultStorage storage = null;
		    StorageUri resultStorageURI = message.getUserJobStorageURI();

		    switch (downloadStorage) {

		    case LOCAL_DOWNLOAD_STORAGE:

			storage = ResultStorageFactory.createLocalResultStorage(resultStorageURI);
			break;

		    case S3_DOWNLOAD_STORAGE:

			storage = ResultStorageFactory.createAmazonS3ResultStorage(resultStorageURI);
			break;
		    }

		    //
		    // Result storing
		    //
		    String get = storage.getStorageLocation(objectName);

		    GSLoggerFactory.getLogger(this.getClass()).info("Storing to {} STARTED", get);

		    javax.ws.rs.core.StreamingOutput streamingOutput = response.readEntity(javax.ws.rs.core.StreamingOutput.class);

		    File tmpFile = File.createTempFile(UserSchedulerWorker.this.getClass().getSimpleName(), ".tmp");
		    tmpFile.deleteOnExit();

		    FileOutputStream baos = new FileOutputStream(tmpFile);
		    streamingOutput.write(baos);
		    baos.close();

		    storage.store(objectName, tmpFile);

		    tmpFile.delete();

		    GSLoggerFactory.getLogger(this.getClass()).info("Uploading file to {} ENDED", downloadStorage.getLabel());

		    GSLoggerFactory.getLogger(this.getClass()).info("Storing to {} ENDED", get);

		    //
		    // Updates the status
		    //
		    status.setDataUri(get);

		} catch (Exception e) {

		    e.printStackTrace();
		    GSLoggerFactory.getLogger(this.getClass()).error("Storing failed: " + e.getMessage());

		    status.setErrorPhase();
		    status.addErrorMessage(e.getMessage());
		}
	    }
	};

	wrapperHandler.setMessageResponseMapper(handler.getMessageResponseMapper());
	wrapperHandler.setMessageResponseFormatter(handler.getMessageResponseFormatter());

	return wrapperHandler;
    }

    /**
     * @param handlerClass
     * @param mapperClass
     * @param formatterClass
     * @return
     * @throws GSException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ProfilerHandler createHandler(String handlerClass, String mapperClass, String formatterClass) throws GSException {

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

	return handler;
    }

    /**
     * @param handlerClass
     * @param mapperClass
     * @param formatterClass
     * @param message
     * @throws GSException
     */
    protected void checkInputs(String handlerClass, String mapperClass, String formatterClass, RequestMessage message) throws GSException {

	if (handlerClass == null || handlerClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing handler class from job data map");
	}

	if (mapperClass == null || mapperClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing mapper class from job data map");
	}

	if (formatterClass == null || formatterClass.length() == 0) {
	    throw createException(EXCEPTION_MISSING_CLASS, "Missing formatter class from job data map");
	}

	// String jobId = getSetting().getJobId();
	//
	// if (jobId == null || jobId.length() == 0) {
	// throw createException(EXCEPTION_MISSING_EXECUTION_ID_HINT, "Missing id from job data map");
	// }

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
    }

    @Override
    protected void storeJobStatus(SchedulerJobStatus status) throws GSException {

	ConfiguredDriverUtils.storeToPersistentStorage(status.getJobIdentifier(), status.getObject());
    }

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }

    /**
     * @param className
     * @return
     * @throws Exception
     */
    private Object createInstance(String className) throws Exception {

	GSLoggerFactory.getLogger(this.getClass()).info("Creating instance of class: " + className);

	Class<?> clazz = Class.forName(className);
	Object ret = clazz.newInstance();
	return ret;
    }

    /**
     * @param errorId
     * @param errorDescription
     * @return
     */
    private GSException createException(String errorId, String errorDescription) {

	return GSException.createException(getClass(), errorDescription, null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR,
		errorId);
    }

}
