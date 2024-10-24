package eu.essi_lab.accessor.hiscentral.lombardia;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * @author boldrini
 */
public class HISCentralLombardiaIdentifierMangler extends KVPMangler {
    private static final String SENSOR_KEY = "sensor";
    private static final String FUNCTION_KEY = "function";
    private static final String OPERATOR_KEY = "operator";
    private static final String PERIOD_KEY = "period";

    public HISCentralLombardiaIdentifierMangler() {
	super(";");
    }

    public String getSensorIdentifier() {
	return getParameterValue(SENSOR_KEY);
    }

    public void setSensorIdentifier(String sensorIdentifier) {
	setParameter(SENSOR_KEY, sensorIdentifier);
    }

    public String getFunctionIdentifier() {
	return getParameterValue(FUNCTION_KEY);
    }

    public void setFunctionIdentifier(String functionIdentifier) {
	setParameter(FUNCTION_KEY, functionIdentifier);
    }

    public String getOperatorIdentifier() {
	return getParameterValue(OPERATOR_KEY);
    }

    public void setOperatorIdentifier(String operatorIdentifier) {
	setParameter(OPERATOR_KEY, operatorIdentifier);
    }

    public void setPeriodIdentifier(String periodIdentifier) {
	setParameter(PERIOD_KEY, periodIdentifier);
    }

    public String getPeriodIdentifier() {
	return getParameterValue(PERIOD_KEY);
    }
}
