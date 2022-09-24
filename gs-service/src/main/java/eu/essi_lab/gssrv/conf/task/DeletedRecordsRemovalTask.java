package eu.essi_lab.gssrv.conf.task;

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

import org.quartz.JobExecutionContext;

import com.marklogic.xcc.ResultSequence;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class DeletedRecordsRemovalTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Deleted records removal task STARTED");

	StorageUri databaseURI = ConfigurationWrapper.getDatabaseURI();

	DatabaseReader dbReader = DatabaseConsumerFactory.createDataBaseReader(databaseURI);
	MarkLogicWrapper wrapper = ((MarkLogicDatabase) dbReader.getDatabase()).getWrapper();

	log(status, "Count deleted records STARTED");

	ResultSequence resultSequence = wrapper.submit(getCountDeletedQuery());

	int count = Integer.valueOf(resultSequence.asString());

	log(status, "Found " + count + " deleted records to remove");

	log(status, "Count deleted records ENDED");

	if (count > 0) {

	    log(status, "Removing deleted records STARTED");

	    wrapper.submit(getRemoveDeletedQuery(count));

	    log(status, "Removing deleted records ENDED");
	}

	log(status, "Deleted records removal task ENDED");
    }

    @Override
    public String getName() {

	return "Deleted records removal";
    }
   
    /**
     * @return
     */
    private String getCountDeletedQuery() {

	String query = "xquery version \"1.0-ml\";\n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "xdmp:estimate(cts:search(doc(),cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'=','true',";

	query += "(\"score-function=linear\"),0.0),(\"unfiltered\", \"score-simple\"),0))\n";

	return query;
    }

    /**
     * @param count
     * @return
     */
    private String getRemoveDeletedQuery(int count) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "for $doc in cts:search(doc()[gs:Dataset or gs:DatasetCollection or gs:Document or gs:Ontology or gs:Service or gs:Observation],\n";

	query += "cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'=','true',(\"score-function=linear\"),0.0) \n";

	query += ",(\"unfiltered\",\"score-simple\"),0)[0 to " + count + "]\n";

	query += "return xdmp:document-delete(fn:document-uri($doc))";

	return query;
    }
}
