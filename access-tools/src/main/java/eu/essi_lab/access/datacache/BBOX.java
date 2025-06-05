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

/**
 * In this calss x is considered the easting, y the northing
 * 
 * @author boldrini
 */
public class BBOX {
    private String crs;

    public String getCrs() {
	return crs;
    }

    public void setCrs(String crs) {
	this.crs = crs;
    }

    private BigDecimal minx;
    private BigDecimal miny;
    private BigDecimal maxx;
    private BigDecimal maxy;

    public BBOX(String crs, double minx, double miny, double maxx, double maxy) {
	this(crs, new BigDecimal(minx), new BigDecimal(miny), new BigDecimal(maxx), new BigDecimal(maxy));
    }

    public BBOX(String crs, BigDecimal minx, BigDecimal miny, BigDecimal maxx, BigDecimal maxy) {
	super();
	this.crs = crs;
	if (miny == null) {
	    miny = maxy;
	}
	if (maxy == null) {
	    maxy = miny;
	}
	if (maxx == null) {
	    maxx = minx;
	}
	if (minx == null) {
	    minx = maxx;
	}
	this.minx = minx;
	this.miny = miny;
	this.maxx = maxx;
	this.maxy = maxy;
    }

    public BigDecimal getMinx() {
	return minx;
    }

    public void setMinx(BigDecimal minx) {
	this.minx = minx;
    }

    public BigDecimal getMiny() {
	return miny;
    }

    public void setMiny(BigDecimal miny) {
	this.miny = miny;
    }

    public BigDecimal getMaxx() {
	return maxx;
    }

    public void setMaxx(BigDecimal maxx) {
	this.maxx = maxx;
    }

    public BigDecimal getMaxy() {
	return maxy;
    }

    public void setMaxy(BigDecimal maxy) {
	this.maxy = maxy;
    }

    public BBOX(String wkt) {
	WKT w = new WKT(wkt);
	if (w.getObjectName().equals("POLYGON")) {
	    BigDecimal minx = null;
	    BigDecimal miny = null;
	    BigDecimal maxx = null;
	    BigDecimal maxy = null;
	    for (int i = 0; i < w.getNumbers().size(); i += 2) {
		BigDecimal x = w.getNumbers().get(i);
		BigDecimal y = w.getNumbers().get(i);
		if (minx == null || x.compareTo(minx) < 0) {
		    minx = x;
		}
		if (maxx == null || x.compareTo(maxx) > 0) {
		    maxx = x;
		}
		if (miny == null || y.compareTo(miny) < 0) {
		    miny = y;
		}
		if (maxy == null || y.compareTo(maxy) > 0) {
		    maxy = y;
		}
	    }
	    setMinx(minx);
	    setMaxx(maxx);
	    setMaxy(maxy);
	    setMiny(miny);

	} else if (w.getObjectName().equals("BBOX")) {
	    setMinx(w.getNumbers().get(0));
	    setMaxx(w.getNumbers().get(1));
	    setMaxy(w.getNumbers().get(2));
	    setMiny(w.getNumbers().get(3));
	} else if (w.getObjectName().equals("POINT")) {
	    setMinx(w.getNumbers().get(0));
	    setMaxx(w.getNumbers().get(0));
	    setMaxy(w.getNumbers().get(1));
	    setMiny(w.getNumbers().get(1));
	}
    }

    public String getWkt() {
	BigDecimal minx = this.minx;
	BigDecimal maxx = this.maxx;
	BigDecimal miny = this.miny;
	BigDecimal maxy = this.maxy;
	if (miny == null) {
	    miny = maxy;
	}
	if (maxy == null) {
	    maxy = miny;
	}
	if (maxx == null) {
	    maxx = minx;
	}
	if (minx == null) {
	    minx = maxx;
	}
	if (miny == null || maxx == null) {
	    throw new RuntimeException("unable to get WKT");
	}
	if (areEquals(miny, maxy) && areEquals(minx, maxx)) {
	    return "POINT (" + minx + " " + miny + ")";
	}
	// return "BBOX(" + minx + ", " + maxx + ", " + maxy + ", " + miny + ")";
	String p1 = minx + " " + miny;
	String p2 = minx + " " + maxy;
	String p3 = maxx + " " + maxy;
	String p4 = maxx + " " + miny;
	return "POLYGON ((" + p1 + ", " + p2 + ", " + p3 + ", " + p4 + ", " + p1 + "))";
    }

    private boolean areEquals(BigDecimal s, BigDecimal n) {
	return Math.abs(s.doubleValue() - n.doubleValue()) < 0.0000001d;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof BBOX) {
	    BBOX bbox = (BBOX) obj;
	    if (!bbox.getCrs().equals(crs)) {
		return false;
	    }
	    if (areEquals(maxx, bbox.getMaxx()) && //
		    areEquals(maxy, bbox.getMaxy()) && //
		    areEquals(minx, bbox.getMinx()) && //
		    areEquals(miny, bbox.getMiny())//
	    ) {
		return true;
	    }
	}
	return super.equals(obj);
    }

    public boolean contains(BBOX bbox) {
	if (!bbox.getCrs().equals(crs)) {
	    return false;
	}
	if (bbox.getMinx().compareTo(minx) >= 0 && //
		bbox.getMaxx().compareTo(maxx) <= 0 && //
		bbox.getMiny().compareTo(miny) >= 0 && //
		bbox.getMaxy().compareTo(maxy) <= 0 //
	) {
	    return true;
	}
	return false;
    }

    public Double getArea() {
	return (getMaxx().doubleValue() - getMinx().doubleValue()) * (getMaxy().doubleValue() - getMiny().doubleValue());
    }
}
