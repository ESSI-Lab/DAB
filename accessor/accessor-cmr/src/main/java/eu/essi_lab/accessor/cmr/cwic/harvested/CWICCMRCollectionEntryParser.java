package eu.essi_lab.accessor.cmr.cwic.harvested;

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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CWICCMRCollectionEntryParser {

    private final XMLDocumentReader xml;
    private Logger logger = GSLoggerFactory.getLogger(CWICCMRCollectionEntryParser.class);
    private static final String CWICATOMECOLLECTIONENTRY_XPATH_ERROR = "CWICATOMECOLLECTIONENTRY_XPATH_ERROR";
    private static final String CWICATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR = "CWICATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR";

    public CWICCMRCollectionEntryParser(InputStream inputStream) throws GSException {
	try {
	    xml = new XMLDocumentReader(inputStream);
	} catch (SAXException | IOException e) {

	    logger.error("Can't create xml parser for cmr osdd", e);

	    throw GSException.createException(getClass(), "Failed instantiating xml parser ", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CWICATOMECOLLECTIONENTRY_XPATH_ERROR, e);
	}
    }

    public String getSecondLevelOpenSearchDD(String clientId) throws GSException {
	try {

	    Node entry = xml.evaluateNode("//*:entry[1]");

	    Node link = xml.evaluateNode(entry, "*:link[@type='application/opensearchdescription+xml']");

	    if (link != null) {
		Node item = link.getAttributes().getNamedItem("href");

		if (item.getTextContent().endsWith("clientId="))
		    return item.getTextContent() + clientId;

		return item.getTextContent();
	    }

	} catch (XPathExpressionException e) {

	    logger.error("XPath exception evaluating osdd", e);

	    throw GSException.createException(getClass(), "Failed parsing of cmr osdd, xpath error", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CWICATOMECOLLECTIONENTRY_XPATH_ERROR, e);
	}

	throw GSException.createException(getClass(), "Failed parsing of cmr osdd", null, ErrorInfo.ERRORTYPE_SERVICE,
		ErrorInfo.SEVERITY_ERROR, CWICATOMECOLLECTIONENTRY_NOGRANULESEARCH_FOUND_ERROR);

    }

}
