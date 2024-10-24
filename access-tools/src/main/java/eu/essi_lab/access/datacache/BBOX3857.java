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

public class BBOX3857 extends BBOX {

    public BBOX3857(double minx, double miny, double maxx, double maxy) {
	this(new BigDecimal(minx), new BigDecimal(miny), new BigDecimal(maxx), new BigDecimal(maxy));
    }

    public BBOX3857(BigDecimal minx, BigDecimal miny, BigDecimal maxx, BigDecimal maxy) {
	super("EPSG:3857", minx, miny, maxx, maxy);

    }

    public BBOX3857(String wkt) {
	super(wkt);
	setCrs("EPSG:3857");
    }

}
