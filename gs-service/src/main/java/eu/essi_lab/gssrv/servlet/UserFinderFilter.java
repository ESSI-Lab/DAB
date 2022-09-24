package eu.essi_lab.gssrv.servlet;

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

import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.authorization.userfinder.UserFinderFactory;
import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class UserFinderFilter implements Filter {

    private static final String USER_SEARCHING_ERROR = "USER_SEARCHING_ERROR";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
	//nothing to do here
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	GSLoggerFactory.getLogger(ProfilerServiceFilter.class).trace("Executing filter {}", this);

	GSUser user = null;

	try {

	    user = findCurrentUser((HttpServletRequest) request);

	} catch (GSException e) {

	    user = BasicRole.createAnonymousUser();

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred, unable to find current user. Using anonymous user");

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	WebRequest.setCurrentUser(user, (HttpServletRequest) request);

	chain.doFilter(request, response);
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    private GSUser findCurrentUser(HttpServletRequest request) throws GSException {

	UserFinder finder = UserFinderFactory.createUserFinder();

	StorageUri storageUri = ConfigurationUtils.getStorageURI();

	//
	// it is null when the configuration is not yet initialized
	//
	if (storageUri != null) {

	    DatabaseReader reader = new DatabaseConsumerFactory().createDataBaseReader(storageUri);
	    DatabaseWriter writer = new DatabaseConsumerFactory().createDataBaseWriter(storageUri);

	    finder.setClient(writer);
	    finder.setDatabaseReader(reader);
	}

	getConfiguration().ifPresent(finder::setConfiguration);

	GSUser user = null;

	try {
	    user = finder.findUser(request);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USER_SEARCHING_ERROR);
	}

	return user;
    }

    /**
     * @return
     */
    private Optional<GSConfiguration> getConfiguration() {

	try {
	    return Optional.of(ConfigurationSync.getInstance().getClonedConfiguration());
	} catch (GSException e) {
	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

	return Optional.empty();
    }

    @Override
    public void destroy() {
	//nothing to do here
    }
}
