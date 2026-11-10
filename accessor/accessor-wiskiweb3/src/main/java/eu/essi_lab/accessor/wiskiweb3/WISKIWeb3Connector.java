package eu.essi_lab.accessor.wiskiweb3;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per series of a KISTERS WISKI-Web 3 site: for each station parameter listed by the time series layers,
 * the high resolution series of the week file (rolling window of about one week) and the daily mean series of the year file.
 * The extent of each series is read from its file at harvesting time (only the beginning and the end of the year files are
 * downloaded, with range requests). The organisation and the licence are read from the web application, unless set in the
 * connector settings
 *
 * @author boldrini
 */
public class WISKIWeb3Connector extends HarvestedQueryConnector<WISKIWeb3ConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "WISKIWeb3Connector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 20;

    /**
     * Hosts of the known WISKI-Web 3 sites
     */
    static final String[] KNOWN_HOSTS = new String[] { "hlnug.de", "hochwasserportal.nrw" };

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<WISKIWeb3Entity> series;
    private String organisation;
    private String license;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	WISKIWeb3Client client = new WISKIWeb3Client(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureSeriesLoaded(client);

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + SERIES_PER_PAGE, series.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving extents of series [{}-{}/{}]", index, nextIndex, series.size());

	boolean stoppedByMax = false;

	for (WISKIWeb3Entity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    boolean valid;
	    try {
		valid = client.completeSeries(entity);
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to read series {}", entity.getSeriesId());
		valid = false;
	    }

	    if (!valid) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no values", entity.getSeriesId());
		continue;
	    }

	    addSiteInfo(entity);

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(WISKIWeb3Mapper.WISKIWEB3_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	if (stoppedByMax || nextIndex >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param entity
     */
    private void addSiteInfo(WISKIWeb3Entity entity) {

	if (organisation != null) {
	    entity.getObject().put(WISKIWeb3Client.ORGANISATION, organisation);
	}
	if (license != null) {
	    entity.getObject().put(WISKIWeb3Client.LICENSE, license);
	}
	getSetting().getCountry().ifPresent(country -> entity.getObject().put(WISKIWeb3Client.COUNTRY, country));
    }

    /**
     * @param client
     * @throws GSException
     */
    private void ensureSeriesLoaded(WISKIWeb3Client client) throws GSException {

	if (series != null) {
	    return;
	}

	organisation = getSetting().getOrganisation().orElse(null);
	license = getSetting().getLicense().orElse(null);

	if (organisation == null || license == null) {

	    String appURL = getSetting().getAppURL().orElse(WISKIWeb3Client.getDefaultAppURL(client.getEndpoint()));
	    WISKIWeb3Client.SiteInfo info = client.retrieveSiteInfo(appURL);

	    organisation = organisation == null ? info.getOrganisation().orElse(null) : organisation;
	    license = license == null ? info.getLicense().orElse(null) : license;

	    GSLoggerFactory.getLogger(getClass()).info("Organisation: {}, licence: {}", organisation, license);
	}

	List<String> stations = getSetting().getStations();
	List<WISKIWeb3Entity> all = client.retrieveSeries(getSetting().getLayers(), getSetting().getFiles());

	series = new ArrayList<>();
	for (WISKIWeb3Entity entity : all) {
	    if (stations.isEmpty() || stations.contains(entity.getStationNumber())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} WISKIweb3 candidate series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(WISKIWeb3Mapper.WISKIWEB3_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	if (endpoint == null) {
	    return false;
	}

	return endpoint.toLowerCase().contains("wiskiweb") || Arrays.stream(KNOWN_HOSTS).anyMatch(endpoint::contains);
    }

    @Override
    protected WISKIWeb3ConnectorSetting initSetting() {

	return new WISKIWeb3ConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
