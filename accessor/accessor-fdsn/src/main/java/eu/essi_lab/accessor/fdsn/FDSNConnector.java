package eu.essi_lab.accessor.fdsn;

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

import java.io.InputStream;
import java.io.StringWriter;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.ByteStreams;

import eu.essi_lab.accessor.fdsn.handler.FDSNBondHandler;
import eu.essi_lab.accessor.fdsn.md.FDSNMetadataSchemas;
import eu.essi_lab.cdk.query.DistributedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.GSMessageAction;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class FDSNConnector extends DistributedQueryConnector<FDSNConnectorSetting> {

    private static final String FDSN_CONNECTOR_COUNT_ERROR = "FDSN_CONNECTOR_COUNT_ERROR";
    private static final String FDSN_CONNECTOR_QUERY_ERROR = "FDSN_CONNECTOR_QUERY_ERROR";
    private static final String FDSN_ORIGINAL_MD_CONVERSION_ERROR = "ORIGINAL_MD_CONVERSION_ERROR";

    static final String CONNECTOR_TYPE = "FDSNConnector";

    /**
     * 
     */
    public FDSNConnector() {

    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	String request = createFDSNRequest(message, GSMessageAction.DISCOVERY_COUNT, null);
	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	try {

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, request));

	    InputStream is = response.body();

	    ClonableInputStream clone = new ClonableInputStream(is);

	    String countString = new String(ByteStreams.toByteArray(clone.clone()));

	    countResponse.setCount(Integer.parseInt(countString));

	    is.close();

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    FDSN_CONNECTOR_COUNT_ERROR, //
		    e);
	}

	return countResponse;
    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	List<OriginalMetadata> omList;

	String request = createFDSNRequest(message, GSMessageAction.DISCOVERY_RETRIEVE, page);

	HttpResponse<InputStream> response;
	try {
	    response = new Downloader().downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, request));
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    FDSN_CONNECTOR_QUERY_ERROR, //
		    e);
	}

	omList = convertResponseToOriginalMD(response);

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	return rSet;
    }

    private String createFDSNRequest(ReducedDiscoveryMessage reducedMessage, GSMessageAction action, Page page) {

	return convertBondToRequest(reducedMessage, action, page);
    }

    private String convertBondToRequest(ReducedDiscoveryMessage reducedMessage, GSMessageAction action, Page page) {

	boolean ignore = getSetting().isIgnoreComplexQueries();

	Optional<String> eventOrder = reducedMessage.getQuakeMLEventOrder();

	FDSNBondHandler bondHandler = new FDSNBondHandler();
	bondHandler.setIgnoreComplexQuery(ignore);
	if (eventOrder.isPresent()) {
	    bondHandler.setEventOrder(eventOrder.get());
	}

	DiscoveryBondParser bondParser = new DiscoveryBondParser(reducedMessage.getReducedBond());
	bondParser.parse(bondHandler);

	String queryString = bondHandler.getQueryString(page);

	String sourceUrl = "";
	switch (action) {
	case DISCOVERY_COUNT:

	    sourceUrl = getSourceURL().endsWith("/") ? getSourceURL() + "count?" : getSourceURL() + "/count?";

	    // the query is not supported, so the count query is set in order to return 0
	    if (queryString == null) {
		queryString = "updatedafter=2099-01-01T00:00:00Z";
	    }

	    break;
	case DISCOVERY_RETRIEVE:

	    sourceUrl = getSourceURL().endsWith("/") ? getSourceURL() + "query?" : getSourceURL() + "/query?";
	    break;

	default:
	    break;
	}

	return buildQuery(sourceUrl, queryString);
    }

    private List<OriginalMetadata> convertResponseToOriginalMD(HttpResponse<InputStream> response) throws GSException {

	List<OriginalMetadata> omList = new ArrayList<>();

	try {

	    InputStream is = response.body();

	    XMLDocumentReader xmlDocument = new XMLDocumentReader(is);

	    Map<String, String> namespaces = new HashMap<>();
	    namespaces.put("q", "http://quakeml.org/xmlns/quakeml/1.2");

	    xmlDocument.setNamespaces(namespaces);

	    Node input = xmlDocument.evaluateNode("//q:quakeml");

	    input.normalize();

	    NodeList eventParameters = input.getChildNodes();

	    NodeList listEvents = null;

	    for (int i = 0; i < eventParameters.getLength(); i++) {

		Node n = eventParameters.item(i);

		if (n.hasChildNodes()) {
		    listEvents = n.getChildNodes();
		}

	    }

	    if (listEvents != null) {

		for (int j = 0; j < listEvents.getLength(); j++) {

		    if (listEvents.item(j).getNodeName() != null && listEvents.item(j).getNodeName().equals("event")) {

			OriginalMetadata om = new OriginalMetadata();

			Node eventNode = listEvents.item(j);
			Node eventParamNode = eventNode.getParentNode().cloneNode(false);
			Node rootNode = input.cloneNode(false);

			eventParamNode.appendChild(eventNode);
			rootNode.appendChild(eventParamNode);

			StringWriter sw = new StringWriter();
			Transformer serializer = XMLFactories.newTransformerFactory().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(rootNode), new StreamResult(sw));

			om.setMetadata(sw.toString());
			om.setSchemeURI(FDSNMetadataSchemas.QUAKEML.toString());
			omList.add(om);

		    }
		}
	    }

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    FDSN_ORIGINAL_MD_CONVERSION_ERROR, //
		    e);
	}

	return omList;

    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("earthquake.usgs.gov/fdsnws/event/");
    }

    /**
     * Build request: baseUri + queryParams
     *
     * @throws GSException when url is not well-formed
     */
    private String buildQuery(String baseUri, String queryParams) {

	return baseUri + queryParams;
    }

    @Override
    public String getType() {

	return CONNECTOR_TYPE;
    }

    @Override
    protected FDSNConnectorSetting initSetting() {

	return new FDSNConnectorSetting();
    }
}
