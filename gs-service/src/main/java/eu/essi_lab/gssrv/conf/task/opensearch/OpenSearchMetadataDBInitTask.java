package eu.essi_lab.gssrv.conf.task.opensearch;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;

/**
 * This task creates metadata db indexes
 * 
 * @author boldrini
 */
public class OpenSearchMetadataDBInitTask extends AbstractCustomTask {

    // Usage expected
    // url: https://my-host/opensearch");
    // database: db-name
    // user: admin
    // password: my-pass
    // identifier: config-id
    // type: osl
    public enum TaskParameterKey {
	URL("url:"), //
	DATABASE("database:"), //
	USER("user:"), //
	PASSWORD("password:"), //
	IDENTIFIER("identifier:"), //
	TYPE("type:"), //
	;

	private String id;

	TaskParameterKey(String id) {
	    this.id = id;
	}

	@Override
	public String toString() {
	    return id;
	}

	public static String getExpectedKeys() {
	    String ret = "";
	    for (TaskParameterKey key : values()) {
		ret += key.toString() + " ";
	    }
	    return ret;
	}

	public static SimpleEntry<TaskParameterKey, String> decodeLine(String line) {
	    for (TaskParameterKey key : values()) {
		if (line.toLowerCase().startsWith(key.toString().toLowerCase())) {
		    SimpleEntry<TaskParameterKey, String> ret = new SimpleEntry<>(key, line.substring(key.toString().length()).trim());
		    return ret;
		}
	    }
	    return null;
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("DB Init task STARTED");
	log(status, "DB Init task STARTED");

	// SETTINGS RETRIEVAL
	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String settings = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		settings = options;
	    }
	}
	if (settings == null) {
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for db init task");
	    return;
	}
	String[] lines = settings.split("\n");
	String url = null;
	String database = null;
	String user = null;
	String password = null;
	String identifier = null;
	String type = null;
	for (String line : lines) {
	    SimpleEntry<TaskParameterKey, String> decoded = TaskParameterKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass())
			.error("unexpected settings for db init task. Expected: " + TaskParameterKey.getExpectedKeys());
		return;
	    }
	    switch (decoded.getKey()) {
	    case URL:
		url = decoded.getValue();
		break;
	    case DATABASE:
		database = decoded.getValue();
		break;
	    case USER:
		user = decoded.getValue();
		break;
	    case PASSWORD:
		password = decoded.getValue();
		break;
	    case IDENTIFIER:
		identifier = decoded.getValue();
		break;
	    case TYPE:
		type = decoded.getValue();
		break;
	    default:
		break;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Connecting to {} with user {}", url, user);

	StorageInfo osStorageInfo = new StorageInfo(url);
	osStorageInfo.setName(database);
	osStorageInfo.setUser(user);
	osStorageInfo.setPassword(password);
	osStorageInfo.setIdentifier(identifier);
	osStorageInfo.setType(OpenSearchServiceType.decode(type).getProtocol());
	System.setProperty("initIndexes", "true");
	OpenSearchDatabase db = new OpenSearchDatabase();
	db.initialize(osStorageInfo);

	GSLoggerFactory.getLogger(getClass()).info("DB init task ENDED");
	log(status, "DB init task ENDED");
    }

    @Override
    public String getName() {

	return "OpenSearch Metadata DB init task";
    }
}
