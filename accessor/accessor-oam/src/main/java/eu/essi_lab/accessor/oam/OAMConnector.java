/**
 *
 */
package eu.essi_lab.accessor.oam;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class OAMConnector extends HarvestedQueryConnector<OAMConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "OAMConnector";

    private static final int LIMIT = 50;

    private int recordsCount;
    private int found;

    public OAMConnector() {

	found = -1;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	int page = 1;
	if (request.getResumptionToken() != null) {

	    page = Integer.valueOf(request.getResumptionToken());
	}

	String query = getSourceURL() + "limit=" + LIMIT + "&page=" + page;
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(query);
	if (stream.isPresent()) {

	    try {

		ClonableInputStream clone = new ClonableInputStream(stream.get());

		JSONObject jsonObject = JSONUtils.fromStream(clone.clone());

		if (found == -1) {
		    found = jsonObject.getJSONObject("meta").getInt("found");
		}

		ArrayList<String> results = OAMMapper.splitPageResults(clone);

		Optional<Integer> mr = getSetting().getMaxRecords();

		for (String result : results) {

		    recordsCount++;

		    if (getSetting().isMaxRecordsUnlimited() || (mr.isPresent() && recordsCount <= mr.get())) {

			OriginalMetadata metadata = new OriginalMetadata();

			metadata.setSchemeURI(OAMMapper.OAM_SCHEMA_URI);
			metadata.setMetadata(result);

			response.addRecord(metadata);
		    }
		}

		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}/{}", recordsCount, found);

		boolean hasResults = recordsCount < found;

		if (hasResults && (getSetting().isMaxRecordsUnlimited() || (mr.isPresent() && recordsCount <= mr.get()))) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(page) + 1));
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	} else {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve page {}", page);

	}

	return response;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://api.openaerialmap.org/meta");
    }

    @Override
    public String getSourceURL() {

	return super.getSourceURL().endsWith("?") ? super.getSourceURL() : super.getSourceURL() + "?";
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(OAMMapper.OAM_SCHEMA_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected OAMConnectorSetting initSetting() {

	return new OAMConnectorSetting();
    }
}
