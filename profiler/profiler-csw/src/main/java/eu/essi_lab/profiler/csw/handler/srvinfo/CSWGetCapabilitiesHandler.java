package eu.essi_lab.profiler.csw.handler.srvinfo;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.google.common.io.ByteStreams;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.ows._1_0_0.DomainType;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.csw.CSWRequestMethodConverter;
import eu.essi_lab.profiler.csw.CSWRequestMethodConverter.CSWRequest;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
import eu.essi_lab.profiler.csw.profile.CSWProfile;

/**
 * @author Fabrizio
 */
public class CSWGetCapabilitiesHandler extends DefaultRequestHandler {

    protected static final String CSW_GET_CAPABILITIES_ERROR = "CSW_GET_CAPABILITIES_DOC_ERROR";

    /**
     * 
     */
    public static final ArrayList<String> AVAILABLE_SECTIONS = new ArrayList<>();

    static {

	AVAILABLE_SECTIONS.add("ServiceIdentification");
	AVAILABLE_SECTIONS.add("ServiceProvider");
	AVAILABLE_SECTIONS.add("OperationsMetadata");
	AVAILABLE_SECTIONS.add("Filter_Capabilities");
    }

    public CSWGetCapabilitiesHandler() {
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage validate = validator.validate(request);

	return validate;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	try {

	    // ------------------
	    //
	    // get the doc stream
	    //
	    InputStream docStream = getTemplate();

	    // -------------------------------------------------------
	    //
	    // inserts all the supported output schemas and type names
	    //
	    Capabilities capabilities = CommonContext.unmarshal(docStream, Capabilities.class);

	    decorateCapabilities(capabilities);

	    // ------------------------------
	    //
	    // handles the sections parameter
	    //
	    handleSectionsParameter(webRequest, capabilities);

	    // ----------------------------------
	    //
	    // returns the capabilities as string
	    //
	    ByteArrayInputStream capStream = CommonContext.asInputStream(capabilities, false);

	    String out = new String(ByteStreams.toByteArray(capStream));

	    // replaces the BASE_URL token with the right URL
	    String baseURL = webRequest.getUriInfo().getAbsolutePath().toString();
	    String forwardedProto = webRequest.getServletRequest().getHeader("x-forwarded-proto");
	    if (forwardedProto == null) {
		forwardedProto = webRequest.getServletRequest().getHeader("x-forwarded-protocol");
	    }
	    if (baseURL.startsWith("http://") && forwardedProto != null && forwardedProto.equals("https")) {
		baseURL = baseURL.replace("http://", "https://");
	    }
	    out = out.replaceAll("BASE_URL", baseURL);

	    return out;

	} catch (Exception e) {
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_CAPABILITIES_ERROR, e);
	}
    }

    protected InputStream getTemplate() {

	return getClass().getClassLoader().getResourceAsStream("templates/GetCapabilities.xml");
    }

    private void handleSectionsParameter(WebRequest webRequest, Capabilities capabilities) throws JAXBException {

	String queryString = null;
	if (webRequest.isGetRequest()) {
	    queryString = webRequest.getQueryString();
	} else {

	    CSWRequestMethodConverter converter = new CSWRequestMethodConverter();
	    queryString = converter.convert(CSWRequest.GET_CAPABILITIES, webRequest.getBodyStream());
	}

	KeyValueParser parser = new KeyValueParser(queryString);
	String sections = parser.getValue("sections", true);

	if (sections != null) {

	    if (sections.equals(KeyValueParser.UNDEFINED)) {
		sections = "Filter_Capabilities";
	    }

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    List<String> reqSections = new ArrayList(Arrays.asList(sections.split(",")));
	    if (!reqSections.contains("Filter_Capabilities")) {
		reqSections.add("Filter_Capabilities");
	    }

	    for (String section : AVAILABLE_SECTIONS) {
		if (!reqSections.contains(section)) {
		    switch (section) {
		    case "ServiceIdentification":
			capabilities.setServiceIdentification(null);
			break;
		    case "ServiceProvider":
			capabilities.setServiceProvider(null);
			break;
		    case "OperationsMetadata":
			capabilities.setOperationsMetadata(null);
			break;
		    case "Filter_Capabilities":
			capabilities.setFilterCapabilities(null);
			break;
		    }
		}
	    }
	}
    }

    private void decorateCapabilities(Capabilities capabilities) {

	List<String> schemas = CSWProfile.getAllSupportedOutputSchemas();
	List<QName> names = CSWProfile.getAllSupportedTypeNames();

	List<Operation> operations = capabilities.getOperationsMetadata().getOperation();
	for (Operation op : operations) {

	    if (op.getName().equals("DescribeRecord")) {
		List<DomainType> parameter = op.getParameter();
		for (DomainType domainType : parameter) {
		    String name = domainType.getName();
		    if (name.equalsIgnoreCase("typename")) {
			for (QName qName : names) {
			    domainType.getValue().add(qName.getPrefix() + ":" + qName.getLocalPart());
			}
		    }
		}
	    }

	    if (op.getName().equals("GetRecords")) {

		List<DomainType> parameter = op.getParameter();

		for (DomainType domainType : parameter) {
		    String name = domainType.getName();

		    if (name.equalsIgnoreCase("typenames")) {
			for (QName qName : names) {
			    domainType.getValue().add(qName.getPrefix() + ":" + qName.getLocalPart());
			}
		    }

		    if (name.equalsIgnoreCase("outputSchema")) {
			domainType.getValue().addAll(schemas);
		    }
		}
	    }

	    if (op.getName().equals("GetRecordById")) {

		List<DomainType> parameter = op.getParameter();

		for (DomainType domainType : parameter) {
		    String name = domainType.getName();

		    if (name.equalsIgnoreCase("outputSchema")) {
			domainType.getValue().addAll(schemas);
		    }
		}
	    }
	}

	List<DomainType> constraint = capabilities.getOperationsMetadata().getConstraint();
	for (DomainType domainType : constraint) {
	    if (domainType.getName().equalsIgnoreCase("isoprofiles")) {
		for (String schema : schemas) {
		    if (!schema.equals(CommonNameSpaceContext.CSW_NS_URI)) {
			domainType.getValue().add(schema);
		    }
		}
	    }
	}
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.valueOf("text/xml;charset=UTF-8");
    }
}
