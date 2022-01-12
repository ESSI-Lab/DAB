package eu.essi_lab.shared.controller;

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
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.configuration.SharedContentConfiguration;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
import eu.essi_lab.shared.model.IGSSharedContentCategory;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
public class Controller {

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private static final java.lang.String NO_SHARED_REPOSITORY_DRIVER_ERR_ID = "NO_SHARED_REPOSITORY_DRIVER_ERR_ID";
    private final SharedContentConfiguration sharedRepoConfiguration;

    public Controller(SharedContentConfiguration configuration) {

	sharedRepoConfiguration = configuration;

    }

    private ISharedRepositoryDriver findDriver(IGSSharedContentCategory contentCategory) throws GSException {

	logger.trace("Looking for driver of category {}", contentCategory.getType());

	ISharedRepositoryDriver driver = sharedRepoConfiguration.getDriver(contentCategory);

	if (driver == null) {
	    logger.warn("No write driver found");

	    throw GSException.createException(this.getClass(), "No available driver for category " + contentCategory.getName(), null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, NO_SHARED_REPOSITORY_DRIVER_ERR_ID);
	}

	return driver;

    }

    public void store(SharedContent content) throws GSException {

	IGSSharedContentCategory contentCategory = content.getCategory();

	ISharedRepositoryDriver driver = findDriver(contentCategory);

	logger.trace("Submit write request to driver {}", driver.getClass());

	driver.store(content);

	logger.trace("Driver completed write operation");

	return;

    }

    /**
     * Reads {@link SharedContentType} with id identifier, of the given catefory and type. If no element is found with the given identifier,
     * null is returned.
     *
     * @param type
     * @param category
     * @param identifier
     * @return
     * @throws GSException
     */
    public SharedContent readSharedContent(SharedContentType type, IGSSharedContentCategory category, String identifier)
	    throws GSException {

	ISharedRepositoryDriver driver = findDriver(category);

	logger.trace("Submit read by id request to driver {}", driver.getClass());

	SharedContent content = driver.readSharedContent(identifier, type);

	logger.trace("Driver completed read by id operation");

	return content;

    }

    /**
     * Reads {@link SharedContentType} elements matching the given query. If no element is found with the given identifier, an empty list is
     * returned.
     *
     * @param type
     * @param category
     * @param query
     * @return
     * @throws GSException
     */
    public List<SharedContent> readSharedContent(SharedContentType type, IGSSharedContentCategory category, SharedContentQuery query)
	    throws GSException {

	ISharedRepositoryDriver driver = findDriver(category);

	logger.trace("Submit read by id request to driver {}", driver.getClass());

	List<SharedContent> contents = driver.readSharedContent(type, query);

	logger.trace("Driver completed read by id operation");

	return contents;
    }

    /**
     * Reads the number of elements of the given type and category currently stored.
     *
     * @param category
     * @param type
     * @return
     */
    public Long count(IGSSharedContentCategory category, SharedContentType type) throws GSException {

	ISharedRepositoryDriver driver = findDriver(category);

	logger.trace("Submitting count to driver {}", driver.getClass());

	Long c = driver.count(type);

	logger.trace("Count operation completed");

	return c;
    }
}
