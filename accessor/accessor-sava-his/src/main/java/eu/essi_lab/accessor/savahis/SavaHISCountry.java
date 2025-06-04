package eu.essi_lab.accessor.savahis;

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

public enum SavaHISCountry {

    UNKNOWN(-1, "Unknown"), //
    BOSNIA_HERZEGOVINA(1, "Bosnia and Herzegovina"), //
    CROATIA(2, "Croatia"), //
    MONTENEGRO(5, "Montenegro"), //
    SERBIA(4, "Serbia"), //
    SLOVENIA(3, "Slovenia");//

    int code;

    String name;

    SavaHISCountry(int code, String name) {
	this.code = code;
	this.name = name;
    }

    public int getCode() {
	return code;
    }

    public String getName() {
	return name;
    }

    public static SavaHISCountry decode(String code) {
	if (code != null)
	    for (SavaHISCountry value : values()) {
		if (code.equals("" + value.getCode())) {
		    return value;
		}
	    }
	return decode("-1");
    }

}
