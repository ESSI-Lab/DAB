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

import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.xpath.XPathExpressionException;

import org.quartz.JobExecutionContext;

import com.marklogic.xcc.ResultSequence;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.StorageUri;

/**
 * @author Fabrizio
 */
public class UsersViewerTask extends AbstractCustomTask {

    /**
     * 
     */
    public UsersViewerTask() {
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	StorageUri databaseURI = ConfigurationWrapper.getDatabaseURI();

	DatabaseReader dbReader = DatabaseConsumerFactory.createDataBaseReader(databaseURI);
	MarkLogicDatabase dataBase = ((MarkLogicDatabase) dbReader.getDatabase());

	String query = getCountQuery();

	Integer usersCount = Integer.valueOf(dataBase.execXQuery(query).asString());

	log(status, "- Number of users: " + usersCount + "\n");

	if (usersCount == 0) {

	    return;
	}

	query = getQuery();

	ResultSequence resultSequence = dataBase.execXQuery(query);

	while (resultSequence.hasNext()) {

	    InputStream stream = resultSequence.next().asInputStream();
	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    String id = reader.evaluateTextContent("//*:identifier/text()").get(0);

	    String enabled = "true";

	    if (reader.evaluateBoolean("exists(//*:enabled)")) {

		enabled = reader.evaluateTextContent("//*:enabled/text()").get(0);
	    }

	    String role = new String(reader.evaluateTextContent("//*:role/text()").get(0).getBytes(Charset.forName("UTF-8")));
	    String firstName = new String(readProperty(reader, "firstName").getBytes(Charset.forName("UTF-8")));
	    String lastName = new String(readProperty(reader, "firstName").getBytes(Charset.forName("UTF-8")));
	    String email = new String(readProperty(reader, "email").getBytes(Charset.forName("UTF-8")));
	    String country = new String(readProperty(reader, "country").getBytes(Charset.forName("UTF-8")));
	    String institutionType = new String(readProperty(reader, "institutionType").getBytes(Charset.forName("UTF-8")));
	    String registrationDate = new String(readProperty(reader, "registrationDate").getBytes(Charset.forName("UTF-8")));

	    log(status, "- Identifier: " + id);
	    log(status, "- Enabled: " + enabled);
	    log(status, "- Role: " + role);
	    log(status, "- First name: " + firstName);
	    log(status, "- Last name: " + lastName);
	    log(status, "- Email: " + email);
	    log(status, "- Country: " + country);
	    log(status, "- Institution type: " + institutionType);
	    log(status, "- Registration date: " + registrationDate + "\n");
	}
    }

    @Override
    public String getName() {

	return "Users viewer";
    }

    /**
     * @param reader
     * @param property
     * @return
     * @throws XPathExpressionException
     */
    private static String readProperty(XMLDocumentReader reader, String property) throws XPathExpressionException {

	String value = "missing";

	if (reader.evaluateBoolean("exists(//*:property[name='" + property + "'])")) {

	    value = reader.evaluateTextContent("//*:property[name='" + property + "']/*:value/text()").get(0);
	}

	return value;
    }

    /**
     * @return
     */
    private static String getQuery() {

	String query = "xquery version \"1.0-ml\";\n";
	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "cts:search(doc(),cts:directory-query(\"/preprodenvconf_users/\", \"infinity\"),\n";
	query += "(\"unfiltered\",\"score-simple\"),0)\n";

	return query;
    }

    /**
     * @return
     */
    private static String getCountQuery() {

	return getQuery().replace("cts:search", "xdmp:estimate(cts:search").replace(",0)", ",0))");
    }
}
