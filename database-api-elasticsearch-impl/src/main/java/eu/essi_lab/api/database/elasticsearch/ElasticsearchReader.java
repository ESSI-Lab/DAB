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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class ElasticsearchReader implements DatabaseReader {

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
    public Optional<GSUser> getUser(String userName) throws GSException {

	return Optional.empty();
    }

    @Override
    public List<GSUser> getUsers() throws GSException {

	return null;
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {

	return Optional.empty();
    }

    @Override
    public List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException {

	return null;
    }

    @Override
    public boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException {

	return false;
    }

    @Override
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException {

	return null;
    }

    @Override
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {

	return false;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException {

	return null;
    }
}
