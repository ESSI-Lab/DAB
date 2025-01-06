package eu.essi_lab.gssrv.health.methods;

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
import java.util.Properties;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;

/**
 * @author Fabrizio
 */
public class FreeMemoryMethod implements GSPingMethod {

    /**
     * 150 MB
     */
    private static final int DEFAULT_TRESHOLD = 150;

    @Override
    public void ping() throws Exception {

	int freeMemory = GSLogger.getFreeMemory();
	int treshold = readTreshold();

	if (freeMemory < treshold) {

	    throw new Exception("Free memory of " + freeMemory + " MB is under the treshold of " + treshold + " MB");
	}
    }

    @Override
    public String getDescription() {

	return "Checks that the free memory of the current task is over the treshold of " + readTreshold() + " MB";
    }

    /**
     * @return
     */
    private int readTreshold() {

	int treshold = DEFAULT_TRESHOLD;

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (keyValueOption.isPresent()) {

	    treshold = Integer
		    .valueOf(keyValueOption.get().getProperty("freeMemoryHealthCheckMethodTreshold", String.valueOf(DEFAULT_TRESHOLD)));
	}

	return treshold;
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
	    return false;
	case ACCESS:
	case INTENSIVE:
	case AUGMENTER:
	}
	return true;
    }
}
