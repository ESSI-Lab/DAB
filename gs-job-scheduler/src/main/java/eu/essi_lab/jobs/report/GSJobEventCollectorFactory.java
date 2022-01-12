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

import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.shared.SHARED_CONTENT_TYPES;
import eu.essi_lab.shared.SharedRepositoryFactory;
import eu.essi_lab.shared.model.IGSSharedContentCategory;
import eu.essi_lab.shared.model.PersistentGSSharedContentCategory;
import eu.essi_lab.shared.model.SharedContentType;
import eu.essi_lab.shared.repository.SharedRepository;
public class GSJobEventCollectorFactory {

    private GSJobEventCollectorFactory() {
	//force using static
    }

    public static GSJobStatusCollector getGSJobStatusCollector(SharedRepositoryInfo configuration) {

	SharedRepository repo = SharedRepositoryFactory.getSharedRepository(configuration);

	return getCollector(repo);

    }

    static GSJobStatusCollector getCollector(SharedRepository repository) {
	IGSSharedContentCategory category = new PersistentGSSharedContentCategory();

	SharedContentType type = new SharedContentType();

	type.setType(SHARED_CONTENT_TYPES.GS_JOB_STATUS);

	repository.setCategory(category);
	repository.setType(type);

	GSJobStatusCollector jobStatusCollector = new GSJobStatusCollector();

	jobStatusCollector.setRepository(repository);

	return jobStatusCollector;
    }
}
