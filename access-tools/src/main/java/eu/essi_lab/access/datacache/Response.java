package eu.essi_lab.access.datacache;

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

import java.util.List;

public class Response<T> {

    private List<T> records;

    public Response(List<T> records, long index, long total, boolean completed) {
	super();
	this.records = records;
	this.index = index;
	this.total = total;
	this.completed = completed;
    }

    private long index;
    private long total;
    private boolean completed;

    public List<T> getRecords() {
	return records;
    }

    public void setRecords(List<T> records) {
	this.records = records;
    }

    public long getIndex() {
	return index;
    }

    public void setIndex(long index) {
	this.index = index;
    }

    public long getTotal() {
	return total;
    }

    public void setTotal(long total) {
	this.total = total;
    }

    public boolean isCompleted() {
	return completed;
    }

    public void setCompleted(boolean completed) {
	this.completed = completed;
    }

}
