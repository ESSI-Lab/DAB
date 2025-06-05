package eu.essi_lab.indexes.marklogic;

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

import java.util.Arrays;

/***
 * @author Fabrizio
 */
public enum MarkLogicScalarType {

    /**
    *
    */
    STRING("string"),
    /**
    *
    */
    INT("int"),
    /**
    *
    */
    UNSIGNED_INT("unsignedInt"),
    /**
    *
    */
    LONG("long"),
    /**
    *
    */
    UNSIGNED_LONG("unsignedLong"),
    /**
    *
    */
    FLOAT("float"),
    /**
    *
    */
    DOUBLE("double"),
    /**
    *
    */
    DECIMAL("decimal"),
    /**
    *
    */
    DATE_TIME("dateTime"),
    /**
    *
    */
    TIME("time"),
    /**
    *
    */
    DATE("date"),
    /**
    *
    */
    G_YEAR_MONTH("gYearMonth"),
    /**
    *
    */
    G_YEAR("gYear"),
    /**
    *
    */
    G_MONTH("gMonth"),
    /**
    *
    */
    G_DAY("gDay"),
    /**
    *
    */
    YEAR_MONTH_DURATION("yearMonthDuration"),
    /**
    *
    */
    DAY_TIME_DURATION("dayTimeDuration"),
    /**
    *
    */
    ANY_URI("anyURI");

    private String type;

    private MarkLogicScalarType(String type) {
	this.type = type;

    }

    public String getType() {
	return type;
    }

    public String toString() {

	return type;
    }

    /**
     * @param type
     * @return
     */
    public static MarkLogicScalarType decode(String type) {
	
	return Arrays.asList(MarkLogicScalarType.values()). //
	stream(). //
	filter(t -> t.getType().equals(type)). //
	findFirst(). //
	orElseThrow( () -> new IllegalArgumentException("No enum for type: " + type)); //
    }
}
