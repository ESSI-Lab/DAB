package eu.essi_lab.profiler.oaipmh.handler.srvinfo;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.RequestType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.oaipmh.OAIPMHNameSpaceMapper;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfiler;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestValidator;

/**
 * Subclasses must provide implementation of the protected methods, while creation of the {@link Response} is handled by
 * this class
 * 
 * @author Fabrizio
 */
public abstract class OAIPMHServiceInfoHandler extends DefaultRequestHandler {

    private static final String OAI_PMH_INFO_RESPONSE_MARSHALLING_ERROR = "OAI_PMH_RESPONSE_MARSHALLING_ERROR";

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.valueOf("application/xml;charset=UTF-8");
    }

    /**
     * <ul>
     * <li>invokes {@link #createResponseElement(WebRequest)}</li>
     * <li>set the {@link RequestType} with {@link #getVerbType()} to the element</li>
     * <li>set the the response date to the element</li>
     * <li>creates the {@link Response} with marshaled element as entity and with media type
     * "application/xml;charset=UTF-8"</li>
     * </ul>
     */
    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	OAIPMHtype element = createResponseElement(webRequest);

	RequestType requestType = new RequestType();
	requestType.setVerb(getVerbType());
	requestType.setValue(webRequest.getUriInfo().getAbsolutePath().toString());

	element.setRequest(requestType);

	try {
	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
	    element.setResponseDate(calendar);

	} catch (DatatypeConfigurationException ex) {
	    // it should not happen, and even if if happens, it is not so bad. a log a warning should be enough
	}

	try {

	    String ret = CommonContext.asString(element, true, new OAIPMHNameSpaceMapper(),OAIPMHProfiler.SCHEMA_LOCATION);
	    return ret;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    OAI_PMH_INFO_RESPONSE_MARSHALLING_ERROR, //
		    e);
	}

    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	OAIPMHRequestValidator transformer = new OAIPMHRequestValidator();
	transformer.setServiceInfoValidation();

	return transformer.validate(request);
    }

    /**
     * Creates the {@link OAIPMHtype} to return as response body
     * 
     * @param webRequest
     * @return
     */
    protected abstract OAIPMHtype createResponseElement(WebRequest webRequest) throws GSException;

    /**
     * Return the request {@link VerbType}
     * 
     * @return
     */
    protected abstract VerbType getVerbType();
}
