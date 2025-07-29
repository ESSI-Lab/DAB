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
import java.util.List;

import eu.essi_lab.profiler.om.JSONObservation;

public class EmptyResultWriter extends ResultWriter {

	public EmptyResultWriter(OutputStreamWriter writer) {
		super(writer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void writeDataContent(String date, BigDecimal v, String quality, JSONObservation observation, List<Double> coord)
			throws IOException {
		// TODO Auto-generated method stub

	}

}
