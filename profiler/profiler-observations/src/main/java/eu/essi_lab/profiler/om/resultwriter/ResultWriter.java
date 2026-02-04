package eu.essi_lab.profiler.om.resultwriter;

import java.io.IOException;

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

import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import eu.essi_lab.profiler.om.JSONObservation;

public abstract class ResultWriter {
	boolean first = true;
	protected OutputStreamWriter writer;

	public ResultWriter(OutputStreamWriter writer) {
		this.writer = writer;
	}

	public void writeDataObject(String date, BigDecimal v, String quality, JSONObservation observation, List<Double> coord)
			throws IOException {
		// Convert single quality string to map for backward compatibility
		Map<String, String> qualifiers = new HashMap<>();
		if (quality != null && !quality.trim().isEmpty()) {
			qualifiers.put("quality", quality);
		}
		writeDataObject(date, v, qualifiers, observation, coord);
	}

	public void writeDataObject(String date, BigDecimal v, Map<String, String> qualifiers, JSONObservation observation, List<Double> coord)
			throws IOException {
		if (!first) {
			writeDataSeparator();
		}
		writeDataContent(date, v, qualifiers, observation, coord);
		first = false;
	}

	public abstract void writeDataContent(String date, BigDecimal v, Map<String, String> qualifiers, JSONObservation observation,
			List<Double> coord) throws IOException;

	protected void writeDataSeparator() throws IOException {

	}

	public boolean isFirst() {
		return first;
	}

	public void writeHeader() throws IOException {
		
	}
	
	public void writeMetadataObject(JSONObject feature) throws IOException {
		
	}
	
	public void writeMetadataSeparator() throws IOException {
		
	}

	public void writeMetadataFooter() throws IOException {
		
	}
	
	public void writeFooter(String resumptionToken,Integer count) throws IOException {
		
	}

}
