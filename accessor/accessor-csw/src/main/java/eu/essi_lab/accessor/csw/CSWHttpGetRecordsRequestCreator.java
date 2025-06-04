package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractQueryType;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CSWHttpGetRecordsRequestCreator {

    private final String getRecordsURL;
    private HttpRequest baseRequest;
    private static final String CSW_CONNECTOR_GET_RECORDS_CREATION_ERROR = "CSW_CONNECTOR_GET_RECORDS_CREATION_ERROR";

    public CSWHttpGetRecordsRequestCreator(CSWConnector.Binding binding, CSWConnector connector, GetRecords getRecords) throws GSException {
	switch (binding) {
	case GET:
	    getRecordsURL = connector.getRecordsURLGET;
	    try {
		baseRequest = createRequestGET(getRecords, connector);
	    } catch (URISyntaxException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	    break;
	case POST_SOAP:
	    GSLoggerFactory.getLogger(CSWHttpGetRecordsRequestCreator.class).error("POST SOAP NOT IMPLEMENTED");
	    getRecordsURL = null;
	    baseRequest = null;
	    break;
	case POST_XML:
	default:
	    getRecordsURL = connector.getRecordsURLPOSTXML;
	    baseRequest = createRequestPOST(getRecords);
	    break;
	}

    }

    String findElementSetName(GetRecords getRecords) {

	String[] elementSetName = new String[] { "" };

	Optional.of(getRecords.getAbstractQuery()).ifPresent(aq -> {
	    AbstractQueryType value = aq.getValue();

	    if (value instanceof QueryType) {
		QueryType queryType = (QueryType) value;

		Optional.of(queryType.getElementSetName())
			.ifPresent(esn -> Optional.of(esn.getValue()).ifPresent(v -> elementSetName[0] = v.value()));

	    }
	});

	return elementSetName[0];

    }

    String findTyoeNames(GetRecords getRecords) {

	StringBuilder sb = new StringBuilder();

	Optional.of(getRecords.getAbstractQuery()).ifPresent(aq -> {
	    AbstractQueryType value = aq.getValue();

	    if (value instanceof QueryType) {
		QueryType queryType = (QueryType) value;

		queryType.getTypeNames()
			.forEach(typeName -> sb.append(typeName.getPrefix()).append(":").append(typeName.getLocalPart()).append(" "));

	    }
	});

	return sb.toString().trim().replace(" ", ",");

    }

    private HttpRequest createRequestGET(GetRecords getRecords, CSWConnector connector) throws URISyntaxException {

	String url = strip80Port(connector.normalizeURL(getRecordsURL));

	String elementSetName = findElementSetName(getRecords);

	String typeNames = findTyoeNames(getRecords);

	url += "service=CSW&request=GetRecords&version=2.0.2&outputFormat=" + getRecords.getOutputFormat() + "&outputSchema="
		+ getRecords.getOutputSchema() + "&ElementSetName=" + elementSetName + "&resultType=" + getRecords.getResultType().value()
		+ "&typeNames=" + typeNames + connector.getConstraintLanguageParameter() + "&startPosition=" + getRecords.getStartPosition()
		+ "&maxRecords=" + getRecords.getMaxRecords();

	return HttpRequestUtils.build(MethodNoBody.GET, url);
    }

    /**
     * The default 80 port can safely be always stripped, as it can give problem in general with some services, such as
     * the FAO service
     * (i.e. http://www.fao.org/geonetwork/srv/en/csw?)
     *
     * @param urlString
     * @return
     */
    private String strip80Port(String urlString) {
	try {
	    URL url = new URL(urlString);
	    if (url.getPort() == 80) {
		urlString = urlString.replace(":80", "");
	    }
	} catch (MalformedURLException e) {
	    GSLoggerFactory.getLogger(CSWHttpGetRecordsRequestCreator.class).error(e.getMessage(), e);
	}
	return urlString;

    }

    private HttpRequest createRequestPOST(GetRecords getRecords) throws GSException {

	String url = strip80Port(getGetRecordsUrl());

	ByteArrayInputStream stream = null;
	try {
	    stream = CommonContext.asInputStream(getRecords, false);

	    return HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    url, //
		    stream, //
		    HttpHeaderUtils.build(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML));

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    "Unable to get output schemas supported by the GetRecords operation", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_CONNECTOR_GET_RECORDS_CREATION_ERROR);
	}

    }

    public HttpRequest getHttpRequest() {
	return baseRequest;
    }

    public String getGetRecordsUrl() {
	return getRecordsURL;
    }

}
