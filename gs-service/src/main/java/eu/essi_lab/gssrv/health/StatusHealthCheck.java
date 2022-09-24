package eu.essi_lab.gssrv.health;

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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.status.core.CheckResultSet;
import com.indeed.status.core.CheckResultSystemReport;
import com.indeed.status.core.CheckStatus;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.SystemPropertyChecker;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.request.executor.discover.Distributor;
public class StatusHealthCheck {

    private final GSDependencyManager dependencyManager;
    private CheckResultSet resultSet;

    public static boolean START_CHECK_PASSED = false;

    private Logger logger = LoggerFactory.getLogger(StatusHealthCheck.class);

    public StatusHealthCheck() {

	dependencyManager = GSDependencyManager.getInstance();
    }

    /**
     * @return
     */
    public CheckResultSet status() {

	resultSet = dependencyManager.evaluate();

	try {
	    logger.info("System status {} with report {}", resultSet.getSystemStatus(), printResultSet(true));
	} catch (IOException e) {
	    logger.warn("Exception printing system status report");
	}

	return resultSet;
    }

    /**
     * @return
     */
    public boolean isHealthy() {
	return isHealthy(false);
    }

    /**
     * @return
     */
    public boolean isHealthy(boolean firstRun) {

	if (firstRun || START_CHECK_PASSED) {

	    // if profiler blockage
	    if (Profiler.everythingIsBlocked()) {
		GSLoggerFactory.getLogger(getClass()).info("Unhealthy, profiler is blocked!");
		return false;
	    }

	    // if more than 10 timeout errors during xml document reader happened, probably the node is not in a good
	    // state
	    XMLDocumentReaderCheck.check();
	    int xmlErrors = XMLDocumentReaderCheck.getErrorsCount();
	    GSLoggerFactory.getLogger(getClass()).info("Number of XML errors: {}", xmlErrors);
	    if (xmlErrors > 2) {
		GSLoggerFactory.getLogger(getClass()).info("Unhealthy, too many XML errors: {}", xmlErrors);
		return false;
	    }

	    // if more than 10 timeout errors during count have occurred, probably the node is not in a good state.
	    Integer distributorTimeouts = Distributor.getTimeoutErrorsDuringCount();
	    GSLoggerFactory.getLogger(getClass()).info("Number of distributor timeouts: {}", distributorTimeouts);
	    if (distributorTimeouts > 10) {
		GSLoggerFactory.getLogger(getClass()).info("Unhealthy, too many distributor timeouts: {}", distributorTimeouts);
		return false;
	    }

	    return status().getSystemStatus().isBetterThan(CheckStatus.OUTAGE);

	} else {
	    // at start only one gi suite starter thread should make health check and that should pass. Other threads
	    // will return false.
	    GSLoggerFactory.getLogger(getClass()).info("Not gi suite starter thread");
	    return false;

	}
    }

    /**
     * @param healthy
     * @param detail
     * @return
     */
    public String toJson(Boolean healthy, Boolean detail) {

	if (resultSet != null) {

	    try {
		return printResultSet(detail);
	    } catch (IOException e) {
		logger.warn("Can't parse Check Result System Report", e);
	    }

	}

	return "{\"healthy\" : " + healthy + "}";
    }

    /**
     * @param detail
     * @return
     * @throws IOException
     */
    private String printResultSet(boolean detail) throws IOException {

	CheckResultSystemReport report = resultSet.summarizeBySystemReporter(detail);

	Writer jsonWriter = new StringWriter();

	JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);

	new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(jsonGenerator, report);

	jsonGenerator.flush();

	return jsonWriter.toString();
    }
}
