package eu.essi_lab.accessor.savahis;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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


public enum SavaHISParameter {

	DISCHARGE("Q", "Discharge"), //
	PRECIPITATION("P", "Precipitation"), //
	SEDIMENTATION("Sed", "Sedimentation"), //
	TEMPERATURE("T", "Temperature"), //
	WATER_LEVEL("H", "WaterLevel");

	private String variable;

	public String getVariable() {
	    return variable;
	}

	public String getName() {
	    return name;
	}

	private String name;

	SavaHISParameter(String variable, String name) {
	    this.variable = variable;
	    this.name = name;
	}

	public static SavaHISParameter decode(String string) {
	    switch (string.substring(0, 1)) {
	    case "H":
		return SavaHISParameter.WATER_LEVEL;

	    case "Q":
		return SavaHISParameter.DISCHARGE;

	    case "P":
		return SavaHISParameter.PRECIPITATION;

	    case "T":
		return SavaHISParameter.TEMPERATURE;

	    case "S":
		return SavaHISParameter.SEDIMENTATION;

	    default:
		return null;
	    }
	}

}
