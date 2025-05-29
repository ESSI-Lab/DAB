package eu.essi_lab.accessor.usgswatersrv;

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

public class USGSIdentifierMangler extends KVPMangler {
    private static final String PARAMETER_KEY = "parameter";
    private static final String STATISTICAL_CODE_KEY = "statisticalCode";
    private static final String TIME_SERIES_KEY = "timeSeries";
    private static final String SITE_KEY = "site";

    public USGSIdentifierMangler() {
	super(";;");
    }

    public void setParameterIdentifier(String parameter) {
	setParameter(PARAMETER_KEY, parameter);
    }

    public String getParameterIdentifier() {
	return getParameterValue(PARAMETER_KEY);
    }

    public void setStatisticalCode(String parameter) {
	setParameter(STATISTICAL_CODE_KEY, parameter);
    }

    public String getStatisticalCode() {
	return getParameterValue(STATISTICAL_CODE_KEY);
    }

    public void setTimeSeries(String parameter) {
	setParameter(TIME_SERIES_KEY, parameter);
    }

    public String getTimeSeries() {
	return getParameterValue(TIME_SERIES_KEY);
    }

    public void setSite(String parameter) {
	setParameter(SITE_KEY, parameter);
    }

    public String getSite() {
	return getParameterValue(SITE_KEY);
    }

}
