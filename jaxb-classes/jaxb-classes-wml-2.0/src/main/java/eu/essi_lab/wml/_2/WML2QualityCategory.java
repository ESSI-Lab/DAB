package eu.essi_lab.wml._2;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

public enum WML2QualityCategory {

    GOOD, //
    SUSPECT, //
    ESTIMATE, //
    POOR, //
    UNCHECKED, //
    MISSING, //
    ;

    public String getUri() {
	if (this.equals(WML2QualityCategory.MISSING)) {
	    return "http://www.opengs.net/def/nil/OGC/0/missing";
	}
	return getVocabulary() + "/" + getLabel();
    }

    public String getLabel() {
	return toString().toLowerCase();
    }

    public String getVocabulary() {
	return "http://www.opengis.net/def/waterml/2.0/quality";
    }

    public static WML2QualityCategory decode(String quality) {
	for (WML2QualityCategory q : values()) {
	    if (q.getLabel().equalsIgnoreCase(quality)) {
		return q;
	    }	   
	}
	return decodeUri(quality);
    }

    public static WML2QualityCategory decodeUri(String quality) {
	for (WML2QualityCategory q : values()) {
	    if (q.getUri().equalsIgnoreCase(quality)) {
		return q;
	    }
	}
	return null;
    }

}
