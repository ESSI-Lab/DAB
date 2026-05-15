package eu.essi_lab.gssrv.conf.task.trigger;

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.quartz.JobExecutionContext;

import java.util.EnumMap;
import java.util.Optional;

public class COGS3Task extends AbstractCustomTask {



    public enum COGS3TaskTaskOptions implements OptionsKey {
	SOURCE_URL, BUCKET_NAME, VAR_NAMES;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	log(status, "COGS3Task STARTED");

	//CustomTaskSetting setting = retrieveSetting(context);
	//Optional<String> taskOptions = setting.getTaskOptions();

	//String bucketName = "s3-demo-geotiff"; // Default
	//String[] variables = {"shiwe", "2t", "2r", "utci"};

	Optional<EnumMap<COGS3Task.COGS3TaskTaskOptions, String>> taskOptions = readTaskOptions(context, COGS3Task.COGS3TaskTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified");
	    return;
	}
	String bucketName = taskOptions.get().get(COGS3TaskTaskOptions.BUCKET_NAME);
	if (bucketName == null && bucketName == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No bucket name option specified");
	    return;
	}

	String varName = taskOptions.get().get(COGS3TaskTaskOptions.VAR_NAMES);
	if (varName == null && varName == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No variable names option specified");
	    return;
	}

	String sourceURL = taskOptions.get().get(COGS3TaskTaskOptions.SOURCE_URL);
	if (sourceURL == null && sourceURL == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No Geotiff url option specified");
	    return;
	}
	String[] variables = varName.split(",");


	Optional<S3TransferWrapper> manager = getS3TransferManager();

	if (manager.isPresent()) {
	    COGS3SyncProcessor processor = new COGS3SyncProcessor(manager.get(), bucketName, variables, sourceURL);
	    processor.execute();
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("S3 manager not present");
	}

	log(status, "COGS3Task ENDED");
    }

    @Override
    public String getName() {
	return "COG S3Task";
    }
}
