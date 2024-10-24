package eu.essi_lab.model.resource;

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

public enum InterpolationType {

    CONTINUOUS("http://www.opengis.net/def/waterml/2.0/interpolationType/Continuous", "Continuous/Instantaneous"), //
    DISCONTINUOUS("http://www.opengis.net/def/waterml/2.0/interpolationType/Discontinuous","Discontinuous"), // these are from
											     // wml 2.0
    INSTANT_TOTAL("http://www.opengis.net/def/waterml/2.0/interpolationType/InstantTotal","Instantaneous total"), // , // e.g. tipping bucket
    AVERAGE(null,"Average"), //
    MAX(null,"Maximum"), //
    MIN(null,"Minimum"),//
    MAX_DAILY_AVERAGES(null,"Maximum of daily averages"), //
    MIN_DAILY_AVERAGES(null,"Minimum of daily averages"),//
    TOTAL(null,"Total"), //
    AVERAGE_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/AveragePrec","Average in preceding interval"), //
    MAX_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxPrec","Maximum in preceding interval"), //
    MIN_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/MinPrec","Minimum in preceding interval"), //
    TOTAL_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalPrec","Preceding total"), //
    AVERAGE_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc","Average in succeeding interval"), //
    MAX_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxSucc","Maximum in succeeding interval"), //
    MIN_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/MinSucc","Minimum in succeeding interval"), //
    TOTAL_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalSucc","Succeeding total"), //
    CONST(null,"Constant"), CONST_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstPrec","Constant in preceding interval"), //
    CONST_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstSucc","Constant in succeeding interval"), // e.g. alarms
    STATISTICAL("http://www.opengis.net/def/waterml/2.0/interpolationType/Statistical", "Statistical"), //
    INCREMENTAL(null, "Incremental"), // this is from wml 1.1
    CATEGORICAL(null, "Categorical"); // this is from wml 1.1

    private String wml2URI;
    private String label;

    InterpolationType(String wml2URI, String label) {
	this.wml2URI = wml2URI;
	this.label = label;
    }

    public String getWML2URI() {
	return wml2URI;
    }

    public String getLabel() {
	return label;
    }

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
