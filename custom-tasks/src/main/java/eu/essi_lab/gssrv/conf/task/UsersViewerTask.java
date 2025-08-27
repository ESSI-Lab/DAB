package eu.essi_lab.gssrv.conf.task;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;

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

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);

	List<GSUser> users = dbReader.getUsers();

	Integer usersCount = users.size();

	log(status, "---\n");
	log(status, "[Number of users]: " + usersCount + "\n");
	log(status, "---\n\n");

	if (usersCount == 0) {

	    return;
	}

	HashMap<String, List<String>> map = new HashMap<>();

	for (GSUser user : users) {

	    StringBuilder builder = new StringBuilder();

	    builder.append("- URI: " + user.getUri() + "\n");
	    builder.append("- Role: " + user.getRole() + "\n");
	    builder.append("- Identifier: " + user.getIdentifier() + "\n");
	    builder.append("- Identifier type: " + (user.getUserIdentifierType()==null?"missing":user.getUserIdentifierType())  + "\n");
	    builder.append("- Enabled: " + user.isEnabled() + "\n");
	    builder.append("- First name: " + readProperty(user, "firstName") + "\n");
	    builder.append("- Last name: " + readProperty(user, "lastName") + "\n");
	    builder.append("- Email: " + readProperty(user, "email") + "\n");
	    builder.append("- Country: " + readProperty(user, "country") + "\n");
	    builder.append("- Institution type: " + readProperty(user, "institutionType") + "\n");
	    builder.append("- Registration date: " + readProperty(user, "registrationDate") + "\n");

	    List<String> list = map.get(user.getRole());

	    if (list == null) {

		list = new ArrayList<String>();
		map.put(user.getRole(), list);
	    }

	    list.add(builder.toString());
	}

	String[] keys = map.keySet().toArray(new String[] {});

	for (int i = 0; i < keys.length; i++) {

	    log(status, "[ROLE] : " + keys[i] + "\n");

	    map.get(keys[i]).forEach(user -> log(status, user));
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
    private static String readProperty(GSUser user, String property) throws XPathExpressionException {

	return user.//
		getProperties().//
		stream().//
		filter(p -> p.getName().equals(property)).//
		map(p -> p.getValue().toString()).//
		findFirst().//
		orElse("missing");
    }
}
