package eu.essi_lab.accessor.mapper;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.http.HttpResponse;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CWICCMRSecondLevelOSDDResolver {

    private final String osdDURL;

    private Logger logger = GSLoggerFactory.getLogger(CWICCMRSecondLevelOSDDResolver.class);
    private static final String CWICOSDD_READ_BAD_CODE_ERR = "CWICOSDD_READ_BAD_CODE_ERR";
    private static final String CWICOSDD_READ_IOEXC_ERR = "CWICOSDD_READ_IOEXC_ERR";
    private static final String CWICOSDD_XPATH_ERROR = "CWICOSDD_XPATH_ERROR";
    private static final String CWICOSDD_NOTEMPLATE_ERROR = "CWICOSDD_NOTEMPLATE_ERROR";
    private static final String BAD_OSDD_URI_ERR = "BAD_OSDD_URI_ERR";

    public CWICCMRSecondLevelOSDDResolver(String url) {
	osdDURL = url;
    }

    public String getSearchBaseUrl() throws GSException {

	Downloader downloader = new Downloader();

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(osdDURL);

	    int code = response.statusCode();

	    logger.trace("Retrieved http code {} from {}", code, osdDURL);

	    if (code - 200 != 0)

		throw GSException.createException(getClass(), "Failed validation of url " + osdDURL + " returned " + "http code is " + code,
			null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_WARNING, CWICOSDD_READ_BAD_CODE_ERR);

	    InputStream stream = response.body();

	    return parseOSDD(stream);

	} catch (Exception e) {

	    throw GSException.createException(getClass(), "CWICCMRSecondLevelOSDDResolver_getSearchBaseUrlError", e);
	}
    }

    public String parseOSDD(InputStream stream) throws GSException {

	try {

	    XMLDocumentReader xml = new XMLDocumentReader(stream);

	    Node urlEntry = xml.evaluateNode("//*:Url[1]");

	    if (urlEntry != null) {

		Node item = urlEntry.getAttributes().getNamedItem("template");

		return item.getTextContent();
	    }

	} catch (SAXException | IOException e) {

	    logger.error("Can't create xml parser for cmr osdd", e);

	    throw GSException.createException(getClass(), "Failed instantiating xml parser ", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CWICOSDD_XPATH_ERROR, e);

	} catch (XPathExpressionException e) {
	    logger.error("XPath exception evaluating osdd", e);

	    throw GSException.createException(getClass(), "Failed parsing of cmr osdd, xpath error", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CWICOSDD_XPATH_ERROR, e);

	}

	throw GSException.createException(getClass(), "No template found in osdd", null, ErrorInfo.ERRORTYPE_INTERNAL,
		ErrorInfo.SEVERITY_ERROR, CWICOSDD_NOTEMPLATE_ERROR);

    }
}
