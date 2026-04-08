package eu.essi_lab.services.impl;

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

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.message.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class TestService extends AbstractManagedService {

    private boolean running;

    /**
     *
     */
    public TestService() {
    }

    @Override
    public void start() {

	running = true;

	publish(MessageChannel.MessageLevel.INFO, "Started service: " + getId());

	while (running) {

	    GSLoggerFactory.getLogger(getClass()).info("*** [ Running service: {} ] ***", getId());

	    getSetting().getServiceOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Options: {}", o));
	    getSetting().getKeyValueOptions().ifPresent(o -> GSLoggerFactory.getLogger(getClass()).info("Key-value options: {}", o));

	    publish(MessageChannel.MessageLevel.INFO, "Running service: " + getId());

	    publish(getId(), ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds());

	    List<Map.Entry<String, String>> store = read();

	    for (Map.Entry<String, String> stringStringEntry : store) {

		GSLoggerFactory.getLogger(getClass()).info("Key: {}, Value: {}", stringStringEntry.getKey(), stringStringEntry.getValue());
	    }

	    try {
		Thread.sleep(Duration.of(5, ChronoUnit.SECONDS));
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    @Override
    public void stop() {

	running = false;

	publish(MessageChannel.MessageLevel.INFO, "Stopped service: " + getId());
    }

}
