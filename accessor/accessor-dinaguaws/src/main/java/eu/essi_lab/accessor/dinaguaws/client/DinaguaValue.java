package eu.essi_lab.accessor.dinaguaws.client;

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
import java.util.Date;

public class DinaguaValue implements Comparable<DinaguaValue> {

    private BigDecimal value;
    private String units;

    public DinaguaValue(BigDecimal value, Date date) {
	super();
	this.value = value;
	this.date = date;
    }

    /**
     * @return the units
     */
    public String getUnits() {
	return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(String units) {
	this.units = units;
    }

    public BigDecimal getValue() {
	return value;
    }

    public void setValue(BigDecimal value) {
	this.value = value;
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    private Date date;

    @Override
    public int compareTo(DinaguaValue o) {
	return date.compareTo(o.getDate());
    }

    @Override
    public String toString() {

	return "- Date: " + date + " - Value: " + value;
    }

}
