package eu.essi_lab.accessor.ukeafloodmonitoring;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * A time series of the flood-monitoring API: a measure (e.g. "1029TH-level-stage-i-15_min-mASD"), described by the measure
 * properties combined with the properties of its station
 *
 * @author boldrini
 */
public class EAFloodMonitoringEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public EAFloodMonitoringEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public EAFloodMonitoringEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the measure notation, e.g. "1029TH-level-stage-i-15_min-mASD"
     */
    public String getSeriesId() {

	return getObject().optString(EAFloodMonitoringClient.NOTATION, null);
    }

    /**
     * @return the station reference, e.g. "1029TH"
     */
    public String getStationReference() {

	return getString(EAFloodMonitoringClient.STATION_REFERENCE);
    }

    /**
     * @return the station label, or the station reference if not set
     */
    public String getStationLabel() {

	String label = getString(EAFloodMonitoringClient.STATION_LABEL);
	return label.isEmpty() ? getStationReference() : label;
    }

    /**
     * @return the parameter, e.g. "level", "flow", "temperature", "rainfall"
     */
    public String getParameter() {

	return getString(EAFloodMonitoringClient.PARAMETER);
    }

    /**
     * @return the qualifier, e.g. "Stage", "Downstream Stage", "Tidal Level", "Groundwater", "Water"
     */
    public String getQualifier() {

	return getString(EAFloodMonitoringClient.QUALIFIER);
    }

    /**
     * @return the unit name, e.g. "mASD", "m3/s", or an empty string if not set or unknown ("---")
     */
    public String getUnit() {

	String unit = getString(EAFloodMonitoringClient.UNIT_NAME);
	return unit.equals(EAFloodMonitoringClient.UNKNOWN_UNIT) ? "" : unit;
    }

    /**
     * @return the value type, e.g. "instantaneous", "mean", "total"
     */
    public String getValueType() {

	return getString(EAFloodMonitoringClient.VALUE_TYPE);
    }

    /**
     * @return the period of the readings, in seconds
     */
    public Optional<Integer> getPeriod() {

	if (!getObject().has(EAFloodMonitoringClient.PERIOD)) {
	    return Optional.empty();
	}

	int period = getObject().optInt(EAFloodMonitoringClient.PERIOD, 0);
	return period > 0 ? Optional.of(period) : Optional.empty();
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return EAFloodMonitoringClient.optString(getObject(), key);
    }
}
