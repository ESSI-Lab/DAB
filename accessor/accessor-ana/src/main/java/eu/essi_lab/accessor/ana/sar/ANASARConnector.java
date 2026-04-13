package eu.essi_lab.accessor.ana.sar;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ANASARConnector extends HarvestedQueryConnector<ANASARConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ANASARConnector";

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.equals(ANASARClient.serviceEndpoint);
    }

    private static final int PAGE_SIZE = 10;

    /** Metadata records emitted in the current harvesting run (aligned with {@link eu.essi_lab.accessor.ana.ANAConnector}). */
    private int partialNumbers;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ANASARClient client = new ANASARClient();
	String token = request.getResumptionToken();
	if (token == null) {
	    token = "0";
	}
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();
	if (!unlimited && mr.isPresent() && partialNumbers > mr.get() - 1) {
	    ret.setResumptionToken(null);
	    partialNumbers = 0;
	    return ret;
	}

	int from = Integer.parseInt(token);
	List<JSONObject> stations = client.getStations();
	int stationCount = stations.size();
	int to = Math.min(stationCount, from + PAGE_SIZE);
	List<JSONObject> page = stations.subList(from, to);

	boolean stoppedByMax = false;

	stationsLoop: for (JSONObject item : page) {
	    if (item.has("res_id") && item.has("tsi_id")) {
		String resId = "unknown";
		try {
		    resId = item.get("res_id").toString().trim();

		    List<JSONObject> series = client.getSeriesInformation(resId);
		    for (JSONObject serie : series) {
			if (!unlimited && mr.isPresent() && partialNumbers > mr.get() - 1) {
			    stoppedByMax = true;
			    break stationsLoop;
			}
			OriginalMetadata record = new OriginalMetadata();
			record.setMetadata(serie.toString());
			record.setSchemeURI(CommonNameSpaceContext.ANA_SAR_URI);
			ret.addRecord(record);
			partialNumbers++;
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error for reservoir: " + resId + " (skipping)");
		}

	    } else {
		GSLoggerFactory.getLogger(getClass()).error("Missing res id or tsi id ");
	    }
	}

	if (stoppedByMax) {
	    ret.setResumptionToken(null);
	    partialNumbers = 0;
	    return ret;
	}

	if (to != stationCount) {
	    ret.setResumptionToken("" + to);
	} else {
	    ret.setResumptionToken(null);
	    partialNumbers = 0;
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.ANA_SAR_URI);
	return toret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected ANASARConnectorSetting initSetting() {

	return new ANASARConnectorSetting();
    }

}
