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

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ANASARClient client = new ANASARClient();
	String token = request.getResumptionToken();
	if (token == null) {
	    token = "0";
	}
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	int from = Integer.parseInt(token);
	int to = Math.min(client.getStations().size(), from + PAGE_SIZE);
	if (to != client.getStations().size()) {
	    ret.setResumptionToken("" + to);
	}
	List<JSONObject> page = client.getStations().subList(from, to);
	Optional<Integer> mr = getSetting().getMaxRecords();
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && to > mr.get()) {
	    ret.setResumptionToken(null);
	    if (page.size() > mr.get()) {
		page = page.subList(0, mr.get());
	    }
	}
	for (JSONObject item : page) {
	    if (item.has("res_id") && item.has("tsi_id")) {
		String resId = "unknown";
		try {
		    resId = item.get("res_id").toString().trim();

		    List<JSONObject> series = client.getSeriesInformation(resId);
		    for (JSONObject serie : series) {
			OriginalMetadata record = new OriginalMetadata();
			record.setMetadata(serie.toString());
			record.setSchemeURI(CommonNameSpaceContext.ANA_SAR_URI);
			ret.addRecord(record);
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error for reservoir: " + resId + " (skipping)");
		}

	    }else {
		GSLoggerFactory.getLogger(getClass()).error("Missing res id or tsi id " );
	    }
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
