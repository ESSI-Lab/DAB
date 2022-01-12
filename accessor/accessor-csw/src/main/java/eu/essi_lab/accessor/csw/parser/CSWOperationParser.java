package eu.essi_lab.accessor.csw.parser;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.slf4j.Logger;

import eu.essi_lab.accessor.csw.CSWConnector;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.ows._1_0_0.DomainType;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.jaxb.ows._1_0_0.RequestMethodType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class CSWOperationParser {

    private final Operation operation;
    private String getRecordsURLPOSTXML;
    private String getRecordsURLPOSTSOAP;
    private String getRecordsURLGET;
    private List<String> supportedOutputSchemas = new ArrayList<>();
    private Map<String, QName> supportedTypesBySchema = new HashMap<>();
    private final CSWConnector connector;
    private transient Logger logger = GSLoggerFactory.getLogger(CSWOperationParser.class);
    private static final String OUTPUTSCHEMA_PARAMETER_NAME = "outputschema";
    private List<String> typenames = Arrays.asList("TypeName", "TypeNames");

    public CSWOperationParser(Operation op, CSWConnector cswConnector) {
	operation = op;
	connector = cswConnector;
    }

    public String getRecordsURLPOSTXML() {
	return getRecordsURLPOSTXML;
    }

    public String getRecordsURLPOSTSOAP() {
	return getRecordsURLPOSTSOAP;
    }

    public String getRecordsURLGET() {
	return getRecordsURLGET;
    }

    public List<String> getSupportedOutputSchemas() {
	return supportedOutputSchemas;
    }

    public Map<String, QName> getSupportedTypesBySchema() {
	return supportedTypesBySchema;
    }

    private String findEndpointByBinding(CSWConnector.Binding binding, String encoding) {

	String requestedBindingString;
	boolean[] matchEncoding = new boolean[] { false };

	switch (binding) {
	case GET:
	    requestedBindingString = "Get";
	    break;
	case POST_XML:
	case POST_SOAP:
	default:
	    requestedBindingString = "Post";
	    matchEncoding[0] = encoding != null;
	}

	List<JAXBElement<RequestMethodType>> methodTypeJAXBElements = findAllMethods();

	for (JAXBElement<RequestMethodType> methodTypeJAXBElement : methodTypeJAXBElements) {

	    if (requestedBindingString.equals(methodTypeJAXBElement.getName().getLocalPart())) {

		String href = getHref(methodTypeJAXBElement.getValue());

		if (matchEncoding[0]) {

		    if (matchEncoding(methodTypeJAXBElement.getValue(), encoding))
			return href;

		} else
		    return href;

	    }
	}

	return null;
    }

    private List<JAXBElement<RequestMethodType>> findAllMethods() {

	List<JAXBElement<RequestMethodType>> mlist = new ArrayList<>();

	Optional.of(operation.getDCP()).ifPresent(list -> list.stream().findFirst().ifPresent(dcp -> Optional.of(dcp.getHTTP())
		.ifPresent(http -> Optional.of(http.getGetOrPost()).ifPresent(l -> l.forEach(mlist::add)))));

	return mlist;

    }

    private boolean matchEncoding(RequestMethodType methodType, String encoding) {

	final boolean[] matched = new boolean[] { false };

	Optional.of(methodType.getConstraint()).ifPresent(list -> {

	    Optional<DomainType> withEncoding = list.stream().filter(constraint -> "PostEncoding".equals(constraint.getName())).findFirst();

	    withEncoding.ifPresent(constraint -> Optional.of(constraint.getValue()).ifPresent(encodingsList -> {

		Optional<String> found = encodingsList.stream().filter(encoding::equalsIgnoreCase).findFirst();

		found.ifPresent(v -> matched[0] = true);
	    }));

	});

	return matched[0];
    }

    private String getHref(RequestMethodType methodType) {

	String href = methodType.getHref();

	if (href != null && !href.contains("localhost")) { // some not configured Geonetworks

	    return href;

	} else {
	    // point to localhost

	    return connector.normalizeURL(connector.getSourceURL());

	}
    }

    private void parseEndpoints() {

	String getEndpoint = findEndpointByBinding(CSWConnector.Binding.GET, null);

	String postXmlEndpoint = findEndpointByBinding(CSWConnector.Binding.POST_XML, "xml");

	if (postXmlEndpoint == null)
	    postXmlEndpoint = findEndpointByBinding(CSWConnector.Binding.POST_XML, null);

	String postSoapEndpoint = findEndpointByBinding(CSWConnector.Binding.POST_SOAP, "soap");

	if (postSoapEndpoint == null)
	    postSoapEndpoint = findEndpointByBinding(CSWConnector.Binding.POST_SOAP, null);

	if (getEndpoint != null)
	    getRecordsURLGET = getEndpoint;

	if (postXmlEndpoint != null) {
	    getRecordsURLPOSTXML = postXmlEndpoint;
	}
	if (postSoapEndpoint != null) {
	    getRecordsURLPOSTSOAP = postSoapEndpoint;
	}

	if (getRecordsURLGET == null && getRecordsURLPOSTXML == null && getRecordsURLPOSTSOAP == null) {
	    getRecordsURLGET = connector.normalizeURL(connector.getSourceURL());
	    getRecordsURLPOSTXML = connector.normalizeURL(connector.getSourceURL());
	    getRecordsURLPOSTSOAP = connector.normalizeURL(connector.getSourceURL());
	}
    }

    private void parseSchemas() {

	operation.getParameter().stream().filter(parameter -> OUTPUTSCHEMA_PARAMETER_NAME.equalsIgnoreCase(parameter.getName()))
		.forEach(p ->

	p.getValue().forEach(v -> supportedOutputSchemas.add(v))

	);

    }

    private boolean matchTypeNameParameterName(String name) {

	Optional<String> optional = typenames.stream().filter(n -> n.equalsIgnoreCase(name)).findFirst();

	return optional.isPresent();

    }

    private void parseTypeNames() {

	operation.getParameter().//
		stream().//
		filter(parameter -> matchTypeNameParameterName(parameter.getName())).//
		forEach(p ->

	p.getValue().forEach(value -> {

	    if (value.contains(":")) {
		String[] split = value.split(":");
		String prefix = split[0];
		String localname = split[1];

		Optional.ofNullable(capabilitiesString()).ifPresent(capString -> {

		    int prefixIndex = capString.indexOf("xmlns:" + prefix);

		    if (prefixIndex != -1) {

			String subCap = capString.substring(prefixIndex);

			subCap = subCap.substring(subCap.indexOf('\"') + 1);

			String uri = subCap.substring(0, subCap.indexOf('\"'));

			QName qname = new QName(uri, localname, prefix);

			this.supportedTypesBySchema.put(uri, qname);
		    }
		}
		);
	    }
	})
	);
    }

    String capabilitiesString() {
	try {
	    return CommonContext.asString(connector.getCapabilities(connector.getSourceURL(), true), true);
	} catch (JAXBException | UnsupportedEncodingException e) {
	    logger.error("Exception retrieving capabilities from connector", e);
	} catch (GSException gse) {

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(gse)));
	}

	return null;
    }

    public void parse() {

	parseEndpoints();

	parseSchemas();

	parseTypeNames();
    }
}
