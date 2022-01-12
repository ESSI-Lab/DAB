package eu.essi_lab.model.resource;

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

public enum InterpolationType {
    CONTINUOUS, DISCONTINUOUS, // these are from wml 2.0
    INSTANT_TOTAL, // e.g. tipping bucket
    AVERAGE, MAX, MIN, TOTAL, //
    AVERAGE_PREC, MAX_PREC, MIN_PREC, TOTAL_PREC, //
    AVERAGE_SUCC, MAX_SUCC, MIN_SUCC, TOTAL_SUCC, //
    CONST, CONST_PREC, CONST_SUCC, // e.g. alarms
    STATISTICAL, //
    INCREMENTAL, // this is from wml 1.1
    CATEGORICAL; // this is from wml 1.1

    public static InterpolationType decode(String str) {
	if (str == null) {
	    return null;
	}
	for (InterpolationType it : values()) {
	    if (it.name().equalsIgnoreCase(str)) {
		return it;
	    }
	}
	return null;
    }
}
