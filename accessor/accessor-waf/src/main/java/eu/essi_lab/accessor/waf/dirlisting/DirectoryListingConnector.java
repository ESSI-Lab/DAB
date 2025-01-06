/**
 *
 */
package eu.essi_lab.accessor.waf.dirlisting;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IterationLogger;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class DirectoryListingConnector extends HarvestedQueryConnector<DirectoryListingConnectorSetting> {

    private static final String DIR_LISTING_ERROR = "DIR_LISTING_ERROR";
    private static final int STEP = 50;

    /**
     * 
     */
    public static final String TYPE = "DirectoryListingConnector";

    private WAFClient client;
    private List<URL> allFiles;
    private IterationLogger logger;

    /**
     * 
     */
    public DirectoryListingConnector() {
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	initClient(getSourceURL());

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();

	if (allFiles == null) {
	    try {
		GSLoggerFactory.getLogger(getClass()).debug("Listing all files STARTED");

		allFiles = client.deepListFiles();

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && mr.get() < allFiles.size()) {

		    GSLoggerFactory.getLogger(getClass()).debug("List reduced to {} elements", mr.get());

		    allFiles = allFiles.subList(0, mr.get());

		}

		GSLoggerFactory.getLogger(getClass()).debug("Listing all files ENDED");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			DIR_LISTING_ERROR, //
			e);
	    }
	}

	if (logger == null) {
	    logger = new IterationLogger(this, allFiles.size());
	    logger.setMessage("Total progress: ");
	}

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	int end = start + STEP;
	if (end > allFiles.size()) {
	    end = allFiles.size();
	} else {
	    response.setResumptionToken(String.valueOf(end));
	}

	List<URL> subList = allFiles.subList(start, end);
	GSLoggerFactory.getLogger(getClass()).debug("Downloading files [{}-{}/{}] STARTED", start, end, allFiles.size());

	for (URL url : subList) {

	    OriginalMetadata original = new OriginalMetadata();
	    String selectedSchema = getSetting().getSelectedSchema();
	    GSLoggerFactory.getLogger(getClass()).debug("Selected schema: {}", selectedSchema);
	    original.setSchemeURI(getSetting().getSelectedSchema());

	    Downloader downloader = new Downloader();
	    String metadata = downloader.downloadOptionalString(url.toExternalForm()).orElse(null);
	    original.setMetadata(metadata);

	    response.addRecord(original);
	    logger.iterationEnded();
	}

	if (response.getResumptionToken() == null) {
	    logger.iterationEnded();
	}

	GSLoggerFactory.getLogger(getClass()).debug("Downloading files [{}-{}/{}] ENDED", start, end, allFiles.size());

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add("CSW");
	ret.add("OAI-DC");
	ret.add("GMD");
	ret.add("GMI");
	ret.add("MULTI");
	ret.add(NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX);

	return ret;
    }

    @Override

    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	initClient(endpoint);

	try {
	    List<URL> listFiles = client.listFiles();
	    if (!listFiles.isEmpty()) {
		return true;
	    }

	    //
	    // instead of client.listAllFiles, checking only first level
	    // dirs for performance reason
	    //
	    // returns true if one of the first level dirs has at least one file
	    //
	    List<WAF_URL> listDirectories = client.listFolders();
	    if (!listDirectories.isEmpty()) {
		for (WAF_URL wAF_URL : listDirectories) {
		    listFiles = WAFClient.listFiles(wAF_URL);
		    if (!listFiles.isEmpty()) {
			return true;
		    }
		}
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Endpoint not supported {}", endpoint, e);
	}

	return false;
    }

    private void initClient(String endpoint) {

	try {

	    URL url = new URL(endpoint);
	    if (client == null) {
		GSLoggerFactory.getLogger(getClass()).debug("Client initialization STARTED");

		client = new WAFClient(url);

		GSLoggerFactory.getLogger(getClass()).debug("Client initialization ENDED");
	    }
	} catch (MalformedURLException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Bad url {}", endpoint, e);
	}
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected DirectoryListingConnectorSetting initSetting() {

	return new DirectoryListingConnectorSetting();
    }
}
