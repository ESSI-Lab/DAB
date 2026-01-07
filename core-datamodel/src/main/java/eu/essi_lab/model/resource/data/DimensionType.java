package eu.essi_lab.model.resource.data;

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

import java.io.Serializable;
import java.util.Objects;

public class DimensionType implements Serializable{

    public static DimensionType ROW = new DimensionType("row"); //
    public static DimensionType COLUMN = new DimensionType("column"); //
    public static DimensionType VERTICAL = new DimensionType("vertical"); //
    public static DimensionType TRACK = new DimensionType("track"); //
    public static DimensionType CROSS_TRACK = new DimensionType("crossTrack"); //
    public static DimensionType LINE = new DimensionType("line"); //
    public static DimensionType SAMPLE = new DimensionType("sample"); //
    public static DimensionType TIME = new DimensionType("time"); //

    private String value;

    public String getIdentifier() {
	return value;
    }

    public DimensionType(String value) {
	this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof DimensionType) {
	    DimensionType crs = (DimensionType) obj;
	    return Objects.equals(crs.getIdentifier(), getIdentifier());
	}
	return super.equals(obj);
    }

    /**
     * @param identifier
     * @return
     */
    public static DimensionType fromIdentifier(String identifier) {
	switch (identifier) {
	case "row":
	    return DimensionType.ROW;
	case "column":
	    return DimensionType.COLUMN;
	case "vertical":
	    return DimensionType.VERTICAL;
	case "track":
	    return DimensionType.TRACK;
	case "crossTrack":
	    return DimensionType.CROSS_TRACK;
	case "line":
	    return DimensionType.LINE;
	case "sample":
	    return DimensionType.SAMPLE;
	case "time":
	    return DimensionType.TIME;
	}

	throw new IllegalArgumentException("Invalid identifier: " + identifier);
    }

    @Override
    public String toString() {

	return getIdentifier();
    }
}
