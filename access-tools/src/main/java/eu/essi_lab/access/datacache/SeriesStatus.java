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
import java.util.concurrent.TimeUnit;

/**
 * Status of a time series, comparing its metadata with its data cache content and with the outcome of its last update.
 * <br>
 * A base status is stored (see {@link #compute(boolean, boolean, SeriesCacheInfo)}), while the time dependent statuses
 * ({@link #OVERDUE} and {@link #STALE}) are derived when reading (see {@link #effective(SeriesStatus, Date, Date, long)}),
 * so that they are always current. Constants are sorted from the most to the least severe.
 *
 * @author boldrini
 */
public enum SeriesStatus {

    ERROR("The last update failed"), //
    OVERDUE("The next expected record is late by more than one day: the series is not updated as scheduled"), //
    STALE("The metadata is current, but the latest cached observation is older than 30 days"), //
    NOT_SCHEDULED("The metadata is current and the series is cached, but it is not scheduled for update"), //
    MISSING("In the metadata, but not in the cache"), //
    NOT_IN_METADATA("In the cache, but no more in the metadata"), //
    HISTORICAL("Cached, with metadata not current (temporal extent ended more than one year ago): no update expected"), //
    OK("Updated as expected");

    /**
     * A scheduled series is overdue if its next expected record is late by more than this time
     */
    public static final long OVERDUE_MS = TimeUnit.DAYS.toMillis(1);

    /**
     * A current series is stale if its latest cached observation is older than this time
     */
    public static final long STALE_MS = TimeUnit.DAYS.toMillis(30);

    private final String description;

    SeriesStatus(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

    /**
     * @return the severity, 0 being the most severe
     */
    public int getSeverity() {
	return ordinal();
    }

    /**
     * @return true if the status does not need attention
     */
    public boolean isFine() {
	return this == OK || this == HISTORICAL;
    }

    /**
     * @param inMetadata
     * @param current true if the metadata temporal extent ends now or recently
     * @param info the cache content and last update, null if none
     * @return the base status to store: one of {@link #ERROR}, {@link #MISSING}, {@link #NOT_IN_METADATA},
     *         {@link #HISTORICAL}, {@link #NOT_SCHEDULED}, {@link #OK}
     */
    public static SeriesStatus compute(boolean inMetadata, boolean current, SeriesCacheInfo info) {

	boolean cached = info != null && info.getValuesCount() > 0;

	if (!inMetadata) {
	    return NOT_IN_METADATA;
	}
	if (info != null && info.getLastUpdateError() != null) {
	    return ERROR;
	}
	if (!cached) {
	    return MISSING;
	}
	if (!current) {
	    return HISTORICAL;
	}
	if (info.getNextExpected() == null) {
	    return NOT_SCHEDULED;
	}
	return OK;
    }

    /**
     * @param base the stored base status
     * @param nextExpected
     * @param lastObservation
     * @param now
     * @return the status at the given time
     */
    public static SeriesStatus effective(SeriesStatus base, Date nextExpected, Date lastObservation, long now) {

	switch (base) {
	case ERROR:
	case MISSING:
	case NOT_IN_METADATA:
	    return base;
	default:
	}

	if (nextExpected != null && nextExpected.getTime() < now - OVERDUE_MS) {
	    return OVERDUE;
	}
	if (base == OK && (lastObservation == null || lastObservation.getTime() < now - STALE_MS)) {
	    return STALE;
	}
	return base;
    }
}
