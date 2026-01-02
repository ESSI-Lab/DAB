package eu.essi_lab.accessor.fedeo.harvested;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roberto
 */
public class FEDEOOriginalMDWrapper {

    private static final String WRAPPED_ROOT_ELEMENT_NAME = "MD_Metadata";
    private static final String WRAPPED_URL_ELEMENT_NAME = "FEDEOurl";
    private Logger logger = GSLoggerFactory.getLogger(FEDEOOriginalMDWrapper.class);
    private static final String FEDEO_WRAPPER_ROOT_ERROR = "FEDEO_WRAPPER_ROOT_ERROR";
    private static final String FEDEO_WRAPPER_URL_ELEM_ERROR = "FEDEO_WRAPPER_URL_ELEM_ERROR";
    private static final String FEDEO_WRAPPER_ADD_NODE_ERROR = "FEDEO_WRAPPER_ADD_NODE_ERROR";
    private static final String FEDEO_WRAPPER_URL_ELEM_NOT_FOUND_ERROR = "FEDEO_WRAPPER_URL_ELEM_NOT_FOUND_ERROR";

    private static final String UTF8_CHARSET = "UTF-8";
    private static final String FEDEO_WRAPPED_MD_READ_ERROR = "FEDEO_WRAPPED_MD_READ_ERROR";
    private static final String FEDEO_WRAPPED_ORIGINAL_MD_NOT_FOUND_ERROR = "FEDEO_WRAPPED_ORIGINAL_MD_NOT_FOUND_ERROR";

    // public OriginalMetadata wrap(OriginalMetadata mdRecord, String url) throws GSException {
    //
    // String md = mdRecord.getMetadata();
    //
    // Document root = createRoot();
    //
    // Document urlNode = createUrlElement(url);
    //
    // try {
    //
    // ByteArrayInputStream stream = new ByteArrayInputStream(md.getBytes(UTF8_CHARSET));
    //
    // XMLDocumentReader originalMdReader = new XMLDocumentReader(stream);
    //
    // XMLDocumentReader wreppedReader = new XMLDocumentReader(root);
    //
    // XMLDocumentWriter writer = new XMLDocumentWriter(wreppedReader);
    //
    // writer.addNode("/" + WRAPPED_ROOT_ELEMENT_NAME, urlNode.getDocumentElement());
    //
    // writer.addNode("/" + WRAPPED_ROOT_ELEMENT_NAME, originalMdReader.getDocument().getDocumentElement());
    //
    // OriginalMetadata omd = new OriginalMetadata();
    //
    // omd.setSchemeURI(mdRecord.getSchemeURI());
    //
    // omd.setMetadata(XMLDocumentReader.asString(wreppedReader.getDocument()));
    //
    // return omd;
    //
    // } catch (SAXException | IOException | XPathExpressionException | TransformerException e) {
    //
    // logger.error("Can't append url element for wrapping FEDEO original metadata", e);
    //
    // throw GSException.createException(getClass(), "Can't append url element for wrapping FEDEO original metadata",
    // null,
    // ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPER_ADD_NODE_ERROR, e);
    // }
    //
    // }

    private Document createUrlElement(String url) throws GSException {

	String urlElem = "<" + WRAPPED_URL_ELEMENT_NAME + " href=\"" + url.replace("&", "&amp;") + "\"></" + WRAPPED_URL_ELEMENT_NAME + ">";

	logger.trace("Creating url element: {}", urlElem);

	try {
	    ByteArrayInputStream stream = new ByteArrayInputStream(urlElem.getBytes(UTF8_CHARSET));

	    DocumentBuilderFactory builderFactory = XMLFactories.newDocumentBuilderFactory();

	    builderFactory.setNamespaceAware(false);

	    DocumentBuilder builder = builderFactory.newDocumentBuilder();

	    return builder.parse(stream);

	} catch (IOException | SAXException | ParserConfigurationException e) {

	    logger.error("Can't create url element for wrapping FEDEO original metadata", e);

	    throw GSException.createException(getClass(), "Can't create url element for wrapping FEDEO original metadata", null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPER_URL_ELEM_ERROR, e);
	}
    }

    private Document createRoot() throws GSException {

	String root = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><" + WRAPPED_ROOT_ELEMENT_NAME + "></"
		+ WRAPPED_ROOT_ELEMENT_NAME + ">";

	logger.trace("Creating root element: {}", root);

	try {

	    ByteArrayInputStream stream = new ByteArrayInputStream(root.getBytes(UTF8_CHARSET));

	    DocumentBuilderFactory builderFactory = XMLFactories.newDocumentBuilderFactory();

	    builderFactory.setNamespaceAware(false);

	    DocumentBuilder builder = builderFactory.newDocumentBuilder();

	    return builder.parse(stream);

	} catch (IOException | SAXException | ParserConfigurationException e) {

	    logger.error("Can't create root element for wrapping FEDEO original metadata", e);

	    throw GSException.createException(getClass(), "Can't create root element for wrapping FEDEO original metadata", null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPER_ROOT_ERROR, e);

	}

    }

    public String getUrl(OriginalMetadata originalMetadata) throws GSException {

	String metadata = originalMetadata.getMetadata();

	try {
	    XMLDocumentReader reader = new XMLDocumentReader(metadata);

	    return reader.evaluateNode("//*:" + WRAPPED_URL_ELEMENT_NAME).getAttributes().getNamedItem("href").getTextContent();

	} catch (SAXException | IOException | XPathExpressionException e) {
	    logger.error("Can't read FEDEO url element", e);

	    throw GSException.createException(getClass(), "Can't read FEDEO url element", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPER_URL_ELEM_NOT_FOUND_ERROR, e);
	}

    }

    public OriginalMetadata getOriginalMetadata(OriginalMetadata md) throws GSException {

	String metadata = md.getMetadata();

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(metadata);

	    String ossdURL = reader.evaluateString("//*:link[@type='application/opensearchdescription+xml']/@href");

//	    if (ossdURL == null || ossdURL.isEmpty()) {
//		XMLDocumentWriter writer = new XMLDocumentWriter(reader);
//		writer.setText("//*:hierarchyLevel/*:MD_ScopeCode", "dataset");
//		writer.setText("//*:hierarchyLevel/*:MD_ScopeCode/@codeListValue", "dataset");
//	    }

	    Node root = reader.evaluateNode("//*:" + WRAPPED_ROOT_ELEMENT_NAME);
	    String original = XMLDocumentReader.asString(root);

	    OriginalMetadata omd = new OriginalMetadata();

	    omd.setSchemeURI(md.getSchemeURI());

	    omd.setMetadata(original);

	    return omd;

	    // NodeList children = root.getChildNodes();
	    //
	    // for (int i = 0; i < children.getLength(); i++) {
	    //
	    // Node node = children.item(i);
	    //
	    // if (node.getLocalName() != null && !node.getLocalName().equalsIgnoreCase(WRAPPED_URL_ELEMENT_NAME)) {
	    // String original = XMLDocumentReader.asString(node);
	    //
	    // OriginalMetadata omd = new OriginalMetadata();
	    //
	    // omd.setSchemeURI(md.getSchemeURI());
	    //
	    // omd.setMetadata(original);
	    //
	    // return omd;
	    // }

	    // }

	} catch (SAXException | IOException | XPathExpressionException | TransformerException e) {
	    logger.error("Can't read wrapped metadata", e);

	    throw GSException.createException(getClass(), "Can't read wrapped metadata", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPED_MD_READ_ERROR, e);

	}

	// logger.error("Can't find original metadata from \n{}", md.getMetadata());
	//
	// throw GSException.createException(getClass(), "Can't find original metadata from \n " + md.getMetadata(),
	// null,
	// ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, FEDEO_WRAPPED_ORIGINAL_MD_NOT_FOUND_ERROR);

    }
}
