package eu.essi_lab.gssrv.servlet;

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

import static eu.essi_lab.gssrv.rest.AbstractProfilerService.ERR_ID_PROFILER_ALIEN_ERROR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import eu.essi_lab.gssrv.rest.AbstractProfilerService;
import eu.essi_lab.gssrv.rest.exceptions.GSErrorMessage;
import eu.essi_lab.gssrv.rest.exceptions.GSServiceGSExceptionHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.pubsub.PubSubProfiler;
public class PubSubService extends HttpServlet {

    private static final long serialVersionUID = -4044667892770599100L;

    private final Logger logger = GSLoggerFactory.getLogger(PubSubService.class);

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
	doPost(httpRequest, httpResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {

	GSServiceGSExceptionHandler exHandler = null;

	PubSubProfiler profiler = new PubSubProfiler();

	WebRequest webRequest = new WebRequest();

//	webRequest.setServicesPath(AbstractProfilerService.SERVICES_PATH);

	String out = null;

	try {
	    webRequest.setServletRequest(httpRequest);

	    webRequest.setServletResponse(httpResponse);

	    Response response = profiler.handle(webRequest);

	    if (response.getEntity() != null) {
		out = response.getEntity().toString();
	    }
	} catch (GSException ex) {

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));

	} catch (Exception thr) {

	    GSException ex = GSException.createException(//
		    getClass(), //
		    thr.getMessage(), //
		    thr.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    ERR_ID_PROFILER_ALIEN_ERROR, //
		    thr);

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));
	}

	if (exHandler != null) {

	    Response.Status status = exHandler.getStatus();

	    GSErrorMessage gsMessage = exHandler.getErrorMessageForUser();

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(exHandler.getReader()));

	    Response response = profiler.createUncaughtError(webRequest, status, gsMessage.getMessage());

	    //TODO we should create assign a proper json encoding of response to out, after the todo at PubSubProfiler.createUncaughtError is done
	    out = gsMessage.getMessageAndCode();
	}

	if (out != null) {
	    httpResponse.setContentType(MediaType.APPLICATION_JSON);
	    try {
		httpResponse.getWriter().write(out);

		httpResponse.getWriter().flush();
	    } catch (IOException e) {
		logger.warn("IOException writing {}", out, e);
		throw new ServletException(e);
	    }
	}
    }
}
