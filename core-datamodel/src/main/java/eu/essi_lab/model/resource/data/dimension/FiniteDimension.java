package eu.essi_lab.model.resource.data.dimension;

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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A dimension defined by the ordered finite list of its labeled points
 * 
 * @author boldrini
 */
public class FiniteDimension extends DataDimension {

    public FiniteDimension(String name) {
	super(name);
    }

    private List<String> points;

    public List<String> getPoints() {
	return points;
    }

    public void setPoints(List<String> points) {
	this.points = points;
    }

    public Long getSize() {
	return (long) getPoints().size();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof FiniteDimension) {
	    FiniteDimension dd = (FiniteDimension) obj;
	    if (points.equals(dd.points)) {
		return super.equals(obj);
	    } else {
		return false;
	    }
	}
	return super.equals(obj);
    }

    @Override
    public FiniteDimension clone() {

	FiniteDimension dd = new FiniteDimension(getName());

	dd.setType(getType());
	dd.setPoints(Lists.newArrayList(getPoints()));

	return dd;
    }
}
