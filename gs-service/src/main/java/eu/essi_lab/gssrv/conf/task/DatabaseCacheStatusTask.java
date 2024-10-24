package eu.essi_lab.gssrv.conf.task;

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

import java.util.Arrays;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import com.marklogic.xcc.ResultSequence;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class DatabaseCacheStatusTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Cache status task STARTED");

	CustomTaskSetting setting = retrieveSetting(context);
	Optional<String> taskOptions = setting.getTaskOptions();

	if (taskOptions.isPresent()) {

	    String option = taskOptions.get();

	    String[] hosts = option.replace("hosts:", "").trim().split(",");

	    log(status, "\nProvided hosts: " + Arrays.asList(hosts));

	    StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();

	    DatabaseReader dbReader = DatabaseProviderFactory.getDatabaseReader(databaseURI);
	    MarkLogicWrapper wrapper = ((MarkLogicDatabase) dbReader.getDatabase()).getWrapper();

	    ResultSequence resultSequence = wrapper.submit(getQuery(hosts));

	    String cacheStatus = resultSequence.asString();

	    log(status, "\nCache status: \n" + cacheStatus);

	} else {

	    log(status, "Host not provided");
	}

	log(status, "Cache status task ENDED");
    }

    /**
     * @param hosts
     * @return
     */
    private String getQuery(String... hosts) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\";\n";
	query += "import module namespace admin = \"http://marklogic.com/xdmp/admin\" at \"/MarkLogic/admin.xqy\";\n";

	query += "let $config := admin:get-configuration()\n";
	query += "let $host-id-1 :=  admin:host-get-id($config, '" + hosts[0] + "')\n";
	query += "let $host-id-2 :=  admin:host-get-id($config, '" + hosts[1] + "')\n";
	query += "let $host-id-3 :=  admin:host-get-id($config, '" + hosts[2] + "')\n";

	query += "return <cache-status>\n";
	query += "<node-1>{xdmp:cache-status( $host-id-1 )}</node-1>\n";
	query += "<node-2>{xdmp:cache-status( $host-id-2 )}</node-2>\n";
	query += "<node-3>{xdmp:cache-status( $host-id-3 )}</node-3>\n";
	query += "</cache-status>\n";

	return query;
    }

    @Override
    public String getName() {

	return "Database cache status";
    }
}
