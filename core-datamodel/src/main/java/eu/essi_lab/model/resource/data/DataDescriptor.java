package eu.essi_lab.model.resource.data;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * A POJO which describes all the data properties. Note: no cell information is stored at this time -> grid points are
 * at the center of the cells.
 * 
 * @author boldrini
 */
public class DataDescriptor implements Serializable {

    public static final String TIME_DIMENSION_NAME = "time";
    public static final String LATITUDE_DIMENSION_NAME = "Latitude";
    public static final String LONGITUDE_DIMENSION_NAME = "Longitude";
    public static final String X_DIMENSION_NAME = "X";
    public static final String Y_DIMENSION_NAME = "Y";
    private List<DataDimension> spatialDimensions;
    private DataDimension temporalDimension;
    private List<DataDimension> otherDimensions;
    private DataType dataType;
    private DataFormat format;
    private CRS crs;
    private Number rangeMinimum = null;
    private boolean isPreview = false;

    public Boolean isPreview() {
	return isPreview;
    }

    public void setIsPreview(Boolean isPreview) {
	this.isPreview = isPreview;
    }

    public Number getRangeMinimum() {
	return rangeMinimum;
    }

    public void setRangeMinimum(Number rangeMinimum) {
	this.rangeMinimum = rangeMinimum;
    }

    public Number getRangeMaximum() {
	return rangeMaximum;
    }

    public void setRangeMaximum(Number rangeMaximum) {
	this.rangeMaximum = rangeMaximum;
    }

    private Number rangeMaximum = null;

    public DataDescriptor() {

	spatialDimensions = new ArrayList<>();
	otherDimensions = new ArrayList<>();
    }

    /**
     * Compares the <code>targetDescriptor</code> properties with this descriptor properties and
     * copies missing properties from this descriptor to <code>targetDescriptor</code>
     */
    public void fillMissingInformationOf(DataDescriptor targetDescriptor) {

	CRS crs = getCRS();
	CRS targetCRS = targetDescriptor.getCRS();
	if (targetCRS == null || targetCRS.getDecodedCRS() == null) {
	    targetDescriptor.setCRS(crs);
	    targetCRS = crs;
	}

	DataFormat dataFormat = getDataFormat();
	DataFormat targetDataFormat = targetDescriptor.getDataFormat();
	if (targetDataFormat == null) {
	    targetDescriptor.setDataFormat(dataFormat);
	}

	DataType dataType = getDataType();
	DataType targetDataType = targetDescriptor.getDataType();
	if (targetDataType == null) {
	    targetDescriptor.setDataType(dataType);
	}

	// --------------------------------------------
	//
	// Other dimensions
	//
	List<DataDimension> otherDimensions = getOtherDimensions();
	List<DataDimension> targetOtherDimensions = targetDescriptor.getOtherDimensions();

	if (targetOtherDimensions.size() < otherDimensions.size()) {

	    List<String> initNames = otherDimensions.stream().//
		    map(i -> i.getName()).//
		    collect(Collectors.toList());

	    List<String> targetNames = targetOtherDimensions.stream().//
		    map(i -> i.getName()).//
		    collect(Collectors.toList());

	    initNames.removeAll(targetNames);
	    List<DataDimension> missingDimensions = otherDimensions.//
		    stream().//
		    filter(d -> initNames.contains(d.getName())).//
		    map(d -> d.clone()).//
		    collect(Collectors.toList());

	    targetOtherDimensions.addAll(missingDimensions);
	}

	// --------------------------------------------
	//
	// Spatial dimensions
	//
	List<DataDimension> spatialDimensions = getSpatialDimensions();
	List<DataDimension> targetSpatialDimensions = targetDescriptor.getSpatialDimensions();

	if (targetSpatialDimensions.size() < spatialDimensions.size()) {

	    List<String> initNames = spatialDimensions.stream().//
		    map(i -> i.getName()).//
		    collect(Collectors.toList());

	    List<String> targetNames = targetSpatialDimensions.stream().//
		    map(i -> i.getName()).//
		    collect(Collectors.toList());

	    initNames.removeAll(targetNames);

	    List<DataDimension> missingDimensions = spatialDimensions.//
		    stream().//
		    filter(d -> initNames.contains(d.getName())).//
		    map(d -> d.clone()).//
		    collect(Collectors.toList());

	    targetSpatialDimensions.addAll(missingDimensions);
	}

	if (targetCRS != null && targetCRS.equals(crs) && targetSpatialDimensions.size() == spatialDimensions.size()) {

	    for (int j = 0; j < targetSpatialDimensions.size(); j++) {
		DataDimension targetSpatialDimension = targetSpatialDimensions.get(j);
		DataDimension spatialDimension = spatialDimensions.get(j);
		Number targetLower = targetSpatialDimension.getContinueDimension().getLower();
		Number targetUpper = targetSpatialDimension.getContinueDimension().getUpper();
		Long targetSize = targetSpatialDimension.getContinueDimension().getSize();

		Number lower = spatialDimension.getContinueDimension().getLower();
		Number upper = spatialDimension.getContinueDimension().getUpper();
		Long size = spatialDimension.getContinueDimension().getSize();

		if (size != null && size.equals(targetSize)) {

		    if (targetLower == null) {
			targetSpatialDimension.getContinueDimension().setLower(lower);
		    }

		    if (targetUpper == null) {
			targetSpatialDimension.getContinueDimension().setUpper(upper);
		    }

		}

	    }
	} else {
	    // TODO coordinate transformation and filling in case target misses coordinates
	}

	// --------------------------------------------
	//
	// Temporal dimension
	//
	DataDimension temporal = getTemporalDimension();
	DataDimension targetTemporal = targetDescriptor.getTemporalDimension();

	if (targetTemporal == null) {

	    if (temporal != null) {
		targetDescriptor.setTemporalDimension(temporal.clone());
	    }
	}
    }

    /*
     * Getters and setters
     */

    /**
     * @return
     */
    public DataType getDataType() {
	return dataType;
    }

    /**
     * @param dataType
     */
    public void setDataType(DataType dataType) {
	this.dataType = dataType;
    }

    /**
     * @return
     */
    public DataFormat getDataFormat() {
	return format;
    }

    /**
     * @param format
     */
    public void setDataFormat(DataFormat format) {
	this.format = format;
    }

    /**
     * @return
     */
    public CRS getCRS() {
	return crs;
    }

    /**
     * @param crs
     */
    public void setCRS(CRS crs) {
	this.crs = crs;
    }

    public DataDimension getTemporalDimension() {
	return temporalDimension;
    }

    public List<DataDimension> getOtherDimensions() {
	return otherDimensions;
    }

    public void setOtherDimensions(List<DataDimension> otherDimensions) {
	this.otherDimensions = otherDimensions;
    }

    public List<DataDimension> getSpatialDimensions() {
	return spatialDimensions;
    }

    public void setSpatialDimensions(List<DataDimension> spatialDimensions) {
	this.spatialDimensions = spatialDimensions;
    }

    public void setTemporalDimension(DataDimension temporalDimension) {
	this.temporalDimension = temporalDimension;
    }

    /*
     * Utility methods
     */

    /**
     * Sets the spatial dimensions for the case of a single EPSG:4326 point dataset
     * 
     * @param lat
     * @param lon
     */
    public void setEPSG4326SpatialDimensions(Double lat, Double lon) {
	setEPSG4326SpatialDimensions(lat, lon, lat, lon);
	getSpatialDimensions().get(0).getContinueDimension().setSize(1l);
	getSpatialDimensions().get(1).getContinueDimension().setSize(1l);
    }

    /**
     * Sets the spatial dimensions for the case of EPSG:4326 bounding boxed dataset
     * 
     * @param n
     * @param e
     * @param s
     * @param w
     */
    public void setEPSG4326SpatialDimensions(Double n, Double e, Double s, Double w) {

	List<DataDimension> spatialDimensions = new ArrayList<>();

	ContinueDimension dim1 = new ContinueDimension(LATITUDE_DIMENSION_NAME);
	dim1.setType(DimensionType.COLUMN);
	dim1.setLower(s);
	dim1.setUpper(n);
	dim1.setUom(Unit.DEGREE);

	spatialDimensions.add(dim1);

	ContinueDimension dim2 = new ContinueDimension(LONGITUDE_DIMENSION_NAME);
	dim2.setType(DimensionType.ROW);
	dim2.setLower(w);
	dim2.setUpper(e);
	dim2.setUom(Unit.DEGREE);

	spatialDimensions.add(dim2);

	setSpatialDimensions(spatialDimensions);
    }

    public void setEPSG3857SpatialDimensions(Double minx, Double miny, Double maxx, Double maxy) {

	List<DataDimension> spatialDimensions = new ArrayList<>();

	ContinueDimension dim1 = new ContinueDimension(X_DIMENSION_NAME);
	dim1.setType(DimensionType.ROW);
	dim1.setLower(minx);
	dim1.setUpper(maxx);
	dim1.setUom(Unit.METRE);

	spatialDimensions.add(dim1);

	ContinueDimension dim2 = new ContinueDimension(Y_DIMENSION_NAME);
	dim2.setType(DimensionType.COLUMN);
	dim2.setLower(miny);
	dim2.setUpper(maxy);
	dim2.setUom(Unit.METRE);

	spatialDimensions.add(dim2);

	setSpatialDimensions(spatialDimensions);
    }

    /**
     * Sets the temporal dimension
     * 
     * @param temporalDimension
     */
    public void setTemporalDimension(Date begin, Date end) {

	ContinueDimension temporalDimension = new ContinueDimension(TIME_DIMENSION_NAME);

	temporalDimension.setType(DimensionType.TIME);
	temporalDimension.setLower(new Long(begin.getTime()));
	temporalDimension.setUpper(end.getTime());
	temporalDimension.setUom(Unit.MILLI_SECOND);
	temporalDimension.setDatum(Datum.UNIX_EPOCH_TIME());

	if (begin.equals(end)) {
	    temporalDimension.setSize(1l);
	    List<Number> points = new ArrayList<>();
	    points.add(begin.getTime());
	    temporalDimension.setPoints(points);
	}

	this.temporalDimension = temporalDimension;
    }

    /**
     * Sets the vertical dimension
     * 
     * @param metersBottom meters bottom
     * @param metersUp meters up
     */
    public void setVerticalDimension(double metersBottom, double metersUp) {

	ContinueDimension verticalDimension = new ContinueDimension(null);

	verticalDimension.setType(DimensionType.VERTICAL);
	verticalDimension.setLower(metersBottom);
	verticalDimension.setUpper(metersUp);
	verticalDimension.setUom(Unit.METRE);

	this.getOtherDimensions().add(verticalDimension);
    }

    @Override
    public DataDescriptor clone() {

	DataDescriptor outDataDescriptor = new DataDescriptor();

	outDataDescriptor.setCRS(getCRS());
	outDataDescriptor.setDataFormat(getDataFormat());
	outDataDescriptor.setDataType(getDataType());

	outDataDescriptor.setSpatialDimensions(
		getSpatialDimensions() == null ? null : getSpatialDimensions().stream().map(d -> d.clone()).collect(Collectors.toList()));
	outDataDescriptor.setTemporalDimension(getTemporalDimension() == null ? null : getTemporalDimension().clone());
	outDataDescriptor.setOtherDimensions(getOtherDimensions().stream().map(d -> d.clone()).collect(Collectors.toList()));

	return outDataDescriptor;
    }

    @Override
    public boolean equals(Object obj) {

	if (obj instanceof DataDescriptor) {
	    DataDescriptor dd = (DataDescriptor) obj;
	    return (Objects.equals(this.crs, dd.crs) && //
		    Objects.equals(this.dataType, dd.dataType) && //
		    Objects.equals(this.format, dd.format) && //
		    Objects.equals(this.spatialDimensions, dd.spatialDimensions) && //
		    Objects.equals(this.temporalDimension, dd.temporalDimension) && //
		    Objects.equals(this.otherDimensions, dd.otherDimensions));
	}

	return super.equals(obj);
    }

    @Override
    public String toString() {

	String out = "Data type: " + getDataType() + ", ";
	out += "CRS: " + getCRS() + ", ";
	out += "Format: " + getDataFormat() + ", ";
	out += "Spa.dim: " + getSpatialDimensions() + ", ";
	out += "Tem.dim: " + getTemporalDimension() + ",  ";
	out += "Oth.dim: " + getOtherDimensions();

	return out;
    }

    /**
     * Finds a correspondent spatial dimension, even in case of different CRS. E.g. EPSG:4326 latitude is correspondent
     * to EPSG:3857 y (they have the same type: NORTH)
     * 
     * @param name
     * @param type
     * @param i
     * @return
     */
    public DataDimension findCorrespondentSpatialDimension(String name, DimensionType type, int i) {
	for (DataDimension dataDimension : getSpatialDimensions()) {
	    String myName = dataDimension.getName();
	    if (myName != null && name != null && myName.equals(name)) {
		return dataDimension;
	    }
	}

	for (int j = 0; j < getSpatialDimensions().size(); j++) {
	    DataDimension dataDimension = getSpatialDimensions().get(j);
	    DimensionType myType = dataDimension.getType();
	    if (myType == null) {
		if (crs != null) {
		    switch (crs.getAxisOrder()) {
		    case UNAPPLICABLE:
		    case UNKNOWN:
			break;
		    case NORTH_EAST:
			if (j == 0) {
			    myType = DimensionType.COLUMN;
			} else {
			    myType = DimensionType.ROW;
			}
			break;
		    case EAST_NORTH:
		    default:
			if (j == 0) {
			    myType = DimensionType.ROW;
			} else {
			    myType = DimensionType.COLUMN;
			}
			break;
		    }
		}
	    }
	    if (myType != null && type != null && myType.equals(type)) {
		return dataDimension;
	    }
	}
	if (getSpatialDimensions().size() > i) {
	    return getSpatialDimensions().get(i);
	}
	return null;
    }

    public DataDimension findDimension(String dimensionName) {
	for (DataDimension dataDimension : getSpatialDimensions()) {
	    String name = dataDimension.getName();
	    if (name != null && name.equals(dimensionName)) {
		return dataDimension;
	    }
	}
	String temporalName = getTemporalDimension() == null ? null : getTemporalDimension().getName();
	if (temporalName != null && temporalName.equals(dimensionName)) {
	    return getTemporalDimension();
	}
	for (DataDimension dataDimension : getOtherDimensions()) {
	    String name = dataDimension.getName();
	    if (name != null && name.equals(dimensionName)) {
		return dataDimension;
	    }
	}
	return null;
    }

    public DataDimension getFirstSpatialDimension() {
	if (spatialDimensions != null && spatialDimensions.size() > 0) {
	    return spatialDimensions.get(0);
	}
	return null;
    }

    public DataDimension getSecondSpatialDimension() {
	if (spatialDimensions != null && spatialDimensions.size() > 1) {
	    return spatialDimensions.get(1);
	}
	return null;
    }

    public List<String> getOtherDimensionNames() {
	List<String> ret = new ArrayList<>();
	for (DataDimension dimension : getOtherDimensions()) {
	    String dimensionName = dimension.getName();
	    if (dimensionName != null) {
		ret.add(dimensionName);
	    }
	}
	return ret;
    }

    public DataDimension getOtherDimension(String name) {
	for (DataDimension dataDimension : otherDimensions) {
	    String dimensionName = dataDimension.getName();
	    if (dimensionName != null && dimensionName.equals(name)) {
		return dataDimension;
	    }

	}
	return null;
    }

    public DataDimension getOtherDimension(DimensionType type) {
	for (DataDimension dataDimension : otherDimensions) {
	    DimensionType dimensionType = dataDimension.getType();
	    if (dimensionType != null && dimensionType.equals(dimensionType)) {
		return dataDimension;
	    }

	}
	return null;
    }

}
