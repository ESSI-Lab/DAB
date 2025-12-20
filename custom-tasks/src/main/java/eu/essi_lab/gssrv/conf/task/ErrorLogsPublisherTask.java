package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.quartz.JobExecutionContext;

import com.google.common.base.Charsets;

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.configuration.ClusterType;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.ErrorLogListener;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ErrorLogsPublisherTask extends AbstractCustomTask implements ErrorLogListener {

    /**
     *
     */
    private static final File LOGS_FOLDER = new File(IOStreamUtils.getUserTempDirectory() + File.separator + "errorLogs");

    /**
     *
     */
    private static final String ERROR_LOGS_MAP_PATH = LOGS_FOLDER + File.separator + "errorLogsMap";

    /**
     *
     */
    private static final String ERROR_LOGS_BUCKET_NAME = "gip-error-logs";

    /**
     * Maximum number of error logs for a given caller
     */
    private static final int MAX_OCCURRENCES_BY_CLASS = 10;

    @Override
    public void errorOccurred(Class<?> clazz, String msg, Optional<Throwable> throwable) throws Exception {

	synchronized (GSLoggerFactory.class) {

	    manager = getS3TransferManager();

	    if (!LOGS_FOLDER.exists()) {

		createLogFolder();
	    }

	    HashMap<String, Integer> map = deserializeMap();

	    int occurrences = map.getOrDefault(clazz.getCanonicalName(), 0);

	    if (occurrences < MAX_OCCURRENCES_BY_CLASS) {

		map.put(clazz.getCanonicalName(), ++occurrences);

		serializeMap(map);

		File logFile = new File(LOGS_FOLDER, getLogFileName());

		String newLog = createLog(clazz, msg, throwable);

		if (!logFile.exists()) {

		    GSLoggerFactory.getLogger(getClass()).warn("Creating error log file: " + logFile.getAbsolutePath());

		    newLog = "- Cluster: [" + ClusterType.get().getLabel() + "]\n" + //
			    "- Execution mode: [" + ExecutionMode.get() + "]\n\n" + newLog;

		    writeLogFile(manager, logFile, newLog);

		} else {

		    String currentLog = readLogFile(logFile);

		    currentLog += newLog;

		    writeLogFile(manager, logFile, currentLog);
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Too many error log occurrences of caller " + clazz.getCanonicalName());
	    }
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	synchronized (GSLoggerFactory.class) {

	    manager = getS3TransferManager();

	    if (!LOGS_FOLDER.exists()) {

		createLogFolder();
	    }

	    Optional<String> taskOptions = readTaskOptions(context);

	    boolean daysBeforeLogs = taskOptions.isPresent() && taskOptions.get().contains("true");

	    List<File> files = listLogFiles(manager, daysBeforeLogs);

	    for (File logFile : files) {

		GSLoggerFactory.getLogger(getClass()).debug("Error log file found: " + logFile.getAbsolutePath());

		String currentLog = readLogFile(logFile);

		log(status, currentLog, false);

		if (manager.isPresent()) {

		    manager.get().deleteObject(ERROR_LOGS_BUCKET_NAME, logFile.getName());

		    GSLoggerFactory.getLogger(getClass()).debug("Error log file deleted from s3: " + logFile.getAbsolutePath());
		}

		if (!logFile.delete()) {

		    logFile.deleteOnExit();

		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("Error log file deleted from FS: " + logFile.getAbsolutePath());
		}
	    }

	    if (!files.isEmpty()) {

		File map = new File(ERROR_LOGS_MAP_PATH);

		if (!map.delete()) {

		    map.deleteOnExit();

		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("Error map file deleted");
		}
	    } else {

		log(status, "No error logs file found");
	    }
	}
    }

    /**
     * @return
     * @throws Exception
     */
    private HashMap<String, Integer> deserializeMap() throws Exception {

	if (!new File(ERROR_LOGS_MAP_PATH).exists()) {

	    serializeMap(new HashMap<>());
	}

	FileInputStream fis = new FileInputStream(ERROR_LOGS_MAP_PATH);
	ObjectInputStream ois = new ObjectInputStream(fis);

	@SuppressWarnings("unchecked")
	HashMap<String, Integer> map = (HashMap<String, Integer>) ois.readObject();
	ois.close();

	return map;
    }

    /**
     * @throws Exception
     */
    private void createLogFolder() throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Creating error logs folder STARTED");

	boolean created = LOGS_FOLDER.mkdirs();
	if (!created) {

	    throw new Exception("Unable to create error log folder");
	}

	GSLoggerFactory.getLogger(getClass()).debug("Error logs folder: " + LOGS_FOLDER.getAbsolutePath());

	GSLoggerFactory.getLogger(getClass()).debug("Creating error logs folder ENDED");

    }

    /**
     * @throws Exception
     */
    private void serializeMap(HashMap<String, Integer> errorLogsMap) throws Exception {

	File file = new File(ERROR_LOGS_MAP_PATH);
	if (!file.exists()) {

	    boolean created = file.createNewFile();
	    if (!created) {

		throw new Exception("Unable to create error log map");
	    }
	}

	FileOutputStream fos = new FileOutputStream(ERROR_LOGS_MAP_PATH);
	ObjectOutputStream oos = new ObjectOutputStream(fos);
	oos.writeObject(errorLogsMap);
	oos.close();
    }

    /**
     * @param manager
     * @param daysBeforeLogs
     * @return
     */
    private List<File> listLogFiles(Optional<S3TransferWrapper> manager, boolean daysBeforeLogs) {

	if (manager.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Downloading error log files from S3 STARTED");

	    manager.get().listObjectsSummaries(ERROR_LOGS_BUCKET_NAME).

		    stream().//
		    filter(s -> daysBeforeLogs ? !s.key().contains(ISO8601DateTimeUtils.getISO8601Date()) : true).//
		    forEach(summary -> {

		manager.get().download(ERROR_LOGS_BUCKET_NAME, summary.key(), new File(LOGS_FOLDER, summary.key()));
	    });

	    GSLoggerFactory.getLogger(getClass()).debug("Downloading error log files from S3 ENDED");
	}

	// the filter also excludes the map inside the FS folder
	return Arrays.asList(LOGS_FOLDER.listFiles(f -> f.getName().endsWith("txt") && //
		daysBeforeLogs ? !f.getName().contains(ISO8601DateTimeUtils.getISO8601Date()) : true));
    }

    /**
     * @param manager
     * @param logFile
     * @param log
     * @throws IOException
     */
    private void writeLogFile(Optional<S3TransferWrapper> manager, File logFile, String log) throws IOException {

	BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
	writer.write(log);

	writer.close();

	if (manager.isPresent()) {

	    manager.get().uploadFile(logFile.getAbsolutePath(), ERROR_LOGS_BUCKET_NAME);
	}
    }

    /**
     * @param logFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String readLogFile(File logFile) throws FileNotFoundException {

	Scanner sc = new Scanner(logFile);
	StringBuilder builder = new StringBuilder();

	while (sc.hasNextLine()) {

	    builder.append(sc.nextLine() + "\n");
	}

	sc.close();

	return builder.toString();
    }

    /**
     * @return
     */
    private String getLogFileName() {

	return ExecutionMode.get() + "_" + HostNamePropertyUtils.getHostNameProperty() + "_" + ISO8601DateTimeUtils.getISO8601Date()
		+ ".txt";
    }

    /**
     * @param clazz
     * @param msg
     * @param throwable
     * @return
     */
    private String createLog(Class<?> clazz, String msg, Optional<Throwable> throwable) {

	StringBuilder builder = new StringBuilder();

	builder.append(HostNamePropertyUtils.getHostNameProperty() + " - ");
	builder.append("[" + ISO8601DateTimeUtils.getISO8601DateTime() + "] ");

	if (throwable.isPresent() && throwable.get() instanceof GSException) {

	    GSException ex = (GSException) throwable.get();

	    List<ErrorInfo> infoList = ex.getErrorInfoList();

	    for (ErrorInfo errorInfo : infoList) {

		builder.append(errorInfo.toString());

		if (errorInfo.getCause() != null) {

		    ByteArrayOutputStream stream = new ByteArrayOutputStream();

		    errorInfo.getCause().printStackTrace(new PrintStream(stream));

		    builder.append(stream.toString(Charsets.UTF_8));
		}

		// builder.append("\n");
	    }

	} else {

	    builder.append("[" + clazz.getCanonicalName() + "] ");
	    builder.append(msg + "\n");

	    if (throwable.isPresent()) {

		Throwable thr = throwable.get();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		thr.printStackTrace(new PrintStream(stream));

		builder.append(stream.toString(Charsets.UTF_8));
	    }

	}

	// builder.append("\n");

	return builder.toString();
    }

    @Override
    public String getName() {

	return "Error logs publisher task";
    }

    /**
     * @return
     */
    public boolean clearMessagesBeforeStoreStatus() {

	return true;
    }
}
