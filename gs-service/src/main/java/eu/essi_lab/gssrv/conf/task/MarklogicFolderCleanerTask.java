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
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarklogicFolderCleanerTask extends AbstractCustomTask {

    /**
     * 
     */
    private static final int STEP = 10000;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Big folders cleaner task STARTED");

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);
	MarkLogicWrapper wrapper = ((MarkLogicDatabase) dbReader.getDatabase()).getWrapper();

	CustomTaskSetting setting = retrieveSetting(context);

	Optional<String> taskOptions = setting.getTaskOptions();

	if (!taskOptions.isPresent()) {

	    log(status, "Required options not provided, unable to perform task");

	    return;
	}

	String[] options = taskOptions.get().split("\n");

	String targetSource = null;
	String targetDir = null;
	boolean removeDirectory = false;

	try {

	    targetSource = options[0];
	    targetDir = options[1];
	    removeDirectory = Boolean.valueOf(options[2]);

	} catch (Exception ex) {

	    log(status, "Error occurred during options parsing: " + ex.getMessage());
	    return;
	}

	String configFolder = databaseURI.getIdentifier();

	String directoryUri = "/" + configFolder + "_" + targetSource + "-" + targetDir + "/";

	log(status, "Target source: " + targetSource);
	log(status, "Target dir: " + targetDir);
	log(status, "Directory uri: " + directoryUri);

	//
	//
	//

	log(status, "Count records to delete STARTED");

	ResultSequence resultSequence = wrapper.submit(getCountQuery(directoryUri));

	int count = Integer.valueOf(resultSequence.asString());

	log(status, "Records found: " + count);

	log(status, "Count records to delete ENDED");

	if (count == 0) {

	    log(status, "No records to remove found, exit!");

	    return;
	}

	//
	//
	//

	log(status, "Deleting records STARTED");

	int counter = 0;

	DecimalFormat decimalFormat = new DecimalFormat();
	decimalFormat.setMaximumFractionDigits(2);

	for (; counter < count;) {

	    String query = getRemoveQuery(directoryUri);

	    wrapper.submit(query);

	    counter += STEP;

	    double percentage = (((double) counter) / count) * 100;

	    if (count > STEP && (int) percentage % 5 == 0) {

		log(status, "Removed records: " + counter + "/" + count + " - [" + decimalFormat.format(percentage) + " %]");
	    }
	}

	log(status, "Deleting records ENDED");

	//
	//
	//

	if (removeDirectory) {

	    log(status, "Removing directory STARTED");

	    String query = "xdmp:directory-delete('" + directoryUri + "')";

	    wrapper.submit(query);

	    log(status, "Removing directory ENDED");
	}

	//
	//
	//

	log(status, "Big folders cleaner task ENDED");
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

    private String getRemoveQuery(String directoryUri) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\";\n";
	query += "let $var := '" + directoryUri + "'\n";
	query += "for $x in cts:uris(\n";
	query += "'" + directoryUri + "',(), \n";
	query += "cts:directory-query($var,\n";
	query += "'infinity'))[0 to " + STEP + "]\n";
	query += "return xdmp:document-delete($x)";

	return query;
    }

    @Override
    public String getName() {

	return "Big folders cleaner";
    }
}
