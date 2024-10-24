package eu.essi_lab.accessor.nextgeoss.distributed;

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

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
public class NextGEOSSGranulesConnector extends DistributedQueryConnector<NextGEOSSGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NextGEOSSGranulesConnector";

    private static final String NEXTGEOSSGRANULES_CONNECTOR_QUERY_ERROR = "NEXTGEOSSGRANULES_CONNECTOR_QUERY_ERROR";
    private static final String NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT = "NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT";
    private static final String NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT = "NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_URI_SYNTAX_ENDPOINT";
    private static final String CANT_READ_RESPONSE = "Can't read response stream from NEXTGEOSS granule search";
    private static final String NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR = "NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR";
    private static final String NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR = "NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR";
    private static final String NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR = "NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR";

    public final static String NEXTGEOSS_REQUEST = "httpAccept=application/atom%2Bxml&";

    public final static String NEXTGEOSS_TEMPLATE_URL = "productType={eo:productType?}&rows={opensearch:count?}&timerange_end={time:end?}&metadata_modified={eo:modificationDate?}&q={opensearch:searchTerms?}&geom={geo:geometry?}&bbox={geo:box?}&identifier={geo:uid?}&timerange_start={time:start?}&page={opensearch:startPage?}&start_index={opensearch:startIndex?}&clientId=gs-service";

    public final static String NEXGEOSS_BASE_URL = "https://catalogue.nextgeoss.eu/opensearch/search.atom?";

    /**
     * 
     */
    public NextGEOSSGranulesConnector() {

    }

    Page countPage() {

	return new Page(1, 1);
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level count for NEXTGEOSS");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	Optional<String> parentId = ParentIdBondHandler.readParentId(message);

	Integer matches = 0;

	if (parentId.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("NEXTGEOSS Parent id {}", parentId.get());

	    Optional<GSResource> parent = message.getParentGSResource(parentId.get());

	    if (parent.isPresent()) {

		GSResource parentGSResource = parent.get();
		try {
		    OriginalMetadata om = parentGSResource.getOriginalMetadata();

		    XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }

		    String url = NEXGEOSS_BASE_URL + NEXTGEOSS_TEMPLATE_URL;

		    GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		    HttpResponse response = retrieve(message, countPage(), fileIdentifier, url, true);

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

	    } else {
		GSLoggerFactory.getLogger(getClass()).warn(
			"Unable to find parent resource in message for NEXTGEOSS collection {}, returning zero matches", parentId.get());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).trace("Creating NextGEOSS count set");

	countResponse.setCount(matches);

	return countResponse;

    }

    XMLDocumentReader createResponseReader(HttpResponse<InputStream> response) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    reader = new XMLDocumentReader(response.body());

	} catch (SAXException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_SAX_ERROR, e);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_IO_ERROR, e);

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
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);

	}

    }

    String createRequest(ReducedDiscoveryMessage message, Page page, String productType, String templateUrl, boolean isCount) {

	NextGEOSSGranulesBondHandler bondHandler = parse(message, page.getStart(), page.getSize(), productType, templateUrl, isCount);

	return bondHandler.getQueryString();

    }

    HttpResponse<InputStream> executeGet(String get) throws Exception {

	return new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, get));
    }

    HttpResponse<InputStream> retrieve(ReducedDiscoveryMessage message, Page page, String datasetId, String templateUrl, boolean isCount)
	    throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieving Original Metadata");

	String finalRequest = createRequest(message, page, datasetId, templateUrl, isCount);

	GSLoggerFactory.getLogger(getClass()).trace("Request to submit {}", finalRequest);

	try {

	    HttpResponse<InputStream> response = executeGet(finalRequest);

	    GSLoggerFactory.getLogger(getClass()).trace("Original Metadata obtained");

	    return response;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "MalformedURLException in NextGEOSSGranulesConnector for request " + finalRequest,
		    null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, NEXTGEOSSGRANULES_CONNECTOR_ERR_ID_MALFORMED_ENDPOINT, e);

	}
    }

    DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
	return new DiscoveryBondParser(message.getReducedBond());
    }

    NextGEOSSGranulesBondHandler parse(ReducedDiscoveryMessage message, int start, int count, String productType, String templateUrl,
	    boolean isCount) {

	NextGEOSSGranulesBondHandler bondHandler = new NextGEOSSGranulesBondHandler(templateUrl);

	if (!isCount) {
	    if ((start - 1) % 10 == 0 || start % 10 == 0) {
		start = (start / 10) + 1;
	    } else if ((start - 1) % 12 == 0 || start % 12 == 0) {
		start = (start / 12) + 1;
	    } else {
		// default page
		start = 1;
	    }

	}

	bondHandler.setStart(start);

	bondHandler.setProductType(productType);

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

	GSLoggerFactory.getLogger(getClass()).trace("Received second-level query for NEXTGEOSS");

	Optional<String> parentId = ParentIdBondHandler.readParentId(message);

	List<OriginalMetadata> omList = new ArrayList<>();

	if (parentId.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("NEXTGEOSS Parent id {}", parentId.get());

	    Optional<GSResource> parent = message.getParentGSResource(parentId.get());

	    if (parent.isPresent()) {

		GSResource parentGSResource = parent.get();

		try {
		    OriginalMetadata om = parentGSResource.getOriginalMetadata();

		    XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }

		    String url = NEXGEOSS_BASE_URL + NEXTGEOSS_TEMPLATE_URL;

		    GSLoggerFactory.getLogger(getClass()).debug("Created url template {}", url);

		    HttpResponse response = retrieve(message, page, fileIdentifier, url, false);

		    GSLoggerFactory.getLogger(getClass()).trace("Extracting original metadata");

		    omList = convertResponseToOriginalMD(response);

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

	    } else {
		GSLoggerFactory.getLogger(getClass()).warn(
			"Unable to find parent resource in message for NEXTGEOSS collection {}, returning zero matches", parentId.get());
	    }

	}
	GSLoggerFactory.getLogger(getClass()).trace("Creating result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	GSLoggerFactory.getLogger(getClass()).info("Result set created (size: {})", omList.size());

	return rSet;

    }

    List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse<InputStream> response) throws GSException {

	XMLDocumentReader reader = createResponseReader(response);

	try {

	    return reader.evaluateOriginalNodesList("//*:entry").stream().map(node -> {

		OriginalMetadata original = new OriginalMetadata();

		XMLDocumentReader rr = new XMLDocumentReader(node.getOwnerDocument());

		try {

		    original.setMetadata(XMLDocumentReader.asString(node));

		} catch (IOException | TransformerException e) {

		    GSLoggerFactory.getLogger(getClass()).warn("Can't convert NEXTGEOSS granule entry to original metadata");

		    return null;

		}

		original.setSchemeURI(NextGEOSSGranulesMetadataSchemas.ATOM_ENTRY_NEXTGEOSS.toString());

		return original;

	    }).filter(originalMetadata -> originalMetadata != null).collect(Collectors.toList());

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(CANT_READ_RESPONSE, e);

	    throw GSException.createException(getClass(), CANT_READ_RESPONSE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSSGRANULES_CONNECTOR_RESPONSE_STREAM_XPATH_ERROR, e);
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

    @Override
    protected NextGEOSSGranulesConnectorSetting initSetting() {

	return new NextGEOSSGranulesConnectorSetting();
    }

}
