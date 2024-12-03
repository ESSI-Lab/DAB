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
import java.util.AbstractMap.SimpleEntry;

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

    public SimpleEntry<List<String>, List<String>> getIdentifiers() throws Exception {
	String url = endpoint.contains("emodnet-physics.eu")
		? endpoint + "/tabledap/allDatasets.json?datasetID&institution=%22EMODnet%20Physics%22"
		: endpoint + "/tabledap/allDatasets.json?datasetID";
	JSONObject ret = retrieveJSONObject(url);
	JSONArray rows = ret.getJSONObject("table").getJSONArray("rows");
	SimpleEntry<List<String>, List<String>> list = new SimpleEntry<List<String>, List<String>>(new ArrayList<>(), new ArrayList<>());
	for (int i = 0; i < rows.length(); i++) {
	    String row = rows.getJSONArray(i).getString(0);
	    if (row.equals("allDatasets")) {
		continue;
	    }
	    list.getKey().add(row);
	}
	List<String> toRemove = new ArrayList<String>();
	for (String id : list.getKey()) {
	    if (!id.endsWith("_METADATA")) {
		String metadataId = id + "_METADATA";
		if (list.getKey().contains(metadataId)) {
		    toRemove.add(metadataId);
		}
	    }
	}
	list.getKey().removeAll(toRemove);
	list.getValue().addAll(toRemove);
	return list;
    }

    public JSONObject getMetadata(String id) throws Exception {
	String url = endpoint + "/info/" + id + "/index.json";
	JSONObject ret = retrieveJSONObject(url);
	addDistinctValuesFromVariable(ret, id, "metadata_countries");	
	addDistinctValuesFromVariable(ret, id, "metadata_data_owners", "data_owner_EDMO", "data_owner_longname");
	addDistinctValuesFromVariable(ret, id, "metadata_platforms", "PLATFORMCODE", "call_name");

	ret.put("identifier", id);
	return ret;
    }

    private void addDistinctValuesFromVariable(JSONObject ret, String id, String variableName) throws Exception {
	if (ret.toString().contains("\"variable\", \"" + variableName + "\"")) {
	    JSONObject edmoCodes = retrieveJSONObject(endpoint + "/tabledap/" + id + ".json?" + variableName + "&distinct()");
	    if (edmoCodes != null) {
		if (edmoCodes.has("table")) {
		    JSONObject table = edmoCodes.getJSONObject("table");
		    if (table.has("rows")) {
			JSONArray codes = table.getJSONArray("rows");
			ret.put(variableName, codes);
		    }
		}
	    }
	}
    }

    private void addDistinctValuesFromVariable(JSONObject ret, String id, String attributeName, String variableName1, String variableName2)
	    throws Exception {
	String str = ret.toString();
	str = str.replace(" ", "").replace("\n", "");
	if (str.contains("\"variable\",\"" + variableName1 + "\"") && //
		str.contains("\"variable\",\"" + variableName2 + "\"")) {
	    JSONObject edmoCodes = retrieveJSONObject(
		    endpoint + "/tabledap/" + id + ".json?" + variableName1 + "%2C" + variableName2 + "&distinct()");
	    if (edmoCodes != null) {
		if (edmoCodes.has("table")) {
		    JSONObject table = edmoCodes.getJSONObject("table");
		    if (table.has("rows")) {
			JSONArray codes = table.getJSONArray("rows");
			ret.put(attributeName, codes);
		    }
		}
	    }
	}

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
