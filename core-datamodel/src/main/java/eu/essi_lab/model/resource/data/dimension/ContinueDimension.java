package eu.essi_lab.model.resource.data.dimension;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.Unit;

/**
 * A dimension with lower and upper limits, defined as two numbers along with the unit of measure and a datum.
 * Such a dimension might be as well sized, by defining as well its size and even also regular, defining its resolution
 * 
 * @author boldrini
 */
public class ContinueDimension extends DataDimension {

    public enum LimitType {
	/** precise grid value */
	ABSOLUTE,
	//
	/** grid value inside the given value (used in requests only) */
	CONTAINS,
	//
	/** minimum value along the dimension (used in requests only) */
	MINIMUM,
	//
	/** maximum value along the dimension (used in requests only) */
	MAXIMUM
    }

    public static Double TOL = Math.pow(10, -8);

    protected Number lower;
    protected Number lowerTolerance = 0;
    private LimitType lowerType = LimitType.ABSOLUTE;

    protected Number upper;
    protected Number upperTolerance = 0;
    private LimitType upperType = LimitType.ABSOLUTE;
    protected Unit uom;
    protected Datum datum;

    protected List<Number> points = new ArrayList<>();

    /**
     * Optional ordered list of points along the dimension. If present, the first point should equals to lower, the last
     * to upper.
     * 
     * @return
     */
    public List<Number> getPoints() {
	if (points.isEmpty()) {
	    if (lower != null) {
		points.add(lower);
		this.size = 1l;
	    }
	    if (upper != null && lower != upper) {
		points.add(upper);
		this.size = 2l;
	    }

	}
	return points;
    }

    public void setPoints(List<Number> points) {
	this.points = points;
	this.size = (long) points.size();
	this.lower = points.get(0);
	this.upper = points.get(points.size() - 1);
    }

    // size or resolution are not null only for regular dimensions
    private Long size = null;
    private Number resolution = null;
    protected Number resolutionTolerance = 0;

    public Number getResolutionTolerance() {
	return resolutionTolerance;
    }

    public void setResolutionTolerance(Number resolutionTolerance) {
	this.resolutionTolerance = resolutionTolerance;
    }

    public boolean isResolutionApproximate() {
	if (resolutionTolerance == null) {
	    return false;
	}
	if (resolutionTolerance instanceof Long) {
	    return resolutionTolerance.longValue() != 0;
	} else {
	    return Math.abs(resolutionTolerance.doubleValue()) > TOL;
	}
    }

    public boolean isLowerApproximate() {
	if (lowerTolerance == null) {
	    return false;
	}
	if (lowerTolerance instanceof Long) {
	    return lowerTolerance.longValue() != 0;
	} else {
	    return Math.abs(lowerTolerance.doubleValue()) > TOL;
	}
    }

    /**
     * Indicates that the lower value is to be intended not as an exact value, but within the specified tolerance (used
     * only on capabilities)
     * 
     * @param lowerTolerance
     */
    public void setLowerTolerance(Number tolerance) {
	lowerTolerance = tolerance;
    }

    public boolean isUpperApproximate() {
	if (upperTolerance == null) {
	    return false;
	}
	if (upperTolerance instanceof Long) {
	    return upperTolerance.longValue() != 0;
	} else {
	    return Math.abs(upperTolerance.doubleValue()) > TOL;
	}
    }

    /**
     * Indicates that the upper value is to be intended not as an exact value, but within a tolerance (used only on
     * capabilities)
     * 
     * @param lowerTolerance
     */
    public void setUpperTolerance(Number tolerance) {
	this.upperTolerance = tolerance;
    }

    public Number getResolution() {
	return resolution;
    }

    public void setResolution(Number resolution) {
	this.resolution = resolution;
    }

    public void setSize(Long size) {
	this.size = size;
    }

    public Long getSize() {
	return size;
    }

    public Datum getDatum() {
	return datum;
    }

    public void setDatum(Datum datum) {
	this.datum = datum;
    }

    public boolean isRegular() {
	return size != null || resolution != null;
    }

    public Number getLower() {
	return lower;
    }

    public void setLower(Number lower) {
	this.lower = lower;
    }

    public Number getUpper() {
	return upper;
    }

    public void setUpper(Number upper) {
	this.upper = upper;
    }

    public Unit getUom() {
	return uom;
    }

    public void setUom(Unit uom) {
	this.uom = uom;
    }

    public ContinueDimension(String name) {
	super(name);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ContinueDimension) {
	    ContinueDimension sd = (ContinueDimension) obj;
	    boolean lowerEquals = equals(lower, sd.lower);
	    boolean upperEquals = equals(upper, sd.upper);
	    if (lowerEquals && upperEquals && //
		    Objects.equals(datum, sd.datum) && //
		    Objects.equals(uom, sd.uom) && //
		    Objects.equals(isRegular(), sd.isRegular())) {

		if (isRegular()) {
		    boolean eq = Objects.equals(size, sd.size) && //
			    Objects.equals(resolution, sd.resolution);
		    if (!eq) {
			return false;
		    }
		}

		return super.equals(obj);
	    } else {
		return false;
	    }
	}
	return super.equals(obj);
    }

    @Override
    public String toString() {

	String out = super.toString();

	String lowerTypeString = isLowerApproximate() ? "~" : "";
	String upperTypeString = isUpperApproximate() ? "~" : "";

	out += ", l(" + lowerTypeString + lower + "), ";
	out += "u(" + upperTypeString + upper + "), ";
	out += "r(" + resolution + "), ";
	out += "s(" + size + ")";
	return out;
    }

    @Override
    public ContinueDimension clone() {

	ContinueDimension continueDimension = new ContinueDimension(getName());
	continueDimension.setLower(getLower());
	continueDimension.setLowerTolerance(getLowerTolerance());
	continueDimension.setUpper(getUpper());
	continueDimension.setUpperTolerance(getUpperTolerance());
	continueDimension.setUom(getUom());
	continueDimension.setResolution(getResolution());
	continueDimension.setSize(getSize());
	continueDimension.setType(getType());
	continueDimension.setDatum(getDatum());
	continueDimension.setLowerType(getLowerType());
	continueDimension.setUpperType(getUpperType());

	continueDimension.setResolutionTolerance(getResolutionTolerance());

	return continueDimension;
    }

    public LimitType getLowerType() {
	return lowerType;
    }

    public void setLowerType(LimitType lowerType) {
	this.lowerType = lowerType;
    }

    public LimitType getUpperType() {
	return upperType;
    }

    public void setUpperType(LimitType upperType) {
	this.upperType = upperType;
    }

    public Number getLowerTolerance() {
	return lowerTolerance;
    }

    public Number getUpperTolerance() {
	return upperTolerance;
    }

}
