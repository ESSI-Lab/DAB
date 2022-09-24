package eu.essi_lab.gssrv.health.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.indeed.status.core.Urgency;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GSConfigurationManager;
import static eu.essi_lab.configuration.sync.ConfigurationSync.ERR_ID_NODBMANAGER;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
import java.util.Date;
public class ConfClonerPingableComponent implements IGSHealthCheckPingableComponent {

    @Override
    public String getDescription() {
	return "Checks that the clone conf works properly";
    }

    @Override
    public Urgency getUrgency() {
	return Urgency.REQUIRED;
    }

    @Override
    public String getId() {
	return "ConfClonerPingableComponent";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {
	return true;
    }

    @Override
    public void ping() throws Exception {

	try {

	    Long nowts = new Date().getTime();

	    GSConfigurationManager manager = new GSConfigurationManager();

	    GSConfiguration c = manager.getConfiguration();

	    if (c == null) {
		// system not initialized
		return;
	    }

	    validateClonedConfiguration(c, nowts);

	} catch (GSException e) {

	    DefaultGSExceptionReader reader = new DefaultGSExceptionReader(e);
	    DefaultGSExceptionHandler handler = new DefaultGSExceptionHandler(reader);
	    DefaultGSExceptionLogger.log(handler);

	    if (!reader.getLastErrorIdentifier().equalsIgnoreCase(ERR_ID_NODBMANAGER)) {
		throw new Exception("Can't get configuration with GSException " + handler.createGSErrorCode());
	    }
	}
    }

    /**
     * @param cloned
     * @param initialTimeStamp
     * @throws Exception
     */
    void validateClonedConfiguration(GSConfiguration cloned, Long initialTimeStamp) throws Exception {

	Long delta = 5000L;

	Long ts = cloned.getTimeStamp();

	if (initialTimeStamp + delta - ts < 0L) {

	    throw new Exception(
		    "Reading cloned configuration check failed with bad timestamp now: " + initialTimeStamp + " conf ts: " + ts);

	}
    }
}
