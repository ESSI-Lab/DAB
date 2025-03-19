/**
 * 
 */
package eu.essi_lab.gssrv.conf.task;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 * @author boldrini
 */
public class SourceAdderTask extends AbstractCustomTask {

    public enum SourceType {
	SIMPLE_CUAHSI_SOURCES, CUAHSI_HIS_CENTRAL
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	Optional<String> taskOptions = readTaskOptions(context);

	/**
	 * Three options are present:
	 * type
	 * endpoint
	 * id
	 * offset 1 (minutes to wait before starting the tasks)
	 * offset 2 (minutes between the start of each task)
	 * schedule (yes or no)
	 * sourceDeployment
	 * Example: (it will expand the his-central at the given endpoint, scheduling the related hydroserver sources at
	 * interval of 2 minutes from each other. The sources id will have the gfhc prefix)
	 * CUAHSI_HIS_CENTRAL
	 * https://hiscentral.cuahsi.org/webservices/hiscentral.asmx
	 * cuahsi-hc
	 * 0
	 * 2
	 * no
	 * whos
	 */

	String type = null;
	String endpoint = null;
	String id = null;
	Integer offset1 = null;
	Integer offset2 = null;
	String schedule = null;
	String deployment = null;

	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		if (options.contains("\n")) {
		    String[] split = options.split("\n");
		    if (split.length < 7) {
			GSLoggerFactory.getLogger(getClass()).error("Missing options for this task");
			return;
		    }
		    type = split[0].trim();
		    endpoint = split[1].trim();
		    id = split[2].trim();
		    offset1 = Integer.parseInt(split[3].trim());
		    offset2 = Integer.parseInt(split[4].trim());
		    schedule = split[5].trim();
		    deployment = split[6].trim();
		}
	    }
	}
	if (type == null || endpoint == null || id == null || offset1 == null || offset2 == null || schedule == null
		|| deployment == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Missing options for this task");
	    return;
	}

	SourceType sourceType = null;
	String options = "";
	for (SourceType t : SourceType.values()) {
	    if (t.toString().equals(type)) {
		sourceType = t;
	    } else {
		options += t.toString() + " ";
	    }
	}
	if (sourceType == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Wrong source type for this task, possible options are: {}", options);
	}
	SourceFinder finder = null;
	switch (sourceType) {
	case SIMPLE_CUAHSI_SOURCES:
	    finder = new SimpleCUAHSISourceFinder();
	    break;
	case CUAHSI_HIS_CENTRAL:
	    finder = new CUAHSISourceFinder();
	    break;
	default:
	    GSLoggerFactory.getLogger(getClass()).error("This should not happen");
	    return;
	}

	List<HarvestingSetting> sources = finder.getSources(endpoint, id);

	for (HarvestingSetting source : sources) {
	    List<String> deployments = source.getSelectedAccessorSetting().getGSSourceSetting().getSourceDeployment();
	    String deps[];
	    if (deployment.contains(",")) {
		deps = deployment.split(",");
	    } else {
		deps = new String[] { deployment };
	    }
	    for (String dep : deps) {
		if (!deployments.contains(dep)) {
		    source.getSelectedAccessorSetting().getGSSourceSetting().addSourceDeployment(dep);
		}
	    }

	}

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	//
	//
	//

	ConfigurationSource configurationSource = configuration.getSource();
	configurationSource.backup();

	//
	//
	//

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	//
	//
	//

	//
	// scheduling
	//

	boolean bigSet = false;
	if (sources.size() > 30) {
	    // the first sources in a big set starts together
	    bigSet = true;
	}

	for (int i = 0; i < sources.size(); i++) {

	    HarvestingSetting source = sources.get(i);

	    String sourceId = source.getSelectedAccessorSetting().getSource().getUniqueIdentifier();

	    Optional<HarvestingSetting> optional = ConfigurationWrapper.//
		    getHarvestingSettings().//
		    stream().//
		    filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).findFirst();

	    if (schedule.equals("yes")) {
		Scheduling scheduling = source.getScheduling();
		scheduling.setEnabled(true);
		scheduling.setRunIndefinitely();
		scheduling.setRepeatInterval(1000, TimeUnit.DAYS);

		ZonedDateTime now = ZonedDateTime.now();

		// the start offset
		int offset = offset1;

		// the first n sources in a big set starts together (hack for CUAHSI HIS-CENTRAL)
		if (bigSet) {
		    int n = 60;
		    if (i < n) {
			// the first sources start together
		    } else {
			// then the other sources at regular intervals
			offset = offset + (i - n) * offset2;
		    }
		} else {
		    offset = offset + i * offset2;
		}
		now = now.plusMinutes(offset);

		ZonedDateTime tokyoTime = now.withZoneSameInstant(ZoneId.of(schedulerSetting.getUserDateTimeZone().getID())); // Format

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

		String formattedDate = tokyoTime.format(formatter);

		scheduling.setStartTime(formattedDate);

		scheduler.schedule(source);

	    }
	    SelectionUtils.deepClean(source);
	    configuration.put(source);
	    GSLoggerFactory.getLogger(getClass()).info("Added source: {}", source.getName());

	}

	//
	//
	//

	configuration.flush();
    }

    @Override
    public String getName() {

	return "Source adder task";
    }

    public static void main(String[] args) {

	Date startDate = new Date(System.currentTimeMillis() + 5 * TimeUnit.MINUTES.toMillis(2));

	// String iso8601Date = ISO8601DateTimeUtils.getISO8601DateTime(startDate);

	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
	cal.setTime(startDate);

	startDate = cal.getTime();

	System.out.println(startDate);
    }

}
