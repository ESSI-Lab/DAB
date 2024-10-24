package eu.essi_lab.accessor.cmr.distributed;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.distributed.CWICGranulesBondHandler;
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
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CMRIDNGranulesConnector<S extends CMRIDNGranulesConnectorSetting> extends DistributedQueryConnector<S> {

    /**
     * 
     */
    public static final String TYPE = "CMRIDNGranulesConnector";

    private static final String CMRIDN_GRANULES_NO_PARENT_ERR_ID = "CMRIDN_GRANULES_NO_PARENT_ERR_ID";

    private static final String CMRIDNGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT = "CMRIDNGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT";
    private static final String CANT_READ_RESPONSE = "Can't read response stream from CMRIDN granule search";
    private static final String CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR = "CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR";
    private static final String CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR = "CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR";
    private static final String CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR = "CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR";

    private final static String CMRIDN_QUERY_TEMPLATE_URL = "dataCenter={dataCenter?}&shortName={shortName?}&offset={os:startIndex?}&numberOfResults={os:count?}&startTime={time:start}&endTime={time:end}&boundingBox={geo:box}&clientId=gs-service";
    private final static String CMRIDN_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?";

    public CMRIDNGranulesConnector() {
    }

    protected String getParentId(ReducedDiscoveryMessage message) throws GSException {

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getReducedBond());

	ParentIdBondHandler parentIdBondHandler = new ParentIdBondHandler();

	bondParser.parse(parentIdBondHandler);

	if (!parentIdBondHandler.isParentIdFound())
	    throw GSException.createException(getClass(), "No Parent Identifier specified to cmr second-level search", null,
		    ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, CMRIDN_GRANULES_NO_PARENT_ERR_ID);

	return parentIdBondHandler.getParentValue();
    }

    // Optional<String> readSearchUrlFromParent(GSResource parentGSResource) {
    //
    // try {
    //
    // return Optional.ofNullable(parentGSResource.getHarmonizedMetadata().getExtendedMetadata()
    // .getTextContent(CMRCollectionMapper.CWIC_SECOND_LEVEL_TEMPLATE));
    //
    // } catch (XPathExpressionException e) {
    //
    // GSLoggerFactory.getLogger(getClass()).error("Can't find search url in parent GSResource, XPathExpressionException
    // was thrown", e);
    //
    // }
    //
    // return Optional.empty();
    //
    // }

    // Optional<String> createSearchUrlFromParent(GSResource parentGSResource) {
    //
    // try {
    // OriginalMetadata om = parentGSResource.getOriginalMetadata();
    // XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());
    //
    // String configuredBaseURL = xdoc.evaluateString("//*:cmrurl/@href");
    //
    // String fileIdentifier = xdoc.evaluateString("//*:fileIdentifier").trim();
    //
    // if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {
    // configuredBaseURL = CMRIDN_BASE_TEMPLATE_URL;
    // }
    //
    // if (fileIdentifier == null || fileIdentifier.isEmpty()) {
    // fileIdentifier = parentGSResource.getPublicId();
    // }
    // String url = configuredBaseURL.endsWith("?") ? configuredBaseURL + CMRIDN_QUERY_TEMPLATE_URL
    // : configuredBaseURL + "?" + CMRIDN_QUERY_TEMPLATE_URL;
    // CWICGranulesTemplate template = new CWICGranulesTemplate(url);
    // template.setDatasetId(fileIdentifier);
    //
    // return Optional.ofNullable(template.getRequestURL());
    //
    // } catch (XPathExpressionException e) {
    //
    // GSLoggerFactory.getLogger(getClass()).error("Can't find search url in parent GSResource, XPathExpressionException
    // was thrown", e);
    //
    // } catch (SAXException e) {
    // GSLoggerFactory.getLogger(getClass()).error("Can't find search url in parent GSResource, XPathExpressionException
    // was thrown", e);
    //
    // } catch (IOException e) {
    // GSLoggerFactory.getLogger(getClass()).error("Can't find search url in parent GSResource, XPathExpressionException
    // was thrown", e);
    // }
    //
    // return Optional.empty();
    //
    // }

    protected Page countPage() {

	return new Page(1, 1);
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level count for CMRIDN");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	String parentid = getParentId(message);

	GSLoggerFactory.getLogger(getClass()).trace("CWIC Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	Integer matches = 0;

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    // Optional<String> optionalUrl = createSearchUrlFromParent(parentGSResource);

	    // if (optionalUrl.isPresent()) {

	    try {
		OriginalMetadata om = parentGSResource.getOriginalMetadata();
		XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		String configuredBaseURL = xdoc.evaluateString("//*:cmrurl/@href");

		String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		String dataCenter = xdoc.evaluateString("//*:dataCenter").trim();

		String shortName = xdoc.evaluateString("//*:shortName").trim();

		if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {
		    configuredBaseURL = CMRIDN_BASE_TEMPLATE_URL;
		}

		if (fileIdentifier == null || fileIdentifier.isEmpty()) {
		    fileIdentifier = parentGSResource.getPublicId();
		}
		String url = configuredBaseURL.endsWith("?") ? configuredBaseURL + CMRIDN_QUERY_TEMPLATE_URL
			: configuredBaseURL + "?" + CMRIDN_QUERY_TEMPLATE_URL;
		// CWICGranulesTemplate template = new CWICGranulesTemplate(url);
		// template.setDatasetId(fileIdentifier);

		// return Optional.ofNullable(template.getRequestURL());

		// String searchURL = optionalUrl.get();

		// GSLoggerFactory.getLogger(getClass()).debug("Found url in extended metadata {}", searchURL);

		// HttpResponse response = retrieve(message, countPage(), searchURL);

		GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		HttpResponse<InputStream> response = retrieve(message, countPage(), dataCenter, shortName, url);

		GSLoggerFactory.getLogger(getClass()).trace("Extracting count");

		matches = count(response);

		GSLoggerFactory.getLogger(getClass()).info("Found {} matches", matches);

	    } catch (XPathExpressionException e) {

		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

	    } catch (SAXException e) {
		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);
	    }
	} else
	    GSLoggerFactory.getLogger(getClass())
		    .warn("Unable to find second-level search url for CMRIDN collection {}, returning zero matches", parentid);

	// } else
	// GSLoggerFactory.getLogger(getClass()).warn("Unable to find parent resource in message for CWIC collection {},
	// returning zero matches",
	// parentid);

	countResponse.setCount(matches);

	return countResponse;

    }

    private HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String dataCenter, String shortName,
	    String templateUrl) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, dataCenter, shortName, templateUrl);

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest);

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    throw GSException.createException(getClass(), //
		    e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CMRIDNGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT, //
		    e);

	}
    }

    private String createRequest(ReducedDiscoveryMessage message, Page page, String dataCenter, String shortName, String templateUrl) {

	CMRGranulesBondHandler bondHandler = parse(message, page.getStart() - 1, page.getSize(), dataCenter, shortName, templateUrl);

	return bondHandler.getQueryString();

    }

    private CMRGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int size, String dataCenter, String shortName,
	    String templateUrl) {

	CMRGranulesBondHandler bondHandler = new CMRGranulesBondHandler(templateUrl);

	bondHandler.setStart(start);

	bondHandler.setCount(size);

	bondHandler.setDataCenter(dataCenter);

	bondHandler.setShortName(shortName);

	DiscoveryBondParser bondParser = getParser(message);

	GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

	return bondHandler;
    }

    XMLDocumentReader createResponseReader(InputStream stream) throws IOException, SAXException {

	return new XMLDocumentReader(stream);
    }

    XMLDocumentReader createResponseReader(HttpResponse<InputStream> response) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    reader = createResponseReader(response.body());

	} catch (SAXException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR, e);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR, e);

	}

	reader.setNamespaceContext(new CommonNameSpaceContext());

	return reader;

    }

    protected Integer count(HttpResponse response) throws GSException {

	XMLDocumentReader reader = createResponseReader(response);

	try {

	    return reader.evaluateNumber("//opensearch:totalResults").intValue();

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);

	}

    }

    String createRequest(ReducedDiscoveryMessage message, Page page, String datasetId, String templateUrl) {

	CWICGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), datasetId, templateUrl);

	return bondHandler.getQueryString();

    }

    public HttpResponse<InputStream> executeGet(String url) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));
    }

    protected HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String datasetId, String templateUrl)
	    throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, datasetId, templateUrl);

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest);

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    throw GSException.createException(getClass(), //
		    e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CMRIDNGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT, //
		    e);

	}
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    CWICGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int count, String datasetId, String templateUrl) {

	CWICGranulesBondHandler bondHandler = new CWICGranulesBondHandler(templateUrl);

	bondHandler.setStart(start);

	bondHandler.setCount(count);

	bondHandler.setDatasetId(datasetId);

	DiscoveryBondParser bondParser = getParser(message);

	GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

	bondParser.parse(bondHandler);

	GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

	return bondHandler;

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level query for cmr");

	String parentid = getParentId(message);

	GSLoggerFactory.getLogger(getClass()).trace("CMRIDN Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	List<OriginalMetadata> omList = new ArrayList<>();

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    // Optional<String> optionalUrl = readSearchUrlFromParent(parentGSResource);

	    // if (optionalUrl.isPresent()) {

	    // String searchURL = optionalUrl.get();

	    // GSLoggerFactory.getLogger(getClass()).debug("Found url in extended metadata {}", searchURL);

	    // String datasetId = "";
	    try {
		OriginalMetadata om = parentGSResource.getOriginalMetadata();
		XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		String configuredBaseURL = xdoc.evaluateString("//*:cmrurl/@href");

		String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		String dataCenter = xdoc.evaluateString("//*:dataCenter").trim();

		String shortName = xdoc.evaluateString("//*:shortName").trim();

		if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {
		    configuredBaseURL = CMRIDN_BASE_TEMPLATE_URL;
		}

		if (fileIdentifier == null || fileIdentifier.isEmpty()) {
		    fileIdentifier = parentGSResource.getPublicId();
		}
		String url = configuredBaseURL.endsWith("?") ? configuredBaseURL + CMRIDN_QUERY_TEMPLATE_URL
			: configuredBaseURL + "?" + CMRIDN_QUERY_TEMPLATE_URL;
		// CWICGranulesTemplate template = new CWICGranulesTemplate(url);
		// template.setDatasetId(fileIdentifier);

		// return Optional.ofNullable(template.getRequestURL());

		HttpResponse response = retrieve(message, page, dataCenter, shortName, url);

		GSLoggerFactory.getLogger(getClass()).trace("Extracting CMRIDN original metadata");

		omList = convertResponseToOriginalMD(response);

		// String searchURL = optionalUrl.get();

		// GSLoggerFactory.getLogger(getClass()).debug("Found url in extended metadata {}", searchURL);

		// HttpResponse response = retrieve(message, countPage(), searchURL);

	    } catch (XPathExpressionException e) {

		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

	    } catch (SAXException e) {
		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);

	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass())
			.error("Can't find search url in parent GSResource, XPathExpressionException was thrown", e);
	    }

	    // } else
	    // GSLoggerFactory.getLogger(getClass()).warn("Unable to find second-level search url for CWIC collection
	    // {}, returning zero matches",
	    // parentid);

	} else
	    GSLoggerFactory.getLogger(getClass())
		    .warn("Unable to find parent resource in message for CMRIDN collection {}, returning zero matches", parentid);

	GSLoggerFactory.getLogger(getClass()).trace("Creating CMRIDN wresult set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	GSLoggerFactory.getLogger(getClass()).info("CMRIDN Result set created (size: {})", omList.size());

	return rSet;

    }

    protected List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse response) throws GSException {

	XMLDocumentReader reader = createResponseReader(response);

	try {

	    return reader.evaluateOriginalNodesList("//*:entry").stream().map(node -> {

		OriginalMetadata original = new OriginalMetadata();

		XMLDocumentReader rr = new XMLDocumentReader(node.getOwnerDocument());

		try {

		    original.setMetadata(XMLDocumentReader.asString(node));

		} catch (IOException | TransformerException e) {

		    GSLoggerFactory.getLogger(getClass()).warn("Can't convert cmr granule entry to original metadata");

		    return null;

		}

		original.setSchemeURI(CMRGranulesMetadataSchemas.ATOM_ENTRY.toString());

		return original;

	    }).filter(originalMetadata -> originalMetadata != null).collect(Collectors.toList());

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRIDNGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);
	}

    }

    /**
     * This always returns false because this connector is supposed to be used anly in a mixed configuration
     *
     * @param source
     * @return
     */
    @Override
    public boolean supports(GSSource source) {
	return false;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected S initSetting() {

	return (S) new CMRIDNGranulesConnectorSetting();
    }
}
