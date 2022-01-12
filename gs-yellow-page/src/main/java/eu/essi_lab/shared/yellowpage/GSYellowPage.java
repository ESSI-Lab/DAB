package eu.essi_lab.shared.yellowpage;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.shared.messages.SharedContentQuery;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.shared.messages.IReadSharedContent;
import eu.essi_lab.shared.messages.IWriteSharedContent;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.messages.SharedContentWriteResponse;
import eu.essi_lab.shared.messages.WriteResult;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.repository.SharedRepository;
public class GSYellowPage implements IReadSharedContent<GSResource>, IWriteSharedContent<GSResource> {

    private SharedRepository repository;

    private transient Logger logger = GSLoggerFactory.getLogger(GSYellowPage.class);

    @Override
    public SharedContentReadResponse<GSResource> read(String identifier) throws GSException {

	logger.debug("Reading yp for identifier {}", identifier);

	SharedContentReadResponse response = repository.read(identifier);

	logger.debug("Completed");

	return response;
    }

    @Override
    public Long count() throws GSException {
	logger.info("Reading count of yp content");

	Long count = repository.count();

	logger.info("Completed");

	return count;
    }

    @Override
    public SharedContentReadResponse<GSResource> read(SharedContentQuery query) throws GSException {

	logger.info("Reading yp query {}", query.toString());

	SharedContentReadResponse response = repository.read(query);

	logger.info("Completed");

	return response;
    }

    @Override
    public SharedContentWriteResponse store(GSResource resource) {

	if (resource == null) {
	    logger.info("Request to write null resource, return SUCCESS");
	    SharedContentWriteResponse response = new SharedContentWriteResponse();
	    response.setResult(WriteResult.SUCCESS);
	    return response;
	}

	logger.info("Request to store {}", resource.getPrivateId());

	SharedContent<GSResource> shared = new SharedContent();

	shared.setIdentifier(resource.getPrivateId());

	shared.setContent(resource);

	SharedContentWriteResponse response = repository.put(shared);

	logger.info("Completed request to store {} with {}", resource.getPrivateId(), response.getResult());

	return response;
    }

    public SharedRepository getRepository() {
	return repository;
    }

    public void setRepository(SharedRepository repository) {
	this.repository = repository;
    }
}
