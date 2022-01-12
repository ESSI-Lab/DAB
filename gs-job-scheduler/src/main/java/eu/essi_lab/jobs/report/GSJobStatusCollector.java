package eu.essi_lab.jobs.report;

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

import java.util.Date;

import org.slf4j.Logger;

import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.messages.IReadSharedContent;
import eu.essi_lab.shared.messages.IWriteSharedContent;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.messages.SharedContentWriteResponse;
import eu.essi_lab.shared.messages.WriteResult;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.repository.SharedRepository;
public class GSJobStatusCollector implements IReadSharedContent<GSJobStatus>, IWriteSharedContent<GSJobStatus> {

    private SharedRepository repository;

    private transient Logger logger = GSLoggerFactory.getLogger(GSJobStatusCollector.class);
    private static final String START_DATE_AXIS = "startDate";
    private static final String END_DATE_AXIS = "endDate";

    @Override
    public SharedContentReadResponse<GSJobStatus> read(String identifier) throws GSException {

	logger.debug("Reading jod status of {}", identifier);

	return repository.read(identifier);

    }

    @Override
    public Long count() throws GSException {

	logger.debug("Counting job statuses");

	return repository.count();

    }

    @Override
    public SharedContentReadResponse<GSJobStatus> read(SharedContentQuery query) throws GSException {

	logger.debug("Reading job statuses with query {} ", query);

	return repository.read(query);

    }

    private SharedContentQuery initQuery(Page page) {

	SharedContentQuery query = new SharedContentQuery();

	query.setPage(page);

	return query;
    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesStartedAfter(Page page, Long from) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setFrom(from, START_DATE_AXIS);

	logger.debug("Reading job statuses started after {} ", new Date(from));

	return repository.read(query);

    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesStartedBefore(Page page, Long to) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setTo(to, START_DATE_AXIS);

	logger.debug("Reading job statuses started before {} ", new Date(to));

	return repository.read(query);

    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesStartedInBetween(Page page, Long from, Long to) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setTo(to, START_DATE_AXIS);

	query.setFrom(from, START_DATE_AXIS);

	logger.debug("Reading job statuses started after {} and before {} ", new Date(from), new Date(to));

	return repository.read(query);

    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesCompletedAfter(Page page, Long from) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setFrom(from, END_DATE_AXIS);

	logger.debug("Reading job statuses completed after {} ", new Date(from));

	return repository.read(query);

    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesCompletedBefore(Page page, Long to) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setTo(to, END_DATE_AXIS);

	logger.debug("Reading job statuses completed before {} ", new Date(to));

	return repository.read(query);

    }

    public SharedContentReadResponse<GSJobStatus> readJobStatusesCompletedInBetween(Page page, Long from, Long to) throws GSException {

	SharedContentQuery query = initQuery(page);

	query.setTo(to, END_DATE_AXIS);

	query.setFrom(from, END_DATE_AXIS);

	logger.debug("Reading job statuses completed after {} and before {} ", new Date(from), new Date(to));

	return repository.read(query);

    }

    @Override
    public SharedContentWriteResponse store(GSJobStatus resource) {

	if (resource == null) {
	    logger.trace("Request to write null resource, return SUCCESS");
	    SharedContentWriteResponse response = new SharedContentWriteResponse();
	    response.setResult(WriteResult.SUCCESS);
	    return response;
	}

	logger.debug("Request to store job status {}", resource.getExecutionid());

	SharedContent<GSJobStatus> shared = new SharedContent();

	shared.setIdentifier(resource.getExecutionid());

	shared.setContent(resource);

	SharedContentWriteResponse response = repository.put(shared);

	logger.debug("Completed store of status of {} with result {}", resource.getExecutionid(), response.getResult());

	return response;
    }

    public SharedRepository getRepository() {
	return repository;
    }

    public void setRepository(SharedRepository repository) {
	this.repository = repository;
    }
}
