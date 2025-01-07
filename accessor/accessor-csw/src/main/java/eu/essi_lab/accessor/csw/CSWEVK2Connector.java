package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CSWEVK2Connector extends CSWConnector {

    private static final String CSWEVK2CONNECTOR_EXTRACTION_ERROR = "CSWEVK2CONNECTOR_EXTRACTION_ERROR";
    private static final String GCO_DECIMAL = "gco:Decimal";

    /**
     * 
     */
    public static final String TYPE = "CSW EVK2 Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * This fix is needed to tweak the metadata records returned by GEOSS services #29. As they are not valid. In
     * particular, the
     * AbstractMD_Identification !? element is used causing a JAXB error. The bounding box is also incorrectly specified
     * (missing the
     * gco:Decimal element, multiple west bound longitude elements). So, the incriminated element is removed and its
     * content merged with the
     * regular MD_DataIdentification. So, the bounding box is fixed.
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    /// fix #1

	    str = str.replace("</gmd:MD_DataIdentification>", "");

	    str = str.replace("<gmd:AbstractMD_Identification>", "");

	    str = str.replace("</gmd:AbstractMD_Identification>", "</gmd:MD_DataIdentification>");

	    /// fix #2

	    reader = new XMLDocumentReader(str);

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    Node[] boxes = reader.evaluateNodes("//gmd:EX_GeographicBoundingBox");
	    for (Node box : boxes) {
		String value1 = reader.evaluateString(box, "*[1]");
		String value2 = reader.evaluateString(box, "*[2]");
		String value3 = reader.evaluateString(box, "*[3]");
		String value4 = reader.evaluateString(box, "*[4]");

		Node[] children = reader.evaluateNodes(box, "*");

		for (Node child : children) {
		    Node parent = child.getParentNode();
		    parent.removeChild(child);
		}

		Element southElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:southBoundLatitude");
		Element southDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		southElement.appendChild(southDecimalElement);
		southDecimalElement.setTextContent(value1);

		Element westElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:westBoundLongitude");
		Element westDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		westElement.appendChild(westDecimalElement);
		westDecimalElement.setTextContent(value2);

		Element northElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:northBoundLatitude");
		Element northDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		northElement.appendChild(northDecimalElement);
		northDecimalElement.setTextContent(value3);

		Element eastElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:eastBoundLongitude");
		Element eastDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		eastElement.appendChild(eastDecimalElement);
		eastDecimalElement.setTextContent(value4);

		box.appendChild(westElement);
		box.appendChild(eastElement);
		box.appendChild(southElement);
		box.appendChild(northElement);

	    }
	} catch (Exception e1) {
	    GSLoggerFactory.getLogger(getClass()).error("XPathExpressionException fixing GetRecords response", e1);

	    throw GSException.createException( //
		    getClass(), //
		    "XPathExpressionException fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWEVK2CONNECTOR_EXTRACTION_ERROR, e1);
	}

	try {

	    File ret = File.createTempFile(getClass().getSimpleName(), ".xml");

	    FileOutputStream fos = new FileOutputStream(ret);

	    ByteArrayInputStream stream = reader.asStream();

	    IOUtils.copy(stream, fos);

	    stream.close();
	    fos.close();

	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Exception fixing GetRecords response", e);

	    throw GSException.createException( //
		    getClass(), //
		    "Exception fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWEVK2CONNECTOR_EXTRACTION_ERROR, e);
	}

    }

}
