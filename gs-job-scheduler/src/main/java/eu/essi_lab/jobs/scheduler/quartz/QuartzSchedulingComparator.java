package eu.essi_lab.jobs.scheduler.quartz;

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

import java.util.Date;

import org.quartz.Trigger;
import org.slf4j.Logger;

import eu.essi_lab.jobs.GSJobUtils;
import eu.essi_lab.jobs.IGSJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
public class QuartzSchedulingComparator {

    private transient Logger logger = GSLoggerFactory.getLogger(QuartzSchedulingComparator.class);

    /**
     * Returns true if: job and trigger have the same start date AND job and trigger have the same next fire date. It is not completely safe
     * but at the moment this is what I get from triggers.
     *
     * @param job
     * @param trigger
     * @return
     */
    public boolean hasSameSchedule(IGSJob job, Trigger trigger) {

	Date jStart = job.getStartDate();
	Date tStart = trigger.getStartTime();

	logger.trace("Job start at     {}", jStart);
	logger.trace("Trigger start at {}", tStart);

	Date sNextDate = GSJobUtils.nextStartDate(job);

	Date tNextDate = trigger.getNextFireTime();

	logger.trace("Job Next start at     {}", sNextDate);
	logger.trace("Trigger Next start at {}", tNextDate);

	if (!compare(jStart.getTime(), tStart.getTime())) {

	    logger.trace("Job and Trigger have different start times, check if this is due to the start date adaptation");

	    if (!compare(sNextDate.getTime(), tStart.getTime()))
		return false;

	}

	logger.trace("Same start recognized, checking next fire");

	return compare(sNextDate.getTime(), tNextDate.getTime());

    }

    private boolean compare(Long millis1, Long millis2) {

	boolean result = millis1.equals(millis2);

	logger.trace("Comparing {} {} result is {}", millis1, millis2, result);

	return result;

    }

}
