package eu.essi_lab.gssrv.conf.task.views;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View;

/**
 * This task can create views
 * 
 * @author boldrini
 */
public class ViewManagerTask extends AbstractCustomTask {

    // Usages expected
    //
    // task: create
    // class: fully.qualified.class.Name
    public enum TaskParameterKey {
	TASK("task:"), //
	CLASS("class:"), //
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
	GSLoggerFactory.getLogger(getClass()).info("View manager task STARTED");
	log(status, "View Manager task STARTED");

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
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for view manager task");
	    return;
	}
	String[] lines = settings.split("\n");
	String task = null;
	String className = null;

	for (String line : lines) {
	    SimpleEntry<TaskParameterKey, String> decoded = TaskParameterKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass())
			.error("unexpected settings for mysql init task. Expected: " + TaskParameterKey.getExpectedKeys());
		return;
	    }
	    switch (decoded.getKey()) {
	    case TASK:
		task = decoded.getValue();
		break;
	    case CLASS:
		className = decoded.getValue();
		break;
	    default:
		break;
	    }
	}

	Class<View> clazz = (Class<View>) Class.forName(className);
        View view = clazz.getDeclaredConstructor().newInstance();

	DatabaseWriter writer = DatabaseProviderFactory.getWriter(ConfigurationWrapper.getStorageInfo());
	writer.store(view);

	GSLoggerFactory.getLogger(getClass()).info("View manager task ENDED");
	log(status, "View manager task ENDED");
    }

    @Override
    public String getName() {

	return "View manager task";
    }
}
