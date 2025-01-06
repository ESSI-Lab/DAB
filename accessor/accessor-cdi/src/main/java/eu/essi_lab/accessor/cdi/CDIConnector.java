package eu.essi_lab.accessor.cdi;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class CDIConnector extends HarvestedQueryConnector<CDIConnectorSetting> {

	/**
	 *
	 */
	public static final String TYPE = "CDIConnector";

	private static final String CDI_STREAM_ERROR = "CDI_STREAM_ERROR";

	private static final String CDI_CONNECTOR_LIST_RECORDS_ERROR = "CDI_CONNECTOR_LIST_RECORDS_ERROR";

	private static final String CDI_CONNECTOR_PARSING_ERROR = "CDI_CONNECTOR_PARSING_ERROR";

	private Downloader downloader;

	private Logger logger = GSLoggerFactory.getLogger(this.getClass());
	/**
	 * This is the cached set of CDI urls, used during subsequent list records.
	 */

	private Set<String> cachedCDIUrls = null;

	public Downloader getDownloader() {
		return downloader == null ? new Downloader() : downloader;
	}

	public void setDownloader(Downloader downloader) {
		this.downloader = downloader;
	}

	public CDIConnector() {
		this.downloader = new Downloader();
	}

	@Override
	public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {
		
		ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

		if (cachedCDIUrls == null) {
			cachedCDIUrls = getCDIUrls();
		}
		
		listRecords.setExpectedRecords(cachedCDIUrls.size());
		
		Iterator<String> iterator = cachedCDIUrls.iterator();
		String id = listRecords.getResumptionToken();
		String nextId = null;
		if (id == null) {
			if (iterator.hasNext()) {
				// we start from the first
				id = iterator.next();
				if (iterator.hasNext()) {
					nextId = iterator.next();
				}
			} else {
				// empty CDI urls
				// nextId remains null
			}
		} else {
			if (cachedCDIUrls.contains(id)) {
				while (iterator.hasNext()) {
					String tmp = iterator.next();
					if (tmp.equals(id) && iterator.hasNext()) {
						nextId = iterator.next();
						break;
					}
					// if it is the last element
					// nextId remains null

				}
			} else {
				// if the package id is not found in the package list
				throw GSException.createException(//
						getClass(), //
						"Unable to resume from resumption token: " + id, //
						null, ErrorInfo.ERRORTYPE_INTERNAL, //
						ErrorInfo.SEVERITY_ERROR, //
						CDI_CONNECTOR_LIST_RECORDS_ERROR //
				);
			}
		}
		if (id != null) {
			OriginalMetadata metadataRecord = new OriginalMetadata();
			if (id.contains("eml-")) {
				metadataRecord.setSchemeURI(CommonNameSpaceContext.EUROBIS_NS_URI);
			} else {
				metadataRecord.setSchemeURI(CommonNameSpaceContext.SDN_NS_URI);
			}

			String metadata = getCDIMetadata(id);
			metadataRecord.setMetadata(metadata);
			ret.addRecord(metadataRecord);
		}
		
		ret.setResumptionToken(nextId);
		return ret;
	}

	@Override
	public List<String> listMetadataFormats() {
		List<String> toret = new ArrayList<>();
		toret.add(CommonNameSpaceContext.SDN_NS_URI);
		return toret;
	}

	@Override
	public boolean supports(GSSource source) {
		String baseEndpoint = source.getEndpoint();

		try {
			Optional<InputStream> stream = getDownloader().downloadOptionalStream(baseEndpoint);

			if (stream.isPresent()) {
				XMLDocumentReader reader = new XMLDocumentReader(stream.get());
				Boolean cdiFormat = reader.evaluateBoolean("/*:cdiGroup");
				if (Boolean.TRUE.equals(cdiFormat)) {
					return true;
				}
			}
		} catch (Exception e) {
			// any exception during download or during XML parsing
			logger.warn("Exception during download or during XML parsing", e);
		}
		return false;
	}

	/**
	 * Retrieves an alphabetically ordered list of CDI urls from the remote CDI
	 * service
	 *
	 * @return a list of CDI urls
	 * @throws GSException
	 */

	public SortedSet<String> getCDIUrls() throws GSException {

		TreeSet<String> toret = new TreeSet<>();
		String cdiGroupUrl = getSourceURL();

		Optional<InputStream> stream = getDownloader().downloadOptionalStream(cdiGroupUrl);

		XMLDocumentReader reader = null;

		if (stream.isPresent()) {
			try {
				reader = new XMLDocumentReader(stream.get());
			} catch (SAXException | IOException e) {

				logger.error("Error parsing the XML CDI group at: {}", cdiGroupUrl);

				throw GSException.createException(//
						getClass(), //
						"Error parsing the XML CDI group at: " + cdiGroupUrl, //
						null, //
						ErrorInfo.ERRORTYPE_INTERNAL, //
						ErrorInfo.SEVERITY_ERROR, //
						CDI_CONNECTOR_PARSING_ERROR //
				);
			}
		} else {

			throw GSException.createException(//
					getClass(), //
					"Unable to download CDI stream", //
					null, //
					ErrorInfo.ERRORTYPE_SERVICE, //
					ErrorInfo.SEVERITY_ERROR, //
					CDI_STREAM_ERROR);
		}

		Node[] cdiUrlNodes;
		try {
			cdiUrlNodes = reader.evaluateNodes("//*:cdiUrl");
		} catch (XPathExpressionException e) {

			logger.error("Error evaluating XPath on the XML CDI group at: {}", cdiGroupUrl);

			throw GSException.createException(//
					getClass(), //
					"Error evaluating XPath on the XML CDI group at: " + cdiGroupUrl, //
					null, //
					ErrorInfo.ERRORTYPE_INTERNAL, //
					ErrorInfo.SEVERITY_ERROR, //
					CDI_CONNECTOR_PARSING_ERROR //
			);
		}

		GSLoggerFactory.getLogger(getClass()).info("Downloaded {} CDI URLs from remote service", cdiUrlNodes.length);
		for (Node nodeResult : cdiUrlNodes) {

			Optional<Integer> mr = getSetting().getMaxRecords();

			if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && toret.size() > mr.get()) {
				break;
			}

			String targetUrl = nodeResult.getTextContent();

			if (cdiGroupUrl.contains("url=")) {
				// we are under a proxy, because only specific ips are allowed to harvest
				// SeaDataNet service
				try {
					String encodedUrl = URLEncoder.encode(targetUrl, "UTF-8");
					targetUrl = cdiGroupUrl.substring(0, cdiGroupUrl.indexOf("url=")) + "url=" + encodedUrl;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

			}

			toret.add(targetUrl);
		}
		GSLoggerFactory.getLogger(getClass()).info("Consolidated {} CDI URLs from remote service", toret.size());
		return toret;
	}

	/**
	 * Retrieves the SeaDataNet CDI metadata from its url
	 *
	 * @param url the package id
	 * @return the package description as a JSON string
	 * @throws GSException
	 */

	public String getCDIMetadata(String url) {

		url = url.contains(" ") ? url.replaceAll(" ", "%20") : url;
		return getDownloader().downloadOptionalString(url).orElse(null);

	}

	@Override
	public String getType() {

		return TYPE;
	}

	@Override
	protected CDIConnectorSetting initSetting() {

		return new CDIConnectorSetting();
	}
}
