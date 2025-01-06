package eu.essi_lab.model.resource.data;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.referencing.crs.AbstractSingleCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.geotools.referencing.operation.projection.MapProjection;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * Extensible class of data CRS
 * 
 * @author boldrini
 */
public class CRS implements Serializable {
    /**
     * Common EPSG CRSes
     */
    public static CRS EPSG_3857() {
	return CRS.fromIdentifier("EPSG:3857");
    }

    public static CRS EPSG_4326() {
	return CRS.fromIdentifier("EPSG:4326");
    }

    /**
     * Common OGC CRSes
     */
    public static CRS OGC_84() {
	return CRS.fromIdentifier("urn:ogc:def:crs:OGC:1.3:CRS84");
    }

    public static CRS OGC_IMAGE() {
	return CRS.fromIdentifier("urn:ogc:def:crs:OGC::imageCRS");
    }

    /**
     * GDAL virtual CRS
     */
    public static CRS GDAL_ALL() {
	return new CRS("GDAL:ALL");
    }

    private String fullIdentifier;
    private Authority authority;
    private String code;
    private AxisOrder axisOrder = AxisOrder.UNKNOWN;
    private transient CoordinateReferenceSystem decodedCRS = null;
    private CFProjection projection;
    private Unit uom;
    private Datum datum;
    private String firstAxisName;
    private String secondAxisName;

    public CFProjection getProjection() {
	return projection;
    }

    private void setProjection(CFProjection projection) {
	this.projection = projection;

    }

    public CoordinateReferenceSystem getDecodedCRS() {
	if (decodedCRS == null) {
	    // it can be null: in case of execution of a batch job
	    // being the field transient it must be regenerated
	    // when the job starts
	    CRS crs = CRS.fromIdentifier(getIdentifier());
	    if (crs != null && crs.decodedCRS != null) {
		setDecodedCRS(crs.getDecodedCRS());
	    }
	}
	return decodedCRS;
    }

    public String getWkt() {
	if (decodedCRS != null) {
	    try {
		return decodedCRS.toWKT();
	    } catch (Exception e) {
		return decodedCRS.toString();
	    }
	}
	return null;
    }

    public AxisOrder getAxisOrder() {
	return axisOrder;
    }

    public void setAxisOrder(AxisOrder axisOrder) {
	this.axisOrder = axisOrder;
    }

    /**
     * @param crs
     * @return
     */
    public List<CRS> resolveList() {
	List<CRS> ret = new ArrayList<>();
	if (this.equals(CRS.GDAL_ALL())) {
	    ret.addAll(CRS.getGDALCrsList());
	} else {
	    ret.add(this);
	}

	return ret;
    }

    /**
     * Something like "EPSG"
     */
    public Authority getAuthority() {
	return authority;
    }

    public void setAuthority(Authority authority) {
	this.authority = authority;
    }

    /**
     * Somethinkg like "4326"
     * 
     * @return
     */
    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    /**
     * Something like "EPSG:4326" or "urn:ogc:def:crs:EPSG::4326"
     * 
     * @return
     */
    public String getIdentifier() {
	return fullIdentifier;
    }

    public void setIdentifier(String identifier) {
	this.fullIdentifier = identifier;

    }

    /**
     * @param identifier
     */
    protected CRS(String identifier) {

	setIdentifier(identifier);
    }

    /**
     * @param originalIdentifier
     * @return
     */
    public static CRS fromIdentifier(String originalIdentifier) {

	if (originalIdentifier == null) {
	    return null;
	}

	CRS ret = new CRS(originalIdentifier);

	// to resolve some cases don't catched by geotools
	String commonIdentifier = originalIdentifier;
	String upperIdentifier = commonIdentifier.toUpperCase();
	switch (upperIdentifier) {
	case "URN:OGC:DEF:CRS:OGC::IMAGECRS":
	    ret.setAuthority(Authority.OGC);
	    ret.setCode("image");
	    ret.setAxisOrder(AxisOrder.EAST_NORTH);
	    break;
	case "CRS84":
	case "OGC:84":
	case "OGC:CRS84":
	    commonIdentifier = "urn:ogc:def:crs:OGC:1.3:CRS84";
	    upperIdentifier = commonIdentifier.toUpperCase();
	    break;
	case "EPSG:0 [LATITUDE_LONGITUDE]":
	    commonIdentifier = "EPSG:4326";
	    upperIdentifier = "EPSG:4326";
	    break;
	default:
	    break;
	}
	// this is useful e.g. for urn:ogc:def:crs:EPSG::102004 that is not recognized by Geotools
	if (upperIdentifier.startsWith("URN:OGC:DEF:CRS:EPSG::")) {
	    String code = upperIdentifier.replace("URN:OGC:DEF:CRS:EPSG::", "");
	    commonIdentifier = "EPSG:" + code;
	    upperIdentifier = commonIdentifier.toUpperCase();
	}
	if (upperIdentifier.startsWith("EPSG:")) {
	    ret.setAuthority(Authority.EPSG);
	    String code = upperIdentifier.replace("EPSG:", "");
	    try {
		Integer c = Integer.parseInt(code);
		ret.setCode("EPSG:" + code);
		switch (c) {
		case 900101:
		    // sinusoidal projection, as used by drought WCS:
		    // http://gis.csiss.gmu.edu/cgi-bin/mapserv?MAP=/media/gisiv01/mapfiles/drought/16days/2012/drought.2012.065.map&
		    ret.setProjection(CFProjection.SINUSOIDAL);
		    break;

		default:
		    break;
		}
	    } catch (Exception e) {
	    }
	}

	try {
	    CoordinateReferenceSystem decodedCRS = org.geotools.referencing.CRS.decode(commonIdentifier);
	    if (decodedCRS != null) {
		ret.setDecodedCRS(decodedCRS);
		// the original identifier is in every case set at the end
		ret.setIdentifier(originalIdentifier);
		return ret;
	    }
	} catch (Exception e1) {
	}

	return ret;

    }

    public static CRS fromWKT(String wkt) {
	CRSFactory factory = new ReferencingObjectFactory();
	CoordinateReferenceSystem crs;
	try {
	    crs = factory.createFromWKT(wkt);
	} catch (FactoryException e) {
	    e.printStackTrace();
	    return null;
	}
	return fromGeoToolsCRS(crs);
    }

    public static CRS fromGeoToolsCRS(CoordinateReferenceSystem geoToolsCRS) {
	CRS ret = new CRS("");
	ret.setDecodedCRS(geoToolsCRS);
	return ret;
    }

    protected void setDecodedCRS(CoordinateReferenceSystem decodedCRS) {
	this.decodedCRS = decodedCRS;
	if (decodedCRS == null) {
	    return;
	}
	org.geotools.referencing.CRS.AxisOrder axisOrder = org.geotools.referencing.CRS.getAxisOrder(decodedCRS);
	MapProjection projection = org.geotools.referencing.CRS.getMapProjection(decodedCRS);
	if (projection != null) {
	    switch (projection.getName()) {
	    case "Albers_Conic_Equal_Area":
		setProjection(CFProjection.ALBERS_CONICAL_EQUAL_AREA);
		break;
	    case "Cassini_Soldner":
		break;
	    case "Cylindrical_Equal_Area":
		setProjection(CFProjection.LAMBERT_CYLINDRICAL_EQUAL_AREA);
		break;
	    case "Equidistant Cylindrical (Spherical)":
		;
		break;
	    case "Equidistant_Cylindrical":
		break;
	    case "Hotine_Oblique_Mercator":
		break;
	    case "Krovak":
		break;
	    case "Lambert_Azimuthal_Equal_Area":
		setProjection(CFProjection.LAMBERT_AZIMUTHAL_EQUAL_AREA);
		break;
	    case "Lambert_Conformal_Conic_1SP":
		setProjection(CFProjection.LAMBERT_CONFORMAL_CONIC);
		break;
	    case "Lambert_Conformal_Conic_2SP":
		setProjection(CFProjection.LAMBERT_CONFORMAL_CONIC);
		break;
	    case "Lambert_Conformal_Conic_2SP_Belgium":
		setProjection(CFProjection.LAMBERT_CONFORMAL_CONIC);
		break;
	    case "Mercator_1SP":
		setProjection(CFProjection.MERCATOR);
		break;
	    case "Mercator_2SP":
		setProjection(CFProjection.MERCATOR);
		break;
	    case "New_Zealand_Map_Grid":
		break;
	    case "Oblique_Mercator":
		setProjection(CFProjection.MERCATOR);
		break;
	    case "Oblique_Stereographic":
		setProjection(CFProjection.STEREOGRAPHIC);
		break;
	    case "Polar Stereographic (variant B)":
		setProjection(CFProjection.POLAR_STEREOGRAPHIC);
		break;
	    case "Polar_Stereographic":
		setProjection(CFProjection.POLAR_STEREOGRAPHIC);
		break;
	    case "Polyconic":
		break;
	    case "Popular Visualisation Pseudo Mercator":
		setProjection(CFProjection.MERCATOR);
		break;
	    case "Sinusoidal":
		setProjection(CFProjection.SINUSOIDAL);
		break;
	    case "Transverse_Mercator":
		setProjection(CFProjection.TRANSVERSE_MERCATOR);
		break;
	    default:
		break;
	    }
	} else {
	    if (decodedCRS instanceof DefaultGeographicCRS) {
		setProjection(CFProjection.LATITUDE_LONGITUDE);
	    }
	}
	if (axisOrder == null) {
	    setAxisOrder(AxisOrder.UNKNOWN);
	} else {
	    switch (axisOrder) {
	    case EAST_NORTH:
		setAxisOrder(AxisOrder.EAST_NORTH);
		break;
	    case NORTH_EAST:
		setAxisOrder(AxisOrder.NORTH_EAST);
		break;
	    case INAPPLICABLE:
		setAxisOrder(AxisOrder.UNAPPLICABLE);
		break;
	    default:
		setAxisOrder(AxisOrder.UNKNOWN);
		break;
	    }
	}

	Iterator<ReferenceIdentifier> it = decodedCRS.getIdentifiers().iterator();
	if (it.hasNext()) {
	    ReferenceIdentifier identifier = it.next();
	    Citation decodedAuthority = identifier.getAuthority();
	    String authorityTitle = decodedAuthority.getTitle().toString();
	    setAuthority(Authority.fromIdentifier(authorityTitle));
	    String code = identifier.getCode();
	    setCode(code);
	    if (code != null && !code.isEmpty()) {
		setIdentifier(getAuthority().getIdentifier() + ":" + code);
	    }
	}
	if (decodedCRS instanceof AbstractSingleCRS) {
	    AbstractSingleCRS asCRS = (AbstractSingleCRS) decodedCRS;
	    org.opengis.referencing.datum.Datum decodedDatum = asCRS.getDatum();
	    if (decodedDatum != null) {
		Set<ReferenceIdentifier> identifiers = decodedDatum.getIdentifiers();
		if (!identifiers.isEmpty()) {
		    ReferenceIdentifier identifier = identifiers.iterator().next();
		    datum = Datum.fromIdentifier(identifier.getCodeSpace() + ":" + identifier.getCode());
		}
	    }
	    int dimensions = asCRS.getDimension();
	    javax.measure.Unit<?> unit;
	    switch (dimensions) {
	    case 0:
		break;
	    case 2:
		CoordinateSystemAxis axis2 = asCRS.getAxis(1);
		this.secondAxisName = axis2.getName().getCode();
	    case 1:
		CoordinateSystemAxis axis1 = asCRS.getAxis(0);
		this.firstAxisName = axis1.getName().getCode();
		unit = axis1.getUnit();
		this.uom = Unit.fromIdentifier(unit.toString());
	    default:
		break;
	    }
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof CRS) {
	    CRS crs = (CRS) obj;

	    // 1 first check full identifier
	    if (crs.getIdentifier() != null && !crs.getIdentifier().isEmpty() && //
		    getIdentifier() != null && !getIdentifier().isEmpty()) { //
		String id1 = normalizeIdentifier(crs.getIdentifier());
		String id2 = normalizeIdentifier(getIdentifier());
		if (id1.equals(id2)) {
		    return true;
		}
	    }

	    // 2 check
	    if (crs.getAuthority() != null && getAuthority() != null && //
		    crs.getAuthority().equals(getAuthority()) && //
		    crs.getCode() != null && getCode() != null && //
		    crs.getCode().equals(getCode())) {
		return true;
	    }

	    // 3 wkt
	    String wkt1 = crs.getWkt();
	    String wkt2 = getWkt();
	    if (wkt1 != null && wkt2 != null && !wkt1.isEmpty() && !wkt2.isEmpty() && wkt1.equals(wkt2)) {
		return true;
	    }

	    if (crs.getDecodedCRS() != null && getDecodedCRS() != null) {
		if (crs.getDecodedCRS().equals(getDecodedCRS())) {
		    return true;
		}
	    }

	}
	return super.equals(obj);
    }

    private String normalizeIdentifier(String identifier) {
	if (identifier.contains("[")) { // ignorable part
	    identifier = identifier.substring(0, identifier.indexOf("["));
	}
	return identifier.toUpperCase().trim();
    }

    @Override
    public int hashCode() {

	if (getAuthority() != null && getCode() != null) {
	    return (getAuthority().getIdentifier() + ":" + getCode()).hashCode();
	}
	return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
	String ret = getIdentifier();
	if (ret == null || ret.equals("")) {
	    CFProjection proj = getProjection();
	    if (proj != null) {
		ret = proj.toString();
	    }
	}
	return ret;
    }

    public List<DataDimension> getDefaultDimensions() {
	List<DataDimension> ret = null;

	ret = new ArrayList<>();
	ContinueDimension dim1 = new ContinueDimension(getFirstAxisName());
	if (axisOrder.equals(AxisOrder.EAST_NORTH)) {
	    dim1.setType(DimensionType.ROW);
	} else {
	    dim1.setType(DimensionType.COLUMN);
	}
	dim1.setUom(getUOM());
	ret.add(dim1);
	ContinueDimension dim2 = new ContinueDimension(getSecondAxisName());
	if (axisOrder.equals(AxisOrder.EAST_NORTH)) {
	    dim2.setType(DimensionType.COLUMN);
	} else {
	    dim2.setType(DimensionType.ROW);
	}
	dim2.setUom(getUOM());
	ret.add(dim2);

	return ret;
    }

    /**
     * @return
     */
    public static List<CRS> getGDALCrsList() {

	return Arrays.asList(//

		CRS.OGC_84(), //
		CRS.fromIdentifier("EPSG:102004"), //
		CRS.fromIdentifier("EPSG:9802"), //
		CRS.fromIdentifier("EPSG:3031"), // antarctic polar stereographic
		CRS.fromIdentifier("EPSG:32661"), // polar stereographic
		CRS.EPSG_3857(), //
		CRS.fromIdentifier("EPSG:4269"), //
		CRS.EPSG_4326() //
	);

    }

    public String getFirstAxisName() {
	return this.firstAxisName;
    }

    public String getSecondAxisName() {
	return this.secondAxisName;
    }

    public Unit getUOM() {
	return this.uom;
    }

    public Datum getDatum() {
	return this.datum;
    }

}
