package eu.essi_lab.accessor.inmet;

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
import java.util.UUID;

import org.slf4j.Logger;

import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class INMETStation {

    String region;
    String state;
    String stationName;
    String wmoCode;
    String wigosId = UUID.randomUUID().toString();

    String latitude;
    String longitude;
    String height;
    String foundationDate;

    String stationCode; // e.g. 01AP003

    String name;
    String lat;
    String lon;
    String prov; // e.g. NB
    String timezone; // e.g. UTC-04:00

    String nameFile;

    String startDate;

    String endDate;

    Integer size;

    List<String> values = new ArrayList<String>();

    private FTPDownloader downloader;

    public FTPDownloader getDownloader() {
	return downloader == null ? new FTPDownloader() : downloader;
    }

    public void setDownloader(FTPDownloader downloader) {
	this.downloader = downloader;
    }

    
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public boolean hasValues() {
	return !values.isEmpty();
    }

    public INMETStation() {

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

    public String getRegion() {
	return region;
    }

    public void setRegion(String region) {
	this.region = region;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public String getStationName() {
	return stationName;
    }

    public void setStationName(String stationName) {
	this.stationName = stationName;
    }

    public String getWmoCode() {
	return wmoCode;
    }

    public void setWmoCode(String wmoCode) {
	this.wmoCode = wmoCode;
    }

    public String getLatitude() {
	return latitude;
    }

    public void setLatitude(String latitude) {
	this.latitude = latitude;
    }

    public String getLongitude() {
	return longitude;
    }

    public void setLongitude(String longitude) {
	this.longitude = longitude;
    }

    public String getHeight() {
	return height;
    }

    public void setHeight(String height) {
	this.height = height;
    }

    public String getFoundationDate() {
	return foundationDate;
    }

    public void setFoundationDate(String foundationDate) {
	this.foundationDate = foundationDate;
    }

    public String getWigosId() {
	return wigosId;
    }

    public void setWigosId(String wigosId) {
	this.wigosId = wigosId;
    }

    public String getNameFile() {
	return nameFile;
    }

    public void setNameFile(String nameFile) {
	this.nameFile = nameFile;
    }

    public Integer getSize() {
	return size;
    }

    public void setSize(Integer size) {
	this.size = size;
    }

}
