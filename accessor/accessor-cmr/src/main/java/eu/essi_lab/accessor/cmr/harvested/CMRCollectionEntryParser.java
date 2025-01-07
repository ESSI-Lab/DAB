package eu.essi_lab.accessor.cmr.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionEntryParser;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CMRCollectionEntryParser {

    private final XMLDocumentReader xml;
    private Logger logger = GSLoggerFactory.getLogger(CWICCMRCollectionEntryParser.class);
    private static final String CMRATOMECOLLECTIONENTRY_XPATH_ERROR = "CWICATOMECOLLECTIONENTRY_XPATH_ERROR";
    private static final String CMRATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR = "CWICATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR";

    private static final String CLIENT_ID_KEY = "clientId=";
    private static final String AND = "&";
    private static final String DATA_CENTER_KEY = "dataCenter=";
    private static final String SHORT_NAME_KEY = "shortName=";
    private static final String VERSION_ID_KEY = "versionId=";
    private String base;
    private static final String DATASETID_KEY = "datasetId=";

    public CMRCollectionEntryParser(InputStream inputStream, String cmrOSDDBaseUrl) throws GSException {

	try {
	    xml = new XMLDocumentReader(inputStream);
	} catch (SAXException | IOException e) {

	    logger.error("Can't create xml parser for cmr collection atom", e);

	    throw GSException.createException(getClass(), "Failed instantiating xml parser ", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRATOMECOLLECTIONENTRY_XPATH_ERROR, e);
	}

	base = cmrOSDDBaseUrl;

	if (!base.endsWith("?"))
	    base += "?";
    }

    public String getSecondLevelOpenSearchDD(String clientId) throws GSException {
	try {

	    Node entry = xml.evaluateNode("//*:entry[1]");

	    String shortName = xml.evaluateString(entry, "*:shortName");

	    String dataCenter = xml.evaluateString(entry, "*:dataCenter");

	    String versionId = xml.evaluateString(entry, "*:versionId");

	    String identifier = xml.evaluateString(entry, "*:identifier");

	    if (shortName != null && !"".equalsIgnoreCase(shortName) && dataCenter != null && !"".equalsIgnoreCase(dataCenter)
		    && identifier != null && !"".equalsIgnoreCase(identifier)) {

		return buildOSDDUrl(clientId, identifier, shortName, dataCenter, versionId);

	    }

	} catch (XPathExpressionException e) {

	    logger.error("XPath exception evaluating osdd", e);

	    throw GSException.createException(getClass(), "Failed parsing of cmr osdd, xpath error", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRATOMECOLLECTIONENTRY_XPATH_ERROR, e);
	}

	throw GSException.createException(getClass(), "Failed parsing of cmr osdd, one of shortName dataCenter or identifier is missing",
		null, ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, CMRATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR);

    }

    private String buildOSDDUrl(String clientId, String datasetid, String shortName, String dataCenter, String versionId) {

	StringBuilder builder = new StringBuilder(base);

	builder.append(DATASETID_KEY).append(datasetid).append(AND);

	builder.append(DATA_CENTER_KEY).append(dataCenter).append(AND);

	builder.append(SHORT_NAME_KEY).append(shortName).append(AND);

	if (versionId != null && !"".equalsIgnoreCase(versionId) && !"Not Provided".equalsIgnoreCase(versionId))
	    builder.append(VERSION_ID_KEY).append(versionId).append(AND);

	builder.append(CLIENT_ID_KEY).append(clientId);

	return builder.toString();
    }

}
