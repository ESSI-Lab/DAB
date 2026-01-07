/**
 * 
 */
package eu.essi_lab.accessor.iris.station;

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

import org.json.JSONObject;

import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceType;

/**
 * @author Fabrizio
 */
class IRISStationWrapper {

    private ResourceType type;
    private String id;
    private String parentId;
    private String netCode;
    private String netDesc;
    private String staCode;
    private String staSiteName;
    private String chanCode;
    private Double lat;
    private Double lon;
    private Double elev;
    private String instrument;
    private String startTime;
    private String endTime;

    public IRISStationWrapper() {
    }

    /**
     * @param datasetCollection
     */
    public IRISStationWrapper(ResourceType type) {

	this.type = type;
    }

    /**
     * @param original
     * @return
     */
    public static IRISStationWrapper asWrapper(OriginalMetadata original) {

	IRISStationWrapper wrapper = new IRISStationWrapper();

	JSONObject object = new JSONObject(original.getMetadata());

	wrapper.setType(ResourceType.fromType(object.getString("type")));
	wrapper.setId(object.getString("id"));
	wrapper.setNetCode(object.getString("netCode"));
	wrapper.setNetDesc(object.getString("netDesc"));
	wrapper.setStaSiteName(object.getString("staSiteName"));
	wrapper.setStaCode(object.getString("staCode"));
	wrapper.setChanCode(object.getString("chanCode"));

	if (object.has("parentId")) {
	    wrapper.setParentId(object.getString("parentId"));
	}
	
	if (object.has("lat")) {
	    wrapper.setLat(object.getDouble("lat"));
	}

	if (object.has("lon")) {
	    wrapper.setLon(object.getDouble("lon"));
	}

	if (object.has("elev")) {
	    wrapper.setElev(object.getDouble("elev"));
	}

	if (object.has("instrument")) {
	    wrapper.setInstrument(object.getString("instrument"));
	}

	if (object.has("startTime")) {
	    wrapper.setStartTime(object.getString("startTime"));
	}

	if (object.has("endTime")) {
	    wrapper.setEndTime(object.getString("endTime"));
	}

	return wrapper;
    }

    /**
     * @param datasetWrapper
     * @return
     */
    public static OriginalMetadata asOriginalMetadata(//
	    IRISStationWrapper datasetWrapper) {

	OriginalMetadata out = new OriginalMetadata();
	out.setSchemeURI("http://www.fdsn.org/text/station");

	JSONObject jsonObject = datasetWrapper.asJSONObject();
	out.setMetadata(jsonObject.toString(3));

	return out;
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
	return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {
	this.parentId = parentId;
    }

    /**
     * @return the staSiteName
     */
    public String getStaSiteName() {
	return staSiteName;
    }

    /**
     * @param staSiteName the staSiteName to set
     */
    public void setStaSiteName(String staSiteName) {
	this.staSiteName = staSiteName;
    }

    /**
     * @return the id
     */
    public String getId() {
	return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
	this.id = id;
    }

    /**
     * @return the netDesc
     */
    public String getNetDesc() {
	return netDesc;
    }

    /**
     * @param netDesc the netDesc to set
     */
    public void setNetDesc(String netDesc) {
	this.netDesc = netDesc;
    }

    /**
     * @return the type
     */
    public ResourceType getType() {

	return type;
    }

    /**
     * @return the netCode
     */
    public String getNetCode() {
	return netCode;
    }

    /**
     * @return the staCode
     */
    public String getStaCode() {
	return staCode;
    }

    /**
     * @return the chanCode
     */
    public String getChanCode() {
	return chanCode;
    }

    /**
     * @return the lat
     */
    public Double getLat() {
	return lat;
    }

    /**
     * @return the lon
     */
    public Double getLon() {
	return lon;
    }

    /**
     * @return the elev
     */
    public Double getElev() {
	return elev;
    }

    /**
     * @return the instrument
     */
    public String getInstrument() {
	return instrument;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
	return startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
	return endTime;
    }

    /**
     * @param type the type to set
     */
    public void setType(ResourceType type) {
	this.type = type;
    }

    /**
     * @param netCode the netCode to set
     */
    public void setNetCode(String netCode) {
	this.netCode = netCode;
    }

    /**
     * @param staCode the staCode to set
     */
    public void setStaCode(String staCode) {
	this.staCode = staCode;
    }

    /**
     * @param chanCode the chanCode to set
     */
    public void setChanCode(String chanCode) {
	this.chanCode = chanCode;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(Double lat) {
	this.lat = lat;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(Double lon) {
	this.lon = lon;
    }

    /**
     * @param elev the elev to set
     */
    public void setElev(Double elev) {
	this.elev = elev;
    }

    /**
     * @param instrument the instrument to set
     */
    public void setInstrument(String instrument) {
	this.instrument = instrument;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(String startTime) {
	this.startTime = startTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(String endTime) {
	this.endTime = endTime;
    }

    private JSONObject asJSONObject() {

	JSONObject object = new JSONObject();
	object.put("type", type.getType());
	object.put("id", id);
	object.put("parentId", parentId);
	object.put("netCode", netCode);
	object.put("staSiteName", staSiteName);
	object.put("staCode", staCode);
	object.put("netDesc", netDesc);
	object.put("chanCode", chanCode);
	object.put("lat", lat);
	object.put("lon", lon);
	object.put("elev", elev);
	object.put("instrument", instrument);
	object.put("startTime", startTime);
	object.put("endTime", endTime);

	return object;
    }

}
