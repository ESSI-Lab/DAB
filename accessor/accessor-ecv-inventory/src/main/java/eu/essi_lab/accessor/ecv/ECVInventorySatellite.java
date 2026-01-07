package eu.essi_lab.accessor.ecv;

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

public class ECVInventorySatellite {

    String domain; // e.g. Ocean

    String responderName; // Deborah Smith

    String responderMail; // e.g. deborah.smith@uah.edu
    
    String editorMail; // e.g. hampapuram.ramapriya@ssaihq.com
    
    String responsibleOrg; // e.g. NASA
    
    String peerReview; // e.g This data record was generated as a ...
    
    String maintenance; //e.g The GHRC DAAC will continue to provide active ... 
    
    String qaProcess; //e.g. To determine the quality of the variable obtained for all satellite microwave radiometer V7 data ...
    
    String ecv; // e.g. Surface Wind Speed and Direction
    
    String ecvProduct; //e.g. Surface Wind Speed and Direction
   
    String physicalQuantity; //e.g. Wind speed over ocean surface (horizontal)
    
    String siUnit; //e.g. meters/second (m/s)
     
    String satInstrument; //e.g.
    
    String linkToSource; //e.g.

    String extent; // e.g. -8.667
    
    String hresolution; //e.g.
    
    String vresolution; //e.g.
    
    String tresolution; //e.g.
    
    String startDate; //e.g. 19310101 (yyyymmdd)

    String endDate; //e.g. 20190831 (yyyymmdd)
    
    String dataLink; //e.g.
    
    String availabilityLink; //e.g.
    
    String dataFormat; //e.g.
    
    String dataRecordID; //e.g.
    
    String dataRecordName; //e.g.
    
    String recordID; //e.g.
    
    String releaseDate; //e.g.
     
    
    public String getReleaseDate() {
        return releaseDate;
    }


    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    public String getRecordID() {
        return recordID;
    }


    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }


    public ECVInventorySatellite() {
	
    }


    public String getDomain() {
        return domain;
    }


    public String getDataRecordID() {
        return dataRecordID;
    }


    public void setDataRecordID(String dataRecordID) {
        this.dataRecordID = dataRecordID;
    }


    public String getDataRecordName() {
        return dataRecordName;
    }


    public void setDataRecordName(String dataRecordName) {
        this.dataRecordName = dataRecordName;
    }


    public void setDomain(String domain) {
        this.domain = domain;
    }


    public String getResponderName() {
        return responderName;
    }


    public void setResponderName(String responderName) {
        this.responderName = responderName;
    }


    public String getResponderMail() {
        return responderMail;
    }


    public void setResponderMail(String responderMail) {
        this.responderMail = responderMail;
    }


    public String getEditorMail() {
        return editorMail;
    }


    public void setEditorMail(String editorMail) {
        this.editorMail = editorMail;
    }


    public String getResponsibleOrg() {
        return responsibleOrg;
    }


    public void setResponsibleOrg(String responsibleOrg) {
        this.responsibleOrg = responsibleOrg;
    }


    public String getPeerReview() {
        return peerReview;
    }


    public void setPeerReview(String peerReview) {
        this.peerReview = peerReview;
    }


    public String getMaintenance() {
        return maintenance;
    }


    public void setMaintenance(String maintenance) {
        this.maintenance = maintenance;
    }


    public String getQaProcess() {
        return qaProcess;
    }


    public void setQaProcess(String qaProcess) {
        this.qaProcess = qaProcess;
    }


    public String getEcv() {
        return ecv;
    }


    public void setEcv(String ecv) {
        this.ecv = ecv;
    }


    public String getEcvProduct() {
        return ecvProduct;
    }


    public void setEcvProduct(String ecvProduct) {
        this.ecvProduct = ecvProduct;
    }


    public String getPhysicalQuantity() {
        return physicalQuantity;
    }


    public void setPhysicalQuantity(String physicalQuantity) {
        this.physicalQuantity = physicalQuantity;
    }


    public String getSiUnit() {
        return siUnit;
    }


    public void setSiUnit(String siUnit) {
        this.siUnit = siUnit;
    }


    public String getSatInstrument() {
        return satInstrument;
    }


    public void setSatInstrument(String satInstrument) {
        this.satInstrument = satInstrument;
    }


    public String getLinkToSource() {
        return linkToSource;
    }


    public void setLinkToSource(String linkToSource) {
        this.linkToSource = linkToSource;
    }


    public String getExtent() {
        return extent;
    }


    public void setExtent(String extent) {
        this.extent = extent;
    }


    public String getHresolution() {
        return hresolution;
    }


    public void setHresolution(String hresoluzion) {
        this.hresolution = hresoluzion;
    }


    public String getVresolution() {
        return vresolution;
    }


    public void setVresolution(String vresolution) {
        this.vresolution = vresolution;
    }


    public String getTresolution() {
        return tresolution;
    }


    public void setTresolution(String tresolution) {
        this.tresolution = tresolution;
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


    public String getDataLink() {
        return dataLink;
    }


    public void setDataLink(String dataLink) {
        this.dataLink = dataLink;
    }


    public String getAvailabilityLink() {
        return availabilityLink;
    }


    public void setAvailabilityLink(String availabilityLink) {
        this.availabilityLink = availabilityLink;
    }


    public String getDataFormat() {
        return dataFormat;
    }


    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }
        
//    public ECVInventorySatellite(String stationCode, String name, String lat, String lon, String elevation, String startDate,
//	    String endDate) {
//	this.stationCode = stationCode;
//	this.name = name;
//	this.lat = lat;
//	this.lon = lon;
//	this.elevation = elevation;
//	this.startDate = startDate;
//	this.endDate = endDate;
//
//    }



}
