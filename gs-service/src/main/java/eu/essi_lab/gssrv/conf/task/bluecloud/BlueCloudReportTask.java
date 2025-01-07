package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini roncella
 */
public class BlueCloudReportTask extends AbstractCustomTask {

	/**
	 * 
	 */
	public BlueCloudReportTask() {

	}

	@Override
	public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

		log(status, "BlueCloud report task STARTED");

		CustomTaskSetting setting = retrieveSetting(context);

		Optional<String> taskOptions = setting.getTaskOptions();

		String[] views = null;
		Integer maxRecords = null;

		if (taskOptions.isPresent()) {

			try {

				String[] options = taskOptions.get().split("\n");
				//
				// view1;view2
				// 200
				//

				if (options.length > 0) {
					views = options[0].split(";");
				}
				if (options.length > 1) {
					maxRecords = Integer.valueOf(options[1]); // max records
					if (maxRecords == -1) {
						maxRecords = null;
					}
				}

			} catch (Exception ex) {
				GSLoggerFactory.getLogger(getClass()).error("Error parsing task options");
			}
		} else {
			GSLoggerFactory.getLogger(getClass()).info("No task options");
		}

		Optional<S3TransferWrapper> manager = getS3TransferManager();

		if (manager.isPresent()) {

			new BLUECloudReport(manager.get(), true, maxRecords, views);

		} else {
			GSLoggerFactory.getLogger(getClass()).error("S3 manager not present");
		}

		log(status, "BlueCloud report task ENDED");
	}

	@Override
	public String getName() {
		return "BlueCloud report task";
	}

}
