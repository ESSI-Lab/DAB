package eu.essi_lab.model.resource;

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

public enum InterpolationType {

    CONTINUOUS("http://www.opengis.net/def/waterml/2.0/interpolationType/Continuous", "Continuous/Instantaneous", "Continuo/Istantaneo"), //
    DISCONTINUOUS("http://www.opengis.net/def/waterml/2.0/interpolationType/Discontinuous","Discontinuous", "Discontinuo"), // these are from
											     // wml 2.0
    INSTANT_TOTAL("http://www.opengis.net/def/waterml/2.0/interpolationType/InstantTotal","Instantaneous total", "Totale istantaneo"), // , // e.g. tipping bucket
    AVERAGE(null,"Average", "Media"), //
    MAX(null,"Maximum", "Massimo"), //
    MIN(null,"Minimum", "Minimo"),//
    MAX_DAILY_AVERAGES(null,"Maximum of daily averages", "Massimo dei valori medi giornalieri"), //
    MIN_DAILY_AVERAGES(null,"Minimum of daily averages", "Minimo dei valori medi giornalieri"),//
    TOTAL(null,"Total", "Totale"), //
    AVERAGE_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/AveragePrec","Average in preceding interval","Media nell'intervallo precedente"), //
    MAX_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxPrec","Maximum in preceding interval", "Massimo nell'intervallo precedente"), //
    MIN_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/MinPrec","Minimum in preceding interval", "Minimo nell'intervallo precedente"), //
    TOTAL_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalPrec","Preceding total", "Totale nell'intervallo precedente"), //
    AVERAGE_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc","Average in succeeding interval", "Media nell'intervallo successivo"), //
    MAX_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxSucc","Maximum in succeeding interval", "Massimo nell'intervallo successivo"), //
    MIN_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/MinSucc","Minimum in succeeding interval", "Minimo nell'intervallo successivo"), //
    TOTAL_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalSucc","Succeeding total", "Totale nell'intervallo successivo"), //
    CONST(null,"Constant","Costante"), CONST_PREC("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstPrec","Constant in preceding interval", "Costante nell'intervallo precedente"), //
    CONST_SUCC("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstSucc","Constant in succeeding interval", "Costante nell'intervallo successivo"), // e.g. alarms
    STATISTICAL("http://www.opengis.net/def/waterml/2.0/interpolationType/Statistical", "Statistical", "Statistico"), //
    INCREMENTAL(null, "Incremental", "Incrementale"), // this is from wml 1.1
    CATEGORICAL(null, "Categorical", "Categorico"); // this is from wml 1.1

    private String wml2URI;
    private String label;
    private String labelit;

    InterpolationType(String wml2URI, String label,String labelit) {
	this.wml2URI = wml2URI;
	this.label = label;
	this.labelit = labelit;
    }

    public String getWML2URI() {
	return wml2URI;
    }

    public String getLabel() {
	return label;
    }

    public String getLabelIt() {
	return labelit;
    }

    public static InterpolationType decode(String str) {
	if (str == null) {
	    return null;
	}
	for (InterpolationType it : values()) {
	    if ((it.getWML2URI()!=null && it.getWML2URI().equals(str))|| it.name().equalsIgnoreCase(str)) {
		return it;
	    }
	}
	return null;
    }
}
