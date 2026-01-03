package eu.essi_lab.iso.datamodel.todo;

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

public enum PolarizationOrientationCodes {

    HORIZONTAL("horizontal"), //
    VERTICAL("vertical"), //
    LEFT_CIRCULAR("leftCircular"), //
    RIGHT_CIRCULAR("rightCircular"), //
    THETA("theta"), //
    PHI("phi");

    private String code;

    private PolarizationOrientationCodes(String c) {

	this.code = c;
    }

    public String getCode() {

	return code;
    }

    public String toString() {

	return this.code;
    }

    public static PolarizationOrientationCodes decode(String s) {

	PolarizationOrientationCodes[] values = PolarizationOrientationCodes.values();

	for (PolarizationOrientationCodes c : values) {
	    if (c.toString().equalsIgnoreCase(s)) {
		return c;
	    }
	}

	return null;
    }
}
