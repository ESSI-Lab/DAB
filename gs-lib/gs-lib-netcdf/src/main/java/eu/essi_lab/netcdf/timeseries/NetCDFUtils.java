package eu.essi_lab.netcdf.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.LinkedHashMap;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

public class NetCDFUtils {

    private static final double TOL = Math.pow(10, -8);

    public static String getNetCDFName(String name) {
	return name.toLowerCase().replace(" ", "_");
    }

    public static String getAxisName(CoordinateAxis axe) {
	Attribute attribute = axe.findAttribute("long_name");
	if (attribute != null) {
	    return attribute.getStringValue();
	} else {
	    return axe.getShortName();
	}
    }

    public static String getAxisUnit(CoordinateAxis axe) {
	Attribute attribute = axe.findAttribute("units");
	if (attribute != null) {
	    return attribute.getStringValue();
	} else {
	    return null;
	}
    }

    public static Double readResolution(Array array) {
	IndexIterator index = array.getIndexIterator();
	Double tmp = null;
	Double res = null;
	while (index.hasNext()) {
	    double d = index.getDoubleNext();
	    if (tmp == null) {
		tmp = d;
	    } else {
		double res2 = Math.abs(d - tmp);
		tmp = d;
		if (res == null) {
		    res = res2;
		} else if (Math.abs(res2 - res) > TOL) {
		    return null;
		}
	    }
	}
	return res;
    }

    public static Double readExtent(Array array) {
	IndexIterator index = array.getIndexIterator();
	Double min = null;
	Double max = null;
	while (index.hasNext()) {
	    double d = index.getDoubleNext();
	    if (min == null) {
		min = d;
	    }
	    if (max == null) {
		max = d;
	    }
	    if (d > max) {
		max = d;
	    }
	    if (d < min) {
		min = d;
	    }
	}
	if (min == null) {
	    return 0.;
	}
	return max - min;
    }

    public static List<Variable> getGeographicVariables(NetcdfDataset dataset) {
	List<CoordinateAxis> axes = dataset.getCoordinateAxes();
	List<String> geographicDimensions = new ArrayList<>();
	for (CoordinateAxis axe : axes) {
	    AxisType axisType = axe.getAxisType();
	    if (axisType != null) {
		switch (axisType) {
		case GeoX:
		case Lon:
		case GeoY:
		case Lat:
		    geographicDimensions.add(axe.getShortName());
		default:
		    break;
		}
	    }
	}
	LinkedHashMap<String, Variable> map = new LinkedHashMap<>();
	for (Variable variable : dataset.getVariables()) {
	    int dimensionsToFind = geographicDimensions.size();
	    List<Dimension> dimensions = variable.getDimensions();
	    for (Dimension dimension : dimensions) {
		String name = dimension.getShortName();
		if (geographicDimensions.contains(name)) {
		    dimensionsToFind--;
		}
	    }
	    if (dimensionsToFind == 0) {
		map.put(variable.getShortName(), variable);
	    }
	}
	List<String> toRemoves = new ArrayList<>();
	for (Variable variable : map.values()) {
	    toRemoves.addAll(extractVariables(variable.findAttribute("ancillary_variables")));
	    toRemoves.addAll(extractVariables(variable.findAttribute("formula_terms")));
	    toRemoves.addAll(extractVariables(variable.findAttribute("coordinates")));
	    toRemoves.addAll(extractVariables(variable.findAttribute("bounds")));
	    toRemoves.addAll(extractVariables(variable.findAttribute("cell_measures")));
	}
	for (String toRemove : toRemoves) {
	    map.remove(toRemove);
	}

	return new ArrayList<Variable>(map.values());
    }

    private static List<String> extractVariables(Attribute attribute) {
	List<String> ret = new ArrayList<>();
	if (attribute != null) {
	    String string = attribute.getStringValue();
	    if (string != null) {
		String[] split = string.split(" ");
		for (String s : split) {
		    ret.add(s);
		}
	    }
	}
	return ret;
    }
}
