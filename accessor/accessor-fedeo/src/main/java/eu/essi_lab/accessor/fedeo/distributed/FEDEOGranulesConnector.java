package eu.essi_lab.accessor.fedeo.distributed;

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
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.fedeo.bond.FEDEOGranulesBondHandler;
import eu.essi_lab.cdk.query.DistributedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.ParentIdBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class FEDEOGranulesConnector extends DistributedQueryConnector<FEDEOGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String CONNECTOR_TYPE = "FEDEOGranulesConnector";

    private static final String FEDEOGRANULES_CONNECTOR_ERR_RETRIEVE = "FEDEOGRANULES_CONNECTOR_ERR_RETRIEVE";
    private static final String CANT_READ_RESPONSE = "Can't read response stream from FEDEO granule search";
    private static final String FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR = "FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR";
    private static final String FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR = "FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR";
    private static final String FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR = "FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR";

    public final static String FEDEO_REQUEST = "httpAccept=application/atom%2Bxml&";

    public final static String FEDEO_TEMPLATE_URL = "startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}&clientId=gs-service";

    // "bbox={geo:box?}&geometry={geo:geometry?}&name={geo:name?}&startDate={time:start?}&endDate={time:end?}&startPage={startPage?}&startRecord=1&maximumRecords=10&uid={geo:uid?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&recordSchema={sru:recordSchema?}";

    /**
     * 
     */
    public FEDEOGranulesConnector() {

    }

    String readParentId(ReducedDiscoveryMessage message) throws GSException {

	return ParentIdBondHandler.readParentId(message).orElseThrow(() -> GSException.createException(//
		getClass(), //
		"No parent id found", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		"FEDEO_GRANULES_CONNECTOR_PARENT_ID_NOT_FOUND"));
    }

    Optional<String> readSearchUrlFromParent(GSResource parentGSResource) {

	return parentGSResource.getExtensionHandler().getFEDEOSecondLevelInfo();
    }

    Page countPage() {

	return new Page(1, 1);
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level count for FEDEO");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	String parentid = readParentId(message);

	GSLoggerFactory.getLogger(getClass()).trace("FEDEO Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	Integer matches = 0;

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    Optional<String> optionalUrl = readSearchUrlFromParent(parentGSResource);

	    if (optionalUrl.isPresent()) {

		String osdd = optionalUrl.get();

		GSLoggerFactory.getLogger(getClass()).debug("Found OSDD in extended metadata {}", osdd);

		String templateURL = extractTemplateURL(osdd);

		if (templateURL != null) {

		    // String baseURL = getSourceURL();
		    //
		    // baseURL = (baseURL != null) ? baseURL : parentGSResource.getSource().getEndpoint();
		    //
		    // baseURL = (baseURL.endsWith("?")) ? baseURL : baseURL + "?";
		    //
		    // String templateURL = baseURL + FEDEO_REQUEST + "parentIdentifier=" + osdd + "&" +
		    // FEDEO_TEMPLATE_URL;

		    HttpResponse response = retrieve(message, new Page(1, 0), templateURL);

		    if (response == null) {
			GSLoggerFactory.getLogger(getClass()).trace("Parameter not found in the template URL: " + templateURL);
			matches = 0;

		    } else {

			GSLoggerFactory.getLogger(getClass()).trace("Extracting count");

			matches = count(response);

			GSLoggerFactory.getLogger(getClass()).info("Found {} matches", matches);
		    }
		}

	    } else
		GSLoggerFactory.getLogger(getClass())
			.warn("Unable to find second-level search url for FEDEO collection {}, returning zero matches", parentid);

	} else
	    GSLoggerFactory.getLogger(getClass())
		    .warn("Unable to find parent resource in message for FEDEO collection {}, returning zero matches", parentid);

	countResponse.setCount(matches);

	return countResponse;
    }

    String extractTemplateURL(String osdd) {

	Downloader down = new Downloader();

	Optional<String> result = down.downloadOptionalString(osdd);

	if (result.isPresent()) {

	    try {

		XMLDocumentReader xdocReader = new XMLDocumentReader(result.get());

		String templateURL = xdocReader.evaluateString("//*:Url[@type='application/atom+xml']/@template");

		return templateURL;

	    } catch (XPathExpressionException | SAXException | IOException e) {

		GSLoggerFactory.getLogger(getClass())
			.warn("Unable to add FEDEO extension  element to collection {}, this collection will not be expandible", osdd, e);

	    }
	}
	return null;
    }

    XMLDocumentReader createResponseReader(HttpResponse<InputStream> response) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    reader = new XMLDocumentReader(response.body());

	} catch (SAXException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR, e);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR, e);

	}

	reader.setNamespaceContext(new CommonNameSpaceContext());

	return reader;

    }

    Integer count(HttpResponse response) throws GSException {

	XMLDocumentReader reader = createResponseReader(response);

	try {

	    int totResults = reader.evaluateNumber("//*:totalResults").intValue();

	    if (totResults == 0) {
		Node[] nodes = reader.evaluateNodes("//*:entry");
		totResults = nodes.length;
	    }

	    return totResults;

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);

	}

    }

    String createRequest(ReducedDiscoveryMessage message, Page page, String templateUrl) {

	FEDEOGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), templateUrl);

	return bondHandler.getQueryString();

    }

    HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, get));
    }

    HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String templateUrl) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, templateUrl);

	if (finalRequest == null) {
	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest);

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), e.getMessage(), null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR,
		    FEDEOGRANULES_CONNECTOR_ERR_RETRIEVE, e);

	}
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    FEDEOGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int count, String templateUrl) {

	FEDEOGranulesBondHandler bondHandler = new FEDEOGranulesBondHandler(templateUrl);

	bondHandler.setStart(start);

	if (count != 0) {
	    bondHandler.setCount(count);
	}

	DiscoveryBondParser bondParser = getParser(message);

	GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

	return bondHandler;

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level query for FEDEO");

	String parentid = readParentId(message);

	GSLoggerFactory.getLogger(getClass()).trace("FEDEO Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	List<OriginalMetadata> omList = new ArrayList<>();

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    Optional<String> optionalURL = readSearchUrlFromParent(parentGSResource);

	    if (optionalURL.isPresent()) {

		String osdd = optionalURL.get();

		GSLoggerFactory.getLogger(getClass()).debug("Found url in extended metadata {}", osdd);

		String templateURL = extractTemplateURL(osdd);

		if (templateURL != null) {

		    // String baseURL = getSourceURL();
		    //
		    // baseURL = (baseURL != null) ? baseURL : parentGSResource.getSource().getEndpoint();
		    //
		    // baseURL = (baseURL.endsWith("?")) ? baseURL : baseURL + "?";
		    //
		    // String templateURL = baseURL + FEDEO_REQUEST + "parentIdentifier=" + osdd + "&" +
		    // FEDEO_TEMPLATE_URL;

		    HttpResponse response = retrieve(message, page, templateURL);

		    if (response == null) {
			// it should never enter here
			GSLoggerFactory.getLogger(getClass()).trace("Parameter not found in the template URL: " + templateURL);

		    } else {

			GSLoggerFactory.getLogger(getClass()).trace("Extracting original metadata");

			omList = convertResponseToOriginalMD(response);
		    }

		}

	    } else
		GSLoggerFactory.getLogger(getClass())
			.warn("Unable to find second-level search url for FEDEO collection {}, returning zero matches", parentid);

	} else
	    GSLoggerFactory.getLogger(getClass())
		    .warn("Unable to find parent resource in message for FEDEO collection {}, returning zero matches", parentid);

	GSLoggerFactory.getLogger(getClass()).trace("Creating result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	GSLoggerFactory.getLogger(getClass()).info("Result set created (size: {})", omList.size());

	return rSet;

    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse response) throws GSException {

	XMLDocumentReader reader = createResponseReader(response);

	try {

	    return reader.evaluateOriginalNodesList("//*:entry").stream().map(node -> {

		OriginalMetadata original = new OriginalMetadata();

		XMLDocumentReader rr = new XMLDocumentReader(node.getOwnerDocument());

		try {

		    original.setMetadata(XMLDocumentReader.asString(node));

		} catch (IOException | TransformerException e) {

		    GSLoggerFactory.getLogger(getClass()).warn("Can't convert FEDEO granule entry to original metadata");

		    return null;

		}

		original.setSchemeURI(FEDEOGranulesMetadataSchemas.ATOM_ENTRY_FEDEO.toString());

		return original;

	    }).filter(originalMetadata -> originalMetadata != null).collect(Collectors.toList());

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, FEDEOGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);
	}

    }

    /**
     * This always returns false because this connector is supposed to be used only in a mixed configuration
     *
     * @param source
     * @return
     */
    @Override
    public boolean supports(GSSource source) {
	return false;
    }

    @Override

    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getType() {

	return "FEDEOGranulesConnector";
    }

    @Override
    protected FEDEOGranulesConnectorSetting initSetting() {

	return new FEDEOGranulesConnectorSetting();
    }
}
