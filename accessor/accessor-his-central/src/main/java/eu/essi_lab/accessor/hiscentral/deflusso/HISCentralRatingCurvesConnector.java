package eu.essi_lab.accessor.hiscentral.deflusso;

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

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ratings.RatingCurves;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests rating curves ("scale di deflusso") published as {@code .xlsx} files in a shared SharePoint folder.
 * The folder sharing link is the source endpoint. Each station found in the spreadsheets becomes one record.
 *
 * @author boldrini
 */
public class HISCentralRatingCurvesConnector extends HarvestedQueryConnector<HISCentralRatingCurvesConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralRatingCurvesConnector";

    private int countDataset = 0;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	try {

	    String endpoint = getSourceURL();

	    RatingCurvesClient client = new RatingCurvesClient(endpoint);
	    List<RatingCurves> stations = client.harvest();

	    int maxRecords = stations.size();
	    if (getSetting().getMaxRecords().isPresent()) {
		maxRecords = Math.min(maxRecords, getSetting().getMaxRecords().get());
	    }

	    for (int i = 0; i < maxRecords; i++) {

		RatingCurves station = stations.get(i);
		ret.addRecord(HISCentralRatingCurvesMapper.create(endpoint, station));
		countDataset++;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Total number of rating curve datasets: {}", countDataset);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error while harvesting rating curves", e);
	}

	// single page harvesting
	ret.setResumptionToken(null);

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.contains("sharepoint.com");
    }

    @Override
    protected HISCentralRatingCurvesConnectorSetting initSetting() {

	return new HISCentralRatingCurvesConnectorSetting();
    }
}
