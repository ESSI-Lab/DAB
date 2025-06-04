package eu.essi_lab.profiler.wms.extent.feature.info;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.profiler.wms.extent.WMSRequest.Parameter;

public class DatasetFeatureInfoGenerator implements WMSFeatureInfoGenerator {

    @Override
    public InputStream getInfoPage(List<StationRecord> stations, String contentType, WMSGetFeatureInfoRequest request) {
	String html = "<html>\n" + "  <head>\n" + "    <title>DAB GetFeatureInfo output</title>\n" + "  </head>\n"
		+ "  <style type=\"text/css\">\n" + "	table.featureInfo, table.featureInfo td, table.featureInfo th {\n"
		+ "		border:1px solid #ddd;\n" + "		border-collapse:collapse;\n" + "		margin:0;\n"
		+ "		padding:0;\n" + "		font-size: 90%;\n" + "		padding:.2em .1em;\n" + "	}\n"
		+ "	table.featureInfo th {\n" + "	    padding:.2em .2em;\n" + "		font-weight:bold;\n"
		+ "		background:#eee;\n" + "	}\n" + "	table.featureInfo td{\n" + "		background:#fff;\n" + "	}\n"
		+ "	table.featureInfo tr.odd td{\n" + "		background:#eee;\n" + "	}\n"
		+ "	table.featureInfo caption{\n" + "		text-align:left;\n" + "		font-size:100%;\n"
		+ "		font-weight:bold;\n" + "		padding:.2em .2em;\n" + "	}\n" + "  </style>\n" + "  <body>\n"
		+ "  \n" + "<table class=\"featureInfo\">\n" + "  <caption class=\"featureInfo\">selected dataset(s)</caption>\n"
		+ "  <tr>\n" + //

		"    <th >Dataset name</th>\n" + //
		"    <th >Metadata information</th>\n" + //
		"    <th >Download URL</th>\n" + //
		"  </tr>\n" + //
		"\n";

	JSONArray json = new JSONArray();
	JSONObject geoJson = new JSONObject();
	
	geoJson.put("type", "FeatureCollection");
	geoJson.put("totalFeatures", "unknown");
	geoJson.put("numberReturned", stations.size());
	geoJson.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTime());
	geoJson.put("crs", request.getParameterValue(Parameter.CRS));

	for (StationRecord station : stations) {
	    // JSON
	    JSONObject obj = new JSONObject();
	    addProperty(obj, "name", "Name", station.getDatasetName(), 1, 0);
	    addProperty(obj, "metadata", "Metadata URL", station.getMetadataUrl(), 0, 1);
	    addProperty(obj, "data", "Download URL", station.getDownloadUrl(), 0, 1);
	    json.put(obj);
	    // GEO JSON
	    JSONObject geo = new JSONObject();
	    geo.put("type", "Feature");
	    geo.put("id","" );
	    JSONObject geometry = new JSONObject();
	    geometry.put("type", "Point");
	    JSONArray coordinates = new JSONArray();
	    coordinates.put(station.getBbox4326().getEast());
	    coordinates.put(station.getBbox4326().getNorth());
	    geometry.put("coordinates", coordinates);
	    geo.put("geometry", geometry);
	    JSONObject properties = new JSONObject();
	    properties.put("name", station.getPlatformName());
	    geo.put("properties", properties);
	    // HTML
	    html += "    <tr>\n" + "\n" + //
		    "  <td>" + station.getDatasetName() + "</td>    \n" + //
		    "      <td><a href='" + station.getMetadataUrl() + "' target='_blank'>Metadata information</a></td>\n" + //
		    "      <td><a href='" + station.getDownloadUrl() + "' target='_blank'>Download</a></td>\n" + //
		    "  </tr>\n";//
	}

	html += "</table>\n" + "<br/>\n" + "\n" + "  </body>\n" + "</html>";

	if (stations.isEmpty()) {
	    html = "\n" + "<html>\n" + "  <head>\n" + "    <title>DAB GetFeatureInfo output</title>\n" + "  </head>\n"
		    + "  <style type=\"text/css\">\n" + "	table.featureInfo, table.featureInfo td, table.featureInfo th {\n"
		    + "		border:1px solid #ddd;\n" + "		border-collapse:collapse;\n" + "		margin:0;\n"
		    + "		padding:0;\n" + "		font-size: 90%;\n" + "		padding:.2em .1em;\n" + "	}\n"
		    + "	table.featureInfo th {\n" + "	    padding:.2em .2em;\n" + "		font-weight:bold;\n"
		    + "		background:#eee;\n" + "	}\n" + "	table.featureInfo td{\n" + "		background:#fff;\n" + "	}\n"
		    + "	table.featureInfo tr.odd td{\n" + "		background:#eee;\n" + "	}\n"
		    + "	table.featureInfo caption{\n" + "		text-align:left;\n" + "		font-size:100%;\n"
		    + "		font-weight:bold;\n" + "		padding:.2em .2em;\n" + "	}\n" + "  </style>\n" + "  <body>\n"
		    + "  \n" + "  </body>\n" + "</html>";
	}

	ByteArrayInputStream bis;

	switch (contentType.toLowerCase()) {
	case "application/json":
	    bis = new ByteArrayInputStream(json.toString().getBytes());
	    break;
	case "application/geo+json":
	case "application/geo json":
	case "application/geo%22json":
	    bis = new ByteArrayInputStream(geoJson.toString().getBytes());
	    break;
	default:
	case "text/html":
	    bis = new ByteArrayInputStream(html.getBytes());
	}

	return bis;
    }

    private void addProperty(JSONObject obj, String key, String name, String value, int isTitle, int isLink) {
	JSONObject json = new JSONObject();
	json.put("name", name);
	json.put("value", value);
	json.put("isTitle", isTitle);
	json.put("isLink", isLink);
	obj.put(key, json);

    }

}
