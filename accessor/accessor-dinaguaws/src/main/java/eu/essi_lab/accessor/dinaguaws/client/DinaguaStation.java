package eu.essi_lab.accessor.dinaguaws.client;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class DinaguaStation {

    private JSONObject json;

    public JSONObject getJson() {
	return json;
    }

    public DinaguaStation(JSONObject json) {
	this.json = json;
    }

    public String getVariablesString() {

	return json.get("variables").toString();

    }

    public String getVar_corto() {

	return json.get("var_corto").toString();

    }

    public String getBasinArea() {
	return json.get("area_cuenca_km2").toString();

    }

    public String getBasinLevel() {
	return json.get("cuenca_nivel_5").toString();

    }

    public String getRiver() {
	return json.get("nombre_curso_observado").toString();

    }

    public String getId() {
	return json.get("id_estacion").toString();

    }

    public String getDepartmentId() {
	return json.get("departamento").toString();

    }

    public String getLatitude() {
	return json.get("latitud").toString();

    }

    public String getLongitude() {
	return json.get("longitud").toString();

    }

    public String getName() {
	return json.get("apodo").toString();

    }

    public Date getBeginDate() throws ParseException {
	String date = json.get("fecha_inicio").toString();
	if (date != null && !date.isEmpty() && !date.equals("null")) {
	    return DinaguaClient.DATE_SDF.parse(date);
	}
	Date endDate = getEndDate();
	long twoMonths = TimeUnit.DAYS.toMillis(60);
	return new Date(endDate.getTime() - twoMonths);
    }

    public Date getEndDate() throws ParseException {
	String ret = json.get("ultima_fecha").toString();
	if (ret == null || ret.isEmpty() || ret.equals("null")) {
	    return new Date();
	}
	return DinaguaClient.DATE_SDF.parse(ret);

    }

    public List<Variable> getVariables() {
	String[] varSplit = getVariablesString().split(",");
	String[] varCortoSplit = getVar_corto().split(",");
	List<Variable> ret = new ArrayList<>();
	for (int i = 0; i < varCortoSplit.length; i++) {
	    String abbreviation = varCortoSplit[i].trim();
	    String label = varSplit[i].trim();
	    ret.add(new Variable(abbreviation, label));
	}

	return ret;

    }
}
