package eu.essi_lab.jobs;

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

import eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD;
public class GSJobUtils {

    private GSJobUtils() {
	//nothing to do here
    }

    public static Date nextStartDate(IGSJob gsjob) {

	long startTime = gsjob.getStartDate().getTime();
	long now = System.currentTimeMillis();

	long interval = gsjob.getInterval();
	long nextStart = startTime;

	GS_JOB_INTERVAL_PERIOD period = gsjob.getIntervalPeriod();

	while (nextStart < now) {

	    switch (period) {
	    case MONTHS:
		nextStart += interval * 1000 * 60 * 60 * 24 * 30L;
		break;
	    case WEEKS:
		nextStart += interval * 1000 * 60 * 60 * 24 * 7L;
		break;
	    case DAYS:
		nextStart += interval * 1000 * 60 * 60 * 24L;
		break;
	    case HOURS:
		nextStart += interval * 1000 * 60 * 60L;
		break;
	    case MINUTES:
		nextStart += interval * 1000 * 60L;
		break;
	    case SECONDS:
		nextStart += interval * 1000L;
		break;
	    }
	}

	return new Date(nextStart);

    }
}
