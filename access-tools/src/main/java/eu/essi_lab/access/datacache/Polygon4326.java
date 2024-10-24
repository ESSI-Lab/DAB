package eu.essi_lab.access.datacache;

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

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Polygon4326 {

    private List<SimpleEntry<BigDecimal, BigDecimal>> coordinates = new ArrayList<>();

    public Polygon4326(List<SimpleEntry<BigDecimal, BigDecimal>> coordinates) {
	super();
	this.coordinates = coordinates;
    }

    public Polygon4326(String wkt) {
	// POLYGON ((x1 y1,x2 y2))";
	wkt = wkt.replace("POLYGON", "").replace("(", "").replace(")", "").trim();
	String[] split = wkt.split(",");
	for (String s : split) {
	    s = s.trim();
	    String[] inner = s.split(" ");
	    BigDecimal x = new BigDecimal(inner[0]);
	    BigDecimal y = new BigDecimal(inner[1]);
	    coordinates.add(new SimpleEntry<>(x, y));
	}
    }

    public List<SimpleEntry<BigDecimal, BigDecimal>> getCoordinates() {
	return coordinates;
    }

    public void setCoordinates(List<SimpleEntry<BigDecimal, BigDecimal>> coordinates) {
	this.coordinates = coordinates;
    }

    public String getWkt() {
	String tmp = "";
	for (SimpleEntry<BigDecimal, BigDecimal> coordinate : coordinates) {
	    tmp += coordinate.getKey() + " " + coordinate.getValue() + ", ";
	}
	tmp = tmp.trim();
	if (tmp.endsWith(",")) {
	    tmp = tmp.substring(0, tmp.length() - 1);
	}

	return "POLYGON ((" + tmp + "))";
    }

}
