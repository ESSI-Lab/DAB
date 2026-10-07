package eu.essi_lab.gssrv.rest;

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

/**
 * Request of the harvesting status: the time window (the last <code>hours</code>, or the
 * <code>from</code> - <code>to</code> range, in ISO8601 format) and an optional source filter
 *
 * @author boldrini
 */
public class HarvestStatusRequest {

    private Long hours;
    private String from;
    private String to;
    private String source;

    /**
     * Default constructor for JSON deserialization
     */
    public HarvestStatusRequest() {
    }

    public Long getHours() {
	return hours;
    }

    public void setHours(Long hours) {
	this.hours = hours;
    }

    public String getFrom() {
	return from;
    }

    public void setFrom(String from) {
	this.from = from;
    }

    public String getTo() {
	return to;
    }

    public void setTo(String to) {
	this.to = to;
    }

    public String getSource() {
	return source;
    }

    public void setSource(String source) {
	this.source = source;
    }
}
