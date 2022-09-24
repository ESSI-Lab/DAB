package eu.essi_lab.gssrv.rest;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;

import org.slf4j.Logger;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.authentication.configuration.IOAuthAuthenticatorConfigurable;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.gssrv.rest.exceptions.GSErrorMessage;
import eu.essi_lab.gssrv.rest.exceptions.GSServiceGSExceptionHandler;
import eu.essi_lab.gssrv.starter.ConfigurationLookup;
import eu.essi_lab.gssrv.starter.GISuiteStarter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.GSInitConfiguration;
import eu.essi_lab.model.configuration.GSInitConfigurationResponse;
import eu.essi_lab.model.configuration.GSInitConfigurationResponse.Message;
import eu.essi_lab.model.configuration.GSInitConfigurationResponse.Result;
import eu.essi_lab.model.configuration.InitOAuthProvider;
import eu.essi_lab.model.configuration.InitOAuthProvidersResponse;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.profiler.administration.AdminGetHandler;
import eu.essi_lab.profiler.administration.AdminPostHandler;

@WebService
@Path("/")
public class ESSIAdminService {

    public static final String ERR_ID_ADMINISTRATION_ALIEN_ERROR = "ERR_ID_ADMINISTRATION_ALIEN_ERROR";
    public static final String USER_NOT_AUTHRORIZED_FOR_ADMIN = "USER_NOT_AUTHRORIZED_FOR_ADMIN";
    private transient Logger logger = GSLoggerFactory.getLogger(ESSIAdminService.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configuration")
    public Response admin(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	return execAdminREST(hsr, uriInfo, wscontext, "configuration");

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/option")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response optionPost(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	return execAdminREST(hsr, uriInfo, wscontext, "option");

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/source")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sourcePost(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	return execAdminREST(hsr, uriInfo, wscontext, "source");

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configuration")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response configurationPost(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	return execAdminREST(hsr, uriInfo, wscontext, "configuration");

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/init")
    @Consumes(MediaType.APPLICATION_JSON)
    public GSInitConfigurationResponse initPost(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @Context WebServiceContext wscontext, GSInitConfiguration json) {

	return executeInitPost(json);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oauthproviders")
    public InitOAuthProvidersResponse getOAuthProviders(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @Context WebServiceContext wscontext) {

	return executeGetOAuthProviders();

    }

    public InitOAuthProvidersResponse executeGetOAuthProviders() {

	InitOAuthProvidersResponse response = new InitOAuthProvidersResponse();

	List<InitOAuthProvider> list = new ArrayList<>();

	Iterator<IOAuthAuthenticatorConfigurable> it = ServiceLoader.load(IOAuthAuthenticatorConfigurable.class).iterator();

	while (it.hasNext()) {

	    IOAuthAuthenticatorConfigurable authenticator = it.next();

	    InitOAuthProvider provider = new InitOAuthProvider();

	    provider.setKey(authenticator.getKey());
	    provider.setLabel(authenticator.getLabel());

	    list.add(provider);
	}

	response.setProviders(list);

	return response;
    }

    public GSInitConfigurationResponse executeInitPost(GSInitConfiguration init) {
	GSServiceGSExceptionHandler exHandler = null;

	try {

	    GSInitConfigurationResponse resp = validateInitPostRequest(init);

	    if (resp.getResult().compareTo(Result.FAIL) == 0)
		return resp;

	    GISuiteStarter starter = new GISuiteStarter();

	    StorageUri initializedURL = starter.applyInitRequest(init);

	    logger.debug("Applied conf URL {}", initializedURL.getUri());

	    IGSConfigurationStorage dbconf = new ConfigurationLookup().getDBGIsuiteConfiguration(initializedURL);

	    if (dbconf == null) {

		logger.debug("GSConfigurationStorage is null");

		resp.setResult(GSInitConfigurationResponse.Result.FAIL);

		resp.setMessage(Message.INVALID_URL);

	    } else {

		logger.trace("GSConfigurationStorage is not null");

		resp.setResult(GSInitConfigurationResponse.Result.SUCCESS);
		Message m = Message.CONFIGURATION_URL;

		m.setMessage(dbconf.getStorageUri().getUri());

		resp.setMessage(m);

		logger.trace("Setting configuration");

		ConfigurationSync.getInstance().setDBGISuiteConfiguration(dbconf);

		if (init.getUseExisting())
		    starter.initializeConfiguration(ConfigurationSync.getInstance().getClonedConfiguration());
		else {

		    logger.trace("Flushing configuration");
		    new GSConfigurationManager().flush();
		}
	    }

	    return resp;

	} catch (GSException thr) {

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(thr));

	} catch (Throwable thr) {

	    GSException alienEx = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setCause(thr);
	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_ADMINISTRATION_ALIEN_ERROR);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	    alienEx.addInfo(ei);

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(alienEx));

	}

	GSErrorMessage gsMessage = exHandler.getErrorMessageForUser();

	DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(exHandler.getReader()));

	GSInitConfigurationResponse resp = new GSInitConfigurationResponse();

	resp.setResult(Result.FAIL);

	Message m = Message.UNKNOWN;

	m.setMessage(gsMessage.getMessage());

	resp.setMessage(m);

	return resp;

    }

    private Response execAdminREST(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext,
	    String resource) {

	GSServiceGSExceptionHandler exHandler = null;

	try {

	    GSUser user = WebRequest.getCurrentUser(hsr);

	    logger.info("Requested Admin Action by user {}", user.getIdentifier());

	    boolean allowed = canExecAdminAction(user, hsr.getMethod(), resource);

	    logger.debug("Permission for Admin Action for {} is: {}", user.getIdentifier(), allowed);

	    if (!allowed) {
		throw GSException.createException(//
			this.getClass(), //
			"Requested admin action by non authorized user", //
			"You are not authorized to execute this action, try to log in if you have not yet done or contact administrators", //
			ErrorInfo.ERRORTYPE_CLIENT, //
			ErrorInfo.SEVERITY_WARNING, USER_NOT_AUTHRORIZED_FOR_ADMIN);
	    }

	    WebRequest webRequest = new WebRequest();

	    webRequest.setServletRequest(hsr);
	    webRequest.setUriInfo(uriInfo);

	    WebRequestHandler handler = null;

	    if (webRequest.isGetRequest()) {

		handler = new AdminGetHandler(resource);

	    } else {

		handler = new AdminPostHandler(resource);
	    }

	    return handler.handle(webRequest);

	} catch (GSException ex) {

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));

	} catch (Throwable thr) {

	    exHandler = handleThrowable(thr);

	}

	return createErrorResponse(exHandler);
    }

    private GSInitConfigurationResponse validateInitPostRequest(GSInitConfiguration init) {

	GSInitConfigurationResponse resp = new GSInitConfigurationResponse();

	if (init.getUrl() == null || init.getUrl() == "") {

	    resp.setResult(GSInitConfigurationResponse.Result.FAIL);

	    resp.setMessage(Message.INVALID_URL);
	    return resp;
	}

	if (!init.getUseExisting()) {

	    if (init.getRootUser() == null) {

		resp.setResult(GSInitConfigurationResponse.Result.FAIL);

		resp.setMessage(Message.ROOT_USER_REQUIRED);
		return resp;
	    }

	    if (init.getOauthProviderName() == null || init.getOauthProviderName().equalsIgnoreCase("")) {

		resp.setResult(GSInitConfigurationResponse.Result.FAIL);

		resp.setMessage(Message.NO_OAUTH_PROVIDER);
		return resp;
	    }

	    if (init.getOauthProviderId() == null || init.getOauthProviderId().equalsIgnoreCase("")) {

		resp.setResult(GSInitConfigurationResponse.Result.FAIL);

		resp.setMessage(Message.NO_OAUTH_PROVIDER_ID);
		return resp;
	    }

	    if (init.getOauthProviderSecret() == null || init.getOauthProviderSecret().equalsIgnoreCase("")) {
		resp.setResult(GSInitConfigurationResponse.Result.FAIL);

		resp.setMessage(Message.NO_OAUTH_PROVIDER_SECRET);
		return resp;
	    }
	}

	resp.setResult(Result.SUCCESS);
	return resp;
    }
    private boolean canExecAdminAction(GSUser user, String method, String resource) throws GSException {

	GSConfiguration configuration = ConfigurationSync.getInstance().getClonedConfiguration();

	Optional<String> configRootUser = configuration.readAdminIdentifier();

	if (configRootUser.isPresent()) {

	    return user.getIdentifier() != null && user.getIdentifier().equalsIgnoreCase(configRootUser.get());
	}

	return false;
    }

    private Response createErrorResponse(GSServiceGSExceptionHandler exHandler) {

	Status status = exHandler.getStatus();

	GSErrorMessage gsMessage = exHandler.getErrorMessageForUser();

	DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(exHandler.getReader()));

	return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(gsMessage.toJSONString()).build();

    }

    private GSServiceGSExceptionHandler handleThrowable(Throwable thr) {

	GSException alienEx = new GSException();

	ErrorInfo ei = new ErrorInfo();

	ei.setCause(thr);
	ei.setContextId(this.getClass().getName());
	ei.setErrorId(ERR_ID_ADMINISTRATION_ALIEN_ERROR);
	ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	alienEx.addInfo(ei);

	return new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(alienEx));

    }

}
