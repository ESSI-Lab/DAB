package eu.essi_lab.accessor.hiscentral.emilia.simc;

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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A single observation extracted from BUFR-style EMR JSON: one (observed property, value)
 * with its time/level context and station metadata.
 * Observed property is resolved via {@link BCodes} from arpa-emr/dballe.txt.
 */
public final class BUFRObservation {

    /** Raw B-code (e.g. B12101, B13003). */
    private final String observedPropertyCode;
    private final String interpolationType;
    private final String aggregationPeriod;
    private final String level;
    private final Object value;
    private final String stationName;
    private final String referenceDateTime;
    private final String network;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final BigDecimal heightAboveMeanSeaLevel;
    private final BigDecimal barometerHeightAboveMeanSeaLevel;

    BUFRObservation(
            String observedProperty,
            String interpolationType,
            String aggregationPeriod,
            String level,
            Object value,
            String stationName,
            String referenceDateTime,
            String network,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal heightAboveMeanSeaLevel,
            BigDecimal barometerHeightAboveMeanSeaLevel) {
        this.observedPropertyCode = observedProperty;
        this.interpolationType = interpolationType;
        this.aggregationPeriod = aggregationPeriod;
        this.level = level;
        this.value = value;
        this.stationName = stationName;
        this.referenceDateTime = referenceDateTime;
        this.network = network;
        this.latitude = latitude;
        this.longitude = longitude;
        this.heightAboveMeanSeaLevel = heightAboveMeanSeaLevel;
        this.barometerHeightAboveMeanSeaLevel = barometerHeightAboveMeanSeaLevel;
    }

    /**
     * Observed property label from dballe.txt (e.g. "TEMPERATURE/DRY-BULB TEMPERATURE").
     * Falls back to the raw B-code if not found in the conversion table.
     */
    public String getObservedProperty() {
        return BCodes.getInstance().getObservedPropertyLabel(observedPropertyCode);
    }

    /** Raw B-code of the observed property (e.g. B12101, B13003, B13011). */
    public String getObservedPropertyCode() {
        return observedPropertyCode;
    }

    /** Unit of the observed property from dballe.txt (e.g. K, %, M/S), or null if unknown. */
    public String getObservedPropertyUnit() {
        return BCodes.getInstance().getUnit(observedPropertyCode).orElse(null);
    }

    /** Interpolation/statistic type (e.g. instantaneous, aggregation_preceding_interval). */
    public String getInterpolationType() {
        return interpolationType;
    }

    /** ISO 8601 duration of the aggregation period (e.g. PT1H, P1D), or null for instantaneous. */
    public String getAggregationPeriod() {
        return aggregationPeriod;
    }

    /** Human-readable level (e.g. "surface", "2m above ground"). */
    public String getLevel() {
        return level;
    }

    /** Observed value (Number or String). */
    public Object getValue() {
        return value;
    }

    /** Station/site name (from B01019). */
    public String getStationName() {
        return stationName;
    }

    /** Reference date-time in ISO 8601 (UTC). */
    public String getReferenceDateTime() {
        return referenceDateTime;
    }

    /** Network identifier (e.g. agrmet). */
    public String getNetwork() {
        return network;
    }

    /** Latitude in degrees (high accuracy, B05001). */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /** Longitude in degrees (high accuracy, B06001). */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /** Height of station ground above mean sea level in metres (B07030). */
    public BigDecimal getHeightAboveMeanSeaLevel() {
        return heightAboveMeanSeaLevel;
    }

    /** Height of barometer above mean sea level in metres (B07031). */
    public BigDecimal getBarometerHeightAboveMeanSeaLevel() {
        return barometerHeightAboveMeanSeaLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BUFRObservation that = (BUFRObservation) o;
        return Objects.equals(observedPropertyCode, that.observedPropertyCode)
                && Objects.equals(interpolationType, that.interpolationType)
                && Objects.equals(aggregationPeriod, that.aggregationPeriod)
                && Objects.equals(level, that.level)
                && Objects.equals(value, that.value)
                && Objects.equals(stationName, that.stationName)
                && Objects.equals(referenceDateTime, that.referenceDateTime)
                && Objects.equals(network, that.network)
                && Objects.equals(latitude, that.latitude)
                && Objects.equals(longitude, that.longitude)
                && Objects.equals(heightAboveMeanSeaLevel, that.heightAboveMeanSeaLevel)
                && Objects.equals(barometerHeightAboveMeanSeaLevel, that.barometerHeightAboveMeanSeaLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(observedPropertyCode, interpolationType, aggregationPeriod, level, value,
                stationName, referenceDateTime, network, latitude, longitude,
                heightAboveMeanSeaLevel, barometerHeightAboveMeanSeaLevel);
    }

    @Override
    public String toString() {
        return "BUFRObservation{"
                + "observedProperty='" + getObservedProperty() + '\''
                + ", observedPropertyCode='" + observedPropertyCode + '\''
                + ", interpolationType='" + interpolationType + '\''
                + ", aggregationPeriod='" + aggregationPeriod + '\''
                + ", level='" + level + '\''
                + ", value=" + value
                + ", stationName='" + stationName + '\''
                + ", referenceDateTime='" + referenceDateTime + '\''
                + ", network='" + network + '\''
                + '}';
    }
}
