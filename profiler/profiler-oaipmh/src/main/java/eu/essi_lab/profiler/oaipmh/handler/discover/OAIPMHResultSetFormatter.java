package eu.essi_lab.profiler.oaipmh.handler.discover;

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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.GetRecordType;
import eu.essi_lab.jaxb.oaipmh.ListIdentifiersType;
import eu.essi_lab.jaxb.oaipmh.ListRecordsType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHerrorType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHerrorcodeType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHtype;
import eu.essi_lab.jaxb.oaipmh.RequestType;
import eu.essi_lab.jaxb.oaipmh.ResumptionTokenType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.oaipmh.OAIPMHNameSpaceMapper;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfiler;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestReader;
import eu.essi_lab.profiler.oaipmh.OAIPMRequestFilter;
import eu.essi_lab.profiler.oaipmh.token.ResumptionToken;

public class OAIPMHResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * The encoding name of {@link #OAI_PMH_FORMATTING_ENCODING}
     */
    public static final String OAI_PMH_FORMATTING_ENCODING_NAME = "oai-pmh-frm-encoding";
    /**
     * The encoding version of {@link #OAI_PMH_FORMATTING_ENCODING}
     */
    public static final String OAI_PMH_FORMATTING_ENCODING_VERSION = "2.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding OAI_PMH_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	OAI_PMH_FORMATTING_ENCODING.setEncoding(OAI_PMH_FORMATTING_ENCODING_NAME);
	OAI_PMH_FORMATTING_ENCODING.setEncodingVersion(OAI_PMH_FORMATTING_ENCODING_VERSION);
	OAI_PMH_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    private static final String OAI_PMH_RECORDS_RESPONSE_BUILDING_ERROR = "OAI_PMH_RECORDS_RESPONSE_BUILDING_ERROR";

    @SuppressWarnings("incomplete-switch")
    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	OAIPMHtype response = new OAIPMHtype();
	OAIPMHRequestReader reader = OAIPMHRequestTransformer.createReader(message.getWebRequest());

	// ------------------------------------------
	//
	// set the response date
	//
	try {
	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
	    response.setResponseDate(calendar);
	} catch (DatatypeConfigurationException ex) {
	    // it should not happen, and even if if happens, it is not so bad. a log a warning should be enough
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to create XMLGregorianCalendar", ex);
	}

	// -------------------------------------------
	//
	// creates the resumption token
	//
	//
	int totalResults = mappedResultSet.getCountResponse().getCount();

	int advancement = 1;

	String prefix = reader.getMetadataPrefix();
	String resumptionToken = reader.getResumptionToken();
	String tokenId = null;

	if (resumptionToken != null) {

	    ResumptionToken rt = ResumptionToken.of(resumptionToken);

	    advancement = rt.getAdvancement();
	    prefix = rt.getMetadataPrefix();
	    tokenId = rt.getId();
	}

	ResumptionTokenType rtt = null;

	if (totalResults < advancement + message.getPage().getSize()) {

	    // the tokenId is null if this is the first request (and in this case also the last)
	    // so a resumption token has never been created before and since this is the last request
	    // there is no need to create a token id (it will be never used)
	    if (tokenId == null) {
		tokenId = "";
	    }
	    rtt = ResumptionToken.createEmpty(tokenId, totalResults, advancement);

	} else {

	    String searchAfter = mappedResultSet.//
		    getSearchAfter().//
		    map(v -> v.toStringValue().orElse(ResumptionToken.NONE_SEARCH_AFTER)).//
		    orElse(ResumptionToken.NONE_SEARCH_AFTER);

	    rtt = ResumptionToken.of(//
		    reader, //
		    tokenId, //
		    totalResults, //
		    advancement, //
		    message.getPage().getSize(), //
		    prefix, //
		    searchAfter);
	}

	// to let harvesting stop after the first page decomment the following:
	// rtt = ResumptionToken.createEmpty(tokenId, OAIPMHRequestTransformer.DEFAULT_ITEMS_PER_PAGE,
	// OAIPMHRequestTransformer.DEFAULT_ITEMS_PER_PAGE);

	List<String> recordsList = mappedResultSet.getResultsList();

	// -------------------------------
	//
	// set the request type
	//
	RequestType requestType = createRequestType(message, reader);
	response.setRequest(requestType);

	// -----------------------------------------------------------
	//
	// set the response type, the resumption token and the results
	//

	String results = "";

	switch (VerbType.fromValue(reader.getVerb())) {
	case LIST_RECORDS:
	    response.setListRecords(new ListRecordsType());
	    response.getListRecords().setResumptionToken(rtt);
	    for (String recordType : recordsList) {
		if (!recordType.isEmpty()) {
		    results += recordType;
		}
	    }
	    break;
	case LIST_IDENTIFIERS:
	    response.setListIdentifiers(new ListIdentifiersType());
	    response.getListIdentifiers().setResumptionToken(rtt);
	    for (String recordType : recordsList) {
		results += recordType;
	    }
	    break;
	case GET_RECORD:
	    response.setGetRecord(new GetRecordType());
	    if (!recordsList.isEmpty()) {
		results += recordsList.get(0);
	    } else {
		OAIPMHerrorType errorType = new OAIPMHerrorType();
		errorType.setValue("Unknown identifier argument");
		errorType.setCode(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST);

		response.getError().add(errorType);
		try {
		    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar();
		    response.setResponseDate(calendar);
		} catch (DatatypeConfigurationException ex) {
		}
	    }
	    break;
	}

	// -------------------------------
	//
	// creates the response
	//
	ResponseBuilder builder = Response.status(Status.OK);
	try {

	    String asString = CommonContext.asString(response, false, new OAIPMHNameSpaceMapper(), OAIPMHProfiler.SCHEMA_LOCATION);

	    switch (VerbType.fromValue(reader.getVerb())) {
	    case LIST_RECORDS:
		asString = asString.replace("<ListRecords>", "<ListRecords>" + results);
		break;
	    case LIST_IDENTIFIERS:
		asString = asString.replace("<ListIdentifiers>", "<ListIdentifiers>" + results);
		break;
	    case GET_RECORD:
		asString = asString.replace("<GetRecord/>", "<GetRecord>" + results + "</GetRecord>");
		break;
	    }

	    builder = builder.entity(asString);
	    builder = builder.type(MediaType.APPLICATION_XML);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    OAI_PMH_RECORDS_RESPONSE_BUILDING_ERROR, //
		    e);
	}

	return builder.build();
    }

    private RequestType createRequestType(DiscoveryMessage message, OAIPMHRequestReader reader) {

	RequestType requestType = new RequestType();
	requestType.setVerb(VerbType.fromValue(reader.getVerb()));
	requestType.setFrom(reader.getFrom());
	requestType.setUntil(reader.getUntil());
	requestType.setIdentifier(reader.getIdentifier());
	requestType.setMetadataPrefix(reader.getMetadataPrefix());
	requestType.setSet(reader.getSet());

	String queryString = null;
	if (message.getWebRequest().isGetRequest()) {
	    queryString = message.getWebRequest().getQueryString();

	} else {
	    try {
		queryString = OAIPMRequestFilter.extractQueryString(message.getWebRequest().getBodyStream().clone());
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	requestType.setValue(queryString);

	return requestType;
    }

    @Override
    public FormattingEncoding getEncoding() {

	return OAI_PMH_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
