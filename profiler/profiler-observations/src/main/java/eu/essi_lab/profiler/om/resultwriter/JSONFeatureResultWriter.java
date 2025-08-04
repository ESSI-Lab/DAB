package eu.essi_lab.profiler.om.resultwriter;

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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.json.JSONObject;

public class JSONFeatureResultWriter extends JSONObservationResultWriter {

	private String viewId;

	protected String getSetName() {
		return "results";
	}

	public JSONFeatureResultWriter(OutputStreamWriter writer, String viewId) {
		super(writer, true);
		this.viewId = viewId;
	}

	public String getGeometryName() {
		if (viewId == null || !(viewId.equals("i-change") || viewId.equals("trigger"))) {
			return "shape";
		} else {
			return "geometry";
		}
	}

	@Override
	public void writeMetadataObject(JSONObject feature) throws IOException {

		JSONObject monitoringPoint = feature.getJSONObject("featureOfInterest");
		monitoringPoint.remove("type");

		String geometryName = getGeometryName();
		if (monitoringPoint.has(geometryName)) {
			JSONObject shape = monitoringPoint.getJSONObject(geometryName);
			monitoringPoint.put(getGeometryName(), shape);
		}

		writer.write("{\n");

		Iterator<String> keys = monitoringPoint.keys();
		boolean first = true;
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equals("result")) {
				// Stop before writing the array
				continue;
			}
			if (!first) {
				writer.write(",\n");
			}
			first = false;
			writer.write(JSONObject.quote(key));
			writer.write(": ");
			Object optObj = monitoringPoint.opt(key);
			if (optObj != null) {
				if (optObj instanceof String) {

					writer.write(JSONObject.quote(monitoringPoint.get(key).toString()));

				} else {
					writer.write(monitoringPoint.get(key).toString());

				}
			}
		}

	}

	@Override
	public void writeMetadataFooter() throws IOException {
		writer.write("}\n");
	}

}
