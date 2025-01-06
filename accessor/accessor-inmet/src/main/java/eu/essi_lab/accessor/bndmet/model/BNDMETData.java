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

import org.json.JSONObject;

public class BNDMETData extends BNDMETObject {

    @Override
    protected BNDMETData clone() throws CloneNotSupportedException {
	JSONObject copy = new JSONObject(getJsonObject(), JSONObject.getNames(getJsonObject()));
	return new BNDMETData(copy);
    }

    public BNDMETData(JSONObject value) {
	super(value);
    }

    public String getValue(BNDMET_Data_Code key) {
	return super.getValue(key.name());
    }

    public enum BNDMET_Data_Code {
	VL_MEDICAO, // e.g. "29,8",
	DT_MEDICAO, // e.g. "2002-01-01",
	PERIODICIDADE, // e.g. "Diario",
	CD_ESTACAO, // e.g. "A001",
	HR_MEDICAO, // e.g. "1200",
	UNIDADE // e.g. "mm"
    }
}
