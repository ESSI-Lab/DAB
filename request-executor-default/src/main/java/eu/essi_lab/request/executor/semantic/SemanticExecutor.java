package eu.essi_lab.request.executor.semantic;

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

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.request.executor.ISemanticExecutor;

/**
 * @author Fabrizio
 */
public class SemanticExecutor implements ISemanticExecutor {

    @Override
    public SemanticCountResponse count(SemanticMessage message) throws GSException {

	StorageUri uri = message.getDataBaseURI();

	DatabaseReader reader = DatabaseConsumerFactory.createDataBaseReader(uri);

	GSLoggerFactory.getLogger(getClass()).info("Count STARTED");

	SemanticCountResponse count = reader.count(message);

	GSLoggerFactory.getLogger(getClass()).info("Count ENDED");

	return count;
    }

    @Override
    public SemanticResponse<GSKnowledgeResourceDescription> retrieve(SemanticMessage message) throws GSException {

	StorageUri uri = message.getDataBaseURI();

	DatabaseReader reader = DatabaseConsumerFactory.createDataBaseReader(uri);

	GSLoggerFactory.getLogger(getClass()).info("Retrieve STARTED");

	SemanticResponse<GSKnowledgeResourceDescription> response = reader.execute(message);

	GSLoggerFactory.getLogger(getClass()).info("Retrieve ENDED");

	return response;
    }

    @Override
    public boolean isAuthorized(SemanticMessage message) throws GSException {
	// TODO Auto-generated method stub
	return true;
    }
}
