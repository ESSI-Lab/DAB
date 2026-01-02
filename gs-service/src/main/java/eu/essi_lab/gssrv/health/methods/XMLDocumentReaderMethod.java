package eu.essi_lab.gssrv.health.methods;

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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class XMLDocumentReaderMethod implements GSPingMethod, Runnable {

    /**
     * 
     */
    private static final ExecutorService EXEC_SERVICE = Executors.newSingleThreadExecutor();

    /**
     * 
     */
    private static final int MAX_ERRORS_COUNT = 5;

    /**
     * 
     */
    private static final int WAIT_TIMEOUT_MILLIS = (int) TimeUnit.MINUTES.toMillis(2);

    /**
     * 
     */
    private static int errorsCount;

    @Override
    public void run() {

	TaskListExecutor<Boolean> tle = new TaskListExecutor<Boolean>(1);

	tle.addTask(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {

		try {
		    new XMLDocumentReader("<test/>");
		    return true;
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
		}

		return false;
	    }
	});

	List<Future<Boolean>> ret = tle.executeAndWait(WAIT_TIMEOUT_MILLIS);

	if (!ret.isEmpty()) {

	    try {
		Boolean result = ret.get(0).get();
		if (result) {
		    return;
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	    }
	}

	errorsCount++;
    }

    @Override
    public void ping() throws Exception {

	if (errorsCount >= MAX_ERRORS_COUNT) {

	    throw new Exception("Too many XML errors: " + errorsCount);
	}

	EXEC_SERVICE.execute(this);
    }

    @Override
    public String getDescription() {

	return "Verifies that no problems occur during XML documents creation";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	switch (mode) {
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	    return false;
	case BATCH:
	case MIXED:
	case FRONTEND:
	case ACCESS:
	case INTENSIVE:
	default:
	    return true;
	}
    }
}
