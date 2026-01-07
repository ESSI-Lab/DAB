/**
 *
 */
package eu.essi_lab.accessor.hydroshare;

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
public class HydroshareConnector extends HarvestedQueryConnector<HydroshareConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HydroshareConnector";

    private int recordsCount;
    private int count;

    public HydroshareConnector() {

	count = -1;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	String page = "1";

	if (request.getResumptionToken() != null) {

	    page = request.getResumptionToken();
	}

	String query = getSourceURL() + "page=" + page + "&format=json&count=1000";
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(query);

	if (stream.isPresent()) {
	    try {

		Optional<Integer> maxRecords = getSetting().getMaxRecords();

		ClonableInputStream clone = new ClonableInputStream(stream.get());

		List<String> results = HydroshareMapper.splitPageResults(clone);
		for (String result : results) {

		    recordsCount++;

		    if (getSetting().isMaxRecordsUnlimited() || (maxRecords.isPresent() && recordsCount <= maxRecords.get())) {

			OriginalMetadata metadata = new OriginalMetadata();

			metadata.setSchemeURI(HydroshareMapper.HYDROSHARE_SCHEME_URI);
			metadata.setMetadata(result);

			response.addRecord(metadata);
		    }
		}

		if (count == -1) {
		    JSONObject jsonObject = JSONUtils.fromStream(clone.clone());
		    count = jsonObject.getInt("count");
		}

		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}/{}", recordsCount, count);

		boolean hasResults = recordsCount < count;

		if (hasResults && (getSetting().isMaxRecordsUnlimited() || (maxRecords.isPresent() && recordsCount <= maxRecords.get()))) {
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
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(HydroshareMapper.HYDROSHARE_SCHEME_URI);
    }

    @Override
    public String getSourceURL() {

	String url = super.getSourceURL();
	if (!url.endsWith("?")) {
	    url += "?";
	}

	return url;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://www.hydroshare.org/hsapi/resource");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HydroshareConnectorSetting initSetting() {

	return new HydroshareConnectorSetting();
    }

}
