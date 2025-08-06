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

public class BBOX4326 extends BBOX {

    public BBOX4326() {
	
    }
    
    public BigDecimal getSouth() {
	return getMiny();
    }

    public void setSouth(BigDecimal south) {
	setMiny(south);
    }

    public BigDecimal getNorth() {
	return getMaxy();
    }

    public void setNorth(BigDecimal north) {
	setMaxy(north);
    }

    public BigDecimal getWest() {
	return getMinx();
    }

    public void setWest(BigDecimal west) {
	setMinx(west);
    }

    public BigDecimal getEast() {
	return getMaxx();
    }

    public void setEast(BigDecimal east) {
	setMaxx(east);
    }

    public BBOX4326(BigDecimal s, BigDecimal n, BigDecimal w, BigDecimal e) {
	super("CRS:84", w, s, e, n);
    }

    public BBOX4326(String wkt) {
	super(wkt);
	setCrs("CRS:84");
    }

}
