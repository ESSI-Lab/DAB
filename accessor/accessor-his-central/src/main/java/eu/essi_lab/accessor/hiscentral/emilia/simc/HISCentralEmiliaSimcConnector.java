package eu.essi_lab.accessor.hiscentral.emilia.simc;

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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests station–dataset time series from the ARPAE-SIMC Eve REST API
 * ({@link ArpaeSimcMeteoOpenDataClient}).
 */
public class HISCentralEmiliaSimcConnector extends HarvestedQueryConnector<HISCentralEmiliaSimcConnectorSetting> {

    private static final String HISCENTRAL_EMILIA_SIMC_URL_NOT_FOUND_ERROR = "HISCENTRAL_EMILIA_SIMC_URL_NOT_FOUND_ERROR";

    /** Default Eve REST root (see {@link ArpaeSimcMeteoOpenDataClient#DEFAULT_BASE_URL}). */
    public static final String BASE_URL = ArpaeSimcMeteoOpenDataClient.DEFAULT_BASE_URL;

    /** Eve max_results per page when listing stations and the dataset catalogue. */
    private static final int API_PAGE_SIZE = 200;

    static final String TYPE = "HISCentralEmiliaSimcConnector";

    private final List<OriginalMetadata> harvestRecords = new ArrayList<>();
    private int partialCount;

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.contains("apps.arpae.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (harvestRecords.isEmpty()) {
	    populateHarvestRecords();
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.parseInt(token);
	}

	int pageSize = getSetting().getPageSize();
	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start >= mr.get()) {
	    maxNumberReached = true;
	}

	if (start < harvestRecords.size() && !maxNumberReached) {
	    int end = Math.min(start + pageSize, harvestRecords.size());
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    for (int i = start; i < end; i++) {
		ret.addRecord(harvestRecords.get(i));
		partialCount++;
	    }
	    if (end < harvestRecords.size() && (getSetting().isMaxRecordsUnlimited() || !mr.isPresent() || end < mr.get())) {
		ret.setResumptionToken(String.valueOf(end));
	    } else {
		ret.setResumptionToken(null);
		logger.info("Emilia-SIMC harvest finished: {} dataset records", harvestRecords.size());
		partialCount = 0;
	    }
	} else {
	    ret.setResumptionToken(null);
	    logger.debug("Emilia-SIMC listRecords complete (total {})", harvestRecords.size());
	    partialCount = 0;
	}

	return ret;
    }

    private void populateHarvestRecords() throws GSException {
	String baseUrl = getSourceURL();
	if (baseUrl == null || baseUrl.isEmpty()) {
	    baseUrl = BASE_URL;
	}
	ArpaeSimcMeteoOpenDataClient client = new ArpaeSimcMeteoOpenDataClient(baseUrl);
	try {
	    Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> datasetIndex = loadDatasetIndex(client);
	    List<ArpaeSimcMeteoOpenDataClient.SimcStation> stations = client.listAllStations(null, "_id", API_PAGE_SIZE);
	    logger.info("Emilia-SIMC: loaded {} stations, {} dataset catalogue entries", stations.size(), datasetIndex.size());

	    for (ArpaeSimcMeteoOpenDataClient.SimcStation station : stations) {
		List<ArpaeSimcMeteoOpenDataClient.SimcStationSummary> summaries = station.summaries();
		if (summaries == null || summaries.isEmpty()) {
		    continue;
		}
		for (ArpaeSimcMeteoOpenDataClient.SimcStationSummary summary : summaries) {
		    String datasetResource = datasetResourceFromHref(client, summary.href());
		    ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor descriptor = lookupDataset(client, datasetIndex,
			    summary.href());
		    harvestRecords.add(HISCentralEmiliaSimcMapper.create(station, summary, descriptor, datasetResource));
		}
	    }
	} catch (IOException e) {
	    throw GSException.createException(getClass(), "Unable to read ARPAE-SIMC open data", null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, HISCENTRAL_EMILIA_SIMC_URL_NOT_FOUND_ERROR, e);
	}
    }

    static Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> loadDatasetIndex(ArpaeSimcMeteoOpenDataClient client)
	    throws IOException {
	List<ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> rows = client.listAllDatasets(null, "_id", API_PAGE_SIZE);
	Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> index = new HashMap<>();
	for (ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor dd : rows) {
	    if (dd.id() != null && !dd.id().isEmpty()) {
		index.putIfAbsent(dd.id(), dd);
	    }
	    if (dd.href() != null && !dd.href().isEmpty()) {
		String resolved = client.resolveHref(dd.href());
		String seg = lastPathSegment(resolved);
		if (seg != null && !seg.isEmpty()) {
		    index.putIfAbsent(seg, dd);
		}
	    }
	}
	return index;
    }

    static ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor lookupDataset(ArpaeSimcMeteoOpenDataClient client,
	    Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> index, String href) {
	if (href == null || href.isEmpty()) {
	    return null;
	}
	String key = datasetResourceFromHref(client, href);
	ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor dd = index.get(key);
	if (dd != null) {
	    return dd;
	}
	return index.get(href.trim());
    }

    static String datasetResourceFromHref(ArpaeSimcMeteoOpenDataClient client, String href) {
	if (href == null || href.isEmpty()) {
	    return null;
	}
	String resolved = client.resolveHref(href);
	return lastPathSegment(resolved);
    }

    static String lastPathSegment(String urlOrPath) {
	if (urlOrPath == null || urlOrPath.isEmpty()) {
	    return null;
	}
	try {
	    URI u = URI.create(urlOrPath.contains("://") ? urlOrPath
		    : "https://placeholder.local" + (urlOrPath.startsWith("/") ? urlOrPath : "/" + urlOrPath));
	    String path = u.getPath();
	    while (path.endsWith("/") && path.length() > 1) {
		path = path.substring(0, path.length() - 1);
	    }
	    int slash = path.lastIndexOf('/');
	    return slash >= 0 ? path.substring(slash + 1) : path;
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_EMILIA_SIMC_NS_URI);
	return ret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected HISCentralEmiliaSimcConnectorSetting initSetting() {
	return new HISCentralEmiliaSimcConnectorSetting();
    }
}
