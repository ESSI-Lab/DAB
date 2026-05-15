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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Wrapper for EMR BUFR-style JSON (see ingestor/example.json). Parses the structure
 * and exposes station metadata plus a flat list of observations, each with observed
 * property, interpolation type, aggregation period, level, and station/position info.
 */
public final class BUFRData {

    /** B-code: long station or site name. */
    private static final String B01019 = "B01019";
    /** B-code: report mnemonic. */
    private static final String B01194 = "B01194";
    /** B-code: latitude (high accuracy), degrees. */
    private static final String B05001 = "B05001";
    /** B-code: longitude (high accuracy), degrees. */
    private static final String B06001 = "B06001";
    /** B-code: height of station ground above mean sea level, m. */
    private static final String B07030 = "B07030";
    /** B-code: height of barometer above mean sea level, m. */
    private static final String B07031 = "B07031";

    /** BUFR time range type: mean/representative over interval. */
    private static final int TIMERANGE_MEAN = 0;
    /** BUFR time range type: accumulation over preceding interval. */
    private static final int TIMERANGE_ACCUMULATION = 1;
    /** BUFR time range type: maximum over interval (e.g. daily max). */
    private static final int TIMERANGE_MAXIMUM = 2;
    /** BUFR time range type: minimum over interval (e.g. daily min). */
    private static final int TIMERANGE_MINIMUM = 3;
    /** BUFR time range type: instantaneous. */
    private static final int TIMERANGE_INSTANTANEOUS = 254;

    /** BUFR level type: surface / ground. */
    private static final int LEVEL_SURFACE = 1;
    /** BUFR level type: height above ground (L1 typically in mm for metres, e.g. 2000 = 2 m). */
    private static final int LEVEL_HEIGHT_ABOVE_GROUND = 103;

    private final JSONObject root;
    private final String stationName;
    private final String network;
    private final String referenceDateTime;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final BigDecimal heightAboveMeanSeaLevel;
    private final BigDecimal barometerHeightAboveMeanSeaLevel;
    private final Long lonProjected;
    private final Long latProjected;
    private final List<BUFRObservation> observations;

    private BUFRData(
            JSONObject root,
            String stationName,
            String network,
            String referenceDateTime,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal heightAboveMeanSeaLevel,
            BigDecimal barometerHeightAboveMeanSeaLevel,
            Long lonProjected,
            Long latProjected,
            List<BUFRObservation> observations) {
        this.root = root;
        this.stationName = stationName;
        this.network = network;
        this.referenceDateTime = referenceDateTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.heightAboveMeanSeaLevel = heightAboveMeanSeaLevel;
        this.barometerHeightAboveMeanSeaLevel = barometerHeightAboveMeanSeaLevel;
        this.lonProjected = lonProjected;
        this.latProjected = latProjected;
        this.observations = observations;
    }

    /**
     * Parses EMR BUFR-style JSON and returns a BUFRData wrapper.
     *
     * @param jsonString full JSON string (e.g. one line of a JSONL file)
     * @return parsed BUFRData
     */
    public static BUFRData parse(String jsonString) {
        return parse(new JSONObject(jsonString), null);
    }

    /**
     * Parses EMR BUFR-style JSON and returns a BUFRData wrapper, only including observations
     * whose observed property code is in the given set (when non-null and non-empty).
     *
     * @param jsonString                  full JSON string (e.g. one line of a JSONL file)
     * @param desiredObservedPropertyCodes when non-null and non-empty, only B-codes in this set are parsed; null = parse all
     * @return parsed BUFRData
     */
    public static BUFRData parse(String jsonString, Set<String> desiredObservedPropertyCodes) {
        return parse(new JSONObject(jsonString), desiredObservedPropertyCodes);
    }

    /**
     * Wraps the given JSON object as BUFRData (extracts station info and builds observation list).
     *
     * @param root root JSON object with "network", "date", "data", etc.
     * @return BUFRData wrapper
     */
    public static BUFRData parse(JSONObject root) {
        return parse(root, null);
    }

    /**
     * Wraps the given JSON object as BUFRData, optionally only including observations
     * whose observed property code is in the given set.
     *
     * @param root                        root JSON object with "network", "date", "data", etc.
     * @param desiredObservedPropertyCodes when non-null and non-empty, only B-codes in this set are parsed; null = parse all
     * @return BUFRData wrapper
     */
    public static BUFRData parse(JSONObject root, Set<String> desiredObservedPropertyCodes) {
        String network = root.optString("network", null);
        if (network != null && network.isEmpty()) network = null;

        String referenceDateTime = root.optString("date", null);
        if (referenceDateTime != null && referenceDateTime.isEmpty()) referenceDateTime = null;

        Long lonProjected = root.has("lon") ? root.getLong("lon") : null;
        Long latProjected = root.has("lat") ? root.getLong("lat") : null;

        JSONArray data = root.optJSONArray("data");
        if (data == null) {
            return new  BUFRData(root, null, network, referenceDateTime, null, null, null, null,
                    lonProjected, latProjected, new ArrayList<>());
        }

        String stationName = null;
        BigDecimal latitude = null;
        BigDecimal longitude = null;
        BigDecimal heightAboveMeanSeaLevel = null;
        BigDecimal barometerHeightAboveMeanSeaLevel = null;

        List<BUFRObservation> observations = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject block = data.getJSONObject(i);

            if (!block.has("timerange")) {
                // Station metadata block
                JSONObject vars = block.optJSONObject("vars");
                if (vars != null) {
                    stationName = optStringFromVar(vars, B01019);
                    if (network == null) network = optStringFromVar(vars, B01194);
                    latitude = optBigDecimalFromVar(vars, B05001);
                    longitude = optBigDecimalFromVar(vars, B06001);
                    heightAboveMeanSeaLevel = optBigDecimalFromVar(vars, B07030);
                    barometerHeightAboveMeanSeaLevel = optBigDecimalFromVar(vars, B07031);
                }
                continue;
            }

            JSONArray timerange = block.getJSONArray("timerange");
            JSONArray level = block.optJSONArray("level");
            JSONObject vars = block.optJSONObject("vars");
            if (vars == null) continue;

            String interpolationType = interpolationTypeFromTimerange(timerange);
            String aggregationPeriod = aggregationPeriodFromTimerange(timerange);
            String levelDescription = toLevelDescription(level);

            for (String key : vars.keySet()) {
                if (!key.startsWith("B")) continue;
                if (desiredObservedPropertyCodes != null && !desiredObservedPropertyCodes.isEmpty()
                        && !desiredObservedPropertyCodes.contains(key)) continue;
                JSONObject varObj = vars.optJSONObject(key);
                if (varObj == null) continue;
                Object v = varObj.opt("v");
                if (v == null && varObj.has("v")) v = varObj.get("v");

                BUFRObservation obs = new BUFRObservation(
                        key,
                        interpolationType,
                        aggregationPeriod,
                        levelDescription,
                        v,
                        stationName,
                        referenceDateTime,
                        network,
                        latitude,
                        longitude,
                        heightAboveMeanSeaLevel,
                        barometerHeightAboveMeanSeaLevel);
                observations.add(obs);
            }
        }

        return new BUFRData(
                root,
                stationName,
                network,
                referenceDateTime,
                latitude,
                longitude,
                heightAboveMeanSeaLevel,
                barometerHeightAboveMeanSeaLevel,
                lonProjected,
                latProjected,
                observations);
    }

    private static String optStringFromVar(JSONObject vars, String bCode) {
        JSONObject o = vars.optJSONObject(bCode);
        if (o == null) return null;
        Object v = o.opt("v");
        return v == null ? null : String.valueOf(v);
    }

    private static BigDecimal optBigDecimalFromVar(JSONObject vars, String bCode) {
        JSONObject o = vars.optJSONObject(bCode);
        if (o == null) return null;
        if (!o.has("v")) return null;
        Object v = o.get("v");

        if (v instanceof Number) return new BigDecimal(v.toString());
        if (v instanceof String) {
            try {
                return new BigDecimal((String) v);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Maps a dballe/BUFR timerange array {@code [pindicator, p1, p2]} to an interpolation type label.
     */
    public static String interpolationTypeFromTimerange(JSONArray timerange) {
        int trType = timerange != null && timerange.length() > 0 ? timerange.getInt(0) : TIMERANGE_INSTANTANEOUS;
        return toInterpolationType(trType);
    }

    /**
     * Maps a dballe/BUFR timerange array {@code [pindicator, p1, p2]} to an ISO 8601 aggregation period, or null.
     */
    public static String aggregationPeriodFromTimerange(JSONArray timerange) {
        int trType = timerange != null && timerange.length() > 0 ? timerange.getInt(0) : TIMERANGE_INSTANTANEOUS;
        int p2 = timerange != null && timerange.length() > 2 ? timerange.getInt(2) : 0;
        return toAggregationPeriod(trType, p2);
    }

    private static String toInterpolationType(int timerangeType) {
        switch (timerangeType) {
            case TIMERANGE_INSTANTANEOUS:
                return "instantaneous";
            case TIMERANGE_ACCUMULATION:
                return "total_preceding_interval";
            case TIMERANGE_MEAN:
                return "average_preceding_interval";
            case TIMERANGE_MAXIMUM:
                return "maximum_preceding_interval";
            case TIMERANGE_MINIMUM:
                return "minimum_preceding_interval";
            default:
                return "unknown_" + timerangeType;
        }
    }

    private static String toAggregationPeriod(int timerangeType, int p2Seconds) {
        if (timerangeType == TIMERANGE_INSTANTANEOUS && p2Seconds == 0) {
            return null;
        }
        if (p2Seconds <= 0) return null;
        // ISO 8601 duration: P1D, PT1H, PT15M, etc.
        if (p2Seconds >= 86400 && p2Seconds % 86400 == 0) {
            int days = p2Seconds / 86400;
            return "P" + days + "D";
        }
        if (p2Seconds >= 3600 && p2Seconds % 3600 == 0) {
            int hours = p2Seconds / 3600;
            return "PT" + hours + "H";
        }
        if (p2Seconds >= 60 && p2Seconds % 60 == 0) {
            int minutes = p2Seconds / 60;
            return "PT" + minutes + "M";
        }
        return "PT" + p2Seconds + "S";
    }

    private static String toLevelDescription(JSONArray level) {
        if (level == null || level.length() == 0) return "unknown";
        int type = level.getInt(0);
        if (type == LEVEL_SURFACE) return "surface";
        if (type == LEVEL_HEIGHT_ABOVE_GROUND && level.length() >= 2 && !level.isNull(1)) {
            int l1 = level.getInt(1);
            // Common convention: L1 in mm -> metres (2000 mm = 2 m). Alternative: L1 in cm -> 2000 = 20 m.
            double metres = l1 / 1000.0;
            if (metres == (long) metres) {
                return (long) metres + "m above ground";
            }
            return metres + "m above ground";
        }
        if (level.length() >= 2 && !level.isNull(1)) {
            return "level_type_" + type + "_L1_" + level.get(1);
        }
        return "level_type_" + type;
    }

    // --- Station / document-level accessors ---

    /** Station or site long name (B01019), or null if not present. */
    public String getStationName() {
        return stationName;
    }

    /** Network identifier (e.g. agrmet). */
    public String getNetwork() {
        return network;
    }

    /** Reference date-time in ISO 8601 (UTC). */
    public String getReferenceDateTime() {
        return referenceDateTime;
    }

    /** Latitude in degrees (B05001), or null. */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /** Longitude in degrees (B06001), or null. */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /** Height of station ground above mean sea level in metres (B07030), or null. */
    public BigDecimal getHeightAboveMeanSeaLevel() {
        return heightAboveMeanSeaLevel;
    }

    /** Height of barometer above mean sea level in metres (B07031), or null. */
    public BigDecimal getBarometerHeightAboveMeanSeaLevel() {
        return barometerHeightAboveMeanSeaLevel;
    }

    /** Projected longitude from root (e.g. UTM or local CRS), or null. */
    public Long getLonProjected() {
        return lonProjected;
    }

    /** Projected latitude from root (e.g. UTM or local CRS), or null. */
    public Long getLatProjected() {
        return latProjected;
    }

    /** Raw root JSON object. */
    public JSONObject getRoot() {
        return root;
    }

    /** All observations extracted from "data" blocks that have timerange/level/vars. */
    public List<BUFRObservation> getObservations() {
        return new ArrayList<>(observations);
    }

    /** Optional version string from root ("version" key). */
    public Optional<String> getVersion() {
        return root.has("version") ? Optional.of(root.getString("version")) : Optional.empty();
    }

    /** Optional station identifier from root ("ident" key). */
    public Optional<String> getIdent() {
        if (!root.has("ident")) return Optional.empty();
        Object o = root.get("ident");
        return o == null || JSONObject.NULL.equals(o) ? Optional.empty() : Optional.of(String.valueOf(o));
    }
}
