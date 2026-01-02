package eu.essi_lab.profiler.esri.feature.query.parser;

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

public abstract class ESRIToken {

    int begin;
    int end;
    private String parse;

    public ESRIToken(String parse, int begin, int end) {
	this.parse = parse;
	this.begin = begin;
	this.end = end;
    }

    public int getBegin() {
	return begin;
    }

    public void setBegin(int begin) {
	this.begin = begin;
    }

    public int getEnd() {
	return end;
    }

    public void setEnd(int end) {
	this.end = end;
    }

    @Override
    public String toString() {
	String subset = "";
	if (parse == null) {
	    subset = "Null";
	}
	if (parse != null) {
	    int begin = getBegin();
	    int end = getEnd();
	    if (begin < 0 || end > parse.length()) {
		subset = "Out of range!";
	    } else {
		subset = parse.substring(begin, end);
	    }
	}
	return getClass().getSimpleName() + " " + getBegin() + ":" + getEnd() + "<" + subset + ">";
    }

}
