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

import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import com.marklogic.xcc.ResultSequence;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicCacheCleanerTask extends AbstractCustomTask {

    /**
     * 
     */
    private static final String CACHE_FOLDER_NAME = "driverCache";

    /**
     * 
     */
    private static final long RETENTION_TIME = TimeUnit.HOURS.toMillis(1);

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Database cache cleaner task STARTED");

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);
	MarkLogicWrapper wrapper = ((MarkLogicDatabase) dbReader.getDatabase()).getWrapper();

	String cacheFolder = dbReader.getDatabase().getStorageInfo().getIdentifier() + "_" + CACHE_FOLDER_NAME;

	//
	//
	//

	log(status, "Count cached records STARTED");

	ResultSequence resultSequence = wrapper.submit(getCountCachedQuery(cacheFolder));

	int count = Integer.valueOf(resultSequence.asString());

	log(status, "Cached records found: " + count);

	log(status, "Count cached records ENDED");

	//
	//
	//

	log(status, "Count old cached records STARTED");

	resultSequence = wrapper.submit(getCountOldCachedQuery(cacheFolder));

	count = resultSequence.size();

	log(status, "Old cached records found: " + count);

	log(status, "Count old cached records ENDED");

	//
	//
	//

	if (count > 0) {

	    log(status, "Removing old cached records STARTED");

	    wrapper.submit(getRemoveOldCachedQuery(cacheFolder));

	    log(status, "Removing old cached records ENDED");
	}

	log(status, "Database cache cleaner task ENDED");
    }

    /**
     * @param cacheFolder
     * @return
     */
    private String getCountCachedQuery(String cacheFolder) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "let $query := cts:directory-query(\"/" + cacheFolder + "/\", \"infinity\")\n";

	query += "return xdmp:estimate(cts:search(doc(), $query,(\"unfiltered\",\"score-simple\"),0) )\n";

	return query;
    }

    /**
     * @param cacheFolder
     * @return
     */
    private String getCountOldCachedQuery(String cacheFolder) {

	return getOldCachedQuery(true, cacheFolder);
    }

    /**
     * @param cacheFolder
     * @return
     */
    private String getRemoveOldCachedQuery(String cacheFolder) {

	return getOldCachedQuery(false, cacheFolder);
    }

    /**
     * @param count
     * @param cacheFolder
     * @return
     */
    private String getOldCachedQuery(boolean count, String cacheFolder) {

	String action = count ? "$timeStamp" : "xdmp:document-delete(fn:document-uri($x))";

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "let $query := cts:directory-query(\"/" + cacheFolder + "/\", \"infinity\")\n";

	query += "for $x in cts:search(doc(), $query,(\"unfiltered\",\"score-simple\"),0) \n";

	query += "let $timeStamp := xs:long(xdmp:document-properties(fn:document-uri($x))//timeStamp/text()) \n";

	query += "return if ($timeStamp > " + RETENTION_TIME + ") then (" + action + ") else () \n";

	return query;
    }

    @Override
    public String getName() {

	return "Database cache cleaner";
    }
}
