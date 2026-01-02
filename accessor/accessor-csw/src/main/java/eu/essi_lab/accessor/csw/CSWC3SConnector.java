package eu.essi_lab.accessor.csw;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CSWC3SConnector extends CSWConnector {

    private static final String CSWC3SCONNECTOR_EXTRACTION_ERROR = "CSWC3SCONNECTOR_EXTRACTION_ERROR";
    private static final String GCO_DECIMAL = "gco:Decimal";

    /**
     * 
     */
    public static final String TYPE = "CSW C3S Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * This fix is needed to tweak the metadata records returned by GEOSS services #277 (ECMWF Copernicus Climate Change
     * (C3S)). As they
     * contains geographic corrdinates in 360 degrees. So, the bounding box is fixed.
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    reader = new XMLDocumentReader(str);

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    /// fix #1
	    Node[] graphicOverviews = reader.evaluateNodes("//gmd:graphicOverview/*/gmd:fileName/gco:CharacterString");
	    Node[] ids = reader.evaluateNodes("//gmd:fileIdentifier/gco:CharacterString");// gmd:fileIdentifier/gco:CharacterString
	    int k = 0;
	    for (Node overview : graphicOverviews) {
		String putReq = handleThumbnail(overview.getTextContent(), ids[k].getTextContent());
		k++;
		if (putReq != null && !putReq.isEmpty())
		    overview.setTextContent(putReq);
	    }

	    /// fix #2

	    Node[] boxes = reader.evaluateNodes("//gmd:EX_GeographicBoundingBox");

	    for (Node box : boxes) {
		String value1 = reader.evaluateString(box, "*[1]").trim();
		String value2 = reader.evaluateString(box, "*[2]").trim();
		String value3 = reader.evaluateString(box, "*[3]").trim();
		String value4 = reader.evaluateString(box, "*[4]").trim();

		if (value2.equals("360")) {
		    value1 = "-180";
		    value2 = "180";
		}

		Node[] children = reader.evaluateNodes(box, "*");
		for (Node child : children) {
		    Node parent = child.getParentNode();
		    parent.removeChild(child);
		}
		// west
		Element westElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:westBoundLongitude");
		Element westDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		westElement.appendChild(westDecimalElement);
		westDecimalElement.setTextContent(value1);
		// east
		Element eastElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:eastBoundLongitude");
		Element eastDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		eastElement.appendChild(eastDecimalElement);
		eastDecimalElement.setTextContent(value2);

		// south
		Element southElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:southBoundLatitude");
		Element southDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		southElement.appendChild(southDecimalElement);
		southDecimalElement.setTextContent(value3);
		// north
		Element northElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GMD_NS_URI, "gmd:northBoundLatitude");
		Element northDecimalElement = reader.getDocument().createElementNS(CommonNameSpaceContext.GCO_NS_URI, GCO_DECIMAL);
		northElement.appendChild(northDecimalElement);
		northDecimalElement.setTextContent(value4);

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
		    CSWC3SCONNECTOR_EXTRACTION_ERROR, e1);
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
		    CSWC3SCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

    /**
     * @param quickLook
     * @param id
     * @throws Exception
     */
    public String handleThumbnail(String quickLook, String id) throws Exception {

	if (quickLook == null || quickLook.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).trace("No quick look available, skip");
	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).trace("Quick look to handle: " + quickLook);

	Downloader pReq = new Downloader();

	Optional<InputStream> optional = Optional.empty();

	try {
	    optional = pReq.downloadOptionalStream(quickLook);

	} catch (IllegalArgumentException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
	    GSLoggerFactory.getLogger(getClass()).error("Invalid quicklook: " + quickLook);
	}

	if (optional.isPresent()) {

	    String putBase = "http://tiles.geodab.eu/geodab-tiles-nt/tile/thumbnail-";
	    String putRequ = putBase + id + "/0/0/0";

	    ClonableInputStream cis = new ClonableInputStream(optional.get());
	    boolean isImage = false;

	    if (IOStreamUtils.isPNG(cis.clone())) {

		putRequ = putRequ + ".png";
		isImage = true;
	    }

	    if (IOStreamUtils.isJPEG(cis.clone())) {

		putRequ = putRequ + ".jpg";
		isImage = true;
	    }

	    if (isImage) {

		GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading STARTED");
		GSLoggerFactory.getLogger(getClass()).trace("Current upload url: {}", putRequ);

		// HttpPut putRequest = new HttpPut();
		// putRequest.setURI(new URI(putRequ));
		// putRequest.setEntity(new InputStreamEntity(cis.clone()));

		Downloader executor = new Downloader();
 
		HttpRequest putRequest = HttpRequestUtils.build(MethodWithBody.PUT, putRequ, cis.clone());

		HttpResponse<InputStream> response = executor.downloadResponse(putRequest);

		int statusCode = response.statusCode();
		if (statusCode == 200) {

		    GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading ENDED. Thumbnail correctly set");

		    return putRequ;

		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Unable to upload thumbnail : " + response.statusCode());
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).trace("Quick look not available: " + quickLook);
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).trace("Quick look not available: " + quickLook);
	}

	GSLoggerFactory.getLogger(getClass()).trace("Thumbnail uploading ENDED. Thumbnail not set");

	return null;
    }

}
