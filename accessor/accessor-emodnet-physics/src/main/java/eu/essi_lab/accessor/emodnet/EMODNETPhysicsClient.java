package eu.essi_lab.accessor.emodnet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class EMODNETPhysicsClient {

    private String endpoint = null;

    public EMODNETPhysicsClient() {
	this("https://data-erddap.emodnet-physics.eu/erddap");
    }

    public EMODNETPhysicsClient(String endpoint) {
	this.endpoint = endpoint;
    }

    public List<String> getIdentifiers() throws Exception {
	String url = endpoint.contains("emodnet-physics.eu")
		? endpoint + "/tabledap/allDatasets.json?datasetID&institution=%22EMODnet%20Physics%22"
		: endpoint + "/tabledap/allDatasets.json?datasetID";
	JSONObject ret = retrieveJSONObject(url);
	JSONArray rows = ret.getJSONObject("table").getJSONArray("rows");
	List<String> list = new ArrayList<>();
	for (int i = 0; i < rows.length(); i++) {
	    String row = rows.getJSONArray(i).getString(0);
	    if (row.equals("allDatasets")) {
		continue;
	    }
	    list.add(row);
	}
	List<String> toRemove = new ArrayList<String>();
	for (String id : list) {
	    if (!id.endsWith("_METADATA")) {
		String metadataId = id + "_METADATA";
		if (list.contains(metadataId)) {
		    toRemove.add(metadataId);
		}
	    }
	}
	list.removeAll(toRemove);
	return list;
    }

    public JSONObject getMetadata(String id) throws Exception {
	String url = endpoint + "/info/" + id + "/index.json";
	JSONObject ret = retrieveJSONObject(url);
	if (ret.toString().contains("data_owner_EDMO")) {
	    JSONObject edmoCodes = retrieveJSONObject(endpoint + "/tabledap/" + id + ".json?data_owner_EDMO&distinct()");
	    if (edmoCodes != null) {
		if (edmoCodes.has("table")) {
		    JSONObject table = edmoCodes.getJSONObject("table");
		    if (table.has("rows")) {
			JSONArray codes = table.getJSONArray("rows");
			ret.put("data_owner_EDMO", codes);
		    }
		}
	    }
	}
	// JSONObject platformTypes = retrieveJSONObject(endpoint + "/tabledap/" + id +
	// ".json?data_owner_EDMO&distinct()");
	// if (platformTypes != null) {
	// if (platformTypes.has("table")) {
	// JSONObject table = platformTypes.getJSONObject("table");
	// if (table.has("rows")) {
	// JSONArray codes = table.getJSONArray("rows");
	// ret.put("data_owner_EDMO", codes);
	// }
	// }
	// }
	ret.put("identifier", id);
	return ret;
    }

    private JSONObject retrieveJSONObject(String url) throws Exception {
	Downloader downloader = new Downloader();

	HttpResponse<InputStream> response;
	int tries = 3;
	do {
	    response = downloader.downloadResponse(url);
	    if (response.statusCode() != 200) {
		Thread.sleep(2000);
	    }
	} while (response.statusCode() != 200 && tries-- > 0);

	if (response.statusCode() != 200) {
	    return null;
	}
	InputStream stream = response.body();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	String str = new String(baos.toByteArray());
	return new JSONObject(str);
    }

}
