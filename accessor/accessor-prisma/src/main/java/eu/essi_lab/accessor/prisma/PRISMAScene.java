package eu.essi_lab.accessor.prisma;

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

public class PRISMAScene {
    
    //Filename
    //Validity Start 	 
    //Validity Stop 	 
    //Polygon 	 
    //Cloud % 	 
    //Metadata Point of Contact Organization Name 	 
    //Metadata Point of Contact Organization Mail 	
    //Metadata Date Stamp 	 
    //Coordinate Reference System (CRS) 	 
    //Title 	 
    //Dataset Identifier 	 
    //Abstract 	 
    //Dataset Creator Organization Name
    //Dataset Creator Organization Mail
    //Dataset Spatial Resolution
    //Dataset Spatial Units
    //Limitations on Public Access
    //Conditions applying to Access and Use
    //Distribution Format
    //Distribution link
    //Lineage
    //Graphic Overview
    //Keywords
    //Measured Attribute (parameter) Name
    //Band Bound Min 	 
    //Band Bound Max 	 
    //Instrument Name 	 
    //Instrument Identifier
    //Instrument Type
    //Platform Name
    //Platform Identifier
    //Sensor Name
    //Sensor Identifier
    //Sensor Type  

    String fileName; // JAN MAYEN(NOR-NAVY)
    
    String startDate; //e.g. 19310101 (yyyymmdd)

    String endDate; //e.g. 20190831 (yyyymmdd)

    String polygon; // e.g. 70.933
    
    String cloudCoverage; // e.g. ENJA

    String metadataOrganizationName; // e.g. +0009.0

    String metadataOrganizationMail;

    String dateStamp;
    
    String crs;
    
    String title;
    
    String identifier;
    
    String abstrakt;
    
    String creatorOrganizationName; // e.g. +0009.0

    String creatorOrganizationMail;
    
    String spatialResolution;
    
    String spatialUnit;
    
    String limitations;
    
    String conditions;
    
    String distributionFormat;
    
    String distributionLink;
    
    String lineage;
    
    String overview;
    
    String keywords;
    
    String parameter;
    
    String bandMin;
    String bandMax;
    String instrumentName;
    String instrumentIdentifier;
    String instrumentType;
    
    String platformName;
    String platformIdentifier;
    String sensorName;
    String sensorIdentifier;
    String sensorType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPolygon() {
        return polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    public String getCloudCoverage() {
        return cloudCoverage;
    }

    public void setCloudCoverage(String cloudCoverage) {
        this.cloudCoverage = cloudCoverage;
    }

    public String getMetadataOrganizationName() {
        return metadataOrganizationName;
    }

    public void setMetadataOrganizationName(String metadataOrganizationName) {
        this.metadataOrganizationName = metadataOrganizationName;
    }

    public String getMetadataOrganizationMail() {
        return metadataOrganizationMail;
    }

    public void setMetadataOrganizationMail(String metadataOrganizationMail) {
        this.metadataOrganizationMail = metadataOrganizationMail;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAbstrakt() {
        return abstrakt;
    }

    public void setAbstrakt(String abstrakt) {
        this.abstrakt = abstrakt;
    }

    public String getCreatorOrganizationName() {
        return creatorOrganizationName;
    }

    public void setCreatorOrganizationName(String creatorOrganizationName) {
        this.creatorOrganizationName = creatorOrganizationName;
    }

    public String getCreatorOrganizationMail() {
        return creatorOrganizationMail;
    }

    public void setCreatorOrganizationMail(String creatorOrganizationMail) {
        this.creatorOrganizationMail = creatorOrganizationMail;
    }

    public String getSpatialResolution() {
        return spatialResolution;
    }

    public void setSpatialResolution(String spatialResolution) {
        this.spatialResolution = spatialResolution;
    }

    public String getSpatialUnit() {
        return spatialUnit;
    }

    public void setSpatialUnit(String spatialUnit) {
        this.spatialUnit = spatialUnit;
    }

    public String getLimitations() {
        return limitations;
    }

    public void setLimitations(String limitations) {
        this.limitations = limitations;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getDistributionFormat() {
        return distributionFormat;
    }

    public void setDistributionFormat(String distributionFormat) {
        this.distributionFormat = distributionFormat;
    }

    public String getDistributionLink() {
        return distributionLink;
    }

    public void setDistributionLink(String distributionLink) {
        this.distributionLink = distributionLink;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getBandMin() {
        return bandMin;
    }

    public void setBandMin(String bandMin) {
        this.bandMin = bandMin;
    }

    public String getBandMax() {
        return bandMax;
    }

    public void setBandMax(String bandMax) {
        this.bandMax = bandMax;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getInstrumentIdentifier() {
        return instrumentIdentifier;
    }

    public void setInstrumentIdentifier(String instrumentIdentifier) {
        this.instrumentIdentifier = instrumentIdentifier;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
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

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorIdentifier() {
        return sensorIdentifier;
    }

    public void setSensorIdentifier(String sensorIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public PRISMAScene() {
	
    }
        
}
