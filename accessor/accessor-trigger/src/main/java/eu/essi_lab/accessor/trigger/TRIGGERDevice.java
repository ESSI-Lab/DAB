package eu.essi_lab.accessor.trigger;

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

import java.time.LocalDateTime;

import org.json.JSONObject;

public class TRIGGERDevice {

    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private JSONObject variable;

    public TRIGGERDevice(LocalDateTime beginDate, LocalDateTime endDate, JSONObject variable) {
	this.beginDate = beginDate;
	this.endDate = endDate;
	this.variable = variable;
    }

    public LocalDateTime getBeginDate() {
	return beginDate;
    }

    public void setBeginDate(LocalDateTime beginDate) {
	this.beginDate = beginDate;
    }

    public LocalDateTime getEndDate() {
	return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
	this.endDate = endDate;
    }

    public JSONObject getJSONObject() {
	return variable;
    }

    public void setJSONObject(JSONObject variable) {
	this.variable = variable;
    }

    @Override
    public String toString() {
	return "Device{" + "beginDate=" + beginDate + ", endDate=" + endDate + ", variable=" + variable + '}';
    }

}
