package eu.essi_lab.accessor.cmr.cwic.distributed;

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

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.accessor.cmr.distributed.CMRIDNGranulesConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CWICCMRGranulesConnector extends CMRIDNGranulesConnector<CWICCMRGranulesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "CWICCMRGranulesConnector";

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    public final static String CWIC_QUERY_TEMPLATE_URL = "datasetId={datasetId?}&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=gs-service";
    public final static String CWIC_BASE_TEMPLATE_URL = "https://cmr.earthdata.nasa.gov/opensearch/granules.atom?";

    public CWICCMRGranulesConnector() {
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	logger.trace("Received second-level count for CWIC");

	DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

	String parentid = getParentId(message);

	logger.trace("CWIC Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	Integer matches = 0;

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    // Optional<String> optionalUrl = createSearchUrlFromParent(parentGSResource);

	    // if (optionalUrl.isPresent()) {

	    try {
		OriginalMetadata om = parentGSResource.getOriginalMetadata();
		XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		String configuredBaseURL = null;

		String url = null;

		HttpResponse<InputStream> response = null;

		configuredBaseURL = xdoc.evaluateString("//*:link[contains(lower-case(@title),'for granules')]/@href");

		if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {

		    configuredBaseURL = xdoc.evaluateString("//*:cmrurl/@href");

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {
			configuredBaseURL = CWIC_BASE_TEMPLATE_URL;
		    }

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }
		    url = configuredBaseURL.endsWith("?") ? configuredBaseURL + CWIC_QUERY_TEMPLATE_URL
			    : configuredBaseURL + "?" + CWIC_QUERY_TEMPLATE_URL;

		    // CWICGranulesTemplate template = new CWICGranulesTemplate(url);
		    // template.setDatasetId(fileIdentifier);

		    // return Optional.ofNullable(template.getRequestURL());

		    // String searchURL = optionalUrl.get();

		    // logger.debug("Found url in extended metadata {}", searchURL);

		    // HttpResponse response = retrieve(message, countPage(), searchURL);

		    logger.debug("Created url template {}", url);

		    response = retrieve(message, countPage(), fileIdentifier, url);

		} else {
		    url = configuredBaseURL;
		    response = executeGet(url);

		    logger.trace("Original Metadata obtained");

		}

		logger.trace("Extracting count");

		matches = count(response);

		logger.info("Found {} matches", matches);

	    } catch (Exception e) {

		logger.error(e.getMessage(), e);
	    }
	} else
	    logger.warn("Unable to find second-level search url for CWIC collection {}, returning zero matches", parentid);

	// } else
	// logger.warn("Unable to find parent resource in message for CWIC collection {}, returning zero matches",
	// parentid);

	countResponse.setCount(matches);

	return countResponse;

    }

    @Override
    public ResultSet<OriginalMetadata> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	logger.trace("Received second-level query for CWIC");

	String parentid = getParentId(message);

	logger.trace("CWIC Parent id {}", parentid);

	Optional<GSResource> parent = message.getParentGSResource(parentid);

	List<OriginalMetadata> omList = new ArrayList<>();

	if (parent.isPresent()) {

	    GSResource parentGSResource = parent.get();

	    // Optional<String> optionalUrl = readSearchUrlFromParent(parentGSResource);

	    // if (optionalUrl.isPresent()) {

	    // String searchURL = optionalUrl.get();

	    // logger.debug("Found url in extended metadata {}", searchURL);

	    // String datasetId = "";
	    try {
		OriginalMetadata om = parentGSResource.getOriginalMetadata();
		XMLDocumentReader xdoc = new XMLDocumentReader(om.getMetadata());

		String configuredBaseURL = null;

		String url = null;

		HttpResponse<InputStream> response = null;

		configuredBaseURL = xdoc.evaluateString("//*:link[contains(lower-case(@title),'for granules')]/@href");

		if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {

		    configuredBaseURL = xdoc.evaluateString("//*:cmrurl/@href");

		    String fileIdentifier = xdoc.evaluateString("//*:identifier").trim();

		    if (configuredBaseURL == null || configuredBaseURL.isEmpty()) {
			configuredBaseURL = CWIC_BASE_TEMPLATE_URL;
		    }

		    if (fileIdentifier == null || fileIdentifier.isEmpty()) {
			fileIdentifier = parentGSResource.getPublicId();
		    }
		    url = configuredBaseURL.endsWith("?") ? configuredBaseURL + CWIC_QUERY_TEMPLATE_URL
			    : configuredBaseURL + "?" + CWIC_QUERY_TEMPLATE_URL;
		    // CWICGranulesTemplate template = new CWICGranulesTemplate(url);
		    // template.setDatasetId(fileIdentifier);

		    // return Optional.ofNullable(template.getRequestURL());

		    response = retrieve(message, page, fileIdentifier, url);

		} else {
		    url = configuredBaseURL;
		    response = executeGet(url);
		}

		logger.trace("Extracting CWIC original metadata");

		omList = convertResponseToOriginalMD(response);

		// String searchURL = optionalUrl.get();

		// logger.debug("Found url in extended metadata {}", searchURL);

		// HttpResponse response = retrieve(message, countPage(), searchURL);

	    } catch (Exception e) {

		logger.error(e.getMessage(), e);

	    }

	    // } else
	    // logger.warn("Unable to find second-level search url for CWIC collection {}, returning zero matches",
	    // parentid);

	} else
	    logger.warn("Unable to find parent resource in message for CWIC collection {}, returning zero matches", parentid);

	logger.trace("Creating CWIC result set");

	ResultSet<OriginalMetadata> rSet = new ResultSet<>();

	rSet.setResultsList(omList);

	logger.info("CWIC Result set created (size: {})", omList.size());

	return rSet;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CWICCMRGranulesConnectorSetting initSetting() {

	return new CWICCMRGranulesConnectorSetting();
    }

}
