package eu.essi_lab.jobs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class GSJobRequest {

    private Integer start = 1;
    private Integer count = 10;
    private SortProperty sortProerty = SortProperty.DATE_ASCENDING;
    private Date startedAfter;
    private Date startedBefore;
    private Date completedBefore;
    private Date completedAfter;


    /**
     * @return the start index of {@link IGSJob}s to be returned, index of first job is 1.
     */
    public Integer getStart() {
	return start;
    }

    /**
     * The start index of {@link IGSJob}s to be returned, index of first job is 1. Default is 1
     *
     * @param start
     */
    public void setStart(Integer start) {
	this.start = start;
    }

    /**
     * @return the maximum number of {@link IGSJob}s to be returned
     */
    public Integer getCount() {
	return count;
    }

    /**
     * The maximum number of {@link IGSJob}s to be returned, max count is 100. Default is 10.
     *
     * @param count
     */
    public void setCount(Integer count) {
	this.count = count;
    }

    public enum SortProperty {
	DATE_ASCENDING,
	DATE_DESCENDING;
    }

    /**
     * @return the property which is used to order the retrieved {@link IGSJob}s
     */
    public SortProperty getSortProerty() {
	return sortProerty;
    }

    /**
     * The order of the {@link IGSJob}s is determined on the base of the provided {@link SortProperty}. Default is {@link
     * SortProperty#DATE_ASCENDING}
     *
     * @param sortProerty
     */
    public void setSortProerty(SortProperty sortProerty) {
	this.sortProerty = sortProerty;
    }

    public Date getStartedAfter() {
	return startedAfter;
    }

    public void setStartedAfter(Date startedAfter) {
	this.startedAfter = startedAfter;
    }

    public Date getStartedBefore() {
	return startedBefore;
    }

    public void setStartedBefore(Date startedBefore) {
	this.startedBefore = startedBefore;
    }

    public Date getCompletedBefore() {
	return completedBefore;
    }

    public void setCompletedBefore(Date completedBefore) {
	this.completedBefore = completedBefore;
    }

    public Date getCompletedAfter() {
	return completedAfter;
    }

    public void setCompletedAfter(Date completedAfter) {
	this.completedAfter = completedAfter;
    }
}
