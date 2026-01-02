/**
 * 
 */
package eu.essi_lab.accessor.sos.downloader._1_0_0;

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

/**
 * @author Fabrizio
 */
public class DataRecord {

    private String time;
    private BigDecimal value;

    /**
     * 
     */
    public DataRecord() {

    }

    /**
     * @param time
     * @param value
     */
    public DataRecord(String time, BigDecimal value) {
	this.time = time;
	this.value = value;
    }

    /**
     * @return
     */
    public String getTime() {
	return time;
    }

    /**
     * @param time
     */
    public void setTime(String time) {
	this.time = time;
    }

    /**
     * @return
     */
    public BigDecimal getValue() {
	return value;
    }

    /**
     * @param value
     */
    public void setValue(BigDecimal value) {
	this.value = value;
    }

    /**
     * 
     */
    public String toString() {

	return getTime() + ":" + getValue();
    }

}
