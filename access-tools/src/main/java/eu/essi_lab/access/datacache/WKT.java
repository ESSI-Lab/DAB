package eu.essi_lab.access.datacache;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class WKT {

    private String objectName = null;

    public String getObjectName() {
	return objectName;
    }

    public void setObjectName(String objectName) {
	this.objectName = objectName;
    }

    public List<BigDecimal> getNumbers() {
	return numbers;
    }

    public void setNumbers(List<BigDecimal> numbers) {
	this.numbers = numbers;
    }

    private List<BigDecimal> numbers = new ArrayList<>();

    public WKT(String wkt) {
	wkt = wkt.trim();
	objectName = wkt.substring(0, wkt.indexOf("(")).trim().toUpperCase();
	wkt = wkt.substring(wkt.indexOf("("));
	wkt = wkt.replace("(", "");
	wkt = wkt.replace(")", "");
	wkt = wkt.trim();
	String[] split;
	wkt = wkt.replace(",", " ");
	wkt = wkt.replace("  ", " ");
	split = wkt.split(" ");
	for (String s : split) {
	    BigDecimal bd = new BigDecimal(s);
	    numbers.add(bd);
	}
    }

}
