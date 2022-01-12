package eu.essi_lab.model.resource.data;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public class Unit implements Serializable {
    public static Unit METRE = new Unit("metre");
    public static Unit DEGREE = new Unit("degree");
    public static Unit SECOND = new Unit("second");
    public static Unit MILLI_SECOND = new Unit("millisecond");

    private String value;

    public String getIdentifier() {
	return value;
    }

    public Unit(String value) {
	this.value = value;
    }
    public static Unit fromIdentifier(String identifier) {
	if (identifier == null) {
	    return null;
	}
	switch (identifier) {
	case "metre":
	case "m":
	    return METRE;
	case "degree":
	case "degrees_north":
	case "degrees_east":
	case "°":
	    return DEGREE;
	case "second":
	case "s":
	    return SECOND;
	case "millisecond":
	case "ms":
	    return MILLI_SECOND;
	}

	return new Unit(identifier);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Unit) {
	    Unit crs = (Unit) obj;
	    return Objects.equals(crs.getIdentifier(), getIdentifier());
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	return getIdentifier();
    }
}
