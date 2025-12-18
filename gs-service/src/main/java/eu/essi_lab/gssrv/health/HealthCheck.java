package eu.essi_lab.gssrv.health;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.status.core.CheckResultSet;
import com.indeed.status.core.CheckResultSystemReport;
import com.indeed.status.core.CheckStatus;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.ConfiguredSMTPClient;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.configuration.ClusterType;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class HealthCheck {

    private CheckResultSet resultSet;

    /**
     * 
     */
    public static boolean startCheckPassed;

    /**
     * 
     */
    public HealthCheck() {

    }

    /**
     * @return
     */
    public CheckResultSet status() {

	resultSet = GSDependencyManager.getInstance().evaluate();

	try {

	    CheckStatus status = resultSet.getSystemStatus();
	    if (status != CheckStatus.OK) {

		LoggerFactory.getLogger(getClass()).warn("System status: {}", resultSet.getSystemStatus());
		LoggerFactory.getLogger(getClass()).warn("Health check report:\n {}", printResultSet(true));

		Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
		if (keyValueOption.isPresent()) {

		    Boolean sendEmail = Boolean.valueOf(keyValueOption.get().getOrDefault(KeyValueOptionKeys.SEND_HEALTH_CHECK_REPORT.getLabel(), "true").toString());
		    if (sendEmail) {

			ConfiguredSMTPClient.sendEmail("[GS-REPORT][HEALTH-CHECK-REPORT]", printResultSet(true));
		    }
		}
	    }
	} catch (IOException e) {
	    LoggerFactory.getLogger(getClass()).warn("Exception printing Health check status report");
	    LoggerFactory.getLogger(getClass()).warn(e.getMessage());
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

	if (firstRun || startCheckPassed) {

	    return status().getSystemStatus().isBetterThan(CheckStatus.OUTAGE);

	} else {
	    //
	    // at start only one DAB starter thread should make health check and that should pass. Other threads
	    // will return false.
	    //
	    GSLoggerFactory.getLogger(getClass()).info("Not DAB starter thread");
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
		GSLoggerFactory.getLogger(getClass()).error(e);
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

	JSONObject jsonReport = new JSONObject(jsonWriter.toString());
	jsonReport.put("cluster", ClusterType.get().getLabel());
	jsonReport.put("executionMode", ExecutionMode.get());

	return jsonReport.toString(3);
    }
}
