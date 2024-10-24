package eu.essi_lab.gssrv.health.methods;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;
import java.util.Properties;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class TaskAgeMethod implements GSPingMethod {

    /**
     * 
     */
    private static final int DEFAULT_TIME_THRESHOLD = 30; // minutes

    private static Long creationDate;

    @Override
    public void ping() throws Exception {

	int treshold = readTreshold();

	if (creationDate == null) {

	    creationDate = new Date().getTime();

	} else {

	    long now = new Date().getTime();

	    long taskAge = (now - creationDate) / 60000;

	    //
	    // adding random minutes to the treshold in order to avoid
	    // killing too many tasks with very similar age
	    //
	    treshold += (int) (Math.random() * 10);

	    if (taskAge > treshold) {

		GSLoggerFactory.getLogger(getClass()).warn(//
			"Task age of {} minutes is longer than the treshold of {} minutes", //
			taskAge, //
			treshold);

		throw new Exception("Task age of " + taskAge + " minutes is longer than the treshold of " + treshold + " minutes");
	    }
	}
    }

    /**
     * @return
     */
    private int readTreshold() {

	int treshold = DEFAULT_TIME_THRESHOLD;

	Optional<Properties> option = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (option.isPresent()) {

	    treshold = Integer
		    .valueOf(option.get().getProperty("taskAgeHealthCheckMethodTreshold", String.valueOf(DEFAULT_TIME_THRESHOLD)));
	}

	return treshold;
    }

    @Override
    public String getDescription() {

	return "Checks that the current task age is no longer than " + readTreshold() + " minutes (plus some random minutes)";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	switch (mode) {
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	case BATCH:
	case MIXED:
	case FRONTEND:
	case BULK:
	case AUGMENTER:
	    return false;
	case ACCESS:
	case INTENSIVE:
	}
	return true;
    }

}
