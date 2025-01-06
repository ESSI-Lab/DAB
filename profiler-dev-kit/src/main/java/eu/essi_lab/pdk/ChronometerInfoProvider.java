/**
 * 
 */
package eu.essi_lab.pdk;

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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * @author Fabrizio
 */
public class ChronometerInfoProvider extends Chronometer implements RuntimeInfoProvider {

    @Override
    public String getBaseType() {

	return "Chronometer";
    }

    private long elapsedTimeMillis;

    /**
     * @param format
     */
    public ChronometerInfoProvider(TimeFormat format) {
	super(format);
    }

    public long getElapsedTimeMillis() {

	elapsedTimeMillis = super.getElapsedTimeMillis();
	return elapsedTimeMillis;
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();
	map.put(//
		RuntimeInfoElement.CHRONOMETER_TIME_STAMP.getName(), //
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(startTime))));
	map.put(//
		RuntimeInfoElement.CHRONOMETER_TIME_STAMP_MILLIS.getName(), //
		Arrays.asList(String.valueOf(startTime)));

	map.put(//
		RuntimeInfoElement.CHRONOMETER_ELAPSED_TIME_MILLIS.getName(), //
		Arrays.asList(String.valueOf(elapsedTimeMillis == 0 ? getElapsedTimeMillis() : elapsedTimeMillis)));

	map.put(//
		RuntimeInfoElement.CHRONOMETER_FORMATTED_ELAPSED_TIME.getName(), //
		Arrays.asList(formatElapsedTime(elapsedTimeMillis)));

	return map;
    }

    @Override
    public String getName() {

	return "CHRONOMETER";
    }
}
