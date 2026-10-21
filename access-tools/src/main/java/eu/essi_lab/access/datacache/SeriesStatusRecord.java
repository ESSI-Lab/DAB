package eu.essi_lab.access.datacache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Date;

import org.json.JSONObject;

/**
 * The stored status of a time series of a source (see {@link SeriesStatus}), with the metadata and cache information it
 * is computed from
 *
 * @author boldrini
 */
public class SeriesStatusRecord {

    private String sourceIdentifier;
    private String dataIdentifier;

    /**
     * The base status (see {@link SeriesStatus#compute(boolean, boolean, SeriesCacheInfo)})
     */
    private String status;

    // metadata

    private Boolean inMetadata;
    private Boolean current;
    private String title;
    private String platform;
    private String observedProperty;
    private String observedPropertyURI;

    /**
     * "uri:" followed by the observed property URI, or "name:" followed by the observed property name if the URI is
     * missing
     */
    private String conceptKey;

    // cache

    private Long valuesCount;
    private Date firstObservation;
    private Date lastObservation;
    private Date nextExpected;
    private Date lastInsert;
    private Date lastUpdate;
    private Integer lastUpdateValues;
    private String lastUpdateError;

    // processing

    /**
     * Identifier of the reconciliation pass which last wrote the record
     */
    private String passId;
    private Date computed;

    /**
     * Time of the last augmentation attempt of a series to fill or to refresh
     */
    private Date attempted;

    /**
     * Sets the cache information and the base status
     *
     * @param info
     */
    public void update(SeriesCacheInfo info) {

	valuesCount = info == null ? 0l : info.getValuesCount();
	firstObservation = info == null ? null : info.getFirstObservation();
	lastObservation = info == null ? null : info.getLastObservation();
	nextExpected = info == null ? null : info.getNextExpected();
	lastInsert = info == null ? null : info.getLastInsert();
	lastUpdate = info == null ? null : info.getLastUpdate();
	lastUpdateValues = info == null ? null : info.getLastUpdateValues();
	lastUpdateError = info == null ? null : info.getLastUpdateError();

	if (observedPropertyURI == null && info != null) {
	    observedPropertyURI = info.getObservedPropertyURI();
	}
	if (observedProperty == null && info != null) {
	    observedProperty = info.getObservedProperty();
	}
	conceptKey = observedPropertyURI != null ? "uri:" + observedPropertyURI
		: "name:" + (observedProperty == null ? "" : observedProperty);

	status = SeriesStatus.compute(Boolean.TRUE.equals(inMetadata), Boolean.TRUE.equals(current), info).name();
	computed = new Date();
    }

    /**
     * @param now
     * @return the status at the given time
     */
    public SeriesStatus getEffectiveStatus(long now) {

	return SeriesStatus.effective(SeriesStatus.valueOf(status), nextExpected, lastObservation, now);
    }

    public JSONObject toJSON() {

	JSONObject ret = new JSONObject();
	put(ret, "sourceIdentifier", sourceIdentifier);
	put(ret, "dataIdentifier", dataIdentifier);
	put(ret, "status", status);
	put(ret, "inMetadata", inMetadata);
	put(ret, "current", current);
	put(ret, "title", title);
	put(ret, "platform", platform);
	put(ret, "observedProperty", observedProperty);
	put(ret, "observedPropertyURI", observedPropertyURI);
	put(ret, "conceptKey", conceptKey);
	put(ret, "valuesCount", valuesCount);
	put(ret, "firstObservation", firstObservation);
	put(ret, "lastObservation", lastObservation);
	put(ret, "nextExpected", nextExpected);
	put(ret, "lastInsert", lastInsert);
	put(ret, "lastUpdate", lastUpdate);
	put(ret, "lastUpdateValues", lastUpdateValues);
	put(ret, "lastUpdateError", lastUpdateError);
	put(ret, "passId", passId);
	put(ret, "computed", computed);
	put(ret, "attempted", attempted);
	return ret;
    }

    public static SeriesStatusRecord fromJSON(JSONObject json) {

	SeriesStatusRecord ret = new SeriesStatusRecord();
	ret.sourceIdentifier = json.optString("sourceIdentifier", null);
	ret.dataIdentifier = json.optString("dataIdentifier", null);
	ret.status = json.optString("status", SeriesStatus.MISSING.name());
	ret.inMetadata = json.has("inMetadata") ? json.getBoolean("inMetadata") : null;
	ret.current = json.has("current") ? json.getBoolean("current") : null;
	ret.title = json.optString("title", null);
	ret.platform = json.optString("platform", null);
	ret.observedProperty = json.optString("observedProperty", null);
	ret.observedPropertyURI = json.optString("observedPropertyURI", null);
	ret.conceptKey = json.optString("conceptKey", null);
	ret.valuesCount = json.has("valuesCount") ? json.getLong("valuesCount") : null;
	ret.firstObservation = date(json, "firstObservation");
	ret.lastObservation = date(json, "lastObservation");
	ret.nextExpected = date(json, "nextExpected");
	ret.lastInsert = date(json, "lastInsert");
	ret.lastUpdate = date(json, "lastUpdate");
	ret.lastUpdateValues = json.has("lastUpdateValues") ? json.getInt("lastUpdateValues") : null;
	ret.lastUpdateError = json.optString("lastUpdateError", null);
	ret.passId = json.optString("passId", null);
	ret.computed = date(json, "computed");
	ret.attempted = date(json, "attempted");
	return ret;
    }

    private static void put(JSONObject json, String key, Object value) {

	if (value instanceof Date date) {
	    json.put(key, date.getTime());
	} else if (value != null) {
	    json.put(key, value);
	}
    }

    private static Date date(JSONObject json, String key) {

	return json.has(key) && !json.isNull(key) ? new Date(json.getLong(key)) : null;
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

    public String getStatus() {
	return status;
    }

    public Boolean getInMetadata() {
	return inMetadata;
    }

    public void setInMetadata(Boolean inMetadata) {
	this.inMetadata = inMetadata;
    }

    public Boolean getCurrent() {
	return current;
    }

    public void setCurrent(Boolean current) {
	this.current = current;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getPlatform() {
	return platform;
    }

    public void setPlatform(String platform) {
	this.platform = platform;
    }

    public String getObservedProperty() {
	return observedProperty;
    }

    public void setObservedProperty(String observedProperty) {
	this.observedProperty = observedProperty;
    }

    public String getObservedPropertyURI() {
	return observedPropertyURI;
    }

    public void setObservedPropertyURI(String observedPropertyURI) {
	this.observedPropertyURI = observedPropertyURI;
    }

    public String getConceptKey() {
	return conceptKey;
    }

    public Long getValuesCount() {
	return valuesCount;
    }

    public Date getFirstObservation() {
	return firstObservation;
    }

    public Date getLastObservation() {
	return lastObservation;
    }

    public Date getNextExpected() {
	return nextExpected;
    }

    public Date getLastInsert() {
	return lastInsert;
    }

    public Date getLastUpdate() {
	return lastUpdate;
    }

    public Integer getLastUpdateValues() {
	return lastUpdateValues;
    }

    public String getLastUpdateError() {
	return lastUpdateError;
    }

    public String getPassId() {
	return passId;
    }

    public void setPassId(String passId) {
	this.passId = passId;
    }

    public Date getComputed() {
	return computed;
    }

    public Date getAttempted() {
	return attempted;
    }

    public void setAttempted(Date attempted) {
	this.attempted = attempted;
    }
}
