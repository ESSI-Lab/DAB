package eu.essi_lab.api.database.vol;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Iterator;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class VolatileDatabaseWriter extends DatabaseWriter {

    private VolatileDatabase database;
    private StorageInfo dbUri;

    @Override
    public boolean supports(StorageInfo dbUri) {

	this.dbUri = dbUri;

	return dbUri.getName() != null && //
		dbUri.getName().equals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
    }

    @Override
    public void setDatabase(Database db) {

	this.database = (VolatileDatabase) db;
    }

    @Override
    public VolatileDatabase getDatabase() {

	return (VolatileDatabase) this.database;
    }

    @Override
    public void store(GSUser user) throws GSException {

	getDatabase().getUsersList().add(user);
    }

    @Override
    public void removeUser(String userId) {

	synchronized (getDatabase().getUsersList()) {

	    Iterator<GSUser> iterator = getDatabase().getUsersList().iterator();
	    while (iterator.hasNext()) {
		GSUser next = iterator.next();
		if (next.getIdentifier().equals(userId)) {
		    iterator.remove();
		}
	    }
	}
    }

    @Override
    public void store(View view) throws GSException {

	getDatabase().getViewsList().add(view);
    }

    @Override
    public void removeView(String viewId) throws GSException {

	synchronized (getDatabase().getViewsList()) {

	    Iterator<View> iterator = getDatabase().getViewsList().iterator();
	    while (iterator.hasNext()) {
		View next = iterator.next();
		if (next.getId().equals(viewId)) {
		    iterator.remove();
		}
	    }
	}
    }

    @Override
    public void store(GSResource resource) throws GSException {

	synchronized (getDatabase().getResourcesList()) {

	    getDatabase().getResourcesList().add(resource);
	}
    }

    @Override
    public void remove(GSResource resource) throws GSException {

	synchronized (getDatabase().getResourcesList()) {

	    Iterator<GSResource> iterator = getDatabase().getResourcesList().iterator();
	    while (iterator.hasNext()) {
		GSResource next = iterator.next();
		if (next.getPrivateId().equals(resource.getPrivateId())) {
		    iterator.remove();
		}
	    }
	}

    }

    @Override
    public void update(GSResource resource) throws GSException {

	synchronized (getDatabase().getResourcesList()) {

	    Iterator<GSResource> iterator = getDatabase().getResourcesList().iterator();
	    while (iterator.hasNext()) {
		GSResource next = iterator.next();
		if (next.getPrivateId().equals(resource.getPrivateId())) {
		    iterator.remove();
		    break;
		}
	    }

	    getDatabase().getResourcesList().add(resource);
	}
    }

    //
    // --- NOT IMPL ---
    //

    @Override
    public void store(GSKnowledgeResourceDescription object) throws GSException {

    }

    @Override
    public void storeRDF(Node rdf) throws GSException {

    }

    @Override
    public void remove(String propertyName, String propertyValue) throws GSException {

    }
}
