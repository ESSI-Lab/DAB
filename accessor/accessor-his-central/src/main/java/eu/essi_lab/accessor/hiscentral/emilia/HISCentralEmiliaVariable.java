package eu.essi_lab.accessor.hiscentral.emilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector.EMILIA_VARIABLE;

public class HISCentralEmiliaVariable {

    EMILIA_VARIABLE var;

    Integer interpolationCode;
    //Integer level;

    public HISCentralEmiliaVariable(EMILIA_VARIABLE var, Integer interpolationCode) {
	this.var = var;
	this.interpolationCode = interpolationCode;
	//this.level = level;
    }

    public EMILIA_VARIABLE getVar() {
	return var;
    }

    public void setVar(EMILIA_VARIABLE var) {
	this.var = var;
    }

    public Integer getInterpolationCode() {
	return interpolationCode;
    }

    public void setInterpolationCode(Integer interpolationCode) {
	this.interpolationCode = interpolationCode;
    }

//    public Integer getLevel() {
//	return level;
//    }
//
//    public void setLevel(Integer level) {
//	this.level = level;
//    }

}
