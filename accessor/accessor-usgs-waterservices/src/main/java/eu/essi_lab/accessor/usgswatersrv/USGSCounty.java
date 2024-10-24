package eu.essi_lab.accessor.usgswatersrv;

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

public class USGSCounty {

    private String countryCode;
    private String stateCode;
    private String countyCode;
    private String countyName;

    public USGSCounty(String countryCode, String stateCode, String countyCode, String countyName) {
	super();
	this.countryCode = countryCode;
	this.stateCode = stateCode;
	this.countyCode = countyCode;
	this.countyName = countyName;
    }

    public String getCountryCode() {
	return countryCode;
    }

    public void setCountryCode(String countryCode) {
	this.countryCode = countryCode;
    }

    public String getStateCode() {
	return stateCode;
    }

    public void setStateCode(String stateCode) {
	this.stateCode = stateCode;
    }

    public String getCountyCode() {
	return countyCode;
    }

    public void setCountyCode(String countyCode) {
	this.countyCode = countyCode;
    }

    public String getCountyName() {
	return countyName;
    }

    public void setCountyName(String countyName) {
	this.countyName = countyName;
    }

}
