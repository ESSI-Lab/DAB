package eu.essi_lab.profiler.wms.cluster.feature.info;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.math.BigDecimal;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.profiler.wms.cluster.WMSRequest.Parameter;

public class StationFeatureInfoGenerator implements WMSFeatureInfoGenerator {

    @Override
    public InputStream getInfoPage(String viewId, List<StationRecord> stations, int total, String contentType,
	    WMSGetFeatureInfoRequest request) {

	StringBuilder htmlBuilder = new StringBuilder();

	htmlBuilder = build(htmlBuilder, stations.size(), total, false);

	JSONArray json = new JSONArray();

	JSONObject geoJson = new JSONObject();

	geoJson.put("type", "FeatureCollection");
	geoJson.put("totalFeatures", total);
	geoJson.put("numberReturned", stations.size());
	geoJson.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTime());
	geoJson.put("crs", request.getParameterValue(Parameter.CRS));

	JSONArray features = new JSONArray();
	geoJson.put("features", features);

	for (StationRecord station : stations) {

	    //
	    // JSON
	    //
	    JSONObject obj = new JSONObject();
	    addProperty(obj, "name", "Name", station.getDatasetName(), 1, 0);
	    addProperty(obj, "metadata", "Metadata URL", station.getMetadataUrl(), 0, 1);
	    json.put(obj);

	    //
	    // GEO JSON
	    //
	    JSONObject geo = new JSONObject();
	    geo.put("type", "Feature");
	    if (station.getPlatformIdentifier() != null) {
		geo.put("id", station.getPlatformIdentifier());
	    }
	    JSONObject geometry = new JSONObject();

	    BigDecimal w = station.getBbox4326().getWest();
	    BigDecimal e = station.getBbox4326().getEast();
	    BigDecimal s = station.getBbox4326().getSouth();
	    BigDecimal n = station.getBbox4326().getNorth();

	    double TOL = 0.00000001d;
	    JSONArray coordinates = new JSONArray();
	    if (w != null && s != null && e != null && n != null) {
		if (n.doubleValue() - s.doubleValue() > TOL && e.doubleValue() - w.doubleValue() > TOL) {
		    geometry.put("type", "Polygon");
		    JSONArray exteriorRing = new JSONArray();
		    exteriorRing.put(getJSONArray(w.doubleValue(), s.doubleValue()));
		    exteriorRing.put(getJSONArray(w.doubleValue(), n.doubleValue()));
		    exteriorRing.put(getJSONArray(e.doubleValue(), n.doubleValue()));
		    exteriorRing.put(getJSONArray(e.doubleValue(), s.doubleValue()));
		    exteriorRing.put(getJSONArray(w.doubleValue(), s.doubleValue()));
		    coordinates.put(exteriorRing);
		} else {
		    geometry.put("type", "Point");
		    coordinates.put(station.getBbox4326().getEast());
		    coordinates.put(station.getBbox4326().getNorth());
		}
	    }

	    geometry.put("coordinates", coordinates);
	    geo.put("geometry", geometry);

	    JSONObject properties = new JSONObject();
	    if (station.getDatasetName() != null) {
		properties.put("name", station.getDatasetName());
	    }
	    if (station.getPlatformName() != null) {
		properties.put("name", station.getPlatformName());
	    }

	    geo.put("properties", properties);
	    features.put(geo);

	    //
	    // HTML
	    //

	    htmlBuilder = append(htmlBuilder, station, viewId);
	}

	if (stations.isEmpty()) {

	    htmlBuilder = build(new StringBuilder(), stations.size(), 0, true);

	} else {

	    htmlBuilder = close(htmlBuilder);
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
	    bis = new ByteArrayInputStream(htmlBuilder.toString().getBytes());
	}

	return bis;
    }

    /**
     * @param builder
     * @return
     */
    private StringBuilder close(StringBuilder builder) {

	builder.append("</table></div>\n" + "<br/>\n" + "\n" + "  </body>\n" + "</html>");
	return builder;
    }

    /**
     * @param builder
     * @param station
     * @param viewId
     * @return
     */
    private StringBuilder append(StringBuilder builder, StationRecord station, String viewId) {

	String url = "../services/view/" + viewId + "/bnhs/station/" + station.getPlatformIdentifier() + "/";

	String image = "<a href='" + url
		+ "' target='_blank'><i class=\"font-awesome-button-icon fa fa-info-circle\" style=\"color:blue; font-size:15px;\" aria-hidden=\"true\"></i></a>";

	builder.append("<tr>\n");
	builder.append("  <td style='vertical-align: middle;'>" + station.getDatasetName() + "</td>    \n");
	builder.append("  <td style='vertical-align: middle;'>" + station.getSourceLabel() + "</td>    \n");
	builder.append("  <td style='text-align: center;'>" + image + "</td>\n");
//	builder.append("  <td style='text-align: center; cursor:pointer' id='addToSearch_" + station.getDatasetName()
//		+ "'><i class=\"font-awesome-button-icon fa fa-plus-circle\" style=\"color:darkred; font-size:15px;\" aria-hidden=\"true\"></i></td>\n");
	builder.append("  <td style='border: 1px solid transparent; background: transparent;'></td>\n");

	builder.append("</tr>\n");

	return builder;
    }

    /**
     * @param empty
     * @return
     */
    private StringBuilder build(StringBuilder builder, int returned, int total, boolean empty) {

	builder.append("<html>\n");
	builder.append("  <head>\n");
	builder.append("    <title>DAB GetFeatureInfo output</title>\n");
	builder.append("  </head>\n");

	builder.append(" <style type='text/css'>\n");

	builder.append("	table.featureInfo, table.featureInfo td, table.featureInfo th {\n");
	builder.append("		border:1px solid black;\n");
	builder.append("		border-collapse:collapse;\n");
	builder.append("		margin:0;\n");
	builder.append("		font-size: 100%;\n");
	builder.append("		padding:.2em .5em;\n");
	builder.append("	}\n");

	builder.append("	table.featureInfo th {\n");
	builder.append("	    	padding:.5em .5em;\n");
	builder.append("		font-weight:bold;\n");
	builder.append("		background:#eee;\n");
	builder.append("	}\n");

	builder.append("	table.featureInfo td{\n");
	builder.append("		background:#fff;\n");
	builder.append("	}\n");

	builder.append("	table.featureInfo tr.odd td{\n");
	builder.append("		background:#eee;\n");
	builder.append("	}\n");

	builder.append("	table.featureInfo caption{\n");
	builder.append("		text-align:left;\n");
	builder.append("		font-size:100%;\n");
	builder.append("		font-weight:bold;\n");
	builder.append("		padding:.2em .2em;\n");
	builder.append("	}\n");
	builder.append("  </style>\n");
	builder.append("  <body>\n");

	if (!empty) {
	    String info = "";
	    if (returned!=total) {
		info =" (" + returned + " of " + total + ")";
	    }
	    builder.append("<div style='max-height: 400px; overflow-y: auto'><table class='featureInfo'>\n");
	    builder.append("<tr>\n");
	    builder.append(" <th >Station" + info + "</th>\n");
	    builder.append(" <th >Source</th>\n");
	    builder.append(" <th >Station info</th>\n");
//	    builder.append(" <th >Add to search</th>\n");
	    builder.append(
		    " <th title='Close' id='closePopup' style='background: white; cursor: pointer; background:'><i class=\"font-awesome-button-icon fa fa-times\" style=\"font-size:15px;\" aria-hidden=\"true\"></i></th>\n");
	    builder.append(" </tr>\n");

	} else {

	    builder.append("\n</body>\n </html>");
	}

	return builder;
    }

    private JSONArray getJSONArray(double lon, double lat) {
	JSONArray ret = new JSONArray();
	ret.put(lon);
	ret.put(lat);
	return ret;
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
