package eu.essi_lab.access.compliance.wrapper;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

/**
 * @author Fabrizio
 */
public class FiniteDimensionWrapper extends AbstractDimensionWrapper {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<String> points;

    /**
     * @param discreteDimension
     */
    public FiniteDimensionWrapper(FiniteDimension discreteDimension) {

	super(discreteDimension);

	points = new ArrayList<>();

	List<String> discPoints = discreteDimension.getPoints();
	for (String point : discPoints) {
	    points.add(point);
	}
    }

    /**
     * @param dim
     * @return
     */
    public static FiniteDimension wrap(FiniteDimensionWrapper dim) {

	String name = dim.getName();
	List<String> points = dim.getPoints();
	String type = dim.getType();

	FiniteDimension discreteDimension = new FiniteDimension(name);
	if (type != null) {
	    discreteDimension.setType(DimensionType.fromIdentifier(type));
	}
	discreteDimension.setPoints(points);

	return discreteDimension;
    }

    @XmlTransient
    public List<String> getPoints() {

	return points;
    }
}
