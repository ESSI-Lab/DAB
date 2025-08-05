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
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;

public class StationRecord {

    private Date lastHarvesting;

    private Date lastObservation;

    private LatitudeLongitude latitudeLongitude = null;

    private String shape;

    private Polygon4326 polygon;

    private BigDecimal south;

    private BigDecimal west;

    private BigDecimal east;

    private BigDecimal north;

    private BigDecimal minx3857;

    private BigDecimal miny3857;

    private BigDecimal maxx3857;

    private BigDecimal maxy3857;

    private String dataIdentifier;

    private String metadataIdentifier;

    private String datasetName;

    private String platformName;

    private String observedProperty;

    private String observedPropertyURI;

    private String units;

    private String unitsURI;

    private String platformIdentifier;

    private String downloadUrl;

    private String metadataUrl;

    private String featureInfo;

    private String themeCategory;

    private String whosCategory;

    private String sourceIdentifier;
    
    private String sourceLabel;

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    private Integer lastDayObservations;

    private Date begin;

    private Date end;

    public Date getLastHarvesting() {
	return lastHarvesting;
    }

    public void setLastHarvesting(Date lastHarvesting) {
	this.lastHarvesting = lastHarvesting;
    }

    public Date getLastObservation() {
	return lastObservation;
    }

    public void setLastObservation(Date lastObservation) {
	this.lastObservation = lastObservation;
    }

    public LatitudeLongitude getLatitudeLongitude() {
	return latitudeLongitude;
    }

    public void setLatitudeLongitude(LatitudeLongitude latitudeLongitude) {
	this.latitudeLongitude = latitudeLongitude;
    }

    public String getShape() {
	return shape;
    }

    public void setShape(String shape) {
	this.shape = shape;
    }

    public Polygon4326 getPolygon() {
	return polygon;
    }

    public void setPolygon(Polygon4326 polygon) {
	this.polygon = polygon;
    }

    public String getObservedProperty() {
	return observedProperty;
    }

    public String getObservedPropertyURI() {
	return observedPropertyURI;
    }

    public void setObservedProperty(String observedProperty) {
	this.observedProperty = observedProperty;
    }

    public void setObservedPropertyURI(String uri) {
	this.observedPropertyURI = uri;
    }

    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    public String getUnitsURI() {
	return unitsURI;
    }

    public void setUnitsURI(String unitsURI) {
	this.unitsURI = unitsURI;
    }

    public String getPlatformName() {
	return platformName;
    }

    public void setPlatformName(String platformName) {
	this.platformName = platformName;
    }

    public String getPlatformIdentifier() {
	return platformIdentifier;
    }

    public void setPlatformIdentifier(String platformIdentifier) {
	this.platformIdentifier = platformIdentifier;
    }

    public String getWhosCategory() {
	return whosCategory;
    }

    public void setWhosCategory(String whosCategory) {
	this.whosCategory = whosCategory;
    }

    public Date getBegin() {
	return begin;
    }

    public Date getEnd() {
	return end;
    }

    public void setEnd(Date end) {
	this.end = end;
    }

    public Integer getLastDayObservations() {
	return lastDayObservations;
    }

    public void setLastDayObservations(Integer lastDayObservations) {
	this.lastDayObservations = lastDayObservations;
    }

    public StationRecord() {

    }

    public BBOX4326 getBbox4326() {
	return new BBOX4326(south, north, west, east);
    }

    public String getMetadataUrl() {
	return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
	this.metadataUrl = metadataUrl;
    }

    public void setBbox4326(BBOX4326 bbox4326) throws Exception {
	if (bbox4326 == null) {
	    return;
	}
	this.south = bbox4326.getSouth();
	this.north = bbox4326.getNorth();
	this.east = bbox4326.getEast();
	this.west = bbox4326.getWest();
	if (minx3857 == null && miny3857 == null) {
	    BBOX3857 bbox3857 = calculateBBOX3857(bbox4326);
	    setBbox3857(bbox3857);
	}

    }

    public BBOX3857 getBbox3857() {
	return new BBOX3857(minx3857, miny3857, maxx3857, maxy3857);
    }

    public void setBbox3857(BBOX3857 bbox3857) {
	if (bbox3857 == null) {
	    return;
	}
	this.minx3857 = bbox3857.getMinx();
	this.miny3857 = bbox3857.getMiny();
	this.maxx3857 = bbox3857.getMaxx();
	this.maxy3857 = bbox3857.getMaxy();
    }

    public StationRecord(BBOX4326 bbox4326, BBOX3857 bbox3857, String dataIdentifier, String metadataIdentifier, String datasetName,
	    String downloadUrl, String featureInfo, String themeCategory, String sourceIdentifier) throws Exception {
	super();
	setBbox4326(bbox4326);
	if (bbox3857 == null) {
	    bbox3857 = calculateBBOX3857(bbox4326);
	}
	setBbox3857(bbox3857);
	this.dataIdentifier = dataIdentifier;
	this.metadataIdentifier = metadataIdentifier;
	this.datasetName = datasetName;
	this.downloadUrl = downloadUrl;
	this.featureInfo = featureInfo;
	this.themeCategory = themeCategory;
	this.sourceIdentifier = sourceIdentifier;
    }

    public String getThemeCategory() {
	return themeCategory;
    }

    public void setThemeCategory(String themeCategory) {
	this.themeCategory = themeCategory;
    }

    public String getSourceIdentifier() {
	return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
	this.sourceIdentifier = sourceIdentifier;
    }

    public String getDataIdentifier() {
	return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
	this.dataIdentifier = dataIdentifier;
    }

    public String getMetadataIdentifier() {
	return metadataIdentifier;
    }

    public void setMetadataIdentifier(String metadataIdentifier) {
	this.metadataIdentifier = metadataIdentifier;
    }

    public String getDatasetName() {
	return datasetName;
    }

    public void setDatasetName(String datasetName) {
	this.datasetName = datasetName;
    }

    public String getDownloadUrl() {
	return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
	this.downloadUrl = downloadUrl;
    }

    public String getFeatureInfo() {
	return featureInfo;
    }

    public void setFeatureInfo(String featureInfo) {
	this.featureInfo = featureInfo;
    }

    public void setBegin(Date date) {
	this.begin = date;

    }

    private BBOX3857 calculateBBOX3857(BBOX4326 bbox4326) throws Exception {
        if (bbox4326 == null) {
            return null;
        }
        SimpleEntry<Double, Double> lower = new SimpleEntry<>(bbox4326.getSouth().doubleValue(), bbox4326.getWest().doubleValue());
        SimpleEntry<Double, Double> upper = new SimpleEntry<>(bbox4326.getNorth().doubleValue(), bbox4326.getEast().doubleValue());
        SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<>(lower, upper);
        SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = CRSUtils.translateBBOX(sourceCorners, CRS.EPSG_4326(),
        	CRS.EPSG_3857());
        SimpleEntry<Double, Double> lower2 = bbox.getKey();
        SimpleEntry<Double, Double> upper2 = bbox.getValue();
        return new BBOX3857(new BigDecimal(lower2.getKey()), new BigDecimal(lower2.getValue()), new BigDecimal(upper2.getKey()),
        	new BigDecimal(upper2.getValue()));
    }

}
