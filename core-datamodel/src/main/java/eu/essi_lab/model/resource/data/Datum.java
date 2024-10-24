package eu.essi_lab.model.resource.data;

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

import java.io.Serializable;
import java.util.Objects;

public class Datum implements Serializable {

    public static final Datum UNIX_EPOCH_TIME() {
	return new Datum("1970-01-01T00:00:00Z");
    }

    public static final Datum SEA_LEVEL_DATUM_1929() {
	return new Datum("NGVD29");
    }

    private String value;

    public String getIdentifier() {
	return value;
    }

    public Datum(String value) {
	this.value = value;

    }

    /**
     * @param identifier
     * @return
     */
    public static Datum fromIdentifier(String identifier) {
	switch (identifier) {
	case "1970-01-01T00:00:00Z":
	    return UNIX_EPOCH_TIME();
	case "NGVD29":
	    return SEA_LEVEL_DATUM_1929();
	}
	return new Datum(identifier);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Datum) {
	    Datum datum = (Datum) obj;
	    return Objects.equals(datum.getIdentifier(), getIdentifier());
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	return getIdentifier();
    }
}
