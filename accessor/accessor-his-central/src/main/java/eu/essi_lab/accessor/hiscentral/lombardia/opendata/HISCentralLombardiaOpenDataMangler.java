package eu.essi_lab.accessor.hiscentral.lombardia.opendata;


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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * Encodes a series of the Lombardia open data as {@code sensor=<idsensore>;property=<property>}, where the property is
 * a {@link LombardiaOpenDataProperty} name, or {@link #RATING_CURVE} for the rating curves of a river level sensor.
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataMangler extends KVPMangler {

    public static final String RATING_CURVE = "RATING_CURVE";

    private static final String SENSOR_KEY = "sensor";
    private static final String PROPERTY_KEY = "property";

    public HISCentralLombardiaOpenDataMangler() {
	super(";");
    }

    public String getSensorIdentifier() {
	return getParameterValue(SENSOR_KEY);
    }

    public void setSensorIdentifier(String sensorIdentifier) {
	setParameter(SENSOR_KEY, sensorIdentifier);
    }

    public String getProperty() {
	return getParameterValue(PROPERTY_KEY);
    }

    public void setProperty(String property) {
	setParameter(PROPERTY_KEY, property);
    }
}
