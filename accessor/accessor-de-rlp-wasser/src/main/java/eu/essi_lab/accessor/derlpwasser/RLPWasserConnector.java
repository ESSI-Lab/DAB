package eu.essi_lab.accessor.derlpwasser;

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

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per time series (measurement site and parameter) of the flood portal of Rhineland-Palatinate. The sites are
 * listed by the portal configuration, then the measurements of each site are requested (one request per site) to find which
 * parameters are measured and the extent of the rolling window currently published (first and last valid value).
 *
 * @author boldrini
 */
public class RLPWasserConnector extends HarvestedQueryConnector<RLPWasserConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "RLPWasserConnector";

    /**
     * Maximum number of measurement sites requested in a single page
     */
    static final int SITES_PER_PAGE = 10;

    /**
     * Pause between two measurement site requests, to be polite with the portal
     */
    static final long REQUEST_PAUSE = 200;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<JSONObject> sites;

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

	RLPWasserClient client = new RLPWasserClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureSitesLoaded(client);

	if (index >= sites.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + SITES_PER_PAGE, sites.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving measurements of sites [{}-{}/{}]", index, nextIndex, sites.size());

	List<RLPWasserParameter> parameters = getSetting().getParameters();
	boolean stoppedByMax = false;

	for (JSONObject site : sites.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    String number = site.getString(RLPWasserClient.NUMBER);

	    Optional<JSONObject> data = client.retrieveSiteData(number);
	    pause();

	    if (data.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).debug("Site {} not available", number);
		continue;
	    }

	    List<RLPWasserEntity> series = RLPWasserClient.createSeries(site, data.get(), parameters);
	    if (series.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).debug("Site {} has no current measurements", number);
	    }

	    for (RLPWasserEntity entity : series) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(RLPWasserMapper.RLP_WASSER_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }
	}

	if (stoppedByMax || nextIndex >= sites.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     *
     */
    private void pause() {

	try {
	    Thread.sleep(REQUEST_PAUSE);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
    }

    /**
     * @param client
     * @throws GSException
     */
    private void ensureSitesLoaded(RLPWasserClient client) throws GSException {

	if (sites != null) {
	    return;
	}

	List<String> numbers = getSetting().getSites();
	boolean municipal = getSetting().isIncludeMunicipal();
	boolean external = getSetting().isIncludeExternal();

	sites = new ArrayList<>();
	for (JSONObject site : client.retrieveSites()) {

	    RLPWasserEntity entity = new RLPWasserEntity(site);

	    if (!numbers.isEmpty() && !numbers.contains(entity.getSiteNumber())) {
		continue;
	    }
	    if (!municipal && entity.isMunicipal()) {
		continue;
	    }
	    if (!external && entity.isExternal()) {
		continue;
	    }

	    sites.add(site);
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} RLP measurement sites", sites.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(RLPWasserMapper.RLP_WASSER_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("hochwasser.rlp.de");
    }

    @Override
    protected RLPWasserConnectorSetting initSetting() {

	return new RLPWasserConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
