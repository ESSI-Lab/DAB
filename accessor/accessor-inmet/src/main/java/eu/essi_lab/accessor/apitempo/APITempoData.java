package eu.essi_lab.accessor.apitempo;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;

public class APITempoData extends JSONObjectWrapper {
    @Override
    protected APITempoData clone() throws CloneNotSupportedException {
	JSONObject copy = new JSONObject(getJsonObject(), JSONObject.getNames(getJsonObject()));
	return new APITempoData(copy);
    }

    public APITempoData(JSONObject value) {
	super(value);
    }

    public String getValue(APITempoDataCode key) {
	return super.getValue(key.getKey());
    }

    public enum APITempoDataCode {
	STATION_NAME("estacao"), // e.g. WARSZAWA-OKECIE
	PARAMETER_NAME("variavel"), // e.g. totalPrecipitationOrTotalWaterEquivalent
	VALUE("valor"), // e.g. 1.0
	UNITS("unidade"), // e.g. kg m-2
	YEAR("ano"), // e.g. 2020
	MONTH("mes"), // e.g. 5
	DAY("dia"), // e.g. 19
	HOUR("hora"), // e.g. 0
	MINUTE("minuto"); // e.g. 0

	private String key;

	public String getKey() {
	    return key;
	}

	APITempoDataCode(String key) {
	    this.key = key;
	}
    }

    public Date getDate() throws ParseException {

	String year = getValue(APITempoDataCode.YEAR);
	String month = getValue(APITempoDataCode.MONTH);
	if (month.length() == 1) {
	    month = "0" + month;
	}
	String day = getValue(APITempoDataCode.DAY);
	if (day.length() == 1) {
	    day = "0" + day;
	}
	String hour = getValue(APITempoDataCode.HOUR);
	if (hour.length() == 1) {
	    hour = "0" + hour;
	}
	String minute = getValue(APITempoDataCode.MINUTE);
	if (minute.length() == 1) {
	    minute = "0" + minute;
	}

	DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HHmm");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date parsed = iso8601OutputFormat.parse(year + "-" + month + "-" + day + " " + hour + minute);

	return parsed;
    }
}
