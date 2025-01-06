package eu.essi_lab.accessor.bndmet.model;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class BNDMETStation extends BNDMETObject {

    private static final String PARAMETERS = "parameters";

    @Override
    public BNDMETStation clone() {
	JSONObject copy = new JSONObject(getJsonObject(), JSONObject.getNames(getJsonObject()));
	return new BNDMETStation(copy);
    }

    public List<BNDMETParameter> getParameters() {
	List<BNDMETParameter> ret = new ArrayList<>();
	if (!jsonObject.isNull(PARAMETERS)) {
	    JSONArray array = this.jsonObject.getJSONArray(PARAMETERS);
	    for (int i = 0; i < array.length(); i++) {
		JSONObject child = array.getJSONObject(i);
		BNDMETParameter parameter = new BNDMETParameter(child);
		ret.add(parameter);
	    }
	}
	return ret;

    }

    public void setParameters(List<BNDMETParameter> parameters) {
	JSONArray parameterObject = new JSONArray();
	for (BNDMETParameter parameter : parameters) {
	    parameterObject.put(parameter.getJsonObject());
	}
	this.jsonObject.put(PARAMETERS, parameterObject);

    }

    public BNDMETStation(JSONObject station) {
	super(station);
    }

    public String getValue(BNDMET_Station_Code key) {
	return super.getValue(key.name());
    }

    public enum BNDMET_Station_Code {
	CD_OSCAR, // e.g. "0-2000-0-81755"
	DC_NOME, // e.g. "ACARAU"
	FL_CAPITAL, // e.g. "N"
	DT_FIM_OPERACAO, //
	CD_SITUACAO, // e.g. "Operante"
	TP_ESTACAO, // e.g. "Automatica"
	VL_LATITUDE, // e.g. "-3.121067"
	CD_WSI, // e.g. "0-76-0-2300200000000446"
	CD_DISTRITO, // e.g. " 03"
	VL_ALTITUDE, // e.g. "67.15"
	SG_ESTADO, // e.g. "CE"
	SG_ENTIDADE, // e.g. "INMET"
	CD_ESTACAO, // e.g. "A360"
	VL_LONGITUDE, // e.g. "-40.087288"
	DT_INICIO_OPERACAO // e.g. "2009-04-21T21:00:00.000-03:00"

    }

    public String asString() {
	return jsonObject.toString();
    }

}
