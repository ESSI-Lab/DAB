package eu.essi_lab.profiler.om.resultwriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.profiler.om.JSONObservation;

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

public class JSONObservationResultWriter extends ResultWriter {

	public JSONObservationResultWriter(OutputStreamWriter writer) {
		super(writer);
	}

	@Override
	public void writeDataContent(String date, BigDecimal v, String quality, JSONObservation observation,
			List<Double> coord) {
		Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
		try {
//			if (d.isPresent()) {

			JSONObject point = null;
			if (coord != null) {
				point = observation.getPointAndLocationAndQuality(d.get(), v, coord, quality);
			} else {
				point = observation.getPointAndQuality(d.get(), v, quality);
			}
			writer.write(point.toString());
			first = false;
//			} 

		} catch (Exception e) {
		}

	}

	@Override
	protected void writeDataSeparator() throws IOException {
		writer.write(",");

	}

	@Override
	public void writeHeader() throws IOException {
		writer.write("{");
		addIdentifier(writer);
		writer.write("\"" + getSetName() + "\":[");
	}

	protected String getSetName() {
		return "member";
	}

	protected void addIdentifier(OutputStreamWriter writer) throws IOException {
		addProperty(writer, "id", "observation collection");

	}

	private void addProperty(OutputStreamWriter writer, String key, String value) throws IOException {
		writer.write("\"" + key + "\":\"" + value + "\",");

	}

	@Override
	public void writeMetadataObject(JSONObject feature) throws IOException {

		JSONObject jsonFoi = new JSONObject();
		JSONObject foi = feature.getJSONObject("featureOfInterest");
		if (!foi.has("id")) {
			System.err.println(feature);
			System.err.println("feature without id: this should not happen");
		} else {
			String href = foi.getString("id");
			jsonFoi.put("href", href);
		}

		feature.put("featureOfInterest", jsonFoi);

		writer.flush();
		writer.write("{\n");

		Iterator<String> keys = feature.keys();
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
			Object optObj = feature.opt(key);
			if (optObj != null) {
				if (optObj instanceof String) {

					writer.write(JSONObject.quote(feature.get(key).toString()));

				} else {
					writer.write(feature.get(key).toString());

				}
			}
		}
		if (!first) {
			writer.write(",\n");
		}

		writer.write(JSONObject.quote("result"));
		writer.write(": {");

		JSONObject result = feature.optJSONObject("result");
		keys = result.keys();
		first = true;
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.equals("points")) {
				// Stop before writing the array
				continue;
			}
			if (!first) {
				writer.write(",\n");
			}
			first = false;
			writer.write(JSONObject.quote(key));
			writer.write(": ");
			writer.write(result.get(key).toString());
		}
		if (!first) {
			writer.write(",\n");
		}

		writer.write(JSONObject.quote("points"));
		writer.write(": [");

	}

	@Override
	public void writeMetadataSeparator() throws IOException {
		writer.write(",");
	}

	@Override
	public void writeMetadataFooter() throws IOException {

		// for the footer
		writer.write("]\n");
		writer.write("}\n");
		writer.write("}\n");

	}

	@Override
	public void writeFooter(String resumptionToken) throws IOException {
		// close the member array
		writer.write("]\n");

		boolean completed = resumptionToken == null || resumptionToken.isEmpty();
		if (resumptionToken == null) {
			resumptionToken = "";
		}

		writer.write(",\"completed\":" + completed + ",\"resumptionToken\":\"" + resumptionToken + "\" }"); // result
																											// array
																											// closed,
		// main JSON closed
	}

}
