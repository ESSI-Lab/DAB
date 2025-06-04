//package eu.essi_lab.access;

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
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.AbstractMap.SimpleEntry;
//
//import eu.essi_lab.model.resource.data.CRS;
//
//public class FlatDataDescriptor {
//    public CRS crs;
//
//    public Double min1 = null;
//    public Double max1 = null;
//    public Long size1 = null;
//    public Number res1 = null;
//
//    public Double min2 = null;
//    public Double max2 = null;
//    public Long size2 = null;
//    public Number res2 = null;
//
//    public Double verticalBegin = null;
//    public Double verticalEnd = null;
//    public Long sizeV = null;
//    public Number resV = null;
//
//    public Long timeBegin = null;
//    public Long timeEnd = null;
//    public Long sizeT = null;
//    public Number resT = null;
//
//    public HashMap<String, SimpleEntry<Double, Double>> minMaxPerDimension = new HashMap<>();
//    public HashMap<String, Long> sizePerDimension = new HashMap<>();
//    public HashMap<String, Number> resolutionPerDimension = new HashMap<>();
//
//    public List<String> otherDimensionNames = new ArrayList<>();
//
//    @Override
//    public String toString() {
//	String ret = "";
//	ret += "CRS: " + crs.toString() + "\n" + //
//
//		"min1: " + min1 + "\n" + // ;
//		"max1: " + max1 + "\n" + //
//		"size1: " + size1 + "\n" + //
//		"res1: " + res1 + "\n" + //
//
//		"min2: " + min2 + "\n" + //
//		"max2: " + max2 + "\n" + //
//		"size2: " + size2 + "\n" + //
//		"res2: " + res2 + "\n" + //
//
//		"verticalBegin: " + verticalBegin + "\n" + //
//		"verticalEnd: " + verticalEnd + "\n" + //
//		"sizeV: " + sizeV + "\n" + //
//		"resV: " + resV + "\n" + //
//
//		"timeBegin: " + timeBegin + "\n" + //
//		"timeEnd: " + timeEnd + "\n" + //
//		"sizeT: " + sizeT + "\n" + //
//		"resT: " + resT + "\n"; //
//
//	ret += "Dimension names: ";
//	for (String dimensionName : otherDimensionNames) {
//	    ret += dimensionName + " ";
//	}
//	ret += "\n";
//	ret += "MinMax per dimension: ";
//	for (String dimensionName : minMaxPerDimension.keySet()) {
//	    ret += dimensionName + ": " + minMaxPerDimension.get(dimensionName).getKey() + "/"
//		    + minMaxPerDimension.get(dimensionName).getValue();
//	}
//	ret += "\n";
//	ret += "Resolution per dimension: ";
//	for (String dimensionName : resolutionPerDimension.keySet()) {
//	    ret += dimensionName + ": " + resolutionPerDimension.get(dimensionName);
//	}
//	ret += "\n";
//
//	return ret;
//    }
//
//}
