package eu.essi_lab.shared.repository;

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

import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.shared.messages.SharedContentQuery;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.controller.Controller;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.messages.SharedContentWriteResponse;
import eu.essi_lab.shared.messages.WriteResult;
import eu.essi_lab.shared.model.IGSSharedContentCategory;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
public class SharedRepository {

    private Controller controller;
    private static final String RUNTIME_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID = "RUNTIME_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID";
    private SharedContentType type;
    private IGSSharedContentCategory category;
    private static final String NULLCONTENT_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID = "NULLCONTENT_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID";
    private transient Logger logger = GSLoggerFactory.getLogger(SharedRepository.class);

    public SharedContentWriteResponse put(SharedContent content) {

	logger.debug("Requested to store a shared content");

	if (content == null) {

	    logger.warn("Found null content");

	    GSException ex = GSException.createException(this.getClass(), "Can't store null content", null, null,
		    ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_WARNING, NULLCONTENT_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID);

	    return createErrorResponse(ex);
	}

	content.setCategory(category);

	content.setType(type);

	try {

	    logger.trace("Submitting request to controller");

	    controller.store(content);

	    logger.trace("Controller completed with success");

	    SharedContentWriteResponse response = new SharedContentWriteResponse();
	    response.setResult(WriteResult.SUCCESS);
	    return response;

	} catch (GSException ex) {

	    logger.error("Controller has thrown a GSExcetpion");

	    return createErrorResponse(ex);

	} catch (Exception thr) {

	    logger.error("Controller has thrown an unknown Exception");

	    GSException ex = GSException.createException(this.getClass(), "Runtime Excetion storing to shared repository", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, RUNTIME_EXCEPTION_WRITE_SHAREDREPO_ERRI_ID, thr);

	    return createErrorResponse(ex);

	}

    }

    public Long count() throws GSException {

	logger.trace("Submitting count request to controller");

	Long c = controller.count(category, type);

	logger.trace("Completed count");

	return c;

    }

    void logGSException(GSException e) {
	DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
    }

    private SharedContentWriteResponse createErrorResponse(GSException ex) {

	logGSException(ex);

	SharedContentWriteResponse response = new SharedContentWriteResponse();
	response.setResult(WriteResult.ERROR);

	response.setGSException(ex);
	return response;

    }

    public SharedContentReadResponse read(String identifier) throws GSException {

	logger.trace("Submitting read request to controller with id {} type {} and category {}", identifier, type.getType(),
		category.getType());

	SharedContent content = controller.readSharedContent(type, category, identifier);

	logger.trace("Completed read by id");

	return createResponse(content);

    }

    private SharedContentReadResponse createResponse(SharedContent content) {

	SharedContentReadResponse response = new SharedContentReadResponse();

	if (content == null) {
	    logger.trace("Null response received from controller, returning emty list;");

	    return response;
	}

	response.addContent(content);

	logger.trace("Generated response");

	return response;
    }

    public SharedContentReadResponse read(SharedContentQuery query) throws GSException {

	logger.trace("Submitting read request to controller with query {} with type {} and category {}", query, type.getType(),
		category.getType());

	List<SharedContent> contents = controller.readSharedContent(type, category, query);

	logger.trace("Completed read by timestamp");

	SharedContentReadResponse response = new SharedContentReadResponse();
	response.setContents(contents);

	logger.trace("Generated response");

	return response;
    }

    public Controller getController() {
	return controller;
    }

    public void setController(Controller controller) {
	this.controller = controller;
    }

    public IGSSharedContentCategory getCategory() {
	return category;
    }

    public void setCategory(IGSSharedContentCategory category) {
	this.category = category;
    }

    public SharedContentType getType() {
	return type;
    }

    public void setType(SharedContentType type) {
	this.type = type;
    }

}
