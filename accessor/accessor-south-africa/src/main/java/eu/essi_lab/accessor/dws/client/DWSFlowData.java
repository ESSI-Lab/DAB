package eu.essi_lab.accessor.dws.client;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class DWSFlowData {

    private List<DWSData> data = null;

    public List<DWSData> getData() {
	return data;
    }

    public DWSFlowData(InputStream is) {
	BufferedReader reader;
	List<DWSData> datas = new ArrayList<>();
	try {
	    reader = new BufferedReader(new InputStreamReader(is));
	    String line = null;
	    boolean dataStarted = false;
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	    while ((line = reader.readLine()) != null) {
		if (dataStarted) {

		    String dateString = line.substring(0, 8).trim();
		    try {
			Date date = dateFormat.parse(dateString);
			DWSData data = new DWSData(date);
			String valueString = line.substring(9, 18).trim();
			if (!valueString.isEmpty()) {
			    try {
				BigDecimal value = new BigDecimal(valueString);
				data.setValue(value);
			    } catch (Exception e) {
				GSLoggerFactory.getLogger(getClass()).error("Unable to parse value {}", valueString);
			    }
			}
			String qualityString = line.substring(19, 24).trim();
			if (!qualityString.isEmpty()) {
			    data.setQualityCode(qualityString);
			}
			datas.add(data);

		    } catch (ParseException e) {
			GSLoggerFactory.getLogger(getClass()).error("Unable to parse date {}", dateString);
		    }

		}
		if (line.toUpperCase().startsWith("ZZZ")) {
		    break;
		}
		if (line.toUpperCase().startsWith("DATE")) {
		    dataStarted = true;
		}

	    }
	    reader.close();
	} catch (

	IOException e) {
	    e.printStackTrace();
	}
	this.data = datas;
    }

}
