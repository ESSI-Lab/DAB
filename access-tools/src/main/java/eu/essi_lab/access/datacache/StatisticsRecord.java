package eu.essi_lab.access.datacache;

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

import java.util.Date;

/**
 * A class holding statistics about the data harvesting
 * 
 * @author boldrini
 */
public class StatisticsRecord {

    private Date date; // the date of the harvesting
    private String sourceId;
    private String dataIdentifier;
    private Integer harvestedRecords;
    private String error;
    private Date expected; // the date the harvesting was expected to take place
    private Date lastObservationDate; // the date of the more recent harvested record

    public StatisticsRecord(Date date, String sourceId, String dataIdentifier, Integer harvestedRecords, String error, Date expected,
	    Date lastObservationDate) {
	super();
	this.date = date;
	this.sourceId = sourceId;
	this.dataIdentifier = dataIdentifier;
	this.harvestedRecords = harvestedRecords;
	this.error = error;
	this.expected = expected;
	this.lastObservationDate = lastObservationDate;
    }

    public Date getExpected() {
	return expected;
    }

    public void setExpected(Date expected) {
	this.expected = expected;
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public String getSourceId() {
	return sourceId;
    }

    public void setSourceId(String sourceId) {
	this.sourceId = sourceId;
    }

    public String getDataIdentifier() {
	return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
	this.dataIdentifier = dataIdentifier;
    }

    public Integer getHarvestedRecords() {
	return harvestedRecords;
    }

    public void setHarvestedRecords(Integer harvestedRecords) {
	this.harvestedRecords = harvestedRecords;
    }

    public String getError() {
	return error;
    }

    public void setError(String error) {
	this.error = error;
    }

    public Date getLastObservationDate() {
	return lastObservationDate;
    }

    public void setLastObservationDate(Date lastObservationDate) {
	this.lastObservationDate = lastObservationDate;
    }

}
