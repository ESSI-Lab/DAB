package eu.essi_lab.accessor.hmfs;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * 
 * @author boldrini
 */
public class HMFSIdentifierMangler extends KVPMangler {
    private static final String STATION_KEY = "station";
    private static final String SERIES_KEY = "series";
    private static final String VARIABLE_KEY = "variable";
    private static final String QUALIFIER_KEY = "qualifier";
    private static final String TYPE_KEY = "type";
    private static final String FORECAST_DATE_KEY = "date";

    public HMFSIdentifierMangler() {
	super(";");
    }

    public void setStation(String stationIdentifier) {
	setParameter(STATION_KEY, stationIdentifier);
    }

    public String getStation() {
	return getParameterValue(STATION_KEY);
    }

    public void setForecastDate(String forecastDate) {
	setParameter(FORECAST_DATE_KEY, forecastDate);
    }

    public String getForecastDate() {
	return getParameterValue(FORECAST_DATE_KEY);
    }
    
    public void setSeries(String seriesIdentifier) {
	setParameter(SERIES_KEY, seriesIdentifier);
    }

    public String getSeries() {
	return getParameterValue(SERIES_KEY);
    }
    
    public void setVariable(String variableIdentifier) {
	setParameter(VARIABLE_KEY, variableIdentifier);
    }

    public String getVariable() {
	return getParameterValue(VARIABLE_KEY);
    }
    
    public void setQualifier(String qualifierIdentifier) {
	setParameter(QUALIFIER_KEY, qualifierIdentifier);
    }

    public String getQualifier() {
	return getParameterValue(QUALIFIER_KEY);
    }

    public void setType(String type) {
	setParameter(TYPE_KEY, type);
    }
    public String getType() {
	return getParameterValue(TYPE_KEY);
    }

}
