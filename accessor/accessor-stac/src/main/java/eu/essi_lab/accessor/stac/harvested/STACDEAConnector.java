package eu.essi_lab.accessor.stac.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;

public class STACDEAConnector extends STACConnector {

    private static final String DEA_GET_NODES_IDS_ERROR = "DEA_GET_NODES_IDS_ERROR";
    
    /**
     * 
     */
    private static final String DATASET_URL = "https://explorer.digitalearth.africa/stac/collections";
    
    /**
     * 
     */
    public static final String TYPE = "STAC DEA Connector";

    @Override
    public String getType() {

	return TYPE;
    }
    
    @Override
    protected List<JSONObject> getDatasetsCollection() {


	Downloader downloader = new Downloader();
	JSONObject jsonObject = new JSONObject(downloader.downloadOptionalString(DATASET_URL).get());

	return StreamUtils.iteratorToStream(jsonObject.getJSONArray("collections").iterator()).//
		map(o -> (JSONObject) o).//
		collect(Collectors.toList());
    }
    
    
    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://explorer.digitalearth.africa");
    }
}
