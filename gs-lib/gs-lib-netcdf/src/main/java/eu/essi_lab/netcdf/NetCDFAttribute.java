package eu.essi_lab.netcdf;

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

public enum NetCDFAttribute {

    WML_VALUE_TYPE("WML Value Type"), //
    WML_DATA_TYPE("WML Data Type"), //
    WML_GENERAL_CATEGORY("WML General Category"), //
    WML_SAMPLE_MEDIUM("WML Sample Medium"), //
    WML_UNIT_NAME("WML Unit Name"), //
    WML_UNIT_TYPE("WML Unit Type"), //
    WML_UNIT_ABBREVIATION("WML Unit Abbreviation"), //
    WML_UNIT_CODE("WML Unit Code"), //
    WML_SPECIATION("WML Speciation"), //
    WML_TIME_SCALE_IS_REGULAR("WML Time Scale is regular"), //
    WML_TIME_SCALE_UNIT_NAME("WML Time Scale unit name"), //
    WML_TIME_SCALE_UNIT_TYPE("WML Time Scale unit type"), //
    WML_TIME_SCALE_UNIT_ABBREVIATION("WML Time Scale unit abbreviation"), //
    WML_TIME_SCALE_UNIT_CODE("WML Time Scale unit code"), //
    WML_TIME_SCALE_TIME_SUPPORT("WML CUAHSI Time Scale time support"), //
    WML_SITE_COMMENTS("WML Site Comments"), //
    WML_VARIABLE_CODE("WML Variable Code");
    ;

    String label;

    NetCDFAttribute(String label) {
	this.label = label;
    }

    public String getNetCDFName() {
	return label.toLowerCase().replace(" ", "_");
    }

}
