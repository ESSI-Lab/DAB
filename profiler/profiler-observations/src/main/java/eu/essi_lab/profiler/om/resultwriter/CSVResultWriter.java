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
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.profiler.om.JSONObservation;

public class CSVResultWriter extends ResultWriter {

	public enum CSVField {

		TIMESERIES_ID("Timeseries identifier"), MONITORING_POINT("Monitoring point"), UOM("Units"),
		OBSERVED_PROPERTY("Observed property"), DATE_TIME("Date time"), VALUE("Value"), LATITUDE("Latitude"),
		LONGITUDE("Longitude"), QUALITY("Quality");

		private String label;

		CSVField(String label) {
			this.label = label;
		}
	}
	
	CSVField[] fields = new CSVField[] { CSVField.MONITORING_POINT, CSVField.OBSERVED_PROPERTY, CSVField.TIMESERIES_ID,
			CSVField.DATE_TIME, CSVField.VALUE, CSVField.UOM, CSVField.LATITUDE, CSVField.LONGITUDE,
			CSVField.QUALITY };
	
	public CSVResultWriter(OutputStreamWriter writer) {
		super(writer);

	}

	@Override
	public void writeDataContent(String date, BigDecimal v, String quality,JSONObservation observation,
			List<Double> coord) throws IOException {
		Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
		try {
			JSONObject point = null;
			if (coord != null) {
				point = observation.getPointAndLocationAndQuality(d.get(), v, coord, quality);
			} else {
				point = observation.getPointAndQuality(d.get(), v, quality);
			}
			writeCSVobservation(writer, observation, point, fields);

		} catch (Exception e) {
		}

	}

	private void writeCSVobservation(OutputStreamWriter writer, JSONObservation observation, JSONObject point,
			CSVField... fields) throws IOException {

		SimpleEntry<BigDecimal, BigDecimal> latLon = observation.getFeatureOfInterest().getLatLonPoint();
		String lat = "";
		String lon = "";
		if (latLon != null) {
			lat = latLon.getKey() == null ? "" : latLon.getKey().toString();
			lon = latLon.getValue() == null ? "" : latLon.getValue().toString();
		}
		String observedPropertyTitle = observation.getObservedPropertyTitle();
		if (observedPropertyTitle == null) {
			observedPropertyTitle = "";
		}
		String timeseriesId = observation.getId();
		if (timeseriesId == null) {
			timeseriesId = "";
		}
		String platformTitle = observation.getFeatureOfInterest().getSampledFeatureTitle();
		if (platformTitle == null) {
			platformTitle = "";
		}
		String uom = observation.getUOM();
		if (uom == null) {
			uom = "";
		}

		JSONObject timeObject = point.getJSONObject("time");
		String time = timeObject.getString("instant");
		String quality = null;
		if (point.has("metadata")) {
			JSONObject metadataObject = point.getJSONObject("metadata");
			if (metadataObject.has("quality")) {
				JSONObject qualityObject = metadataObject.getJSONObject("quality");
				String term = qualityObject.getString("term");
				String vocab = qualityObject.getString("vocabulary");
				String separator = "";
				if (!vocab.endsWith("/") && !term.startsWith("/")) {
					separator = "/";
				}
				quality = vocab + separator + term;
			}
		}
		BigDecimal value = null;
		if (point.has("value")) {
			value = point.getBigDecimal("value");
		}
		int j = 0;
		for (CSVField field : fields) {
			switch (field) {
			case TIMESERIES_ID:
				writer.write(timeseriesId);
				break;
			case UOM:
				writer.write(uom);
				break;
			case OBSERVED_PROPERTY:
				writer.write(observedPropertyTitle);
				break;
			case MONITORING_POINT:
				writer.write(platformTitle);
				break;
			case DATE_TIME:
				writer.write(time);
				break;
			case VALUE:
				if (value != null) {
					writer.write(value.toString());
				}
				break;
			case LATITUDE:
				writer.write(lat);
				break;
			case LONGITUDE:
				writer.write(lon);
				break;
			case QUALITY:
				if (quality != null) {
					writer.write(quality);
				}
				break;
			default:
				break;
			}
			if (j++ == fields.length - 1) {
				writer.write("\n");
			} else {
				writer.write("\t");
			}
		}

	}
	
	@Override
	public void writeHeader() throws IOException {
		int i = 0;
		for (CSVField field : fields) {
			writer.write(field.label);
			if (i++ == fields.length - 1) {
				writer.write("\n");
			} else {
				writer.write("\t");
			}
		}
	}

}
