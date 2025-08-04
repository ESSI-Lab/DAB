package eu.essi_lab.access.datacache;

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

import java.util.Date;

public class SourceCacheStats {
	private String sourceId;
	private Long uniqueDatasetCount;
	private Date oldestInsert;
	private Date newestInsert;
	private Long averageAgeHours;
	private Long recordCount;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public Long getUniqueDatasetCount() {
		return uniqueDatasetCount;
	}

	public void setUniqueDatasetCount(Long uniqueDatasetCount) {
		this.uniqueDatasetCount = uniqueDatasetCount;
	}

	public Date getOldestInsert() {
		return oldestInsert;
	}

	public void setOldestInsert(Date oldestInsert) {
		this.oldestInsert = oldestInsert;
	}

	public Date getNewestInsert() {
		return newestInsert;
	}

	public void setNewestInsert(Date newestInsert) {
		this.newestInsert = newestInsert;
	}

	public Long getAverageAgeHours() {
		return averageAgeHours;
	}

	public void setAverageAgeHours(Long averageAgeHours) {
		this.averageAgeHours = averageAgeHours;
	}

	public Long getRecordCount() {
	    return this.recordCount;
	}
	public void setRecordCount(long recordCount) {
	  this.recordCount=recordCount;
	    
	}

}
