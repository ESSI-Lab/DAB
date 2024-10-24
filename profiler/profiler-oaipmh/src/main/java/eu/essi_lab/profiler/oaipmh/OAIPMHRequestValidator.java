package eu.essi_lab.profiler.oaipmh;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.jaxb.oaipmh.OAIPMHerrorcodeType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHRequestTransformer;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;
import eu.essi_lab.profiler.oaipmh.token.ResumptionToken;

public class OAIPMHRequestValidator implements WebRequestValidator {

    private static final String OAI_VALIDATION_INVALID_POST_BODY = "OAI_VALIDATION_INVALID_POST_BODY";
    private static final String POST_VALIDATION_EMPTY_BODY = "POST_VALIDATION_EMPTY_BODY";
    private boolean serviceInfoValidation;

    public ValidationMessage validate(WebRequest request) throws GSException {

	boolean getRequest = request.isGetRequest();

	if (getRequest) {

	    String queryString = request.getQueryString();

	    if (queryString == null) {

		ValidationMessage message = new ValidationMessage();
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Query part missing");

		return message;
	    }

	    return doGetRequestValidation(request, queryString);
	}

	InputStream body = request.getBodyStream().clone();

	if (body == null) {

	    throw GSException.createException(getClass(), "Request body missing", null, ErrorInfo.ERRORTYPE_CLIENT,
		    ErrorInfo.SEVERITY_ERROR, POST_VALIDATION_EMPTY_BODY);
	}

	return doPostRequestValidation(request, body);
    }

    public void setServiceInfoValidation() {

	this.serviceInfoValidation = true;
    }

    protected ValidationMessage doGetRequestValidation(WebRequest request, String queryString) throws GSException {

	KeyValueParser keyValueParser = new KeyValueParser(queryString, true);
	return validate(request.getRequestId(), keyValueParser.getParametersMap());
    }

    protected ValidationMessage doPostRequestValidation(WebRequest request, InputStream body) throws GSException {

	KeyValueParser parser = new KeyValueParser(true);
	try {
	    String queryString = OAIPMRequestFilter.extractQueryString(body);
	    parser.setQueryString(queryString);

	    return validate(request.getRequestId(), parser.getParametersMap());

	} catch (IOException e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    OAI_VALIDATION_INVALID_POST_BODY, //
		    e);
	}
    }

    private List<String> getHarvestedSourcesIds() throws GSException {

	List<GSSource> allSources = ConfigurationWrapper.getAllSources();

	ArrayList<String> ids = new ArrayList<String>();
	for (GSSource gsSource : allSources) {
	    if (gsSource.getBrokeringStrategy() == BrokeringStrategy.HARVESTED) {
		ids.add(gsSource.getUniqueIdentifier());
	    }
	}

	return ids;
    }

    private ValidationMessage validate(String requestId, Map<String, String> parameters) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setErrorCode(OAIPMHerrorcodeType.BAD_ARGUMENT.value());
	message.setResult(ValidationResult.VALIDATION_FAILED);

	if (!serviceInfoValidation) {

	    List<String> ids = getHarvestedSourcesIds();
	    if (ids.isEmpty()) {

		message.setErrorCode(OAIPMHerrorcodeType.NO_RECORDS_MATCH.value());
		message.setError("Metadata repository is empty");

		return message;
	    }
	}

	String verb = parameters.get("verb");
	if (verb == null) {

	    message.setErrorCode(OAIPMHerrorcodeType.BAD_VERB.value());
	    message.setError("Missing verb");

	    return message;
	}

	VerbType verbType = null;
	try {

	    verbType = VerbType.fromValue(verb);
	} catch (IllegalArgumentException ex) {

	    message.setErrorCode(OAIPMHerrorcodeType.BAD_VERB.value());
	    message.setError("Illegal OAI verb: " + verb);
	    return message;
	}

	try {

	    switch (verbType) {
	    case IDENTIFY:
		checkParameters(parameters, "verb");
		checkMandatoryParameters(parameters, "verb");
		break;
	    case LIST_SETS:
		checkParameters(parameters, "verb", "resumptionToken");
		if (parameters.get("resumptionToken") == null) {
		    checkMandatoryParameters(parameters, "verb");
		}
		break;
	    case LIST_METADATA_FORMATS:
		checkParameters(parameters, "verb", "identifier");
		checkMandatoryParameters(parameters, "verb");
		break;
	    case GET_RECORD:

		checkParameters(parameters, "verb", "identifier", "metadataPrefix");
		checkMandatoryParameters(parameters, "verb", "identifier", "metadataPrefix");
		if (!checkMedataPrefix(parameters.get("metadataPrefix"))) {
		    message.setErrorCode(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT.value());
		    message.setError("Unsupported format: " + parameters.get("metadataPrefix"));

		    return message;
		}

		break;
	    case LIST_IDENTIFIERS:
	    case LIST_RECORDS:

		checkParameters(parameters, "verb", "from", "until", "set", "resumptionToken", "metadataPrefix");

		String from = parameters.get("from");
		String until = parameters.get("until");

		if (from != null && !from.equals("") && until != null && !until.equals("")) {
		    if (from.contains("T") && !until.contains("T") || !from.contains("T") && until.contains("T")) {

			message.setError("The request has different granularities for the from and until parameters");
			return message;
		    }
		}

		if (parameters.get("resumptionToken") == null) {
		    checkMandatoryParameters(parameters, "verb", "metadataPrefix");
		    if (!checkMedataPrefix(parameters.get("metadataPrefix"))) {
			message.setErrorCode(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT.value());
			message.setError("Unsupported format: " + parameters.get("metadataPrefix"));

			return message;
		    }
		} else {

		    String tokenValue = parameters.get("resumptionToken");
		    try {
			ResumptionToken resumptionToken = new ResumptionToken(tokenValue);

			if (ResumptionToken.isExpired(resumptionToken.getId())) {

			    message.setErrorCode(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN.value());
			    message.setError("Expired resumption token");

			    return message;
			}
		    } catch (Exception ex) {
			message.setErrorCode(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN.value());
			message.setError(ex.getMessage());

			return message;
		    }
		}

		String set = parameters.get("set");
		if (set != null) {
		    if (!ConfigurationWrapper.checkSource(set)) {
			message.setError("Bad argument: a set with name " + set + " does not exist");
			return message;
		    }
		}

		boolean results = true;

		Date fromDate = null;
		Date untilDate = null;
		Date minDateStamp = null;
		Date maxDateStamp = null;

		try {
		    String min = OAIPMHRequestTransformer.getMinMaxDateStamp(requestId, BondOperator.MIN, set);
		    String max = OAIPMHRequestTransformer.getMinMaxDateStamp(requestId, BondOperator.MAX, set);

		    minDateStamp = ISO8601DateTimeUtils.parseISO8601(min);
		    maxDateStamp = ISO8601DateTimeUtils.parseISO8601(max);

		} catch (Exception e) {

		    message.setError("Bad argument: " + e.getMessage());
		    return message;
		}

		if (from != null) {
		    try {
			fromDate = ISO8601DateTimeUtils.parseISO8601(from);
			untilDate = null;

			if (until == null) {

			    untilDate = maxDateStamp;
			} else {

			    untilDate = ISO8601DateTimeUtils.parseISO8601(until);
			}

			if (untilDate.before(fromDate)) {
			    results = false;
			}
		    } catch (Exception e) {

			message.setError("Bad argument: " + e.getMessage());
			return message;
		    }
		}

		if (until != null) {
		    try {
			untilDate = ISO8601DateTimeUtils.parseISO8601(until);
			fromDate = null;

			if (from == null) {

			    fromDate = minDateStamp;
			} else {

			    fromDate = ISO8601DateTimeUtils.parseISO8601(from);
			}

			if (fromDate.after(untilDate)) {
			    results = false;
			}
		    } catch (Exception e) {

			message.setError("Bad argument: " + e.getMessage());
			return message;
		    }
		}

		if (fromDate != null) { // untilDate is also != null no need to check

		    if ((fromDate.before(minDateStamp) && untilDate.before(minDateStamp))
			    || (fromDate.after(maxDateStamp) && untilDate.after(maxDateStamp))) {

			results = false;
		    }
		}

		if (!results) {

		    message.setErrorCode(OAIPMHerrorcodeType.NO_RECORDS_MATCH.value());
		    message.setError("No records match with the present combination of from and until arguments");
		    return message;
		}
	    }
	} catch (IllegalArgumentException ex) {

	    message.setError(ex.getMessage());
	    return message;
	}

	ValidationMessage validMessage = new ValidationMessage();
	validMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return validMessage;
    }

    private boolean checkMedataPrefix(String prefix) {

	List<MetadataFormatType> formats = OAIPMHProfile.getAllSupportedMetadataFormats();

	for (MetadataFormatType format : formats) {
	    String metadataPrefix = format.getMetadataPrefix();
	    if (metadataPrefix.equals(prefix)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Throws an {@link IllegalArgumentException} whether
     * the keys list of the supplied <code>parameters</code> do not contains all the
     * code>mandatoryKeys</code>
     *
     * @param parameters
     * @param mandatoryKeys
     * @throws IllegalArgumentException
     */
    private void checkMandatoryParameters(Map<String, String> parameters, String... mandatoryKeys) throws IllegalArgumentException {

	for (String key : mandatoryKeys) {
	    String value = parameters.get(key);
	    if (value == null) {
		throw new IllegalArgumentException("Missing mandatory parameter: " + key);
	    }
	}
    }

    /**
     * Throws an {@link IllegalArgumentException} whether the list of keys the supplied <code>parameters</code> map
     * contains at least one key not contained in <code>validKeys</code>
     *
     * @param parameters
     * @param validKeys
     * @throws IllegalArgumentException
     */
    private void checkParameters(Map<String, String> parameters, String... validKeys) throws IllegalArgumentException {

	for (String key : parameters.keySet()) {
	    boolean valid = false;
	    for (String validKey : validKeys) {
		if (key.equals(validKey)) {
		    valid = true;
		}
	    }

	    if (!valid) {
		throw new IllegalArgumentException("Not a valid parameter: " + key);
	    }
	}
    }
}
