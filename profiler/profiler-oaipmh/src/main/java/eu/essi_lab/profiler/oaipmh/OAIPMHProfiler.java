package eu.essi_lab.profiler.oaipmh;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.OAIPMHerrorType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHerrorcodeType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter.InspectionStrategy;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHRequestTransformer;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetFormatter;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetMapper;
import eu.essi_lab.profiler.oaipmh.handler.srvinfo.OAIPMHIdentifyHandler;
import eu.essi_lab.profiler.oaipmh.handler.srvinfo.OAIPMHListMetadataFormatsHandler;
import eu.essi_lab.profiler.oaipmh.handler.srvinfo.OAIPMHListSetsHandler;

/**
 * @author Fabrizio
 */
public class OAIPMHProfiler<OAIPS extends OAIPMHProfilerSetting> extends Profiler<OAIPMHProfilerSetting> {

    public static final String SCHEMA_LOCATION = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";

    public OAIPMHProfiler() {
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	DiscoveryHandler<String> discoveryHandler = new DiscoveryHandler<String>();
	discoveryHandler.setRequestTransformer(getWebRequestTransformer());
	discoveryHandler.setMessageResponseMapper(getResultSetMapper());
	discoveryHandler.setMessageResponseFormatter(new OAIPMHResultSetFormatter());

	OAIPMRequestFilter filter = new OAIPMRequestFilter();
	filter.addQueryCondition("verb=ListRecords", InspectionStrategy.LIKE_MATCH);
	filter.addQueryCondition("verb=ListIdentifiers", InspectionStrategy.LIKE_MATCH);
	filter.addQueryCondition("verb=GetRecord", InspectionStrategy.LIKE_MATCH);

	selector.register(filter, discoveryHandler);

	selector.register(//
		new OAIPMRequestFilter("verb=ListSets", InspectionStrategy.EXACT_MATCH), //
		getListSetsHandler());

	selector.register(//
		new OAIPMRequestFilter("verb=Identify", InspectionStrategy.EXACT_MATCH), //
		new OAIPMHIdentifyHandler());

	selector.register(//
		new OAIPMRequestFilter("verb=ListMetadataFormats", InspectionStrategy.LIKE_MATCH), //
		new OAIPMHListMetadataFormatsHandler());

	return selector;
    }

    protected OAIPMHListSetsHandler getListSetsHandler() {

	return new OAIPMHListSetsHandler();
    }

    protected OAIPMHRequestTransformer getWebRequestTransformer() {

	return new OAIPMHRequestTransformer(getSetting());
    }

    protected OAIPMHResultSetMapper getResultSetMapper() {

	return new OAIPMHResultSetMapper();
    }

    /**
     * @param status
     * @param message
     * @return
     */
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	OAIPMHerrorType errorType = new OAIPMHerrorType();
	errorType.setValue(message);
	// there is no error code for internal errors in OAI
	errorType.setCode(OAIPMHerrorcodeType.BAD_ARGUMENT);
	OAIPMHtype oaipmHtype = new OAIPMHtype();
	oaipmHtype.getError().add(errorType);
	try {
	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
	    oaipmHtype.setResponseDate(calendar);
	} catch (DatatypeConfigurationException ex) {
	}

	String string = "";
	try {
	    string = CommonContext.asString(oaipmHtype, false, new OAIPMHNameSpaceMapper());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return Response.status(status).type(MediaType.APPLICATION_XML).entity(ex.getMessage()).build();
	}

	return Response.status(status).type(MediaType.APPLICATION_XML).entity(string).build();
    }

    /**
     * This cannot happens, the transformer checks the request
     */
    protected Response onHandlerNotFound(WebRequest webRequest) {

	return createUncaughtError(webRequest, Status.BAD_REQUEST, "Unsupported OAI request");
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	OAIPMHerrorType errorType = new OAIPMHerrorType();
	errorType.setValue(message.getError());
	errorType.setCode(OAIPMHerrorcodeType.fromValue(message.getErrorCode()));
	OAIPMHtype oaipmHtype = new OAIPMHtype();
	oaipmHtype.getError().add(errorType);
	try {
	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
	    oaipmHtype.setResponseDate(calendar);
	} catch (DatatypeConfigurationException ex) {
	}

	String string = "";
	try {

	    string = CommonContext.asString(oaipmHtype, false, new OAIPMHNameSpaceMapper(), SCHEMA_LOCATION);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	return Response.status(Status.OK).type(MediaType.APPLICATION_XML).entity(string).build();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected OAIPMHProfilerSetting initSetting() {

	return new OAIPMHProfilerSetting();
    }
}
