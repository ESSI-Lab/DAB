package eu.essi_lab.profiler.csw.handler.discover;

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
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXParseException;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.schemas.BooleanValidationHandler;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.common.schemas.SchemaValidator;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractQueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.csw.CSWGetRecordsParser;
import eu.essi_lab.profiler.csw.CSWProfiler;
import eu.essi_lab.profiler.csw.CSWRequestMethodConverter;
import eu.essi_lab.profiler.csw.CSWRequestMethodConverter.CSWRequest;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWDescribeRecordHandler;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWGetCapabilitiesHandler;
import eu.essi_lab.profiler.csw.profile.CSWProfile;

/**
 * @author Fabrizio
 */
public class CSWRequestValidator implements WebRequestValidator {

    private static final String CSW_POST_REQUEST_VALIDATION_ERROR = "CSW_POST_REQUEST_VALIDATION_ERROR";

    public CSWRequestValidator() {
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	if (request.isGetRequest()) {

	    return doGetRequestValidation(request, request.getQueryString());
	}

	return doPostRequestValidation(request, request.getBodyStream());
    }

    /**
     * Validates GetCapabilities, DescribeRecord, GetRecordsById
     */
    protected ValidationMessage doGetRequestValidation(WebRequest webRequest, String queryString) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);

	KeyValueParser parser = new KeyValueParser(queryString);

	// ----------------------------------------
	//
	// mandatory service parameter, MUST be "CSW"
	//
	String service = parser.getValue("service", true);
	if (service == null || service.equals(KeyValueParser.UNDEFINED)) {

	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
	    message.setError("Missing mandatory service parameter");
	    message.setLocator("service");

	    return message;
	}

	service = service.toLowerCase();
	if (!service.equals("csw")) {

	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Unsupported service");
	    message.setLocator("service");

	    return message;
	}

	// ---------------------------
	//
	// mandatory request parameter
	//
	String request = parser.getValue("request", true);
	if (request == null) {

	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
	    message.setError("Missing mandatory request parameter");
	    message.setLocator("request");

	    return message;
	}

	request = request.toLowerCase();
	switch (request) {
	// ---------------------------
	//
	// GetCapabilities
	//
	case "getcapabilities":

	// --------------------------------------------
	//
	// mandatory version or AcceptVersions parameter, MUST be "2.0.2"
	//
	{
	    String version = parser.getValue("version", true);
	    String acceptVersions = parser.getValue("AcceptVersions", true);

	    if (version == null && acceptVersions == null) {

		message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
		message.setError("Missing mandatory version/AcceptVersions parameter");
		message.setLocator("version");

		return message;
	    }

	    if ((version != null && !version.equals("2.0.2")) || (acceptVersions != null && !acceptVersions.contains("2.0.2"))) {

		message.setErrorCode(ExceptionCode.VERSION_NEGOTIAION_FAILED.getCode());
		message.setError("Unsupported version");
		message.setLocator("version");

		return message;
	    }
	}

	    String sections = parser.getValue("Sections", true);
	    if (sections != null && !sections.equals(KeyValueParser.UNDEFINED)) {
		String[] split = sections.split(",");
		for (String section : split) {
		    if (!CSWGetCapabilitiesHandler.AVAILABLE_SECTIONS.contains(section)) {
			message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
			message.setError("Unsupported Section");
			message.setLocator("Sections");
			return message;
		    }
		}
	    }

	    String acceptVersions = parser.getValue("AcceptVersions", true);
	    if (acceptVersions != null) {
		String[] split = acceptVersions.split(",");
		boolean supported = false;
		for (String version : split) {
		    if (CSWProfiler.SUPPORTED_VERSIONS.contains(version)) {
			supported = true;
			continue;
		    }
		}
		if (!supported) {
		    message.setErrorCode(ExceptionCode.VERSION_NEGOTIAION_FAILED.getCode());
		    message.setError("Unsupported version");
		    message.setLocator("AcceptVersions");
		    return message;
		}
	    }

	    break;
	// ---------------------------
	//
	// DescribeRecord
	//
	case "describerecord": {

	    // --------------------------------------------
	    //
	    // mandatory version parameter, MUST be "2.0.2"
	    //
	    {
		String version = parser.getValue("version", true);

		if (version == null) {

		    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
		    message.setError("Missing mandatory version/AcceptVersions parameter");
		    message.setLocator("version");

		    return message;
		}

		if (version != null && !version.equals("2.0.2")) {

		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setError("Unsupported version");
		    message.setLocator("version");

		    return message;
		}
	    }

	    String outputFormat = parser.getValue("outputFormat", true);
	    if (outputFormat != null) {
		if (!CSWProfiler.SUPPORTED_OUTPUT_FORMATS.contains(outputFormat)) {
		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setError("Unsupported outputFormat");
		    message.setLocator("outputFormat");
		    return message;
		}
	    }

	    String schemaLanguage = parser.getValue("schemaLanguage", true);
	    if (schemaLanguage != null) {
		if (!CSWDescribeRecordHandler.SUPPORTED_SCHEMA_LANGUAGES.contains(schemaLanguage)) {
		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setError("Unsupported schemaLanguage");
		    message.setLocator("schemaLanguage");
		    return message;
		}
	    }

	    String typeName = parser.getDecodedValue("TypeName");
	    if (typeName != null) {

		List<QName> supportedTypes = CSWProfile.getAllSupportedTypeNames();
		ArrayList<String> supportedTypesString = new ArrayList<>();
		for (QName name : supportedTypes) {
		    supportedTypesString.add(name.getPrefix() + ":" + name.getLocalPart());
		}

		List<String> requestedNames = Arrays.asList(typeName.split(","));

		for (String reqName : requestedNames) {
		    if (!supportedTypesString.contains(reqName)) {

			message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
			message.setError("Unsupported TypeName");
			message.setLocator("TypeName");
			return message;
		    }
		}
	    }
	}
	    break;
	// --------------
	//
	// GetRecordById
	//
	case "getrecordbyid":

	// --------------------------------------------
	//
	// mandatory version parameter, MUST be "2.0.2"
	//
	{
	    String version = parser.getValue("version", true);

	    //
	    // see GIP-366
	    //
	    boolean cswisogeo = webRequest.//
		    getUriInfo().//
		    getPathSegments().//
		    stream().//
		    filter(s -> s.getPath().equals("cswisogeo")).//
		    findFirst().//
		    isPresent();

	    if (version == null && !cswisogeo) {

		message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
		message.setError("Missing mandatory version/AcceptVersions parameter");
		message.setLocator("version");

		return message;
	    }

	    if (version != null && !version.equals("2.0.2")) {

		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setError("Unsupported version");
		message.setLocator("version");

		return message;
	    }
	}

	    String outputFormat = parser.getValue("outputFormat", true);
	    if (outputFormat != null) {
		if (!CSWProfiler.SUPPORTED_OUTPUT_FORMATS.contains(outputFormat)) {
		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setError("Unsupported outputFormat");
		    message.setLocator("outputFormat");
		    return message;
		}
	    }

	    String outputSchema = parser.getValue("outputSchema", true);
	    if (outputSchema != null) {
		if (!CSWProfile.getAllSupportedOutputSchemas().contains(outputSchema)) {
		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setError("Unsupported outputSchema");
		    message.setLocator("outputSchema");
		    return message;
		}
	    }

	    String elementSetName = parser.getValue("ElementSetName", true);
	    if (elementSetName != null && !elementSetName.equalsIgnoreCase("full") && //
		    !elementSetName.equalsIgnoreCase("brief") && //
		    !elementSetName.equalsIgnoreCase("summary")) {

		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setError("Unsupported ElementSetName");
		message.setLocator("ElementSetName");
		return message;

	    }

	    String value = parser.getValue("id", true);
	    if (value == null || value.equals(KeyValueParser.UNDEFINED)) {
		message.setErrorCode(ExceptionCode.MISSING_PARAMETER.getCode());
		message.setError("Missing mandatory id parameter");
		message.setLocator("id");
		return message;
	    }

	    break;

	default:
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Unsupported request parameter value");
	    message.setLocator("request");
	    return message;
	}

	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return message;
    }

    /**
     * Validates GetRecords, GetCapabilities, DescribeRecord, GetRecordsById
     */
    protected ValidationMessage doPostRequestValidation(WebRequest request, ClonableInputStream stream) throws GSException {

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(stream.clone());
	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    String queryPart = null;

	    CSWRequestMethodConverter converter = new CSWRequestMethodConverter();

	    if (isGetCapabilitiesPOSTRequest(reader)) {
		queryPart = converter.convert(CSWRequest.GET_CAPABILITIES, stream);
	    } else if (isDescribeRecordPOSTRequest(reader)) {
		queryPart = converter.convert(CSWRequest.DESCRIBE_RECORD, stream);
	    } else if (isGetRecordByIdPOSTRequest(reader)) {
		queryPart = converter.convert(CSWRequest.GET_RECORD_BY_ID, stream);
	    } else if (isGetRecordsPOSTRequest(reader)) {
		return doGetRecordsValidation(request, stream);
	    }

	    if (queryPart != null) {
		return doGetRequestValidation(request, queryPart);
	    }

	} catch (SAXParseException spe) {
	    //
	    // in this case probably the request body is not a valid request
	    //
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_POST_REQUEST_VALIDATION_ERROR, e);
	}

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);
	message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	message.setError("Unrecognized request");

	return message;
    }

    private ValidationMessage doGetRecordsValidation(WebRequest webRequest, ClonableInputStream stream) {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	// ------------------------------------------------------------
	//
	// 1: Validates the GetRecords against the CSW Discovery schema
	//
	SchemaValidator schemaValidator = new SchemaValidator();
	CSWGetRecordsParser parser = null;
	try {
	    BooleanValidationHandler handler = schemaValidator.validate(stream.clone(), CommonSchemas.CSW_Discovery());
	    if (!handler.isValid()) {

		ValidationEvent event = handler.getEvent();
		String eventMessage = event.getMessage();

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Invalid GetRecords: " + eventMessage);
		message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
		return message;
	    }

	    parser = new CSWGetRecordsParser(stream.clone());

	} catch (JAXBException e) {
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError(e.getMessage());
	    message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	    return message;
	}

	// -------------------------------------------------------------
	//
	// 2: Validates the parsed GetRecords by checking its properties
	//
	GetRecords getRecords = parser.getGetRecords();
	JAXBElement<? extends AbstractQueryType> query = getRecords.getAbstractQuery();

	if (query == null) {
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Invalid GetRecords: missing Query part");
	    message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	    return message;
	}

	QueryType qType = (QueryType) query.getValue();

	// ------------
	//
	// OutputFormat
	//
	String format = getRecords.getOutputFormat();
	if (format != null) {
	    if (!CSWProfiler.SUPPORTED_OUTPUT_FORMATS.contains(format)) {
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Unsupported format: " + format);
		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setLocator("outputFormat");
		return message;
	    }
	}
	// -----------
	//
	// OutputShema
	//
	String outputSchema = getRecords.getOutputSchema();
	if (outputSchema != null) {
	    if (!CSWProfile.getAllSupportedOutputSchemas().contains(outputSchema)) {
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Unsupported outputSchema");
		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setLocator("outputSchema");
		return message;
	    }
	}

	// ---------------
	//
	// Query TypeNames
	//
	ArrayList<String> supportedTypesString = new ArrayList<>();
	for (QName name : CSWProfile.getAllSupportedTypeNames()) {
	    supportedTypesString.add(name.getPrefix() + ":" + name.getLocalPart());
	}

	List<QName> typeNames = qType.getTypeNames();

	if (typeNames == null || typeNames.isEmpty()) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Missing mandatory attribute Query typeNames");
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setLocator("typeNames");
	    return message;
	}

	for (QName name : typeNames) {

	    if (name == null) {
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("The prefix of one ore more Query@typeNames value refer/s is not bound");
		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setLocator("Query@typeNames");
		return message;
	    }

	    boolean found = false;
	    for (String supported : supportedTypesString) {
		String qName = name.getPrefix() + ":" + name.getLocalPart();
		if (qName.equals(supported)) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Unsupported type name/s");
		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setLocator("typeNames");
		return message;
	    }
	}

	// -----------------------------
	//
	// ElementName or ElementSetName
	//
	List<QName> names = qType.getElementNames();
	ElementSetName sName = qType.getElementSetName();
	ElementSetType setName = null;
	if (sName != null) {
	    setName = sName.getValue();
	}
	if (names.size() > 0 && setName != null) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Both Element and ElementSetName were specified");
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setLocator("ElementName/ElementSetName");
	    return message;
	}

	for (QName qName : names) {
	    if (qName == null) {
		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("The prefix of one ore more ElementName value refer/s is not bound");
		message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		message.setLocator("ElementName");
		return message;
	    }
	}

	if (sName != null) {

	    List<QName> elementSetTypes = sName.getTypeNames();
	    if (elementSetTypes.isEmpty()) {
		sName.getTypeNames().addAll(qType.getTypeNames());
	    }
	    // this set must be a subset of query typeNames (10.8.4.9)
	    for (QName resultName : elementSetTypes) {
		boolean found = false;
		for (QName queryName : qType.getTypeNames()) {
		    if (resultName.equals(queryName)) {
			found = true;
		    }
		}

		if (!found) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError(
			    "The names specified for the ElementSetName typeName attribute shall be a subset of the names specfied in the typeNames attribute of the Query element");
		    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
		    message.setLocator("ElementName/ElementSetName");

		    return message;
		}
	    }
	}

	// -----------------------
	//
	// 3: Validates the filter
	//
	try {
	    parser.parseFilter();

	} catch (IllegalArgumentException e) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError(e.getMessage());
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setLocator("Filter");
	    return message;

	} catch (GSException e) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError(e.getMessage());
	    message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	    return message;
	}

	return message;
    }

    private boolean isGetRecordsPOSTRequest(XMLDocumentReader reader) throws XPathExpressionException {

	return reader.evaluateBoolean("exists(//csw:GetRecords)");
    }

    private boolean isDescribeRecordPOSTRequest(XMLDocumentReader reader) throws XPathExpressionException {

	return reader.evaluateBoolean("exists(//csw:DescribeRecord)");
    }

    private boolean isGetRecordByIdPOSTRequest(XMLDocumentReader reader) throws XPathExpressionException {

	return reader.evaluateBoolean("exists(//csw:GetRecordById)");
    }

    private boolean isGetCapabilitiesPOSTRequest(XMLDocumentReader reader) throws XPathExpressionException {

	return reader.evaluateBoolean("exists(//csw:GetCapabilities)");
    }

}
