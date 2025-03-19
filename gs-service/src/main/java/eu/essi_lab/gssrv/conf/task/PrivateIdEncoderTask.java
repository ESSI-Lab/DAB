package eu.essi_lab.gssrv.conf.task;

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

import java.text.DecimalFormat;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import com.marklogic.xcc.ResultSequence;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class PrivateIdEncoderTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Private identifier encoder task STARTED");

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);
	MarkLogicWrapper wrapper = ((MarkLogicDatabase) dbReader.getDatabase()).getWrapper();

	Optional<String> taskOptions = readTaskOptions(context);

	if (!taskOptions.isPresent()) {

	    log(status, "Required options not provided, unable to perform task");

	    return;
	}

	String[] options = taskOptions.get().split("\n");

	String targetSource = null;
	String targetDir = null;
	int step = 10;
	int index = 0;

	try {

	    targetSource = options[0];
	    targetDir = options[1];
	    step = Integer.valueOf(options[2]);
	    index = Integer.valueOf(options[3]);

	} catch (Exception ex) {

	    log(status, "Error occurred during options parsing: " + ex.getMessage());
	    return;
	}

	String configFolder = databaseURI.getIdentifier();

	String directoryUri = "/" + configFolder + "_" + targetSource + "-" + targetDir + "/";

	log(status, "Target source: " + targetSource);
	log(status, "Target dir: " + targetDir);
	log(status, "Directory uri: " + directoryUri);
	log(status, "Step: " + step);
	log(status, "Start: " + index);

	//
	//
	//

	log(status, "Count records to update STARTED");

	ResultSequence resultSequence = wrapper.submit(getCountQuery(directoryUri));

	int numberOfRecords = Integer.valueOf(resultSequence.asString());

	log(status, "Records found: " + numberOfRecords);

	log(status, "Count records to update ENDED");

	if (numberOfRecords == 0) {

	    log(status, "No records to update found, exit!");

	    return;
	}

	//
	//
	//

	log(status, "Updating records STARTED");

	DecimalFormat decimalFormat = new DecimalFormat();
	decimalFormat.setMaximumFractionDigits(2);

	for (; index < numberOfRecords;) {

	    String query = getUpdateQuery(index, directoryUri, step);

	    wrapper.submit(query);

	    index += step;

	    double percentage = (((double) index) / numberOfRecords) * 100;

	    GSLoggerFactory.getLogger(getClass())
		    .debug("Updated records: " + index + "/" + numberOfRecords + " - [" + decimalFormat.format(percentage) + " %]");

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Task CANCELED");

		status.setPhase(JobPhase.CANCELED);
		break;
	    }
	}

	log(status, "Updating records ENDED");

	//
	//
	//

	log(status, "Private identifier encoder task ENDED");
    }

    /**
     * @param index
     * @param directoryUri
     * @param step
     * @return
     */
    private String getUpdateQuery(int index, String directoryUri, int step) {

	String query = "xquery version \"1.0-ml\";\n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "for $x in cts:search(doc(),\n";

	query += " cts:directory-query('" + directoryUri + "', \"infinity\"), (\"unfiltered\",\"score-simple\"),0)[" + index + " to "
		+ (index + step) + "]\n";

	query += "let $decodedPrivateId := xdmp:url-decode($x//gs:privateId/text())\n";

	query += "let $encodedPrivateId := encode-for-uri($decodedPrivateId)\n";

	query += "return xdmp:node-replace( $x//gs:privateId, <gs:privateId>{$encodedPrivateId}</gs:privateId>)";

	return query;
    }

    /**
     * @param directoryUri
     * @return
     */
    private String getCountQuery(String directoryUri) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "let $query := cts:directory-query('" + directoryUri + "', \"infinity\")\n";

	query += "return xdmp:estimate(cts:search(doc(), $query,(\"unfiltered\",\"score-simple\"),0) )\n";

	return query;
    }

    @Override
    public String getName() {

	return "Private identifier encoder task";
    }
}
