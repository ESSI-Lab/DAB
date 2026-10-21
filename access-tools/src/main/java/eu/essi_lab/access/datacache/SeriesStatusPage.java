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

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;

/**
 * A page of series statuses of a source, sorted from the most severe, with the counts per status and per concept
 *
 * @author boldrini
 */
public class SeriesStatusPage {

    /**
     * The series of an observed property concept
     */
    public static class ConceptCount {

	private final String key;
	private final String uri;
	private final String name;
	private final long count;
	private final long issues;

	public ConceptCount(String key, String uri, String name, long count, long issues) {
	    this.key = key;
	    this.uri = uri;
	    this.name = name;
	    this.count = count;
	    this.issues = issues;
	}

	/**
	 * @return the concept key (see {@link SeriesStatusRecord#getConceptKey()})
	 */
	public String getKey() {
	    return key;
	}

	public String getUri() {
	    return uri;
	}

	/**
	 * @return an observed property name of the concept series
	 */
	public String getName() {
	    return name;
	}

	public long getCount() {
	    return count;
	}

	/**
	 * @return the number of series whose status needs attention
	 */
	public long getIssues() {
	    return issues;
	}
    }

    private final List<SeriesStatusRecord> records = new ArrayList<>();
    private final EnumMap<SeriesStatus, Long> counts = new EnumMap<>(SeriesStatus.class);
    private final List<ConceptCount> concepts = new ArrayList<>();
    private long total;
    private String nextCursor;
    private Date computed;
    private long now;

    /**
     * @return the records of the page
     */
    public List<SeriesStatusRecord> getRecords() {
	return records;
    }

    /**
     * @return the number of series per status, matching the concept and text filters
     */
    public EnumMap<SeriesStatus, Long> getCounts() {
	return counts;
    }

    /**
     * @return the series per concept of the source
     */
    public List<ConceptCount> getConcepts() {
	return concepts;
    }

    /**
     * @return the number of series matching all the filters
     */
    public long getTotal() {
	return total;
    }

    public void setTotal(long total) {
	this.total = total;
    }

    /**
     * @return the cursor of the next page, null if this is the last one
     */
    public String getNextCursor() {
	return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
	this.nextCursor = nextCursor;
    }

    /**
     * @return the time of the most recent status computation of the source, null if no status is stored
     */
    public Date getComputed() {
	return computed;
    }

    public void setComputed(Date computed) {
	this.computed = computed;
    }

    /**
     * @return the time used to derive the time dependent statuses
     */
    public long getNow() {
	return now;
    }

    public void setNow(long now) {
	this.now = now;
    }
}
