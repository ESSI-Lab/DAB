package eu.essi_lab.shared.driver.es.stats;

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

public class IPRange {
    private String prefix;
    private Integer start;
    private Integer end;

    public IPRange(String prefix) {
	this.prefix = prefix;
    }

    public IPRange(String prefix, Integer start, Integer end) {
	this.prefix = prefix;
	this.start = start;
	this.end = end;
    }

    @Override
    public String toString() {
	if (start != null && end != null) {
	    String ret = "";
	    for (int i = start; i < end; i++) {
		ret += prefix + "." + i + ":";
	    }
	    ret += prefix + "." + end;
	    return ret;
	}
	return prefix;
    }
}
