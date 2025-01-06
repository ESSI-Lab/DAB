package eu.essi_lab.accessor.usgswatersrv;

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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

public class USGSMetadata {

    private HashMap<String, String> values = null;
    private SimpleDateFormat format;

    public USGSMetadata(String metadata) {
	String[] split = metadata.split("\n");
	String headers = split[0];
	// String sizes = split[1];
	String datas = split[2];
	String[] headersSplit = headers.split("\t");
	String[] dataSplit = datas.split("\t");
	this.values = new HashMap<>();
	for (int i = 0; i < dataSplit.length; i++) {
	    String header = headersSplit[i];
	    String data = dataSplit[i];
	    values.put(header, data);
	}
	this.format = new SimpleDateFormat("yyyy-MM-dd");
	format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String get(String key) {
	return values.get(key);
    }
    
    public String getCountNumber() {
  	String date = values.get("count_nu");
  	return date;
      }

    public String getBeginDate() {
	String date = values.get("begin_date"); // Begin date: 1978-10-01
	return date;
    }

    public String getEndDate() {
	String date = values.get("end_date"); // End date: 1988-10-01
	return date;
    }

    public Double getLatitude() {
	String decimalLatitude = values.get("dec_lat_va"); // Decimal latitude: 33.1223435
	try {
	    return Double.parseDouble(decimalLatitude);
	} catch (Exception e) {
	    return null;
	}
    }

    public Double getLongitude() {
	String decimalLongitude = values.get("dec_long_va"); // Decimal longitude: -85.2491113
	try {
	    return Double.parseDouble(decimalLongitude);
	} catch (Exception e) {
	    return null;
	}
    }

    public String getTimeZoneCode() {
	return values.get("tz_cd");

    }

    public String getSiteNumber() {
	return values.get("site_no");

    }

    public String getTimeSeriesId() {
	return values.get("ts_id");
    }

    public String getParameterCode() {
	return values.get("parm_cd");
    }
    
    public String getStatisticalCode() {
	return values.get("stat_cd");
    }

}
