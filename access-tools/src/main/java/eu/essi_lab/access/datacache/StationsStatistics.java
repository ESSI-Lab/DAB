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

public class StationsStatistics {
    private BBOX3857 bbox3857;
    private BBOX4326 bbox4326;
    private long count;

    public StationsStatistics(BBOX3857 bbox3857, BBOX4326 bbox4326, long count) {
	super();
	this.bbox3857 = bbox3857;
	this.bbox4326 = bbox4326;
	this.count = count;
    }

    public BBOX3857 getBbox3857() {
	return bbox3857;
    }

    public void setBbox3857(BBOX3857 bbox3857) {
	this.bbox3857 = bbox3857;
    }

    public BBOX4326 getBbox4326() {
	return bbox4326;
    }

    public void setBbox4326(BBOX4326 bbox4326) {
	this.bbox4326 = bbox4326;
    }

    public long getCount() {
	return count;
    }

    public void setCount(long count) {
	this.count = count;
    }
}
