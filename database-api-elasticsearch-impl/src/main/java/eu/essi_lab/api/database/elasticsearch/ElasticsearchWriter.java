/**
 * 
 */
package eu.essi_lab.api.database.elasticsearch;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class ElasticsearchWriter implements DatabaseWriter {

    @Override
    public boolean supports(StorageInfo dbUri) {

	return false;
    }

    @Override
    public void setDatabase(Database dataBase) {

    }

    @Override
    public Database getDatabase() {

	return null;
    }

    @Override
    public void store(GSUser user) throws GSException {

    }

    @Override
    public void removeUser(String userId) throws GSException {

    }

    @Override
    public void store(View view) throws GSException {

    }

    @Override
    public void removeView(String viewId) throws GSException {

    }

    @Override
    public void store(GSResource resource) throws GSException {

    }

    @Override
    public void remove(GSResource resource) throws GSException {

    }

    @Override
    public void store(String identifier, Document document) throws GSException {

    }

    @Override
    public void removeDocument(String identifier) throws GSException {

    }

    @Override
    public void update(GSResource resource) throws GSException {

    }

    @Override
    public void store(GSKnowledgeResourceDescription object) throws GSException {

    }

    @Override
    public void storeRDF(Node rdf) throws GSException {

    }

}
